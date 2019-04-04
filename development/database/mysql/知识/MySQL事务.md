# MySQL事务

## 事务的隔离级别
| 事务隔离级别                 | 脏读 | 不可重复读 | 幻读| |
|------------------------------|------|------------|-------|
| 读未提交（read-uncommitted） | 是   | 是         | 是    |
| 不可重复读（read-committed） | 否   | 是         | 是    |
| 可重复读（repeatable-read）  | 否   | 否         | 是    |
| 串行化（serializable）       | 否   | 否         | 否    |

可重复读时，不可重复读和幻读的区别是：对于当前事务中已经查询的数据，是可以重复读的，但是，对于当前事务未查询的数据（比如新增的记录），在下一次查询的时候是可以读出来的，这种情况是幻读。

## 查看MySQL当前的隔离级别
1. 查看MySQL配置的隔离级别
    ```
    mysql> show variables like '%isolation%';
    +-----------------------+-----------------+
    | Variable_name         | Value           |
    +-----------------------+-----------------+
    | transaction_isolation | REPEATABLE-READ |
    | tx_isolation          | REPEATABLE-READ |
    +-----------------------+-----------------+
    ```
2. 查看当前会话的隔离级别
    ```
    mysql> select @@tx_isolation;
    +-----------------+
    | @@tx_isolation  |
    +-----------------+
    | REPEATABLE-READ |
    +-----------------+
    ```
3. 查看全局的隔离级别
    ```    
    mysql> select @@global.tx_isolation;
    +-----------------------+
    | @@global.tx_isolation |
    +-----------------------+
    | REPEATABLE-READ       |
    +-----------------------+
    ```
    