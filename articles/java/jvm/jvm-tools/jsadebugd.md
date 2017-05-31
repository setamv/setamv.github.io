[Back](index.md)

# Introduction
JVM工具jsadebugd介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jsadebugd命令格式](#cf)
- [示例](#ex)

# Content

## 概述 <a id="o">[≡](#≡)</a>
The jsadebugd command attaches to a Java process or core file and acts as a debug server. Remote clients such as jstack, jmap, and jinfo can attach to the server through Java Remote Method Invocation (RMI).

**注意**  
当需要同时启动jstatd和jsadebugd时，请为jstatd指定一个与默认端口号（1099）不一样的RMI端口，因为jsadebugd也是基于RMI协议的，其默认使用1099端口。

## jsadebugd命令格式 <a id="cf">[≡](#≡)</a>
`jsadebugd pid [server-id]`
`jsadebugd executable core [server-id]`

其中：
- pid
    The process ID of the process to which the debug server attaches. The process must be a Java process, which also known as LVMID (Refer to [jps](jps.md)).    
    当要查看的目标的是一个JVM进程时，使用该参数。
- executable core
    Java虚拟机的Core文件。
    当要查看的目标的是一个Java虚拟机Core文件时，使用该参数。
- server-id
    An optional unique ID that is needed when multiple debug servers are started on the same machine. This ID must be used by remote clients to identify the particular debug server to which to attach. Within a single machine, this ID must be unique.

## 示例 <a id="ex">[≡](#≡)</a>
- `$ jsadebugd 6472 eclipse`
    将PID为6472的JVM进程挂载到jsadebugd服务上，并为该服务命名为'eclipse'，假设当前主机IP为192.168.1.100，则客户端jinfo命令可以通过如下方式访问该JVM的信息：`$ jinfo eclipse@192.168.1.100`。
    
    
    
