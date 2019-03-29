[首页](/index.md) << ... << [上一级](../index.md)

# mysql用户开放远程连接权限

Mysql为了安全性，在默认情况下用户只允许在本地登录，如果要为用户开放远程连接权限，需要进行授权：

1. 不限定远程连接的条件时，可以使用如下方式：
    ```
    GRANT ALL PRIVILEGES ON 数据库名称.* TO '用户名称'@'%' IDENTIFIED BY '远程连接的密码' WITH GRANT OPTION;
    ```

2. 只允许用户在指定ip远程连接：
    ```
    GRANT ALL PRIVILEGES ON 数据库名称.* TO '用户名称'@'172.16.16.152' IDENTIFIED BY '远程连接的密码' WITH GRANT OPTION;
    ```