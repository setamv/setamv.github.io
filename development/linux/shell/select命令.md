[首页](/index.md) << ... << [索引](index.md)

# shell中的select命令
select命令用于提示用户从指定的选项中进行选择，例如，有三个选项可供用户选择：1）选项1；2）选项2；3）选项3
使用select，将在界提示选项列表，用户只需要选择选项的数字序号即可。

## 语法格式
    PS3=选择提示信息
    select var in 选项1 选项2 ... 选项n;
    do
        执行语句
    done

## 示例
+ 示例代码
    ```
    [root@centos-7-192 shell]# vi select.sh
    #!/bin/bash

    # 提示选择需要安装的程序
    PS3='请选择需要安装的程序: '
    select app in "apache tomcat" "mysql" "redis";
    do
            case $app in
                    "apache tomcat")
                    echo 'start install apache tomcat'
                    ;;
            
                    "mysql")
                    echo 'start install mysql'
                    ;;

                    "redis")
                    echo 'start install redis'
                    ;;
            esac
    done
    ```
+ 执行结果
    ```
    [root@centos-7-192 shell]# ./select.sh 
    1) apache tomcat
    2) mysql
    3) redis
    请选择需要安装的程序: 1
    start install apache tomcat
    请选择需要安装的程序: 2
    start install mysql
    请选择需要安装的程序: 3
    start install redis
    请选择需要安装的程序: ^C
    ```