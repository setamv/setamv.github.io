# MySQL基于GTID配置双主复制-问题解决

## 主键冲突问题
基于GTID配置双主复制后，可能会出现主键冲突的问题，特别是ID自增的时候，如果两个master对同一个表生成的自增主键相同，那么同步的过程中肯定会报错。

在发生同步错误的master上执行以下命令：
```
mysql> stop slave;
mysql> set gtid_next='xxxx:5';
mysql> begin; commit;  -- 执行一个空事务
mysql> set gtid_next=automatic;
mysql> start slave;
```