# k8s的安装

## 环境准备
假设准备3台服务器，分别是：192.168.199.110、192.168.199.111、192.168.199.112，其中，192.168.199.110作为master，192.168.199.111和192.168.199.112作为两台node节点。
1. 关闭防火墙
    这一步不是必须的，但是如果不知道如何设置防火墙，练习的时候建议关闭
    方法如下：
    ```
    # systemctl stop firewalld
    # systemctl disable firewalld
    ```
2. 关闭selinux
    这一步不是必须的，但是如果不知道如何设置，练习的时候建议关闭
    方法如下：
    ```
    # sed -i 's/enforcing/disabled/' /etc/selinux/config
    # setenforce 0
    ```
3. 关闭swap
    方法如下：
    ```
    # swapoff -a
    ```
4. 建立主机名和IP的对应关系
    假设192.168.199.110、192.168.199.111、192.168.199.112三台主机的的主机名分别是k8s-master、k8s-node-1、k8s-node-2
    方法为将以下内容追加到/etc/hosts文件的末尾：
    ```
    192.168.199.110 k8s-master
    192.168.199.111 k8s-node-1
    192.168.199.112 k8s-node-2
    ```
5. 将桥接的IPV4流量传递到iptables的链
    ```
    # cat > /etc/sysctl.d/k8s.conf << EOF
    > net.bridge.bridge-nf-call-ip6tables = 1
    > net.bridge.bridge-nf-call-iptables = 1
    > EOF
    ```