[首页](/index.md) << ... << [上一级](../index.md)

# 修改mysql的root密码
为了加强安全性，MySQL5.7为root用户随机生成了一个密码，在error log中，关于error log的位置，如果安装的是RPM包，则默认是/var/log/mysqld.log。

一般可通过log_error设置

复制代码
mysql> select @@log_error;
+---------------------+
| @@log_error         |
+---------------------+
| /var/log/mysqld.log |
+---------------------+
1 row in set (0.00 sec)
复制代码
可通过# grep "password" /var/log/mysqld.log 命令获取MySQL的临时密码

有些时候，忘记了mysql的root用户密码怎么办？可以通过以下方法来修改mysql的root用户密码。

1. 让mysql以不需要密码的方式启动
    打开mysql配置文件（centos一般是/etc/my.cnf文件），在末尾增加以下内容：
    ```
    skip-grant-tables
    ```
    或者，在mysqld启动参数中增加：`# mysqld --user=mysql --skip-grant-tables --skip-networking `
2. 重启mysql
3. 使用root用户登录mysql
    在配置文件中指定了`skip-grant-tables`后，就可以不使用密码登录mysql了。所以可以直接输入空密码登录。
4. 修改mysql的root用户密码
    mysql的用户密码是保存在mysql数据库示例的`user`表中，在5.7版本之前，用户密码是保存`user`表的`password`字段中，而在5.7版本（包括5.7版本）以后，密码存到了`authentication_string`中了。
    可以使用以下语句直接更新root用户的密码：
    ```
    # 5.7版本以前
    update user set password = password('新的密码') where user = 'root';
    # 5.7版本以后
    update user set authentication_string = password('新的密码') where user = 'root';
    ```
5. 修改mysql配置文件，去掉`skip-grant-tables`，并重启
6. 使用新的密码登录