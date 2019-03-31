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
4. 有时候，CentOS 7上默认会安装Maria DB，也需要卸载，方法如下：
    ```
    # rpm -qa | grep -i mariadb         # 找出已经安装的mariadb
    # rpm -e --nodeps mariadb-xxxx      # --nodeps是强制卸载的选项，有时候mariadb的包被其他程序依赖会删不掉，所以需要强制卸载
    ```