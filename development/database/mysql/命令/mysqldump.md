# mysqldump
mysqldump用于从mysql导出数据，该命令有很多的选项，详情可以通过`# man mysqldump`查看

## --master-data[=value]
man帮助内容原文如下所示：
```
Use this option to dump a master replication server to produce a dump file that can be used to set up another server as a slave of the master. It causes the dump output to include a CHANGE MASTER TO statement that indicates the binary log coordinates (file name and position) of the dumped server. These are the master server coordinates from which the slave should start replicating after you load the dump file into the slave.

If the option value is 2, the CHANGE MASTER TO statement is written as an SQL comment, and thus is informative only; it has no effect when the dump file is reloaded. If the option value is 1, the statement is not written as a comment and takes effect when the dump file is reloaded. If no option value is specified, the default value is 1.

This option requires the RELOAD privilege and the binary log must be enabled.

The --master-data option automatically turns off --lock-tables. It also turns on --lock-all-tables, unless --single-transaction also is specified, in which case, a global read lock is acquired only for a short time at the beginning of the dump (see the description for --single-transaction). In all cases, any action on logs happens at the exact moment of the dump.

It is also possible to set up a slave by dumping an existing slave of the master, using the --dump-slave option, which overrides --master-data and causes it to be ignored if both options are used.
```
说明：
1. 该选项用于导出主从结构的主库数据，导出的数据可以用于建立从库。
    使用该选项，会在导出数据的文件中多一行内容，如下所示：
    ```
    -- CHANGE MASTER TO MASTER_LOG_FILE='mysql-bin.000002', MASTER_LOG_POS=6889;
    ```
    上面的内容，当`--master-data=1`时，最前面不会有注释；当`--master-data=2`时才有注释。
    这一行内容的信息包括主库导出数据时的binary log文件名称和位置信息，可以在主库上使用`mysql> show master status\G;`查看。
2. 该选项必须在bianry log选项被打开的时候才能生效
    因为MySQL中从复制需要使用binary log，所以binary log必须打开才能生效，查看binary log是否打开，方法如下所示：
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
    上面的log_bin的值为OFF时，表示binary log被关闭了，通过在MySQL配置文件的[mysqld]下增加一行内容`log-bin=mysql-bin`来打开binary log。
3. 
    
## --lock-all-tables, -x
man帮助内容原文如下所示：
```
Lock all tables across all databases. This is achieved by acquiring a global read lock for the duration of the whole dump. This option automatically turns off --single-transaction and --lock-tables.
```

## --lock-tables, -l
man帮助内容原文如下所示：
```
For each dumped database, lock all tables to be dumped before dumping them. The tables are locked with READ LOCAL to permit concurrent inserts in the case of MyISAM tables. For transactional tables such as InnoDB, --single-transaction is a much better option than --lock-tables because it does not need to lock the tables at all.

Because --lock-tables locks tables for each database separately, this option does not guarantee that the tables in the dump file are logically consistent between databases. Tables in different databases may be dumped in completely different states.

Some options, such as --opt, automatically enable --lock-tables. If you want to override this, use --skip-lock-tables at the end of the option list.
```

## --single-transaction
man帮助内容原文如下所示：
```
This option sets the transaction isolation mode to REPEATABLE READ and sends a START TRANSACTION SQL statement to the server before dumping data. It is useful only with transactional tables such as InnoDB, because then it dumps the consistent state of the database at the time when START TRANSACTION was issued without blocking any applications.

When using this option, you should keep in mind that only InnoDB tables are dumped in a consistent state. For example, any MyISAM or MEMORY tables dumped while using this option may still change state.

While a --single-transaction dump is in process, to ensure a valid dump file (correct table contents and binary log coordinates), no other connection should use the following statements: ALTER TABLE, CREATE TABLE, DROP TABLE, RENAME TABLE, TRUNCATE TABLE. A consistent read is not isolated from those statements, so use of them on a table to be dumped can cause the SELECT that is performed by mysqldump to retrieve the table contents to obtain incorrect contents or fail.

The --single-transaction option and the --lock-tables option are mutually exclusive because LOCK TABLES causes any pending transactions to be committed implicitly.

To dump large tables, combine the --single-transaction option with the --quick option.

Option Groups
    ·   The --opt option turns on several settings that work together to perform a fast dump operation. All of these settings are on by default, because --opt is on by default. Thus you rarely if ever specify --opt. Instead, you can turn these settings off as a group by specifying --skip-opt, the optionally re-enable certain settings by specifying the associated options later on the command line.

    ·   The --compact option turns off several settings that control whether optional statements and comments appear in the output. Again, you can follow this option with other options that re-enable certain settings, or turn all the settings on by using the --skip-compact form.
```

注意，上面`--master-data`选项的说明中有一段内容如下：
```
The --master-data option automatically turns off --lock-tables. It also turns on --lock-all-tables, unless --single-transaction also is specified, in which case, a global read lock is acquired only for a short time at the beginning of the dump
```
即，如果指定了`--single-transaction`选项，将会获取一个全局的只读锁，同时，`--single-transaction`选项会设置事务的隔离级别为`REPEATABLE READ`（即不会出现脏读），所以如果在导出数据的过程中加上该选项，可以保证导出的过程中数据库不会更新新的数据，相当于数据库变成了一个只读的数据库（除了ALTER TABLE, CREATE TABLE, DROP TABLE, RENAME TABLE, TRUNCATE TABLE命令以外）。