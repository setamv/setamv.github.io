[Back](index.md)

# Introduction

JVM远程监控工具jstatd介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jstatd命令格式](#cf)
- [jstatd安全相关的主题](#security)  
- [示例](#ex)
    * [在本地开启jstatd服务](#ex-1)  
- [参考文档](#r)        
- [疑问](#q)


# Content

## 概述 <a id="o">[≡](#≡)</a>

 jstatd是一个基于RMI（Remove Method Invocation）的服务程序，它用于监控基于HotSpot的JVM中资源的创建及销毁，并且提供了一个远程接口允许远程的监控工具连接到本地的JVM执行命令。
 
  jstatd是基于RMI的，所以在运行jstatd的服务器上必须存在RMI注册中心，如果没有通过选项"-p port"指定要连接的端口，jstatd会尝试连接RMI注册中心的默认端口。

## jstatd命令格式 <a id="cf">[≡](#≡)</a>

`jstatd [options]`    

其中options选项包括：
- -nr      
    如果RMI注册中心没有找到，不会创建一个内部的RMI注册中心。  
- -p port       
    RMI注册中心的端口号，默认为1099   
- -n rminame    
    指定远程RMI对象在RMI注册中心绑定的名称。默认为JStatRemoteHost；如果同一台主机上同时运行了多个jstatd服务，rminame可以用于唯一确定一个jstatd服务；这里需要注意一下，如果开启了这个选项，那么监控客户端远程连接时，必须同时指定hostid及vmid，才可以唯一确定要连接的服务
- -Joption   
    给jstatd命令本身调用的Java启动器传递JVM参数，例如：'-J-Xms48m'用于设置虚拟机启动的堆内存为48M。  
    这里有两个比较重要的选型需要注意：    
    * -J-Djava.security.policy
        该选项用于指定一个安全策略文件，如果不指定安全策略文件，jstatd命令启动时可能会报错：   
        ```
        $ jstatd
        Could not create remote object
        access denied ("java.util.PropertyPermission" "java.rmi.server.ignoreSubClasses" "write")
        java.security.AccessControlException: access denied ("java.util.PropertyPermission" "java.rmi.server.ignoreSubClasses" "write")
            at java.security.AccessControlContext.checkPermission(AccessControlContext.java:472)
            at java.security.AccessController.checkPermission(AccessController.java:884)
            at java.lang.SecurityManager.checkPermission(SecurityManager.java:549)
            at java.lang.System.setProperty(System.java:792)
            at sun.tools.jstatd.Jstatd.main(Jstatd.java:139)
        ```
        解决方法是：先创建一个安全策略文件"jstatd.all.policy"，文件内容如下所示：   
        ```
        grant codebase "file:${java.home}/../lib/tools.jar" {
           permission java.security.AllPermission;
        };
        ```
        然后在启动jstatd命令时，通过`-J-Djava.security.policy=path-to-jstatd.all.policy`参数来指定该安全策略文件，注意，'path-to-jstatd.all.policy'部分必须为安全策略文件的全路径。
        
    * -J-Djava.rmi.server.hostname
        该选项用于指定RMI服务的名称，如果不指定该名称，在使用jps客户端工具远程查看虚拟机信息时，可能会出现如下错误：     
        ```
        $ jps 192.168.1.102
        Error communicating with remote host: Connection refused to host: 127.0.0.1; nested exception is:
        java.net.ConnectException: Connection refused: connect   
        ```
        此时，只需要通过添加`-J-Djava.rmi.server.hostname=192.168.1.102`就可以解决。        

## 示例 <a id="ex">[≡](#≡)</a>     

### 在本地开启jstatd服务 <a id="ex-1">[≡](#≡)</a>     
Windows下的命令：   
```
> jstatd -J-Djava.security.policy="D:\DevTools\Java\jdk1.8.0_101\bin\jstatd.all.policy" -J-Djava.rmi.server.hostname=192.168.1.100
```
其中，jstatd.all.policy的内容为：
```
grant codebase "file:${java.home}/../lib/tools.jar" {
   permission java.security.AllPermission;
};
```
主机的IP为：192.168.1.100

## 参考文档 <a id="r">[≡](#≡)</a>     

### 官方文档
  http://docs.oracle.com/javase/7/docs/technotes/tools/share/jstatd.html

### Linux参考手册
  `man jstatd`



