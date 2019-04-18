# MySQL基于GTID配置多源复制
多源复制是指一个slave从多个master上同步复制数据，多源复制架构中，slave是通过channel的方式向多个master同步数据。

下面以两台master（分别为master1、master2），一台slave为例讲解多源复制的配置，他们的配置和关系如下所示：
- master1 服务器IP为 192.168.199.110
- master2 服务器IP为 192.168.199.111
- slave 服务器IP为 192.168.199.112

master1和master2配置为双主，两边可以相互同步；slave配置为master1和master2的从服务器，同时同步master1和master2的数据。

环境搭建的顺序基本和[MySQL基于GTID配置主从复制](MySQL基于GTID配置主从复制.md)一致，不同的地方在下面指出。

## master1的配置
```
[mysqld]
...
character_set_server=utf8
server-id=110
log_bin=mysql-bin
gtid_mode=ON
enforce_gtid_consistency=ON

auto_increment_increment=2
auto_increment_offset=1
```
应为双主复制容易出现主键冲突，上面将主键的自增和偏移量做设置，保证master1和master2上的自增主键刚好形成奇偶间隔。

## master2的配置
```
[mysqld]
...
character_set_server=utf8
server-id=111
log_bin=mysql-bin
gtid_mode=ON
enforce_gtid_consistency=ON

auto_increment_increment=2
auto_increment_offset=2
```

## master1和master2设置双主复制
在master1上执行如下命令，将master1设置为master2的从服务器：
```
mysql> change master to master_host='192.168.199.111',master_port=3306,master_user='repl',master_password='repl', master_auto_position=1;
```

在master2上执行如下命令，将master2设置为master1的从服务器：
```
mysql> change master to master_host='192.168.199.110',master_port=3306,master_user='repl',master_password='repl', master_auto_position=1
```

## slave的配置
```
[mysqld]
...
server-id=112
gtid_mode=ON
enforce_gtid_consistency=ON
master_info_repository=TABLE
relay_log_info_repository=TABLE
```
说明：
- `master_info_repository=TABLE` 多源复制中，slave基于channel的方式同步多个master的数据，这种方式要求`master_info_repository`必须设置为`TABLE`的方式
- `master_info_repository=TABLE` 同`master_info_repository=TABLE`

slave设置master时的命令如下所示：
```
mysql> change master to master_host='192.168.199.110',master_port=3306,master_user='repl',master_password='repl', master_auto_position=1 FOR CHANNEL 'master-1';
mysql> change master to master_host='192.168.199.111',master_port=3306,master_user='repl',master_password='repl', master_auto_position=1 FOR CHANNEL 'master-2';
```
注意，上面末尾的`FOR CHANNEL 'master-1'`与[MySQL基于GTID配置主从复制](MySQL基于GTID配置主从复制.md)中的“为从服务器设置它的master”一节稍有不同。
设置完两个master信息后，通过以下方式查看master的信息：
```
mysql> show slave status\G;
*************************** 1. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.110
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 1041
               Relay_Log_File: 192-relay-bin-master@002d1.000002
                Relay_Log_Pos: 1254
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
                    ....
             Master_Server_Id: 110
                  Master_UUID: 6e826831-44d1-11e9-84fe-000c29c9014b
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
           Retrieved_Gtid_Set: 6e826831-44d1-11e9-84fe-000c29c9014b:1-4
            Executed_Gtid_Set: 6e826831-44d1-11e9-84fe-000c29c9014b:1-4,
f780a852-44d2-11e9-8a8e-000c29f84966:1-3
                Auto_Position: 1
         Replicate_Rewrite_DB: 
                 Channel_Name: master-1
           Master_TLS_Version: 
*************************** 2. row ***************************
               Slave_IO_State: Waiting for master to send event
                  Master_Host: 192.168.199.111
                  Master_User: repl
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 889
               Relay_Log_File: 192-relay-bin-master@002d2.000002
                Relay_Log_Pos: 1102
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: Yes
            Slave_SQL_Running: Yes
                        ...
             Master_Server_Id: 111
                  Master_UUID: f780a852-44d2-11e9-8a8e-000c29f84966
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: Slave has read all relay log; waiting for more updates
                        ...
           Retrieved_Gtid_Set: f780a852-44d2-11e9-8a8e-000c29f84966:1-3
            Executed_Gtid_Set: 6e826831-44d1-11e9-84fe-000c29c9014b:1-4,
f780a852-44d2-11e9-8a8e-000c29f84966:1-3
                Auto_Position: 1
         Replicate_Rewrite_DB: 
                 Channel_Name: master-2
           Master_TLS_Version: 
2 rows in set (0.00 sec)
```
可以看到，上面有2个master，并且在最下面可以通过`Channel_Name`看到两个channel的名称。