[Back](index.md)

# Introduction
JVM线程快照生成工具jstack介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jstack命令格式](#cf)
    * [jstack option](#option)

# Content
 
## 概述 <a id="o">[≡](#≡)</a>
jstack（Stack Trace for Java）命令用于生成虚拟机当前时刻的线程快照（一般称为threaddump或者javacore文件）。线程快照就是当前虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等都是导致线程长时间停顿的常见原因。线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做些什么事情，或者等待着什么资源。

## jstack命令格式 <a id="cf">[≡](#≡)</a>
`jstack [options] pid`
`jstack [options] <executable <core>`
`jstack [options] [server_id@]<remote-hostname-or-IP>`

- options
    为jstack命令的选项，参见[jstack options](#option)一节。
- pid
    JVM虚拟机的进程ID，即jps命令查看到的LVMID。
- executable core
    JVM虚拟机的core dump文件
- server-id    
    An optional unique ID to use when multiple debug servers are running on the same remote host.   
    该选项用于jinfo命令查看远程主机上的JVM信息，此时，远程主机上应开启jsadebugd命令（一个jsadebugd命令可以挂载一个JVM进程或Java Core文件），因为同一个主机上可以开启多个jsadebugd，所以为了唯一的区别每一个JVM进程对应的jsadebugd服务，需要为该jsadebugd服务取一个唯一的名字，这里的server-id就是jsadebugd命令中指定的server-id选项名称，请参考[jsadebugd](jsadebugd.md#cf)
- remote-hostname-or-IP
    JVM虚拟机进程所在的远程主机的主机名或IP地址。
    请参考[jsadebugd](jsadebugd.md#cf)

### jstack options <a id="option">[≡](#≡)</a> 
- -F
    Force a stack dump when jstack [-l] pid does not respond.
- -l
    Long listing. Prints additional information about locks such as a list of owned java.util.concurrent ownable synchronizers.   
    See the AbstractOwnableSynchronizer class description at http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/AbstractOwnableSynchronizer.html
- -m
    Prints a mixed mode stack trace that has both Java and native C/C++ frames.
- -h
    Prints a help message.
- -help
    Prints a help message.   