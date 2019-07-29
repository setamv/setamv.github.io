# keepalived + mysql双主高可用
假设当前两台MySQL服务器已经配置好了双主复制（配置方式参见[MySQL基于GTID配置双主复制](主从复制/MySQL基于GTID配置双主复制.md)，他们的服务器地址分别是：
+ master1: 192.168.199.110
+ master2: 192.168.199.111

## 安装keepalived
1. 到官网下载keepalived
    官网下载地址：https://www.keepalived.org/download.html
2. 解压
    ```
    # tar -xzvf keepalived-xxxx.tar.gz
    ```
    假设解压目录为：`/usr/local/setup/keepalived-xxxx`
3. 安装依赖的软件gcc和ssl
    ```
    # yum -y install openssl-devel
    #  
    ```
4. 安装keepalived
    首先，进入keepalived源码所在的目录：
    ```
    # cd /usr/local/setup/keepalived-xxxx
    ```

    然后，设置keepalived的安装目录，下面表示将keepalived安装到目录`/usr/local/keepalived`下
    ```
    # ./configure --prefix=/usr/local/keepalived
    ```

    编译keepalived
    ```
    # make
    ```

    安装keepalived
    ```
    # make install
    ```
    上面安装完keepalived后，将在/usr/local/keepalived目录下生成四个文件件:`bin`，`etc`，`sbin`,`share`

    将/usr/local/keepalived/sbin目录下的`keepalived`文件拷贝到`/usr/local/sbin`目录中，以后就可以在任何位置执行`keepalived`命令
5. 开启keepalived服务
    通过以下命令查看keepalived服务是否启用：
    ```
    # systemctl list-unit-files | grep keepalived
    keepalived.service                            disabled
    ```
    上面表示服务未启用，使用以下命令启用keepalived服务：
    ```
    # systemctl enable keepalived
    Created symlink from /etc/systemd/system/multi-user.target.wants/keepalived.service to /usr/lib/systemd/system/keepalived.service.
    ```
6. 设置keepalived配置文件
    keepalived默认加载目录`/etc/keepalived`下的配置文件，所以需要创建该目录，并将keepalived安装目录的配置文件拷贝到该目录下：
    ```
    # mkdir /etc/keepalived
    # cp ./etc/keepalived/keepalived.conf /etc/keepalived/
    ```
    上面`./etc/keepalived/keepalived.conf`是相对keepalived安装目录`/usr/local/keepalived`

## 配置keepalived
打开keepalived配置文件`/etc/keepalived/keepalived.conf`，默认的配置内容，请参见[keepalived配置文件模板](keepalived配置文件模板.md)
我们需要对`keepalived配置文件模板`中的内容进行说明，并修改为我们需要的配置。

### keepalived配置文件说明
#### global_defs区域
```
global_defs {
   notification_email {         
     acassen@firewall.loc
     failover@firewall.loc
     sysadmin@firewall.loc
   }
   notification_email_from Alexandre.Cassen@firewall.loc
   smtp_server 192.168.200.1
   smtp_connect_timeout 30
   router_id LVS_DEVEL
   vrrp_skip_check_adv_addr
   vrrp_strict
   vrrp_garp_interval 0
   vrrp_gna_interval 0
}
```
主要是配置故障发生时的通知对象以及机器标识，其中：
+ notification_email 
    故障发生时给谁发邮件通知，可以发送给多个邮箱地址
+ notification_email_from
    故障发生时通知邮件由哪个邮箱账号发出
+ smtp_server
    邮箱服务器
+ smtp_connect_timeout
    邮箱服务器连接超时时间
+ router_id
    标识本节点的字符串标识符，通常设置为主机的hostname就可以了

### vrrp_instance区域
```
vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.200.16
        192.168.200.17
        192.168.200.18
    }
}
```
用于配置VRRP实例，配置项说明如下：
+ vrrp_instance VI_1
    其中`VI_1`为VRRP实例的名称
+ state
    VRRP实例的状态，枚举值包括：`MASTER`和`BACKUP`，如果为`MASTER`，只要keepalived进程启动，就会抢占虚拟IP，导致虚拟IP漂移，如果keepalived进程频繁的重启，可能会导致虚拟IP频繁的漂移，虚拟IP漂移的过程中可能会导致系统访问出现短暂的卡顿，所以一般将所有的示例都设置为`BACKUP`，这样，只有当获得虚拟IP的实例挂掉的时候，才会出现虚拟IP漂移。
+ interface
    用于发送VRRP包的网卡，这里设置固定keepalived所在主机的可以和外网联通的网卡，一般为当前主机IP地址的网卡（可以使用`# ifconfig`查看）
+ virtual_router_id
    取值在0~255之间，用于区分多个实例的VRRP组播，如果多个keepalived同属一个VRRP组，这一项必须配置为相同的值。
+ priority
    优先级，同一个VRRP组中，MASTER的优先级必须必BACKUP高。`nopreemt`用于指定非抢占式，配合`state`设置为BACKUP时，在主库恢复后，IP又会漂移到主库的问题。
+ advert_int
    MASTER和BACKUP负载均衡器之间同步检查的时间间隔，单位为秒
+ authentication
    认证，同属于一个VRRP组中keepalived实例配置为相同的认证信息
+ virtual_ipaddress
    虚拟IP地址，用于同一对外提供服务的虚拟IP

### virtual_server区域
```
virtual_server 192.168.200.100 443 {
    delay_loop 6
    lb_algo rr
    lb_kind NAT
    persistence_timeout 50
    protocol TCP

    real_server 192.168.201.100 443 {
        weight 1
        notify_down kill_keepalived.sh
        TCP_CHECK {
            connect_timeout 3
            nb_get_retry 3
            delay_before_retry 3
            connect_port 3306
        }
        SSL_GET {
            url {
              path /
              digest ff20ad2481f97b1754ef3e12ecd3a9cc
            }
            url {
              path /mrtg/
              digest 9b3a0c85a887a256d6939da88aabd8cd
            }
            connect_timeout 3
            retry 3
            delay_before_retry 3
        }
    }
}
```
virtual_server区域用于
+ virtual_server 192.168.200.100 443
    `192.168.200.100`是当前设置的虚拟IP地址，必须对应`vrrp_instance`区域下`virtual_ipaddress`的一个值。`443`为端口
+ delay_loop
    健康检查的间隔，单位为秒
+ lb_algo
+ persistence_timeout
    会话的保持时间，即在保持时间内，keepalived会将同一个用户的请求转发给同一台真实服务器。
    为什么会有用户请求转发一说？
+ protocol
    转发协议，可选的包括：`TCP`、`UDP`，一般设置为`TCP`
+ real_server
    真实服务器的IP和端口，它下面的配置包括：
    - notify_down
        表示keepalived检测的应用挂掉后，需要执行的脚本
    - TCP_CHECK
        通过TCP来检查真实服务的健康状态
    
### 完整的配置如下：
+ master1的配置：
```
[root@192 keepalived]# cat keepalived.conf 
! Configuration File for keepalived

global_defs {
   notification_email {
     setamv@126.com
   }
   notification_email_from setamv@126.com
   smtp_server smtp.126.com
   smtp_connect_timeout 30
   router_id LVS_DEVEL
}

vrrp_instance VI_1 {
    state BACKUP
    interface ens33
    virtual_router_id 51
    priority 100
    nopreempt
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.199.200
    }
}

virtual_server 192.168.199.200 3306 {
    delay_loop 6
    persistence_timeout 50
    protocol TCP

    real_server 192.168.199.110 3306 {
        weight 1
        notify_down /etc/keepalived/kill_keepalived.sh
        TCP_CHECK {
            connect_timeout 3
            nb_get_retry 3
            delay_before_retry 3
            connect_port 3306
        }
    }
}
```
+ master2的配置如下：
```
[root@192 keepalived]# cat keepalived.conf
! Configuration File for keepalived

global_defs {
   notification_email {
     setamv@126.com
   }
   notification_email_from setamv@126.com
   smtp_server smtp.126.com
   smtp_connect_timeout 30
   router_id LVS_DEVEL
}

vrrp_instance VI_1 {
    state BACKUP
    interface ens33
    virtual_router_id 51
    priority 90
    nopreempt
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.199.200
    }
}

virtual_server 192.168.199.200 3306 {
    delay_loop 6
    persistence_timeout 50
    protocol TCP

    real_server 192.168.199.111 3306 {
        weight 1
        notify_down /etc/keepalived/kill_keepalived.sh
        TCP_CHECK {
            connect_timeout 3
            nb_get_retry 3
            delay_before_retry 3
            connect_port 3306
        }
    }
}
```

其中，kill_keepalived.sh的内容如下所示：
```
[root@192 keepalived]# cat kill_keepalived.sh 
#!/bin/bash
kill -9 $(cat /var/run/keepalived.pid)
```