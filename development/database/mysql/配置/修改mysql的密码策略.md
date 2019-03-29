[首页](/index.md) << ... << [上一级](../index.md)

# 修改mysql的密码策略
有时候修改mysql用户的密码，会遇到以下错误：

mysql>  ALTER USER USER() IDENTIFIED BY '12345678';
ERROR 1819 (HY000): Your password does not satisfy the current policy requirements

这个其实与validate_password_policy的值有关。

validate_password_policy有以下取值：

|Policy|	Tests |Performed|
|---
|0 or LOW|	Length|
|1 or MEDIUM|	Length; numeric, lowercase/uppercase, and special characters|
|2 or STRONG|	Length; numeric, lowercase/uppercase, and special characters; dictionary file|
默认是1，即MEDIUM，所以刚开始设置的密码必须符合长度，且必须含有数字，小写或大写字母，特殊字符。

有时候，只是为了自己测试，不想密码设置得那么复杂，譬如说，我只想设置root的密码为123456。

必须修改两个全局参数：

首先，修改validate_password_policy参数的值

mysql> set global validate_password_policy=0;
Query OK, 0 rows affected (0.00 sec)
这样，判断密码的标准就基于密码的长度了。这个由validate_password_length参数来决定。

复制代码
mysql> select @@validate_password_length;
+----------------------------+
| @@validate_password_length |
+----------------------------+
|                          8 |
+----------------------------+
1 row in set (0.00 sec)
复制代码
validate_password_length参数默认为8，它有最小值的限制，最小值为：

validate_password_number_count
+ validate_password_special_char_count
+ (2 * validate_password_mixed_case_count)
其中，validate_password_number_count指定了密码中数据的长度，validate_password_special_char_count指定了密码中特殊字符的长度，validate_password_mixed_case_count指定了密码中大小字母的长度。

这些参数，默认值均为1，所以validate_password_length最小值为4，如果你显性指定validate_password_length的值小于4，尽管不会报错，但validate_password_length的值将设为4。如下所示：

复制代码
mysql> select @@validate_password_length;
+----------------------------+
| @@validate_password_length |
+----------------------------+
|                          8 |
+----------------------------+
1 row in set (0.00 sec)

mysql> set global validate_password_length=1;
Query OK, 0 rows affected (0.00 sec)

mysql> select @@validate_password_length;
+----------------------------+
| @@validate_password_length |
+----------------------------+
|                          4 |
+----------------------------+
1 row in set (0.00 sec)
复制代码
如果修改了validate_password_number_count，validate_password_special_char_count，validate_password_mixed_case_count中任何一个值，则validate_password_length将进行动态修改。

复制代码
mysql> select @@validate_password_length;
+----------------------------+
| @@validate_password_length |
+----------------------------+
|                          4 |
+----------------------------+
1 row in set (0.00 sec)

mysql> select @@validate_password_mixed_case_count;
+--------------------------------------+
| @@validate_password_mixed_case_count |
+--------------------------------------+
|                                    1 |
+--------------------------------------+
1 row in set (0.00 sec)

mysql> set global validate_password_mixed_case_count=2;
Query OK, 0 rows affected (0.00 sec)

mysql> select @@validate_password_mixed_case_count;
+--------------------------------------+
| @@validate_password_mixed_case_count |
+--------------------------------------+
|                                    2 |
+--------------------------------------+
1 row in set (0.00 sec)

mysql> select @@validate_password_length;
+----------------------------+
| @@validate_password_length |
+----------------------------+
|                          6 |
+----------------------------+
1 row in set (0.00 sec)
复制代码
当然，前提是validate_password插件必须已经安装，MySQL5.7是默认安装的。

那么如何验证validate_password插件是否安装呢？可通过查看以下参数，如果没有安装，则输出将为空。

复制代码
mysql> SHOW VARIABLES LIKE 'validate_password%';
+--------------------------------------+-------+
| Variable_name                        | Value |
+--------------------------------------+-------+
| validate_password_dictionary_file    |       |
| validate_password_length             | 6     |
| validate_password_mixed_case_count   | 2     |
| validate_password_number_count       | 1     |
| validate_password_policy             | LOW   |
| validate_password_special_char_count | 1     |
+--------------------------------------+-------+
6 rows in set (0.00 sec)