# CentOS7上使用tar.gz安装MySQL
使用tar.gz安装MySQL分为两步：
1. 下载解压和安装
    这一步包括下载MySQL的tar.gz安装文件、创建MySQL用户和组、解压安装包、设置环境变量
    这一步可参考MySQL官网的安装指导：[Binary Installation](https://dev.mysql.com/doc/refman/5.7/en/binary-installation.html)
2. MySQL初始化
    这一步包括初始化MySQL的数据目录、启动MySQL服务、设置MySQL的用户账号等
    这一步可参考MySQL官网的指导：[Postinstallation Setup and Testing](https://dev.mysql.com/doc/refman/5.7/en/postinstallation.html)

## 下载解压和安装
1. 下载MySQL的rpm安装包
    到MySQL官网下载对应版本的rpm安装包，MySQL官网下载地址是：[Download MySQL Community Server](https://dev.mysql.com/downloads/mysql/5.7.html#downloads)。
    注意：要下载`.tar.gz`格式的文件，如`mysql-5.7.25-el7-x86_64.tar.gz` ，`.tar`格式的为源文件，需要自行编译的。
2. 创建MySQL用户和组
    登录CentOS系统，创建MySQL用户和组：
    ```
    # groupadd mysql
    # useradd -r -g mysql -s /bin/false mysql
    ```
3. 解压tar安装包    
    将tar.gz文件解压到 /usr/local 目录（MySQL官网推荐的目录）
    ```
    # cd /usr/local
    # tar zxvf /path/to/mysql-VERSION-OS.tar.gz
    ```
4. 创建链接到刚才解压的目录
    创建MySQL链接到刚才解压的目录：
    ```
    # ln -s full-path-to-mysql-VERSION-OS mysql
    ```
    注意：full-path-to-mysql-VERSION-OS为上一步解压的目录，为/usr/local/mysql-xxxxxx
5. 设置环境变量
    将MySQL的bin目录设置到环境变量中，步骤如下：
    1. 打开/etc/profile文件
        ```
        # vi /etc/profile
        ```    
    2. 在/etc/profile文件末尾增加如下内容：
        ```
        export MYSQL_HOME=full-path-to-mysql
        export PATH=$MYSQL_HOME/bin:$PATH
        ```
        上面的full-path-to-mysql为MySQL程序的根目录，上面是放到了/usr/local/mysql-xxxx 中
    3. 编译/etc/profile文件，使上面配置的路径生效
        执行命令：
        ```
        # source /etc/profile
        ```        

## MySQL初始化
1. MySQL数据目录初始化
    MySQL数据目录初始化包括表格以及MySQL数据库文件，以下都是使用mysql用户进行的操作。
    1. 登录mysql用户，并进入mysql根目录
        注意，因为创建mysql用户时，加了`-s /bin/false`，所以，切换到mysql用户后也是root用户在执行操作
        ```    
        # su mysql
        # cd /usr/local/mysql
        ```
    2. 创建mysql-files目录，并设置目录的所有权属于mysql用户和组
        ```
        # mkdir mysql-files
        # chown mysql:mysql mysql-files
        # chmod 750 mysql-files
        ```
    3. 初始化数据目录
        使用`mysqld --initialize`初始化数据目录，并指定mysql用户
        ```
        # bin/mysqld --initialize --user=mysql
        ```
        执行过程中，会打印以下信息：
        ```
        2019-03-30T00:47:26.817988Z 0 [Warning] TIMESTAMP with implicit DEFAULT value is deprecated. Please use --explicit_defaults_for_timestamp server option (see documentation for more details).
        2019-03-30T00:47:27.046428Z 0 [Warning] InnoDB: New log files created, LSN=45790
        2019-03-30T00:47:27.104914Z 0 [Warning] InnoDB: Creating foreign key constraint system tables.
        2019-03-30T00:47:27.186307Z 0 [Warning] No existing UUID has been found, so we assume that this is the first time that this server has been started. Generating a new UUID: 63e925d4-5285-11e9-82e8-000c29bf2dbc.
        2019-03-30T00:47:27.188942Z 0 [Warning] Gtid table is not ready to be used. Table 'mysql.gtid_executed' cannot be opened.
        2019-03-30T00:47:27.191096Z 1 [Note] A temporary password is generated for root@localhost: >#U9o59:exn%
        ```
        注意，上面最后一行打印了为root用户随机分配的密码`>#U9o59:exn%`
2. 启动mysql
    使用mysqld启动mysql服务
    ```
    # mysqld --user=mysql
    ```

## 问题记录
### root用户登录报错：ERROR 2002 (HY000)
1. 问题描述
    root用户登录报如下错误信息
    ```
    ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/tmp/mysql.sock' (2)
    ```
2. 解决方法
    在这之前，需要明白mysql.sock这个文件有什么用？
    连接localhost通常通过一个Unix域套接字文件进行，一般是/tmp/mysql.sock。如果套接字文件被删除了，本地客户就不能连接。
    所以，首先查看文件`/tmp/mysql.sock`是否存在：
    ```
    # ll /tmp/mysql.sock
    ```
    发现文件`/tmp/mysql.sock`确实不存在，然后查看mysql配置文件，如果不知道配置文件的位置，可以使用以下方式查找：
    1）查看MySQL启动的时候都是从哪些位置加载配置文件，命令为：`# mysql --help | grep 'my.cnf'`
    2）搜索系统中的my.cnf文件：`# find / -name my.cnf`
    查看配置文件的内容如下所示：
    ```
    [mysqld]
    datadir=/var/lib/mysql
    socket=/var/lib/mysql/mysql.sock
    ...
    ```
    上面配置的socket为`/var/lib/mysql/mysql.sock`，然后查看文件`/var/lib/mysql/mysql.sock`是存在的，但是mysql为什么会去找`/tmp/mysql.sock`呢？（这里还没明白）
    然后将`/tmp/mysql.sock`做一个软连接到`/var/lib/mysql/mysql.sock`，并修改所有权为mysql用户组，命令如下：
    ```
    # ln -s  /var/lib/mysql/mysql.sock  /tmp/mysql.sock
    # chown mysql:mysql /tmp/mysql.sock
    ```
    然后重启mysql服务就好了

## root用户登录报错：ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: YES)
### 问题描述 
    在MySQL安装完成并启动后，第一次使用root用户和启动生成的随机密码成功登录，但是第二次登录就报如下错误信息：
    ```
    ERROR 1045 (28000): Access denied for user 'root'@'localhost' (using password: YES)
    ```
    不知道是不是第一次登录必须修改密码。
### 解决方法
1. 使用以下方式重启mysql：
    ```
    # mysqld shutdown
    # mysqld --user=mysql --skip-grant-tables --skip-networking 
    ```
2. 使用root用户登录，此时不输入密码，直接回车（相当于空密码）
    ```
    # mysql -uroot -p
    #
    ```
3. 修改root用户密码
    ```
    mysql> use mysql;
    # 5.7版本以前
    mysql> update user set password = password('新的密码') where user = 'root';
    # 5.7版本以后
    mysql> update user set authentication_string = password('新的密码') where user = 'root';
    ```
4. 重启mysql并使用新密码登录
    ```
    # mysqld shutdown
    # mysqld --user=mysql
    ```