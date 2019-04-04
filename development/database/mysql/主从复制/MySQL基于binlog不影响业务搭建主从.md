# MySQL基于binlog不影响业务搭建主从复制
假设主库中已经存在业务数据，并且在搭建的过程中主库仍然不断有数据更新，这种情况下要搭建主从复制，就不能像[MySQL基于binlog主从复制搭建](MySQL基于binlog主从复制搭建.md)中的方法一样进行了。原因有2点：
1. 已有的业务数据无法同步到从库中，因为已有的业务数据，主库不会再产生binary log event了
2. 可能会丢失掉部分在搭建的过程中更新的数据。

这种情况下，需要先将主库的数据导入从库，然后再开始同步，方法如下：

## 将主库的数据导入从库
 1. 首先，需要从主库导出数据
    在从服务器上导出主库的数据，命令如下：
    ```
    # mysqldump -h主库的IP -uroot -proot --default-character-set=utf8 --databases test --single-transaction --master-data=2 > xxx.sql
    ```
    特别注意，其中：
    - single-transaction 用于指定导出数据的事务使用“可重复读”隔离级别，可以保证数据的读一致性。该参数仅适用于innodb引擎。
        可以通过命令查看当前的存储引擎：`mysql> show variables like '%engine%';`
        该选项为什么可以保证导出数据的过程中，数据库不会更新新的数据，请参考[mysqldump命令](../命令/mysqldump.md)一文中的`--single-transaction选项`一节
    - master-data 当值为1时，表示在导出的脚本中不注释 "change master"；当值为2时，表示注释 "change master"

    导出的脚本部分内容如下所示：
    ```
    -- MySQL dump 10.13  Distrib 5.7.25, for el7 (x86_64)
    --
    -- Host: 192.168.199.120    Database: test
    -- ------------------------------------------------------
    -- Server version       5.7.25-log

    /*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
    /*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
    /*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
    /*!40101 SET NAMES utf8 */;
    /*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
    /*!40103 SET TIME_ZONE='+00:00' */;
    /*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
    /*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
    /*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
    /*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

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
    - MASTER_LOG_FILE='mysql-bin.000002' 表示导出数据的时候，主服务器的binary log文件为：mysql-bin.000002
    - MASTER_LOG_POS=6215 表示导出数据的时候，主服务器的binary log的位置在6215，可以在主服务器上执行`mysql> show master status;`核对该信息
    
    MASTER_LOG_FILE和MASTER_LOG_POS的值将在从服务器导入数据后，重新开始同步中用到。即将主服务器的数据导入从服务器后，从服务器应该从主服务器上'mysql-bin.000002'文件的6215位置开始同步。
2. 修改主数据库的部分数据
    修改主数据库的部分数据，模拟搭建主从的过程中，主服务器在不停的写入数据，然后再查看主服务器的binary log位置：
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
    可以看到，主数据库在修改了数据后，binary log的位置变成了6889。
3. 将导出的数据导入从数据库中
    在从数据库中执行如下命令：
    ```
    # mysql -uroot -proot --default-character-set=utf8 < test.sql
    ```

## 为从服务器设置它的master
根据主数据库导出数据脚本中的binary log文件名称（MASTER_LOG_FILE='mysql-bin.000002'）和位置（MASTER_LOG_POS=6215），设置从服务器的master。命令如下所示：
```
mysql> change master to master_host='192.168.199.120',master_port=3306,master_user='repl',master_password='repl',master_log_file='mysql-bin.000001',master_log_pos=154;
```
执行完成后，查看从服务器的状态：
```
mysql> show slave status\G;
*************************** 1. row ***************************
            Slave_IO_State: Waiting for master to send event
                Master_Host: 192.168.199.120
                Master_User: repl
                Master_Port: 3306
                Connect_Retry: 60
            Master_Log_File: mysql-bin.000002
        Read_Master_Log_Pos: 6889
            Relay_Log_File: 192-relay-bin.000002
                Relay_Log_Pos: 994
        Relay_Master_Log_File: mysql-bin.000002
            Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
        Exec_Master_Log_Pos: 6889
            Relay_Log_Space: 1199
                        ......
            Master_Server_Id: 120
                Master_UUID: eaedf947-5475-11e9-8348-000c29409eeb
            Master_Info_File: /var/lib/mysql/master.info
                    SQL_Delay: 0
        SQL_Remaining_Delay: NULL
    Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
        Master_Retry_Count: 86400
                        ...
```
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
线程`11`为发送binary log event的线程。

## 查看结果
在从服务器的数据库可以看到，导出数据后，主库更新的数据也同步到从库中了。