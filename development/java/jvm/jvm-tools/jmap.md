[Back](index.md)

# Introduction
JVM参数分析工具jmap介绍。

# Catalogue
- [概述](#o)
- [jmap命令格式](#cf)
    * [jmap option](#option)

# Content

## 概述 <a id="o">[≡](#≡)</a>
The jmap command prints shared object memory maps or heap memory details of a specified process, core file, or remote debug server.     
If the specified process is running on a 64-bit Java Virtual Machine (JVM), then you might need to specify the -J-d64 option, for example: jmap-J-d64 -heap pid.

## jmap命令格式 <a id="cf">[≡](#≡)</a>
`jmap [options] pid`
`jmap [options] <executable <core>`
`jmap [options] [server_id@]<remote-hostname-or-IP>`

- options
    jmap命令的选项，参见[jmap options](#options)
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
    
### jmap options <a id="option">[≡](#≡)</a> 
- <no option>
    When no option is used, the jmap command prints shared object mappings. For each shared object loaded in the target JVM, the start address, size of the mapping, and the full path of the shared object file are printed. This behavior is similar to the Oracle Solaris pmap utility.

- -dump:[live,] format=b, file=filename
    Dumps the Java heap in hprof binary format to filename. The live suboption is optional, but when specified, only the active objects in the heap are dumped. To browse the heap dump, you can use the [jhat](jhat.md) command to read the generated file.
    该选项生成的堆快照与JVisualVM生成的堆Dump是一样的。

- -finalizerinfo
    Prints information about objects that are awaiting finalization.

- -heap
    Prints a heap summary of the garbage collection used, the head configuration, and generation-wise heap usage. In addition, the number and size of interned Strings are printed.

- -histo[:live]
    Prints a histogram of the heap. For each Java class, the number of objects, memory size in bytes, and the fully qualified class names are printed. The JVM internal class names are printed with an asterisk (\*) prefix. If the live suboption is specified, then only active objects are counted.

- -clstats
    Prints class loader wise statistics of Java heap. For each class loader, its name, how active it is, address, parent class loader, and the number and size of classes it has loaded are printed.

- -F
    Force. Use this option with the jmap -dump or jmap -histo option when the pid does not respond. The live suboption is not supported in this mode.

-h
    Prints a help message.

-help
    Prints a help message.

-Jflag
    Passes flag to the Java Virtual Machine where the jmap command is running. 
     