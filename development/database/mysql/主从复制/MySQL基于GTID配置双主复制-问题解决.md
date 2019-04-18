# MySQL基于GTID配置双主复制-问题解决

## 主键冲突问题
基于GTID配置双主复制后，可能会出现主键冲突的问题，特别是ID自增的时候，如果两个master对同一个表生成的自增主键相同，那么同步的过程中肯定会报错。当slave同步master的过程中发生错误时，slave的SQL进程将会停止运行，必须等到错误被修复以后，才能继续恢复运行。

当发生主键冲突问题的时候，需要在slave库上执行以下操作进行恢复：
1. 停掉slave的IO线程和SQL线程
    使用如下命令停掉slave：
    ```
    mysql> stop slave;
    ```
2. 重新设置`gtid_next`的参数值
    首先，需要查看当前的gtid的值，命令如下：
    ```
    mysql> show variables like '%gtid%';
    +----------------------------------+------------------------------------------------------------------------------------+
    | Variable_name                    | Value                                                                              |
    +----------------------------------+------------------------------------------------------------------------------------+
    | binlog_gtid_simple_recovery      | ON                                                                                 |
    | enforce_gtid_consistency         | ON                                                                                 |
    | gtid_executed_compression_period | 1000                                                                               |
    | gtid_mode                        | ON                                                                                 |
    | gtid_next                        | AUTOMATIC                                                                          |
    | gtid_owned                       |                                                                                    |
    | gtid_purged                      | 6e826831-44d1-11e9-84fe-000c29c9014b:1-7,
    f780a852-44d2-11e9-8a8e-000c29f84966:1-3 |
    | session_track_gtids              | OFF                                                                                |
    +----------------------------------+------------------------------------------------------------------------------------+
    ```

    可以看到上面有一个参数`gtid_purged`和`gtid_next`，将`gtid_next`的值设置为参数`gtid_purged`的值的下一个，如上，下一个值为：`6e826831-44d1-11e9-84fe-000c29c9014b:8`，完整的命令如下所示：
    ```
    mysql> set gtid_next='6e826831-44d1-11e9-84fe-000c29c9014b:8';
    ```

    然后恢复`gtid_next`的参数值为"AUTOMATIC"：
    ```
    mysql> set gtid_next=AUTOMATIC;
    ```
3. 启动slave
    ```
    mysql> start slave;
    ```