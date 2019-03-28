# 关闭防火墙
CentOS 7.0默认使用的是firewall作为防火墙
## 检查防火墙的状态
`$ firewall-cmd --state`

## 关闭防火墙
`systemctl stop firewalld.service #停止firewall`
`systemctl disable firewalld.service #禁止firewall开机启动`

GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root' WITH GRANT OPTION;