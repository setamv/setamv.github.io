[Back](index.md)

# Introduction
JVM参数监视工具jinfo介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jinfo命令格式](#cf)
    * [jinfo option](#option)
- [应用场景](#scn)
    * [利用jinfo动态开启Full GC堆Dump](#scn-1)
- [示例](#ex)

# Content

## 概述 <a id="o">[≡](#≡)</a>
The jinfo command prints Java configuration information for a specified Java process or core file or a remote debug server. The configuration information includes Java system properties and Java Virtual Machine (JVM) command-line flags. 

## jinfo命令格式 <a id="cf">[≡](#≡)</a>
`jinfo [option] pid`    
`jinfo [option] <executable <core>`    
`jinfo [option] [server_id@]<remote server IP or hostname>`   

其中：
- option
    为jinfo命令的选项，参见[jinfo option](#option)一节。
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

### jinfo option <a id="option">[≡](#≡)</a>  
- no-option
    Prints both command-line flags and system property name-value pairs.

- -flag name
    Prints the name and value of the specified command-line flag.

- -flag [+|-]name
    enables or disables the specified Boolean command-line flag.

- -flag name=value
    Sets the specified command-line flag to the specified value.

- -flags
    Prints command-line flags passed to the JVM.

- -sysprops
    Prints Java system properties as name-value pairs.

- -h
    Prints a help message.

- -help
    Prints a help message.   
    
## 应用场景 <a id="scn">[≡](#≡)</a>  
### 利用jinfo动态开启Full GC堆Dump <a id="scn-1">[≡](#≡)</a> 
在生产环境下，如果Java服务突然遇到频繁的Full GC，如果此时想在不重启服务的情况下获取GC堆，怎么办呢？
可以通过jinfo来动态改变JVM的虚拟机参数。要实现在Full GC的前后导出堆Dump，可以使用jinfo实时开启如下JVM参数：
`HeapDumpBeforeFullGC`、`HeapDumpAfterFullGC`、`HeapDumpPath`
这三个参数分别用于指定在Full GC开始前和开始后导出堆Dump文件，并将文件保存到HeapDumpPath指定的路径中，如下所示（windows环境下）：
```
$ jinfo -flag HeapDumpPath="D:\heapdump"
$ jinfo -flag +HeapDumpBeforeFullGC
$ jinfo -flag +HeapDumpAfterFullGC
```
    
## 示例 <a id="ex">[≡](#≡)</a>  
### `$ jinfo -flag UseSerialGC 4504`
查看JVM进程ID为4504的虚拟机是否启用了Serial垃圾收集器。
    
### `$ jinfo -flags eclipse@192.168.1.100 `    
查看远程主机'192.168.1.100'上'server-id'为'eclipse'的'jsadebugd'服务挂载的JVM信息。
请参考[jsadebugd](jsadebugd.md)

    
    