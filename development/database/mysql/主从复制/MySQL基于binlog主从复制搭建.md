# MySQL基于binlog主从复制搭建


1. 设置主服务器的id
    为主服务器设置id，方法如下：
    在my.cnf配置文件的[mysqld]下增加配置项`server-id=120`，其中，`120`为主服务器的id，可以自定义，完整的内容如下所示：
    ```
    [mysqld]
    datadir=/var/lib/mysql
    socket=/var/lib/mysql/mysql.sock
    character_set_server=utf8

    server-id=120
    ```
1. 为主服务器开启binary log
    MySQL默认是关闭binary log的，根据主从复制的原理[MySQL主从复制的原理](MySQL主从复制的原理.md)，主从复制是基于binary log的，所以需要打开binary log。
    查看当前binary log是否开启，可以使用命令：
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
    6 rows in set (0.01 sec)
    ```
    可以看到，当前log_bin的状态是OFF。打开binary log的方法如下：
    在my.cnf配置文件的[mysqld]下增加配置项`log-bin=mysql-bin`，其中，`mysql-bin`为binary log文件的名称，可以自定义，完整的内容如下所示：
    ```
    [mysqld]
    datadir=/var/lib/mysql
    socket=/var/lib/mysql/mysql.sock
    character_set_server=utf8

    server-id=120
    log-bin=mysql-bin
    ```
1. 为主服务器创建MySQL用户
    为主服务器创建MySQL用户“repl”，并为该用户分配主从复制的权限。从服务器使用该用户执行复制操作，后面从服务器设置master时将使用到该用户。
    命令如下：
    ```
    mysql> grant replication slave on *.* to repl@'%' identified by 'repl';
    ```
1. 查看主服务器的master状态
    查看命令如下：
    ```
    mysql> show master status;
    +------------------+----------+--------------+------------------+-------------------+
    | File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
    +------------------+----------+--------------+------------------+-------------------+
    | mysql-bin.000001 |      154 |              |                  |                   |
    +------------------+----------+--------------+------------------+-------------------+
    1 row in set (0.00 sec)
    ```
    上面`show master status`的结果中，显示了binary log的日志文件名称`mysql-bin.000001`，以及当前的位置`154`，这里先记住，后面从服务器设置master时将使用到
1. 为从服务器设置它的master
    执行如下命令：
    ```
    mysql> change master to master_host='192.168.199.120',master_port=3306,master_user='repl',master_password='repl',master_log_file='mysql-bin.000001',master_log_pos=154;
    ```
    执行完成后，查看从服务器的状态：
    ```
    mysql> show slave status\G;
    *************************** 1. row ***************************
               Slave_IO_State: 
                  Master_Host: 192.168.199.120
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 154
               Relay_Log_File: 192-relay-bin.000001
                Relay_Log_Pos: 4
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: No
            Slave_SQL_Running: No
                            ...
             Master_Info_File: /var/lib/mysql/master.info
                            ...
    ```
    可以看到，从服务器的中继日志文件为`192-relay-bin.000001`，IO线程和SQL线程当前没有启动，通过以下命令启动IO线程和SQL线程：
    ```
    mysql> start slave;
    ```
    启动之后，再看从服务器的状态：
    ```
    mysql> show slave status\G;
    *************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.120
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 589
               Relay_Log_File: 192-relay-bin.000004
                Relay_Log_Pos: 755
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
                            ...
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 589
              Relay_Log_Space: 1268
                            ...
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 120
                  Master_UUID: eaedf947-5475-11e9-8348-000c29409eeb
             Master_Info_File: /var/lib/mysql/master.info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
           Master_Retry_Count: 86400
                            ...
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