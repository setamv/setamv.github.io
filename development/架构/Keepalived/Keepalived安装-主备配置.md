# Keepalived概述
Keepalived是一个通过虚拟IP漂移实现主备HA的开源组件。

以下的示例都是基于CentOS7系统。

# 准备工作
## 准备两台服务器，一台作为Mater节点，一台作为Backup节点
假设准备的两台服务器作为两个节点，其节点名称和IP对应关系如下：
+ 节点1（以下简称Node-1），ip：192.168.199.121（设为Master节点）
+ 节点2（以下简称Node-2），ip：192.168.199.122（设为Backup节点）

## 安装并启动nginx
在两个节点上都安装Nginx，作为验证使用。
启动Nginx：`# nginx`

## 防火墙设置
keepalived使用 224.0.0.18作为VRRP协议的组播地址， 所以，需要在防火墙中放开改ip地址的访问限制。
下面分别从firewall和iptables两种CentOS最常用的防火墙讲述如何配置。
另外，作为验证，需要开放Nginx默认的80端口

### firewall设置
1. 开放VRRP协议的组播地址
    `# firewall-cmd --direct --permanent --add-rule ipv4 filter INPUT 0 --in-interface ens33 --destination 224.0.0.18 --protocol vrrp -j ACCEPT`
    其中，ens33位网络接口名称，需要根据机器的具体网络接口进行设置（通过`# ip a`命令查看）。

2. 开放80端口，命令如下：
    ```
    # firewall-cmd --zone=public --add-port=80/tcp --permanent
    # firewall-cmd --reload     # 重新加载firewall的过滤规则
    ```
    最后，通过访问ip地址，检查nginx和防火墙是否都启动和配置OK了。如Node-1通过访问：192.168.199.121

### iptables设置

# 安装keepalived

## 安装keepalived需要的依赖包
```
# yum -y install openssl-devel
# yum -y install popt-devel
# yum -y install ipvsadm
# yum -y install libnl*
```

## 下载并安装keepalived
`# yum install keepalived`

# 配置keepalived的主备
下面对主备节点的keepalived配置文件进行配置，需要注意以下几个信息：
+ virtual_ipaddress，即虚拟IP地址，主备节点的虚拟IP地址配置相同，都为：192.168.199.128
+ priority，即优先级，一般Mater节点的优先级高一些，值设置的大一些。

编辑Node-1节点的配置文件，配置文件位置：/etc/keepalived/keepalived.conf，内容如下：
```
global_defs {
   notification_email {

   }
   notification_email_from setamv@126.com
   smtp_server smpt.126.com
   smtp_connect_timeout 30
   router_id LVS_1
   vrrp_skip_check_adv_addr
   vrrp_strict
   vrrp_garp_interval 0
   vrrp_gna_interval 0
}

vrrp_instance VI_1 {
    state MASTER            # Mater节点标识
    interface ens33
    virtual_router_id 51
    priority 101
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.199.128/24      # 虚拟IP地址
    }
}
```

在Node-2节点上，编辑配置文件 /etc/keepalived/keepalived.conf，内容如下：
```
global_defs {
   notification_email {

   }
   notification_email_from setamv@126.com
   smtp_server smpt.126.com
   smtp_connect_timeout 30
   router_id LVS_1
   vrrp_skip_check_adv_addr
   vrrp_strict
   vrrp_garp_interval 0
   vrrp_gna_interval 0
}

vrrp_instance VI_1 {
    state BACKUP            # Mater节点标识
    interface ens33
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.199.128/24          # 虚拟IP地址
    }
}
```


# 启动Keepalived

## 启动命令
 `# service keepalived start` 

## 启动日志查看 
可以通过日志 `# tail -f /var/log/messages` 查看keepalived的启动过程。
可以看到，Node-1上的日志如下所示：
```
Apr 28 05:24:47 localhost systemd: Starting LVS and VRRP High Availability Monitor...
Apr 28 05:24:47 localhost Keepalived[1644]: Starting Keepalived v1.3.5 (03/19,2017), git commit v1.3.5-6-g6fa32f2
Apr 28 05:24:47 localhost Keepalived[1644]: Unable to resolve default script username 'keepalived_script' - ignoring
Apr 28 05:24:47 localhost Keepalived[1644]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:47 localhost Keepalived[1645]: Starting Healthcheck child process, pid=1646
Apr 28 05:24:47 localhost Keepalived[1645]: Starting VRRP child process, pid=1647
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: Registering Kernel netlink reflector
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: Registering Kernel netlink command channel
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: Registering gratuitous ARP shared channel
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:47 localhost systemd: Started LVS and VRRP High Availability Monitor.
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: (VI_1): Cannot start in MASTER state if not address owner
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) removing protocol VIPs.
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) removing protocol iptable drop rule
Apr 28 05:24:47 localhost kernel: IPVS: Registered protocols (TCP, UDP, SCTP, AH, ESP)
Apr 28 05:24:47 localhost kernel: IPVS: Connection hash table configured (size=4096, memory=64Kbytes)
Apr 28 05:24:47 localhost kernel: IPVS: Creating netns size=2040 id=0
Apr 28 05:24:47 localhost kernel: IPVS: ipvs loaded.
Apr 28 05:24:47 localhost Keepalived_healthcheckers[1646]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: Using LinkWatch kernel netlink reflector...
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) Entering BACKUP STATE
Apr 28 05:24:47 localhost Keepalived_vrrp[1647]: VRRP sockpool: [ifindex(2), proto(112), unicast(0), fd(10,11)]
Apr 28 05:24:51 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) Transition to MASTER STATE
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) Entering MASTER STATE
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) setting protocol iptable drop rule
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) setting protocol VIPs.
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: Sending gratuitous ARP on ens33 for 192.168.199.128
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: VRRP_Instance(VI_1) Sending/queueing gratuitous ARPs on ens33 for 192.168.199.128
Apr 28 05:24:52 localhost Keepalived_vrrp[1647]: Sending gratuitous ARP on ens33 for 192.168.199.128
```
其中，”Entering MASTER STATE” 表示当前节点为Master节点。

Node-2节点的日志如下：
```
Apr 28 05:24:55 localhost systemd: Starting LVS and VRRP High Availability Monitor...
Apr 28 05:24:55 localhost Keepalived[12070]: Starting Keepalived v1.3.5 (03/19,2017), git commit v1.3.5-6-g6fa32f2
Apr 28 05:24:55 localhost Keepalived[12070]: Unable to resolve default script username 'keepalived_script' - ignoring
Apr 28 05:24:55 localhost Keepalived[12070]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:55 localhost Keepalived[12071]: Starting Healthcheck child process, pid=12072
Apr 28 05:24:55 localhost Keepalived[12071]: Starting VRRP child process, pid=12073
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: Registering Kernel netlink reflector
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: Registering Kernel netlink command channel
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: Registering gratuitous ARP shared channel
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:55 localhost systemd: Started LVS and VRRP High Availability Monitor.
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) removing protocol VIPs.
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) removing protocol iptable drop rule
Apr 28 05:24:55 localhost kernel: IPVS: Registered protocols (TCP, UDP, SCTP, AH, ESP)
Apr 28 05:24:55 localhost kernel: IPVS: Connection hash table configured (size=4096, memory=64Kbytes)
Apr 28 05:24:55 localhost kernel: IPVS: Creating netns size=2040 id=0
Apr 28 05:24:55 localhost kernel: IPVS: ipvs loaded.
Apr 28 05:24:55 localhost Keepalived_healthcheckers[12072]: Opening file '/etc/keepalived/keepalived.conf'.
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: Using LinkWatch kernel netlink reflector...
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) Entering BACKUP STATE
Apr 28 05:24:55 localhost Keepalived_vrrp[12073]: VRRP sockpool: [ifindex(2), proto(112), unicast(0), fd(10,11)]
```
其中，”Entering BACKUP STATE” 表示当前节点为Backup节点。

## 启动后虚拟IP查看
通过命令 `# ip a` 可以在Node-1节点上看到如下信息：
```
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:37:53:ad brd ff:ff:ff:ff:ff:ff
    inet 192.168.199.121/24 brd 192.168.199.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.199.128/24 scope global secondary ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::93a7:1de2:d3cd:5004/64 scope link 
       valid_lft forever preferred_lft forever
    inet6 fe80::e398:bb9d:23e2:90da/64 scope link tentative dadfailed 
       valid_lft forever preferred_lft forever
```
其中，inet 192.168.199.128/24 就是绑定的虚拟IP，表示在Mater节点上已经启用了虚拟IP，此时如果通过虚拟IP访问，将会访问到Master节点上。
可以通过访问：192.168.199.128，并查看Node-1节点上Nginx的日志，确定请求最终由Node-1节点处理。

## 通过tcpdump查看VRRP协议的组播消息
通过命令：`# tcpdump -i ens33 dst 224.0.0.18`，可以看到从 224.0.0.18 收到的消息包以及发往 224.0.0.18 的组播消息，抓包结果如下所示：
1、Node-1节点的抓包数据：
```
[root@localhost keepalived]# tcpdump -i ens33 dst 224.0.0.18
tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
listening on ens33, link-type EN10MB (Ethernet), capture size 262144 bytes
05:33:46.476691 IP localhost.localdomain > vrrp.mcast.net: VRRPv2, Advertisement, vrid 51, prio 101, authtype simple, intvl 1s, length 20
05:33:47.477936 IP localhost.localdomain > vrrp.mcast.net: VRRPv2, Advertisement, vrid 51, prio 101, authtype simple, intvl 1s, length 20
05:33:48.479186 IP localhost.localdomain > vrrp.mcast.net: VRRPv2, Advertisement, vrid 51, prio 101, authtype simple, intvl 1s, length 20
```

2、Node-2节点的抓包数据：
```
[root@localhost keepalived]# tcpdump -i ens33 dst 224.0.0.18
tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
listening on ens33, link-type EN10MB (Ethernet), capture size 262144 bytes
05:34:14.533795 IP 192.168.199.121 > vrrp.mcast.net: VRRPv2, Advertisement, vrid 51, prio 101, authtype simple, intvl 1s, length 20
05:34:15.534574 IP 192.168.199.121 > vrrp.mcast.net: VRRPv2, Advertisement, vrid 51, prio 101, authtype simple, intvl 1s, length 20
```

# 验证主备切换
在Node-1节点上执行命令 `# service stop keepalived` 将keepalived关闭，此时，可以看到Node-2节点上的日志 /var/log/messages 中打印如下信息：
```
Apr 28 05:35:56 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) Transition to MASTER STATE
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) Entering MASTER STATE
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) setting protocol iptable drop rule
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) setting protocol VIPs.
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: Sending gratuitous ARP on ens33 for 192.168.199.128
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: VRRP_Instance(VI_1) Sending/queueing gratuitous ARPs on ens33 for 192.168.199.128
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: Sending gratuitous ARP on ens33 for 192.168.199.128
Apr 28 05:35:57 localhost Keepalived_vrrp[12073]: Sending gratuitous ARP on ens33 for 192.168.199.128
```
说明，Node-2节点已经成为Master节点了。此时通过命令 `# ip a` 查看两个节点，分别如下：
1、Node-1节点
```
[root@localhost keepalived]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:37:53:ad brd ff:ff:ff:ff:ff:ff
    inet 192.168.199.121/24 brd 192.168.199.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::93a7:1de2:d3cd:5004/64 scope link 
       valid_lft forever preferred_lft forever
    inet6 fe80::e398:bb9d:23e2:90da/64 scope link tentative dadfailed 
       valid_lft forever preferred_lft forever
```

2、Node-2节点
```
[root@localhost keepalived]# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP qlen 1000
    link/ether 00:0c:29:77:8e:bd brd ff:ff:ff:ff:ff:ff
    inet 192.168.199.122/24 brd 192.168.199.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet 192.168.199.128/24 scope global secondary ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::e398:bb9d:23e2:90da/64 scope link 
       valid_lft forever preferred_lft forever
```
可以看到，虚拟IP已经漂移到Node-2节点上去了。
通过虚拟IP访问Nginx，结合Nginx的日志，也可以看到，此时：192.168.199.128 访问的是Node-2节点上的Nginx了。