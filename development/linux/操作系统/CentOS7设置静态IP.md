# CentOS7配置静态IP
1. 找到网卡配置文件
    到目录`vi /etc/sysconfig/network-scripts`下找你网卡对应的配置文件，比如是：`/etc/sysconfig/network-scripts/ifcfg-enos6`
2. 设置静态IP信息
    使用vi编辑上面的配置文件，修改或增加如下内容：
    ```
    BOOTPROTO=static        #dhcp改为static（修改）
    ONBOOT=yes              #开机启用本配置，一般在最后一行（修改）

    IPADDR=192.168.1.204    #静态IP（增加）
    GATEWAY=192.168.1.2     #默认网关，虚拟机安装的话，通常是2，也就是VMnet8的网关设置（增加）
    NETMASK=255.255.255.0   #子网掩码（增加）
    DNS1=192.168.1.2        #DNS 配置，虚拟机安装的话，DNS就网关就行，多个DNS网址的话再增加（增加）
    ```
3. 重启网络服务
    命令如下：`service network restart`    