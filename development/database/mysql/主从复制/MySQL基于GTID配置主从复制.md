# MySQL基于GTID配置主从复制
GTID（Global Transaction ID），也就是全局事务ID，其保证为每一个在master上提交的事务在复制集群中可以生成一个唯一的ID。

基于GTID的复制是从MySQL 5.6开始支持的一种新的复制方式，与传统的基于binlog日志的方式存在很大的差异：
基于binlog日志复制时，slave服务器连接到master并告诉master要从哪个binlog文件的指定偏移量（位置）开始执行增量同步，这时如果指定的偏移量不对，就会造成主从数据的不一致，而基于GTID的复制可以避免这种情况。
 
基于GTID的复制中，首先slave告诉master在slave上已经执行完了的事务GTID值，然后master会将所有没有在slave上执行的事务，发送到slave上进行执行，并且使用GTID的复制可以保证同一个事务只在指定的slave上执行一次，这样可以避免由于偏移量的问题造成数据不一致。

slave上基于GTID的事务执行顺序是如何保证的？

GTID=source_id:transaction_id
其中：
- source_id 执行事务的主库的server-uuid的值
    可通过查看文件`/var/lib/mysql/auto.cnf`的server-uuid的值，或通过命令`mysql> show variables like '%uuid%';`
- transaction_id 事务ID，是一个从1开始的自增序列，表示这个事务是在主库上执行的第几个事务

下面开始基于GTID配置主从复制。

## 修改主数据库和从数据库的字符集
参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“修改主数据库和从数据库的字符集”一节

## 设置主数据库的ID
参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“设置主数据库的ID”一节

## 为主服务器开启binlog
参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“为主服务器开启binlog”一节

## 在master打开GTID
MySQL默认是关闭GTID的。查看当前master的GTID是否开启，可以使用命令：
```
mysql> show variables like '%GTID%';
+----------------------------------+-----------+
| Variable_name                    | Value     |
+----------------------------------+-----------+
| binlog_gtid_simple_recovery      | ON        |
| enforce_gtid_consistency         | OFF       |
| gtid_executed_compression_period | 1000      |
| gtid_mode                        | OFF       |
| gtid_next                        | AUTOMATIC |
| gtid_owned                       |           |
| gtid_purged                      |           |
| session_track_gtids              | OFF       |
+----------------------------------+-----------+
```

上面的配置项说明：
- gtid_mode 是否开启GTID模式，其值为“ON”或“OFF”。
- enforce_gtid_consistency 
- gtid_mode
- gtid_owned
- gtid_purged


可以看到，当前`gtid_mode`和`enforce_gtid_consistency`的状态是OFF，要开启GTID，只需要在my.cnf配置文件的[mysqld]下增加配置项`gtid_mode=ON`和`enforce_gtid_consistency=ON`（注意，增加配置项后需要重启数据库），完整的内容如下所示：
```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
character_set_server=utf8

server-id=120
log-bin=mysql-bin

gtid_mode=ON
enforce_gtid_consistency=ON
```

再次查看配置，
```
mysql> show variables like '%gtid%';
+----------------------------------+-----------+
| Variable_name                    | Value     |
+----------------------------------+-----------+
| binlog_gtid_simple_recovery      | ON        |
| enforce_gtid_consistency         | ON        |
| gtid_executed_compression_period | 1000      |
| gtid_mode                        | ON        |
| gtid_next                        | AUTOMATIC |
| gtid_owned                       |           |
| gtid_purged                      |           |
| session_track_gtids              | OFF       |
+----------------------------------+-----------+
```
可以看到，`gtid_mode`和`enforce_gtid_consistency`选项已经打开了


## 为主数据库创建从服务器复制的用户
为主服务器创建MySQL用户“repl”，并为该用户分配主从复制的权限。从服务器使用该用户执行复制操作，后面从服务器设置master时将使用到该用户。
命令如下：
```
mysql> grant replication slave, replication client on *.* to repl@'%' identified by 'repl';
```

## 查看主服务器的master状态
通过上面的步骤，主服务器已经配置完了，可以使用如下命令查看主服务器的状态。
```
mysql> show master status;
+------------------+----------+--------------+------------------+------------------------------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set                        |
+------------------+----------+--------------+------------------+------------------------------------------+
| mysql-bin.000010 |      609 |              |                  | 03729520-4008-11e9-97a7-000c2918cf39:1-2 |
+------------------+----------+--------------+------------------+------------------------------------------+
```
上述结果中各个字段的说明：
- File  请参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“查看主服务器的master状态”一节
- Position  请参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“查看主服务器的master状态”一节
- Binlog_Do_DB  请参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“查看主服务器的master状态”一节
- Binlog_Ignore_DB  请参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“查看主服务器的master状态”一节
- Executed_Gtid_Set 为已经执行的事务ID


## 在slave上开启GTID
在slave数据库的my.cnf配置文件的[mysqld]下增加配置项`gtid_mode=ON`和`enforce_gtid_consistency=ON`（注意，增加配置项后需要重启数据库），完整的内容如下所示：
```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
character_set_server=utf8

server-id=121

gtid_mode=ON
enforce_gtid_consistency=ON
```

## 清理从服务器
有时候，从服务器在配置主从复制之前，可能已经配置过主从复制，里面存留了一些配置信息，为了不影响新的主从复制配置，必须将之前的配置信息清理干净，包括：
- 清理通过`change master`设置的master信息，命令为`mysql> reset slave;`；
- 如果slave以前配置过GTID，也需要清理掉，可以查看slave服务器上的mysql.gtid_executed表，如果里面有数据，可以使用命令`mysql> reset master;`进行清理。

## 为从服务器设置它的master
设置从服务器的master。命令如下所示（注意，下面的命令必须在从服务器停掉IO线程和SQL线程后执行，命令时：`mysql> stop slave`）：
```
mysql> change master to master_host='192.168.199.120',master_port=3306,master_user='repl',master_password='repl', master_auto_position=1;
```
其中：
- master_auto_position 

注意：上面和[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“为从服务器设置它的master”一节的有一些区别，不再需要设置slave复制master的binlog的起始position。

执行完成后，查看从服务器的状态：
```
mysql> show slave status\G;
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.101
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000010
          Read_Master_Log_Pos: 609
               Relay_Log_File: 192-relay-bin.000003
                Relay_Log_Pos: 822
        Relay_Master_Log_File: mysql-bin.000010
             Slave_IO_Running: NO
            Slave_SQL_Running: NO
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 609
              Relay_Log_Space: 1027
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: 0
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 101
                  Master_UUID: 03729520-4008-11e9-97a7-000c2918cf39
             Master_Info_File: /var/lib/mysql/master.info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 03729520-4008-11e9-97a7-000c2918cf39:1-2
            Executed_Gtid_Set: 03729520-4008-11e9-97a7-000c2918cf39:1-2
                Auto_Position: 1
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
```
上面的大部分字段都与“基于binlog主从复制”中的一致，参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“为从服务器设置它的master”一节，下面说明几个不一样的字段：
- Retrieved_Gtid_Set 已经从master接收到的GTID
- Executed_Gtid_Set 已经执行的GTID

## 启动从服务器的IO线程和SQL线程
参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“启动从服务器的IO线程和SQL线程”一节

## 将主库的数据导入从库
将主库的数据导入从库，分为两步：1）从主库导出数据；2）修改主库中的部分数据；3）将导出的数据导入从库
其中第2）步不是必须的，只是为了模拟正式环境下，在步骤1）和步骤3）之间可能出现数据变化的情况。

### 从主库导出数据
在从服务器上导出主库的数据，命令如下：
```
# mysqldump -h主库的IP -uroot -proot --default-character-set=utf8 --databases test --single-transaction --master-data=2 --triggers --routines --events > xxx.sql
```
特别注意，其中：
- single-transaction 用于指定导出数据的事务使用“可重复读”隔离级别，可以保证数据的读一致性。该参数仅适用于innodb引擎。
    可以通过命令查看当前的存储引擎：`mysql> show variables like '%engine%';`
    该选项为什么可以保证导出数据的过程中，数据库不会更新新的数据，请参考[mysqldump命令](../命令/mysqldump.md)一文中的`--single-transaction选项`一节
- master-data 当值为1时，表示在导出的脚本中不注释 "change master"；当值为2时，表示注释 "change master"
    请参考[mysqldump命令](../命令/mysqldump.md)一文中的`--master-data选项`一节

导出的脚本部分内容如下所示：
```
SET @@GLOBAL.GTID_PURGED='03729520-4008-11e9-97a7-000c2918cf39:1-3';

--
-- Position to start replication or point-in-time recovery from
--

-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000010', MASTER_LOG_POS=905;

--
-- Current Database: `test1` 
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `test1` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `test1`;
```

上面的内容中，需要特别关注的一行内容是：
```
SET @@GLOBAL.GTID_PURGED='03729520-4008-11e9-97a7-000c2918cf39:1-3';
```
表示导出的GTID为1-3.


### 修改主数据库的部分数据
修改主数据库的部分数据，模拟搭建主从的过程中，主服务器在不停的写入数据，然后再查看主服务器的binlog位置：
```
mysql> show master status;
+------------------+----------+--------------+------------------+------------------------------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set                        |
+------------------+----------+--------------+------------------+------------------------------------------+
| mysql-bin.000010 |     1476 |              |                  | 03729520-4008-11e9-97a7-000c2918cf39:1-5 |
+------------------+----------+--------------+------------------+------------------------------------------+
```
可以看到，主数据库在修改了数据后，`Executed_Gtid_Set`的值由导出数据时的“1-3”变为了“1-5”

### 将导出的数据导入从数据库中
在从数据库中执行如下命令：
```
# mysql -uroot -proot --default-character-set=utf8 < test.sql
```

## 查看结果
在从服务器的数据库可以看到，导出数据后，主库更新的数据也同步到从库中了。通过如下命令查看slave库当前同步的状态：
```
mysql> show slave status\G;
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.101
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000010
                        ...........
             Master_Server_Id: 101
                  Master_UUID: 03729520-4008-11e9-97a7-000c2918cf39
             Master_Info_File: /var/lib/mysql/master.info
           Retrieved_Gtid_Set: 03729520-4008-11e9-97a7-000c2918cf39:4-5
            Executed_Gtid_Set: 03729520-4008-11e9-97a7-000c2918cf39:1-5
```