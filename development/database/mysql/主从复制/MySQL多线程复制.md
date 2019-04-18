# MySQL多线程复制
MySQL主从复制架构中，master是通过并发线程提交，事务通过LSN写入binlog；但是Slave只有一个IO线程和一个SQL线程，是单线程，当业务量非常大的情况下，很容易造成主从延时。

MySQL 5.5及以前的版本只支持单线程复制，即master上一个binlog dump线程；slave上一个IO线程和一个SQL线程。

从MySQL 5.6开始，支持基于schema的多线程复制，每个schema可以使用单独的线程进行复制，但是同一个shcema还是单线程复制

从MySQL 5.7开始，支持基于组提交的并行复制

## 基于schema的多线程复制
从MySQL 5.6.3版本开始就支持所谓的并行复制了，但是其并行只是基于schema的，也就是基于库的。如果用户的MySQL数据库实例中存在多个schema，对于从机复制的速度的确可以有比较大的帮助。但在一般的MySQL使用中，一库多表比较常见，所以MySQL 5.6的并行复制对真正用户来说属于雷声大雨点小，不太合适生产使用。
当开启基于schema的并行程复制功能（变量：slave_parallel_workers > 0），那么SQL线程就变为了coordinator（协作）线程，coordinator线程主要用于协调多个worker线程之间的协作，其负责以下两部分内容：
1. 如果判断事务可以在多个schema之间并行执行，那么选择worker线程执行事务的二进制日志。
2. 如果判断事务不可以并行执行，如该操作是DDL，亦或者是事务跨schema操作，则等待所有的worker线程执行完成之后，再执行当前的日志

coordinator线程不仅将日志发送给worker线程，自己也可以回放日志，但是所有可以并行的操作交付由worker线程完成。
以上机制实现的基于schema的并行复制，每个schema可以使用单独的线程进行复制，但是同一个shcema还是单线程复制。如果用户的数据库实例只有一个库，就无法实现并行复制，甚至性能会比原来的单线程更差，而单库多表是比多库多表更为常见的一种情形。

要配置基于schema的多线程复制，只需要在slave数据库的配置文件my.cnf中的`[mysqld]`段中加入参数`slave_parallel_workers=n`，其中，n一般设置为CPU的核数。完整的配置如下所示：
```
[mysqld]
...
slave_parallel_workers=2
```

## 基于组提交的并行复制
MySQL 5.6基于库的并行复制出来后，基本无人问津，在沉寂了一段时间之后，MySQL 5.7出来了，MySQL 5.7才可称为真正的并行复制，这其中最为主要的原因就是slave服务器的回放与master是一致的，即master服务器上是怎么并行执行的，那么slave上就怎样进行并行回放。不再有库的并行复制限制，对于二进制日志格式也无特殊的要求（基于库的并行复制也没有要求）。
该并行复制的思想最早是由MariaDB的Kristain提出，并已在MariaDB 10中出现，相信很多选择MariaDB的小伙伴最为看重的功能之一就是并行复制。
下面来看基于MTS的并行复制的基本原理：

组复制（group commit）：通过对事务进行分组，优化减少了生成二进制日志所需的操作数。当事务同时提交时，它们将在单个操作中写入到二进制日志中。如果事务能同时提交成功，那么它们就不会共享任何锁，这意味着它们没有冲突，因此可以在Slave上并行执行。所以通过在主机上的二进制日志中添加组提交信息，这些Slave可以并行地安全地运行事务。

首先，MySQL 5.7的并行复制基于一个前提，即所有已经处于prepare阶段的事务，都是可以并行提交的。这些当然也可以在从库中并行提交，因为处理这个阶段的事务，都是没有冲突的，该获取的资源都已经获取了。反过来说，如果有冲突，则后来的会等已经获取资源的事务完成之后才能继续，故而不会进入prepare阶段。这是一种新的并行复制思路，完全摆脱了原来一直致力于为了防止冲突而做的分发算法，等待策略等复杂的而又效率低下的工作。MySQL 5.7并行复制的思想一言以蔽之：一个组提交（group commit）的事务都是可以并行回放，因为这些事务都已进入到事务的prepare阶段，则说明事务之间没有任何冲突（否则就不可能提交）。

根据以上描述，这里的重点是如何来定义哪些事务是处于prepare阶段的？以及在生成的Binlog内容中该如何告诉Slave哪些事务是可以并行复制的？为了兼容MySQL 5.6基于库的并行复制，5.7引入了新的变量`slave_parallel_type`，其可以配置的值有：DATABASE（默认值，基于库的并行复制方式）、LOGICAL_CLOCK（基于组提交的并行复制方式）。

要实现基于组提交的并行复制，首先需要将参数`slave_parallel_type`设置为“LOGICAL_CLOCK”，并设置`slave_parallel_workers=n`，其中，n一般设置为CPU的核数。完整的配置如下所示：
```
[mysqld]
...
slave_parallel_type=LOGICAL_CLOCK
slave_parallel_workers=2
```

## 多线程复制相关的其他优化参数
多线程复制相关的参数还包括以下这些参数，如果要设置这些参数，可以参数的设置加到MySQL配置文件my.cnf中的`[mysqld]`下：
+ `master_info_repository` 和 `relay_log_info_repository`
    `master_info_repository` 和 `relay_log_info_repository` 参数分别用于指定slave数据库中保存master信息 和 中继日志信息的方式，可选的值为：
    - FILE 将master信息和中继日志信息保存在文件中
        这是默认的方式，可以通过以下方式查看master信息保存的文件位置：
        ```
        mysql> show slave status\G;
        *************************** 1. row ***************************
                    Slave_IO_State: Waiting for master to send event
                        Master_Host: 192.168.199.102
                        Master_User: repl
                        Master_Port: 3306
                     Relay_Log_File: 192-relay-bin.000014
                                ......
                        Master_UUID: a7dd76e2-4008-11e9-9e0d-000c291a3b87
                    Master_Info_File: /var/lib/mysql/master.info
        ```
        上面可以看到，master的信息被保存在`/var/lib/mysql/master.info`文件中，而中继日志被保存在`192-relay-bin.000014`文件中。
    - TABLE 将master信息保存在表中
        如果设置为“TABLE”的方式，master的信息将存储在mysql实例的slave_master_info表中，可以通过以下方式查看保存master信息的表：
        ```
        mysql> show slave status\G;
        *************************** 1. row ***************************
                    Slave_IO_State: Waiting for master to send event
                        Master_Host: 192.168.199.102
                        Master_User: repl
                        Master_Port: 3306
                     Relay_Log_File: mysql.slave_relay_log_info
                                ......
                        Master_UUID: a7dd76e2-4008-11e9-9e0d-000c291a3b87
                    Master_Info_File: mysql.slave_master_info
                                ......
        ```
        
        查看mysql.slave_master_info表和mysql.slave_master_info的内容：
        ```
        mysql> select * FROM slave_master_info\G;
        *************************** 1. row ***************************
            Number_of_lines: 25
            Master_log_name: mysql-bin.000006
                Master_log_pos: 194
                        Host: 192.168.199.102
                    User_name: repl
                User_password: repl
                        Port: 3306
                Connect_retry: 60
                Enabled_ssl: 0
                        Ssl_ca: 
                    Ssl_capath: 
                    Ssl_cert: 
                    Ssl_cipher: 
                    Ssl_key: 
        Ssl_verify_server_cert: 0
                    Heartbeat: 30
                        Bind: 
            Ignored_server_ids: 0
                        Uuid: a7dd76e2-4008-11e9-9e0d-000c291a3b87
                Retry_count: 86400
                    Ssl_crl: 
                Ssl_crlpath: 
        Enabled_auto_position: 1
                Channel_name: 
                Tls_version: 

        mysql> select * FROM slave_master_info\G;
        *************************** 1. row ***************************
            Number_of_lines: 25
            Master_log_name: mysql-bin.000006
                Master_log_pos: 194
                        Host: 192.168.199.102
                    User_name: repl
                User_password: repl
                        Port: 3306
                Connect_retry: 60
                Enabled_ssl: 0
                        Ssl_ca: 
                    Ssl_capath: 
                    Ssl_cert: 
                    Ssl_cipher: 
                    Ssl_key: 
        Ssl_verify_server_cert: 0
                    Heartbeat: 30
                        Bind: 
            Ignored_server_ids: 0
                        Uuid: a7dd76e2-4008-11e9-9e0d-000c291a3b87
                Retry_count: 86400
                    Ssl_crl: 
                Ssl_crlpath: 
        Enabled_auto_position: 1
        ```

    当slave数据库开启MTS（基于组的多线程复制）后，会频繁的读取master info，将master的信息存储到表中，可以减小开销。
+ `relay_log_recovery`
    当参数`relay_log_recovery`的值为“ON”时（“OFF”表示关闭该参数），如果slave的IO线程crash的时候中继日志被损坏，slave将自动放弃所有未执行的中继日志，并重新从master上获取日志，保证中继日志的完整性
+ `slave_preserve_commit_order`
    在slave上应用事务的顺序是无序的，和relay log（中继日志）中记录的事务顺序不一样，这样数据的一致性是无法保证的。为了保证事务是按照relay log中记录的顺序回放，就需要开启参数`slave_preserve_commit_order`（将该参数的值设置为“ON”），虽然MySQL 5.7添加MTS后，slave可以并行应用relay log，但commit部分仍然是顺序提交，其中可能会有等待的情况。