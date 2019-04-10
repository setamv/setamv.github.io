# MySQL基于GTID配置双主复制
本文是基于[MySQL基于GTID配置主从复制](MySQL基于GTID配置主从复制.md)的基础上进行的配置，所以，在执行下面的配置前，先完成“MySQL基于GTID配置主从复制”的配置。

MySQL双主复制是在主从复制的基础上，让master也可以同步slave的变化，这样，也可以对slave数据库执行更新操作，然后同步到master数据库上。这样，两台服务器相当于都是master服务器。

下面假设101为master1,102为master2，且在[MySQL基于GTID配置主从复制](MySQL基于GTID配置主从复制.md)基础上， master1已经配置好了主服务器， master2已经配置好了从服务器。

## 设置自增主键
在双主复制时，如果两台master数据库都使用自增的主键，可能会导致主键冲突，为了解决这个问题，需要为两个master设置不同的主键生成策略，下面使用奇偶数来避免主键的重复，即master1的自增主键从1开始，master2的自增主键从2开始，从步长都设置为2，这样，master1的主键都会是奇数，而master2的主键都会是偶数。

首先查看master1和master2的自增主键设置：
```
mysql> show variables like '%auto_increment%';
+--------------------------+-------+
| Variable_name            | Value |
+--------------------------+-------+
| auto_increment_increment | 1     |
| auto_increment_offset    | 1     |
+--------------------------+-------+
```

然后修改他们的自增主键设置，在master1和master2的`my.cnf`文件的`[mysqld]`段落下增加如下内容：
- master1
    ```
    [mysqld]
    ...
    character_set_server=utf8
    server-id=101
    log-bin=mysql-bin
    binlog_format=ROW
    gtid_mode=ON
    enforce_gtid_consistency=ON

    auto_increment_increment=2
    auto_increment_offset=1
    ```
- master2
    ```
    [mysqld]
    ...
    character_set_server=utf8
    server-id=102
    gtid_mode=ON
    enforce_gtid_consistency=ON

    auto_increment_increment=2
    auto_increment_offset=2
    ```

## 将master2的binlog打开
在master2的`my.cnf`配置文件的[mysqld]下增加配置项`log-bin=mysql-bin`（注意，增加配置项后需要重启数据库），其中，`mysql-bin`为binlog文件的名称，可以自定义，完整的内容如下所示：
```
[mysqld]
...
character_set_server=utf8
server-id=102
log-bin=mysql-bin
binlog_format=ROW
gtid_mode=ON
enforce_gtid_consistency=ON

auto_increment_increment=2
auto_increment_offset=2
```

## 为master2创建从服务器复制的用户
为master2创建MySQL用户“repl”，并为该用户分配主从复制的权限。从服务器使用该用户执行复制操作，后面从服务器设置master时将使用到该用户。
命令如下：
```
mysql> grant replication slave, replication client on *.* to repl@'%' identified by 'repl';
```

## 为master1设置它的master为master2
设置从服务器的master。命令如下所示（注意，下面的命令必须在从服务器停掉IO线程和SQL线程后执行，命令时：`mysql> stop slave`）：
```
mysql> change master to master_host='192.168.199.102',master_port=3306,master_user='repl',master_password='repl',master_auto_position=1;
```

## 启动master1上的IO线程和SQL线程
参见[MySQL基于binlog不影响业务配置主从复制](MySQL基于binlog不影响业务配置主从复制.md)中“启动从服务器的IO线程和SQL线程”一节


## 验证
在master1和master2上都修改数据，看数据是否能同步过去。