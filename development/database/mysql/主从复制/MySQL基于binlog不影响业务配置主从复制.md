# MySQL基于binlog不影响业务配置主从复制
假设主库中已经存在业务数据，并且在搭建的过程中主库仍然不断有数据更新，这种情况下要搭建主从复制，就不能像[MySQL基于binlog主从复制搭建](MySQL基于binlog主从复制搭建.md)中的方法一样进行了。原因有2点：
1. 已有的业务数据无法同步到从库中，因为已有的业务数据，主库不会再产生binlog event了
2. 可能会丢失掉部分在搭建的过程中更新的数据。

这种情况下，需要先将主库的数据导入从库，然后再开始同步，完整的过程如下：

## 修改主数据库和从数据库的字符集
将MySQL的字符集设置为utf8，查看MySQL当前的字符集，可使用如下命令查看：
```
mysql> show variables like '%char%';
+--------------------------------------+----------------------------+
| Variable_name                        | Value                      |
+--------------------------------------+----------------------------+
| character_set_client                 | utf8                       |
| character_set_connection             | utf8                       |
| character_set_database               | latin                       |
| character_set_filesystem             | binary                     |
| character_set_results                | utf8                       |
| character_set_server                 | latin                       |
| character_set_system                 | utf8                       |
| character_sets_dir                   | /usr/share/mysql/charsets/ |
| validate_password_special_char_count | 1                          |
+--------------------------------------+----------------------------+
```
可以看到，`character_set_database` 和 `character_set_server`当前的字符集为“latin”，在my.cnf配置文件的[mysqld]下增加配置项`character_set_server=utf8`，就可以修改上面两项配置项的字符集，完整的内容如下所示：
```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
character_set_server=utf8
```

## 设置主数据库的ID
查看主数据库当前的ID，如下：
```
mysql> show variables like '%server%id%';
+----------------+--------------------------------------+
| Variable_name  | Value                                |
+----------------+--------------------------------------+
| server_id      | 0                                  |
| server_id_bits | 32                                   |
| server_uuid    | 03729520-4008-11e9-97a7-000c2918cf39 |
+----------------+--------------------------------------+
```
可以看到，`server_id`当前的值为0，表示还未设置主数据库的ID，在my.cnf配置文件的[mysqld]下增加配置项`server_id=101`，就可以将主数据库的ID设置为101了，完整的内容如下：
```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
character_set_server=utf8

server-id=120
```

另外，可以从上面看到有个选项`server_uuid`的值，这个值是在数据库第一次启动的时候生成的一个随机字符串。对主从复制中的所有数据库，该值必须唯一，如果发现存在多个数据库有相同的`server_uuid`（例如使用虚拟机克隆时，数据库文件也会被完整的复制），必须修改数据库的`server_uuid`。`server_uuid`的值是保存在`/var/lib/mysql/auto.cnf`文件中的，如下所示：
```
# cat /var/lib/mysql/auto.cnf
[auto]
server-uuid=03729520-4008-11e9-97a7-000c2918cf39
```
现将该文件备份，然后删除原文件并重启MySQL。MySQL就会在下一次启动的时候自动生成该文件。

## 为主服务器开启binlog
MySQL默认是关闭binlog的，根据主从复制的原理[MySQL主从复制的原理](MySQL主从复制的原理.md)，主从复制是基于binlog的，所以需要打开binlog。查看当前binlog是否开启，可以使用命令：
```
mysql> show variables like '%log_bin%';
+---------------------------------+-------+
| Variable_name                   | Value |
+---------------------------------+-------+
| log_bin                         | OFF   |
| log_bin_basename                |       |
| log_bin_index                   |       |
| log_bin_trust_function_creators | OFF   |
| log_bin_use_v1_row_events       | OFF   |
| sql_log_bin                     | ON    |
+---------------------------------+-------+
```

可以看到，当前`log_bin`的状态是OFF。要打开binlog，只需要在my.cnf配置文件的[mysqld]下增加配置项`log-bin=mysql-bin`（注意，增加配置项后需要重启数据库），其中，`mysql-bin`为binlog文件的名称，可以自定义，完整的内容如下所示：
```
[mysqld]
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
character_set_server=utf8

server-id=120
log-bin=mysql-bin
```

再次查看配置，
```
mysql> show variables like '%log_bin%';
+---------------------------------+--------------------------------+
| Variable_name                   | Value                          |
+---------------------------------+--------------------------------+
| log_bin                         | ON                             |
| log_bin_basename                | /var/lib/mysql/mysql-bin       |
| log_bin_index                   | /var/lib/mysql/mysql-bin.index |
| log_bin_trust_function_creators | OFF                            |
| log_bin_use_v1_row_events       | OFF                            |
| sql_log_bin                     | ON                             |
+---------------------------------+--------------------------------+
```
可以看到：
- `log_bin` 选项已经被打开。
- `log_bin_basename` 值变为“/var/lib/mysql/mysql-bin”，该值是binlog文件全路径的前缀，实际的binlog文件名称一般在`log_bin_basename`后面加上`-00000n`的后缀，例如：mysql-bin.000001。
- `log_bin_index` 值变为“/var/lib/mysql/mysql-bin.index”，这个路径是一个文件，里面记载了MySQL的bianry log文件列表，内容如下所示：
    ```
    [root@localhost ~]# cat /var/lib/mysql/mysql-bin.index
    ./mysql-bin.000001
    ./mysql-bin.000002
    ```
    上面显示总共有2个binlog文件，可以通过`# ll /var/lib/mysql | grep 'mysql-bin'`看到确实有两个bianry log文件：
    ```
    [root@localhost ~]# ll /var/lib/mysql | grep 'mysql-bin'
    -rw-r-----. 1 mysql mysql      177 Mar  6 21:14 mysql-bin.000001
    -rw-r-----. 1 mysql mysql      177 Mar  6 21:17 mysql-bin.000002
    -rw-r-----. 1 mysql mysql       95 Mar  6 22:44 mysql-bin.index
    ```

    其中，`log_bin_basename`为binlog文件全路径的前缀，实际的binlog文件名称一般在`log_bin_basename`后面加上`-00000n`的后缀，例如：mysql-bin.000001。

## 为主数据库创建从服务器复制的用户
为主服务器创建MySQL用户“repl”，并为该用户分配主从复制的权限。从服务器使用该用户执行复制操作，后面从服务器设置master时将使用到该用户。
命令如下：
```
mysql> grant replication slave on *.* to repl@'%' identified by 'repl';
```

## 查看主服务器的master状态
通过上面的步骤，主服务器已经配置完了，可以使用如下命令查看主服务器的状态。
```
mysql> show master status;
+------------------+----------+--------------+------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+------------------+----------+--------------+------------------+-------------------+
| mysql-bin.000001 |      154 |              |                  |                   |
+------------------+----------+--------------+------------------+-------------------+
1 row in set (0.00 sec)
```
上述结果中各个字段的说明：
- File  当前binlog的日志文件名称`mysql-bin.000001`，该文件一般位于`/var/lib/mysql`目录下
- Position  binlog记录的当前位置，该位置在数据库发生变更的时候会变成不同的值，用于唯一标识binlog事件的位置，从服务器在设置master的时候可以指定该位置，表示从哪一个binlog事件开始复制。
- Binlog_Do_DB  表示哪些数据库的变化记录到binlog中。默认是所有的数据库
- Binlog_Ignore_DB  表示哪些数据库的变化不会记录到binlog中。默认是没有。

如果只需要记录指定数据库的变化到binlog中，或者不记录某个数据库的变化到binlog中，可以在`my.cnf`配置文件中的[mysqld]段落下增加配置项`binlog_do_db=xx_db`和`binlog_ignore_db`即可。
例如，如果有三个数据库test1、test2，如果只要记录test1数据库的变化到binlog，可以如下配置：
```
[mysqld]
...
binlog_do_db=test1
```
上面也可以使用短横线代替下划线：`binlog-do-db=test1`

然后，再次查看master的状态如下：
```
mysql> show master status;
+------------------+----------+--------------+------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+------------------+----------+--------------+------------------+-------------------+
| mysql-bin.000006 |      154 | test1        |                  |                   |
+------------------+----------+--------------+------------------+-------------------+
```

可以看到，`Binlog_Do_DB`的值已经变成“test1”数据库了，后面配置好从库的主从复制后，可以测试，分别修改test1和test2库的数据，看是哪些修改会被同步到从库上。正确的应该是test1的修改会被同步；二test2的修改不会被同步。

如果要设置多个数据库，多个数据库的名称之间使用逗号“,”分隔，如：`binlog_do_db=test1,test2`

当然，如下的配置和上面的效果也是一样的，即test1的修改会被同步；二test2的修改不会被同步，当时其他数据库（如mysql等）的修改也会被记录到binlog：
```
[mysqld]
...
binlog_ignore_db=test2
```

其中，File和Position在这里先记住，后面从服务器设置master时将使用到。

## 为从服务器设置它的master
设置从服务器的master。命令如下所示（注意，下面的命令必须在从服务器停掉IO线程和SQL线程后执行，命令时：`mysql> stop slave`）：
```
mysql> change master to master_host='192.168.199.120',master_port=3306,master_user='repl',master_password='repl',master_log_file='mysql-bin.000001',master_log_pos=154;
```
其中：
- master_log_file 为主数据库上执行命令`mysql> show master status;`结果中的`File`字段的值；
- master_log_pos 为主数据库上执行命令`mysql> show master status;`结果中的`Position`字段的值；

执行完成后，查看从服务器的状态：
```
mysql> show slave status\G;
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.101
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000008
          Read_Master_Log_Pos: 963
               Relay_Log_File: localhost-relay-bin.000002
                Relay_Log_Pos: 320
        Relay_Master_Log_File: mysql-bin.000008
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
          Exec_Master_Log_Pos: 963
              Relay_Log_Space: 531
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
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
```
结果字段说明：
- Master_Log_File 当前slave数据库复制的master数据库的binlog文件名，与`mysql> show master status;`中的结果应该是一致的
- Read_Master_Log_Pos 当前slave数据库复制master数据库的binlog事件的位置
- Relay_Log_File 当前salve数据库同步的中继日志文件名，该文件一般在`/var/lib/mysql`目录下
- Slave_IO_Running slave数据库上的IO线程运行状态，“NO”说明IO线程未启动。IO线程是什么？参见[MySQL主从复制的原理](MySQL主从复制的原理.md)中的IO线程
- Slave_SQL_Running slave数据库上的SQL线程运行状态，“NO”说明SQL线程未启动。SQL线程是什么？参见[MySQL主从复制的原理](MySQL主从复制的原理.md)中的SQL线程
- Replicate_Do_DB 指定salve数据库在同步master数据库时，只同步哪些数据库的数据变化。
    如果要修改该选项的值，例如，指定slave数据库只同步master上的test库的变化，可以在slave数据库配置文件“my.cnf”中增加如下选项：
    ```
    [mysqld]
    replicate_do_db=test
    ```
    上面也可以使用短横线代替下划线：`replicate-do-db=test`
- Replicate_Ignore_DB 指定salve数据库在同步master数据库时，不同步哪些数据库的变化。
    该选项对应“my.cnf”中`[mysqld]`段落下的配置项`replicate_ignore_db`，也可以使用短横线代替下划线：`replicate-ignore-db`
- Replicate_Do_Table 指定salve数据库在同步master数据库时，只同步哪些数据表的变化。
    该选项对应“my.cnf”中`[mysqld]`段落下的配置项`replicate_do_table`，也可以使用短横线代替下划线：`replicate-do-table`。例如，如果只复制test数据库上的table1表，配置方式如下所示：
    ```
    [mysqld]
    replicate_do_table=test.table1
    ```
    注意：
    1. 上面的表名前面一定要加表所属的schema。
    2. 如果指定只同步指定的表，那未指定的表（包括数据库）都不会同步。
- Replicate_Ignore_Table 指定salve数据库在同步master数据库时，不同步哪些数据表的变化。指定表时，表名前面需要加schema。如：test.table1
- Replicate_Wild_Do_Table 该选项和`Replicate_Do_Table`的区别是，其可以指定通配符
    例如：test.abc%，将同步test库下所有名称以abc开头的表。
- Replicate_Wild_Ignore_Table 该选项和`Replicate_Ignore_Table`的区别是，其可以指定通配符
    例如：test.abc%，将不同步test库下所有名称以abc开头的表。
- Master_Info_File slave数据库保存master配置信息的文件
    可以查看该文件的内容，如下所示：
    ```
    # cat /var/lib/mysql/master.info
    25
    mysql-bin.000009
    1840
    192.168.199.101
    repl
    repl
    3306
    ...
    03729520-4008-11e9-97a7-000c2918cf39
    ...
    ```
    上面的内容大部分都是通过`change master`命令设置的。
- Last_Error 上一次同步时发生的错误记录
- Last_SQL_Error 上一次同步时发生的SQL错误记录。如果同步发生错误，可以从这里查看错误原因
- Slave_SQL_Running_State sale数据库的SQL线程运行状态
- Last_SQL_Error_Timestamp 上一次同步时发生SQL错误的时间

可以看到，从服务器的中继日志文件为`192-relay-bin.000001`，IO线程和SQL线程当前没有启动

## 启动从服务器的IO线程和SQL线程
通过以下命令启动IO线程和SQL线程：
```
mysql> start slave;
```
启动之后，再看从服务器的状态：
```
mysql> show slave status\G;
```
可以看到，从服务器已经启动了IO线程和SQL线程，并且已经连接上主服务器了。
此时，还可以通过命令`show processlist`查看从服务器当前的线程情况，如下：
```
mysql> show processlist;
+----+-------------+-----------+-------+---------+------+--------------------------------------------------------+------------------+
| Id | User        | Host      | db    | Command | Time | State                                                  | Info             |
+----+-------------+-----------+-------+---------+------+--------------------------------------------------------+------------------+
|  1 | system user |           | NULL  | Connect |  124 | Waiting for master to send event                       | NULL             |
|  2 | system user |           | NULL  | Connect |  420 | Slave has read all relay log; waiting for more updates | NULL             |
|  4 | root        | localhost | mysql | Query   |    0 | starting                                               | show processlist |
+----+-------------+-----------+-------+---------+------+--------------------------------------------------------+------------------+
```
可以看到，processlist中有两个线程`1`和`2`就是IO线程和SQL线程。
主服务器当前的线程情况如下所示：
```    
mysql> show processlist;
+----+------+-----------------------+------+-------------+------+---------------------------------------------------------------+------------------+
| Id | User | Host                  | db   | Command     | Time | State                                                         | Info             |
+----+------+-----------------------+------+-------------+------+---------------------------------------------------------------+------------------+
|  2 | root | localhost             | NULL | Query       |    0 | starting                                                      | show processlist |
| 11 | repl | 192.168.199.121:55936 | NULL | Binlog Dump |  218 | Master has sent all binlog to slave; waiting for more updates | NULL             |
+----+------+-----------------------+------+-------------+------+---------------------------------------------------------------+------------------+
```
线程`11`为发送binlog event的线程。


## 将主库的数据导入从库
将主库的数据导入从库，分为两步：1）从主库导出数据；2）修改主库中的部分数据；3）将导出的数据导入从库
其中第2）步不是必须的，只是为了模拟正式环境下，在步骤1）和步骤3）之间可能出现数据变化的情况。

### 从主库导出数据
在从服务器上导出主库的数据，命令如下：
```
# mysqldump -h主库的IP -uroot -proot --default-character-set=utf8 --databases test --single-transaction --master-data=2 > xxx.sql
```
特别注意，其中：
- single-transaction 用于指定导出数据的事务使用“可重复读”隔离级别，可以保证数据的读一致性。该参数仅适用于innodb引擎。
    可以通过命令查看当前的存储引擎：`mysql> show variables like '%engine%';`
    该选项为什么可以保证导出数据的过程中，数据库不会更新新的数据，请参考[mysqldump命令](../命令/mysqldump.md)一文中的`--single-transaction选项`一节
- master-data 当值为1时，表示在导出的脚本中不注释 "change master"；当值为2时，表示注释 "change master"
    请参考[mysqldump命令](../命令/mysqldump.md)一文中的`--master-data选项`一节

导出的脚本部分内容如下所示：
```
-- MySQL dump 10.13  Distrib 5.7.25, for el7 (x86_64)
--
-- Host: 192.168.199.120    Database: test
-- ------------------------------------------------------
-- Server version       5.7.25-log

...

--
-- Position to start replication or point-in-time recovery from
--

-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000002', MASTER_LOG_POS=6215;

--
-- Current Database: `test`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `test` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `test`;
```

上面的内容中，需要特别关注的一行内容是：
```
-- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000002', MASTER_LOG_POS=6215;
```
特别关注的原因是：
- 其中的 "CHANGE MASTER TO"部分被注释掉了（因为mysqldump加了参数--master-data=2），在从服务器上执行数据导入脚本时，就不会执行这一行。
- MASTER_LOG_FILE='mysql-bin.000002' 表示导出数据的时候，主服务器的binlog文件为：mysql-bin.000002
- MASTER_LOG_POS=6215 表示导出数据的时候，master数据库的binlog的位置在6215，可以在主服务器上执行`mysql> show master status;`核对该信息

MASTER_LOG_FILE和MASTER_LOG_POS的值将在从服务器导入数据后，重新开始同步中用到。即将主服务器的数据导入从服务器后，从服务器应该从主服务器上'mysql-bin.000002'文件的6215位置开始同步。

### 修改主数据库的部分数据
修改主数据库的部分数据，模拟搭建主从的过程中，主服务器在不停的写入数据，然后再查看主服务器的binlog位置：
```
mysql> show master status\G;
*************************** 1. row ***************************
            File: mysql-bin.000002
        Position: 6889
    Binlog_Do_DB: 
Binlog_Ignore_DB: 
Executed_Gtid_Set: 
1 row in set (0.00 sec)
```
可以看到，主数据库在修改了数据后，binlog的位置变成了6889。

### 将导出的数据导入从数据库中
在从数据库中执行如下命令：
```
# mysql -uroot -proot --default-character-set=utf8 < test.sql
```

## 查看结果
在从服务器的数据库可以看到，导出数据后，主库更新的数据也同步到从库中了。