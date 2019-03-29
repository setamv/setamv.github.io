# CentOS 7上卸载MySQL
1. 查看是否安装了MySQL
    ```
    # rpm -qa | grep -i mysql
    ```
2. 卸载MySQL安装包
    ```
    # yum remove mysql-server ...
    ```
3. 删除残留的mysql目录或文件
    ```
    # whereis mysql
    ```