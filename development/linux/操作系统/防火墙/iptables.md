# 安装iptables
`$ yum -y install iptables-service`

安装完成后，会在目录/etc/sysconfig下新增iptables配置文件。

# 修改iptables配置文件
```
$ vi /etc/
```

# 查看当前应用的规则
`$ iptables -L -n --line-number`

# 开发指定的端口
开放目标端口： `$ iptables -I INPUT -p tcp --dport 22 -j ACCEPT`
开放源端口：  `$ iptables -I OUTPUT -p tcp --sport 22 -j ACCEPT`
其中，
+ -A: 表示添加一条规则
+ INPUT: 表示数据从外部进入服务器
+ OUTPUT: 表示数据从服务器流向外部
+ -p: 协议类型
+ --dport: 目标端口，当数据从外部进入服务器为目标端口
+ --sport: 数据源端口，数据从服务器出去 则为
+ -j: 就是指定是 ACCEPT 接收 或者 DROP 不接收

# 开发/禁止指定的ip访问
`$ iptables -A OUTPUT -o ens33 -d 224.0.0.18 -j ACCEPT`
上面表示放开 224.0.0.18 对本机的访问，其中，-o 用于指定网络接口。

# 保存配置变更
通过下面的命令保存后，将永久生效。
`$ service iptables save`