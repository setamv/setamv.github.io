MySQL 5.6基于库的并行复制出来后，基本无人问津，在沉寂了一段时间之后，MySQL 5.7出来了，MySQL 5.7才可称为真正的并行复制，这其中最为主要的原因就是slave服务器的回放与master是一致的，即master服务器上是怎么并行执行的，那么slave上就怎样进行并行回放。不再有库的并行复制限制，对于二进制日志格式也无特殊的要求（基于库的并行复制也没有要求）。
该并行复制的思想最早是由MariaDB的Kristain提出，并已在MariaDB 10中出现，相信很多选择MariaDB的小伙伴最为看重的功能之一就是并行复制。
下面来看基于MTS的并行复制的基本原理：

组复制（group commit）：通过对事务进行分组，优化减少了生成二进制日志所需的操作数。当事务同时提交时，它们将在单个操作中写入到二进制日志中。如果事务能同时提交成功，那么它们就不会共享任何锁，这意味着它们没有冲突，因此可以在Slave上并行执行。所以通过在主机上的二进制日志中添加组提交信息，这些Slave可以并行地安全地运行事务。

首先，MySQL 5.7的并行复制基于一个前提，即所有已经处于prepare阶段的事务，都是可以并行提交的。这些当然也可以在从库中并行提交，因为处理这个阶段的事务，都是没有冲突的，该获取的资源都已经获取了。反过来说，如果有冲突，则后来的会等已经获取资源的事务完成之后才能继续，故而不会进入prepare阶段。这是一种新的并行复制思路，完全摆脱了原来一直致力于为了防止冲突而做的分发算法，等待策略等复杂的而又效率低下的工作。MySQL 5.7并行复制的思想一言以蔽之：一个组提交（group commit）的事务都是可以并行回放，因为这些事务都已进入到事务的prepare阶段，则说明事务之间没有任何冲突（否则就不可能提交）。

根据以上描述，这里的重点是如何来定义哪些事务是处于prepare阶段的？以及在生成的Binlog内容中该如何告诉Slave哪些事务是可以并行复制的？为了兼容MySQL 5.6基于库的并行复制，5.7引入了新的变量`slave-parallel-type`，其可以配置的值有：DATABASE（默认值，基于库的并行复制方式）、LOGICAL_CLOCK（基于组提交的并行复制方式）。

### 支持并行复制的GTID
那么如何知道事务是否在同一组中？原版的MySQL并没有提供这样的信息。在MySQL 5.7版本中，其设计方式是将组提交的信息存放在GTID中。如果用户没有开启GTID功能，即将参数`gtid_mode`设置为OFF呢？故MySQL 5.7又引入了称之为Anonymous_Gtid（ANONYMOUS_GTID_LOG_EVENT）的二进制日志event类型，如：
```
mysql> show binlog events in 'mysql-bin.000013';
+------------------+-----+----------------+-----------+-------------+--------------------------------------------------------------------+
| Log_name         | Pos | Event_type     | Server_id | End_log_pos | Info                                                               |
+------------------+-----+----------------+-----------+-------------+--------------------------------------------------------------------+
| mysql-bin.000013 |   4 | Format_desc    |       101 |         123 | Server ver: 5.7.25-log, Binlog ver: 4                              |
| mysql-bin.000013 | 123 | Previous_gtids |       101 |         194 | 03729520-4008-11e9-97a7-000c2918cf39:1-8                           |
| mysql-bin.000013 | 194 | Anonymous_Gtid |       101 |         259 | SET @@SESSION.GTID_NEXT= '03729520-4008-11e9-97a7-000c2918cf39:9'  |
| mysql-bin.000013 | 259 | Query          |       101 |         332 | BEGIN                                                              |
| mysql-bin.000013 | 332 | Table_map      |       101 |         380 | table_id: 108 (test1.stu)                                          |
| mysql-bin.000013 | 380 | Write_rows     |       101 |         428 | table_id: 108 flags: STMT_END_F                                    |
| mysql-bin.000013 | 428 | Xid            |       101 |         459 | COMMIT /* xid=6 */                                                 |
| mysql-bin.000013 | 459 | Anonymous_Gtid |       101 |         524 | SET @@SESSION.GTID_NEXT= '03729520-4008-11e9-97a7-000c2918cf39:10' |
| mysql-bin.000013 | 524 | Query          |       101 |         597 | BEGIN                                                              |
| mysql-bin.000013 | 597 | Table_map      |       101 |         649 | table_id: 109 (test1.class)                                        |
| mysql-bin.000013 | 649 | Write_rows     |       101 |         703 | table_id: 109 flags: STMT_END_F                                    |
| mysql-bin.000013 | 703 | Xid            |       101 |         734 | COMMIT /* xid=35 */                                                |
| mysql-bin.000013 | 734 | Anonymous_Gtid |       101 |         799 | SET @@SESSION.GTID_NEXT= '03729520-4008-11e9-97a7-000c2918cf39:11' |
| mysql-bin.000013 | 799 | Query          |       101 |         872 | BEGIN                                                              |
| mysql-bin.000013 | 872 | Table_map      |       101 |         924 | table_id: 109 (test1.class)                                        |
| mysql-bin.000013 | 924 | Write_rows     |       101 |         978 | table_id: 109 flags: STMT_END_F                                    |
| mysql-bin.000013 | 978 | Xid            |       101 |        1009 | COMMIT /* xid=37 */                                                |
+------------------+-----+----------------+-----------+-------------+--------------------------------------------------------------------+
```
- Previous_gtids
    用于表示上一个binlog最后一个gitd的位置，每个binlog只有一个，当没有开启GTID时此事件为空。
- Anonymous_Gtid
    当开启GTID时，每一个操作语句（DML/DDL）执行前就会添加一个GTID事件，记录当前全局事务ID；同时在MySQL 5.7版本中，组提交信息也存放在GTID事件中，有两个关键字段last_committed，sequence_number就是用来标识组提交信息的。在InnoDB中有一个全局计数器（global counter），在每一次存储引擎提交之前，计数器值就会增加。在事务进入prepare阶段之前，全局计数器的当前值会被储存在事务中，这个值称为此事务的commit-parent（也就是last_committed）。

这意味着在MySQL 5.7版本中即使不开启GTID，每个事务开始前也是会存在一个Anonymous_Gtid，而这个Anonymous_Gtid事件中就存在着组提交的信息。反之，如果开启了GTID后，就不会存在这个Anonymous_Gtid了，从而组提交信息就记录在非匿名GTID事件中

### LOGICAL_CLOCK
通过上述的SHOW BINLOG EVENTS，我们并没有发现有关组提交的任何信息。但是通过mysqlbinlog工具，用户就能发现组提交的内部信息：
```
$ mysqlbinlog mysql-bin.0000006 | grep last_committed
#150520 14:23:11 server id 88 end_log_pos 259   CRC32 0x4ead9ad6 GTID last_committed=0  sequence_number=1
#150520 14:23:11 server id 88 end_log_pos 1483  CRC32 0xdf94bc85 GTID last_committed=0  sequence_number=2
#150520 14:23:11 server id 88 end_log_pos 2708  CRC32 0x0914697b GTID last_committed=0  sequence_number=3
#150520 14:23:11 server id 88 end_log_pos 3934  CRC32 0xd9cb4a43 GTID last_committed=0  sequence_number=4
#150520 14:23:11 server id 88 end_log_pos 5159  CRC32 0x06a6f531 GTID last_committed=0  sequence_number=5
#150520 14:23:11 server id 88 end_log_pos 6386  CRC32 0xd6cae930 GTID last_committed=0  sequence_number=6
#150520 14:23:11 server id 88 end_log_pos 7610  CRC32 0xa1ea531c GTID last_committed=6  sequence_number=7
#150520 14:23:11 server id 88 end_log_pos 8834  CRC32 0x96864e6b GTID last_committed=6  sequence_number=8
#150520 14:23:11 server id 88 end_log_pos 10057 CRC32 0x2de1ae55 GTID last_committed=6  sequence_number=9
#150520 14:23:11 server id 88 end_log_pos 11280 CRC32 0x5eb13091 GTID last_committed=6  sequence_number=10
#150520 14:23:11 server id 88 end_log_pos 12504 CRC32 0x16721011 GTID last_committed=6  sequence_number=11
#150520 14:23:11 server id 88 end_log_pos 13727 CRC32 0xe2210ab6 GTID last_committed=6  sequence_number=12
#150520 14:23:11 server id 88 end_log_pos 14952 CRC32 0xf41181d3 GTID last_committed=12 sequence_number=13
...
```
可以发现MySQL 5.7二进制日志较之原来的二进制日志内容多了last_committed和sequence_number，last_committed表示事务提交的时候，上次事务提交的编号，如果事务具有相同的last_committed，表示这些事务都在一组内，可以进行并行的回放。例如上述last_committed为0的事务有6个，表示组提交时提交了6个事务，而这6个事务在从机是可以进行并行回放的，而sequence_number是顺序增长的，每个事务对应一个序列号。另外，还有一个细节，其实每一个组的last_committed值，都是上一个组中事务的sequence_number最大值，也是本组中事务sequence_number最小值减1。同时这两个值的有效作用域都在文件内，只要换一个文件（flush binary logs），这两个值就都会从0开始计数。上述的last_committed和sequence_number代表的就是所谓的LOGICAL_CLOCK。

那么此时，还有一个重要的技术问题–MySQL是如何做到将这些事务分组的呢？要搞清楚这个问题，首先需要了解一下MySQL事务提交方式。

### 事务两阶段提交
事务的提交主要分为两个主要步骤：
1. 准备阶段（Storage Engine（InnoDB） Transaction Prepare Phase）
    此时SQL已经成功执行，并生成xid信息及redo和undo的内存日志。然后调用prepare方法完成第一阶段，papare方法实际上什么也没做，将事务状态设为TRX_PREPARED，并将redo log刷磁盘。
2. 提交阶段(Storage Engine（InnoDB）Commit Phase)
    - 记录Binlog日志。
        如果事务涉及的所有存储引擎的prepare都执行成功，则调用TC_LOG_BINLOG::log_xid方法将SQL语句写到binlog（write()将binary log内存日志数据写入文件系统缓存，fsync()将binary log文件系统缓存日志数据永久写入磁盘）。此时，事务已经铁定要提交了。否则，调用ha_rollback_trans方法回滚事务，而SQL语句实际上也不会写到binlog。
    - 告诉引擎做commit
        调用引擎的commit完成事务的提交。会清除undo信息，刷redo日志，将事务设为TRX_NOT_STARTED状态。

### ordered commit
