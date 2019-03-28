[Back](index.md)

# Introduction

JVM工具'JPS'（JVM Process Tools）介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jps命令格式](#cf)
- [jps命令的输出结果](#cr)          
- [参考文档](#r)        
- [疑问](#q)
    * [jps无法连接到远程主机](#q-1)

# Content

## 概述 <a id="o">[≡](#≡)</a>

JPS工具用于列出正在运行的虚拟机进程，并显示虚拟机执行主类（Main Class,main（）函数所在的类）名称
以及这些进程的本地虚拟机唯一ID（Local Virtual Machine Identifier,LVMID）。

对于本地虚拟机进程来说，LVMID与操作系统的进程ID（Process Identifier,PID）是一致的，使用Windows的任务管理器或者UNIX的ps命令也可以查询到虚拟机进程的LVMID，但如果同时启动了多个虚拟机进程，无法根据进程名称定位时，那就只能依赖jps命令显示主类的功能才能区分了。

## jps命令格式 <a id="cf">[≡](#≡)</a>

`jps [-q] [-mlvV] [-Joption] [<hostid>]`    

其中：
- -q    
    只输出LVMID，省略主类的名称。  
- -m   
    输出虚拟机进程启动时传递给主类main()函数的参数。   
- -l    
    输出主类的全名（包括包名的全路径），如果进程执行的是jar包，输出jar包的文件全路径。   
    jps命令不带任何参数时，默认是输出类名（不带包名）或jar包的名称（不带文件路径）。
- -v    
    输出虚拟机进程启动时JVM的参数。
- -V
    输出虚拟机进程启动时JVM的参数，它与-v的区别是：-V输出的是通过标记文件传递给JVM的参数，这类文件包括'.hotspotrc'文件或通过'-XX:Flags=<filename>'参数指定的文件。
- -Joption   
    给jps命令本身调用的Java启动器传递JVM参数，例如：'-J-Xms48m'用于设置虚拟机启动的堆内存为48M。
- hostid <a id="hostid">[≡](#≡)</a> 
    指定JVM进程所在的主机，如果未指定hostid信息，jps命令将列出本地主机的JVM进程信息。    
    该参数主要用于支持查看远程主机中的JVM进程信息。此时，远程主机必须提供RMI支持，Sun提供的jstatd工具可以很方便地建立远程RMI服务器。    
    指定远程主机时，hostid的格式为：     
    `[protocol:][[//]hostname][:port][/servername]`    
    其中，    
    * protocol    
        指定与主机通讯的协议。如果protocol选项和hostname选项同时都未指定，则默认的协议是一个本地相关的，优化的本地协议；如果protocol选项未指定但指定了hostname选项，则默认为RMI协议。
    * hostname    
        JVM进程所在目标主机的名称或IP地址。如果hostname未指定，默认为本机。    
        注意，此处的hostname如果直接指定IP地址，有可能会遇到如下连接不上的错误，请参考[jps无法连接到远程主机](#) 
    * port    
        与目标主机通信的默认端口。  
        如果hostname未指定，或者protocol选项指定了一个优化的本地协议，则port选项将被忽略。   
        对于RMI协议来说，port选项指定的是目标主机上RMI服务所注册的端口号。如果未指定，则使用RMI注册时的默认端口号1099。
    * servername     
        如果protocol选项指定了一个优化的本地协议，则servername选项将被忽略。   
        对于RMI协议来说，该选项表示目标主机上的RMI远程对象的名称。详细的信息，请参考jstatd命令的-n选项。
        
## jps命令的输出结果 <a id="cr">[≡](#≡)</a>           


## 参考文档 <a id="r">[≡](#≡)</a>     

### 官方文档
  http://docs.oracle.com/javase/7/docs/technotes/tools/share/jps.html

### Linux参考手册
  `man jps`


## 问题 <a id="q">[≡](#≡)</a>

### jps无法连接到远程主机 <a id="q-1">[≡](#≡)</a>
- 问题描述
    在使用jps连接远程主机时，报错，错误如下所示：    
    ``
    $ jps 192.168.1.102
    Error communicating with remote host: Connection refused to host: 127.0.0.1; nested exception is:
    java.net.ConnectException: Connection refused: connect        
    ```     
- 问题分析和解决方法
    这是因为目标主机192.168.1.102在启动RMI服务时未指定hostname值，在使用jstatd启动RMI服务时，可以通过添加参数`-J-Djava.rmi.server.hostname=192.168.1.102`来指定RMI服务的hostname来解决该问题。      
    详情请参考[jstatd](jstatd.md/#cf)一节中关于'-J-Djava.rmi.server.hostname'选项的描述。
    
    

