# CentOS7设置国内的yum源
## 设置网易的yum源
参考网易yum源的官方说明，地址：http://mirrors.163.com/.help/centos.html
步骤：
1. 首先备份/etc/yum.repos.d/CentOS-Base.repo
    备份命令：`mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup`

2. 下载对应版本repo文件, 放入/etc/yum.repos.d/(操作前请做好相应备份)
    http://mirrors.163.com/.help/CentOS7-Base-163.repo

3. 运行以下命令生成缓存
    ```
    yum clean all
    yum makecache
    ```
# 安装ifconfig命令
如果安装CentOS的过程中，选择的最小安装包，可能系统会找不到ifconfig命令，此时要安装ifconfig，步骤如下：
1. 查看ifconfig命令是哪个安装包提供的
    命令如下：`yum provides ifconfig`，这里注意，有些命令是没有单独的rpm安装包的，是被包含在其他工具包里，这是用要查看哪些工具包提供该命令，可以使用命令（以 netstat 为例）`yum provides *netstat`
    命令的输出结果如下：
    ```
    Loaded plugins: fastestmirror
    Loading mirror speeds from cached hostfile
    * base: centos.aol.in
    * extras: centos.aol.in
    * updates: centos.aol.in
    net-tools-2.0-0.17.20131004git.el7.x86_64 : Basic networking tools
    Repo        : @base
    Matched from:
    Filename    : /usr/sbin/ifconfig
    ```
    从上面可以看出来，net-tools安装包中包含了ifconfig命令
2. 安装ifconfig
    命令如下：`yum install net-tools`

