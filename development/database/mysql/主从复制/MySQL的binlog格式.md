# MySQL的binlog格式

MySQL的binlog格式有3种，分别是：ROW、STATEMENT、MIXED

## ROW格式
ROW格式下，binlog中会记录每一行数据的修改，然后slave端复制时，再对相同的数据进行修改。例如，下面的update语句更新了10000条匹配的行，日志中记录的不是这条update语句所对应的event事件（mysql是以事件的形式来记录bin-log日志），而是这个update语句所更新的每一条记录的变化情况，即binlog中将会生成10000条数据的修改记录：
```
mysql> update test set name = 'aaa';
```
### ROW格式的优点
1. 这种方式在数据库操作涉及的数据行非常多时，binlog记录的信息会非常多，从而导致binlog日志文件非常大。
### ROW格式的缺点
1. binlog中不需要记录执行的sql语句的上下文相关的信息，仅仅只需要记录哪一条记录被修改了，修改成什么样了，可以非常清楚的记录下每一行数据修改的细节


## STATEMENT格式
STATEMENT格式下，每一条修改数据的sql以及sql执行的上下文都会记录到master的bin-log中。slave在复制的时候，sql进程会解析成和原来master端执行过的相同的sql来再次执行。
### STATEMENT格式的优点
1. STATEMENT格式没有ROW格式的缺点，即当一个更新语句涉及的记录行非常多时，ROW格式会记录每一条被更新的记录的变化，所以STATEMENT格式可以减少日质量，节约IO，提高性能
### STATEMENT格式的缺点
1. 由于它是记录的执行语句，为了让这些语句在slave端也能正确执行，它还必须记录每条sql语句在执行时候的上下文信息。另外，由于mysql现在发展比较快，很多的新功能加入，使mysql的复制遇到了不小的挑战，复制的时候涉及到越复杂的内容，bug也就越容易出现。目前已经发现的就有不少情况会造成mysql的复制问题，主要是修改数据的时候使用了某些特定的函数或者功能的时候会出现，比如sleep()在有些版本就不能正确复制。

## MIXED格式
MIXED格式是ROW格式和STATEMENT格式的结合，在MIXED格式下，mysql会根据执行的每一条具体的sql语句来区分对待记录的日志形式，也就是在statement和row之间选一种。

## 查看MySQL当前的binlog格式
```
mysql> show variables like '%format%';
+---------------------------+-------------------+
| Variable_name             | Value             |
+---------------------------+-------------------+
| binlog_format             | ROW               |
| date_format               | %Y-%m-%d          |
| datetime_format           | %Y-%m-%d %H:%i:%s |
| default_week_format       | 0                 |
| innodb_default_row_format | dynamic           |
| innodb_file_format        | Barracuda         |
| innodb_file_format_check  | ON                |
| innodb_file_format_max    | Barracuda         |
| time_format               | %H:%i:%s          |
+---------------------------+-------------------+
```
从上面可以看到，binlog_format的值为ROW格式

## 修改MySQL的binlog格式
在MySQL配置文件（一般是/etc/my.cnf，也可以通过`# mysql --help | grep 'my.cnf'`查看配置文件的位置）中的[mysqld]一段中增加如下配置：
```
[mysqld]
binlog_format=ROW  # 或者是 STATEMENT 、 MIXED
```

## 查看binlog日志文件的内容
首先查看binlog日志文件的位置，命令如下：
```
mysql> show master status;
+------------------+----------+--------------+------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+------------------+----------+--------------+------------------+-------------------+
| mysql-bin.000003 |     1164 |              |                  |                   |
+------------------+----------+--------------+------------------+-------------------+
```
从上面的结果中，得到当前binlog日志文件为“mysql-bin.000003”，在目录`/var/lib/mysql`下。

查看binlog日志文件内容的命令为`mysqlbinlog`，如下所示：
```
# mysqlbinlog -v --base64-output=DECODE-ROWS --/var/lib/mysql/mysql-bin.000003
```
其中：
- -v 指定将base64格式的内容进行解码
- --base64-output=DECODE-ROWS 用于过滤输一些特殊的内容，类似下面的这种内容：
    ```
    T8h/XA9lAAAAdwAAAHsAAAABAAQANS43LjI1LWxvZwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    AAAAAAAAAAAAAAAAAABPyH9cEzgNAAgAEgAEBAQEEgAAXwAEGggAAAAICAgCAAAACgoKKioAEjQA
    AZFaOjg=
    ```

假设当前test表的内容如下所示：
```
mysql> select * from test;
+----+---------+
| id | name    |
+----+---------+
|  1 | setamv. |
|  2 | susie.  |
+----+---------+
```
下面分别在ROW格式和statement格式下，执行语句`update test set name = concat(name, '.');`，然后查看binlog中的记录情况。

1. ROW格式
```
# mysqlbinlog -v --base64-output=DECODE-ROWS /var/lib/mysql/mysql-bin.000004
...
SET TIMESTAMP=1551883502/*!*/;
SET @@session.pseudo_thread_id=3/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1436549152/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8 *//*!*/;
SET @@session.character_set_client=33,@@session.collation_connection=33,@@session.collation_server=33/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 291
#190306 22:45:02 server id 101  end_log_pos 341 CRC32 0x63e699fb        Table_map: `test`.`test` mapped to number 108
# at 341
#190306 22:45:02 server id 101  end_log_pos 449 CRC32 0xd4f93b2b        Update_rows: table id 108 flags: STMT_END_F
### UPDATE `test`.`test`
### WHERE
###   @1=1
###   @2='setamv.'
### SET
###   @1=1
###   @2='setamv..'
### UPDATE `test`.`test`
### WHERE
###   @1=2
###   @2='susie.'
### SET
###   @1=2
###   @2='susie..'
# at 449
#190306 22:45:02 server id 101  end_log_pos 480 CRC32 0xab7a1cc9        Xid = 11
...
```
可以看到，binlog中记录了两条数据的改变

2. STATEMENT格式
```
# mysqlbinlog -v --base64-output=DECODE-ROWS /var/lib/mysql/mysql-bin.000004
...
use `test`/*!*/;
...
SET TIMESTAMP=1551882792/*!*/;
UPDATE test SET name = CONCAT(name, '.')
...
```
可以看到，binlog中只记录了update语句