# firewall防火墙设置
CentOS 7.0默认使用的是firewall作为防火墙

## 查看防火墙是否运行
`$ firewall-cmd --state`

## 开启 / 关闭firewall
+ 开启: `$ systemctl start firewalld.service #停止firewall`
+ 关闭: `$ systemctl stop firewalld.service #停止firewall`

## 启用 / 禁用firewall
下面的操作在下一次开机后仍然会生效。
+ 启用：`$ systemctl enable firewalld.service #禁止firewall开机启动`
+ 禁用: `$ systemctl disable firewalld.service #禁止firewall开机启动`

## 查看区域信息: 
`$ firewall-cmd --get-active-zones`
命令执行结果示例如下所示：
```
$ firewall-cmd --get-active-zones
public
  interfaces: ens33
```
上面的结果表示存在"public"区域，其绑定的网络接口为ens33（通过命令 `ip a` 可以看到当前的网络接口列表）

## 查看指定接口所属区域：
`$ firewall-cmd --get-zone-of-interface=ens33`

# 将接口添加到区域
所有的接口默认都在public，下面显示将接口 ens33 添加到区域 public，永久生效再加上 --permanent
`$ firewall-cmd --zone=public --add-interface=ens33`

## 查看所有打开的端口
`$ firewall-cmd --zone=public --list-ports`

## 开放指定的端口
`$ firewall-cmd --zone=public --add-port=80/tcp --permanent`
说明：
+ --zone：作用域，参考 "查看区域信息" 一节。
+ --add-port=80/tcp: 需要开放的端口和协议，：端口/通讯协议
+ --permanent: 永久生效，没有此参数重启后失效

## 更新防火墙规则
`$ firewall-cmd --reload`

# 打开一个服务
打开一个服务类似于将端口可视化，服务需要在配置文件中添加，/etc/firewalld 目录下有services文件夹，这个不详细说了，详情参考文档，命令如下：
`$ firewall-cmd --zone=work --add-service=smtp`
 
# 移除服务
`$ firewall-cmd --zone=work --remove-service=smtp`

firewall-cmd --zone=public –-add-service=keepalived --permanent


