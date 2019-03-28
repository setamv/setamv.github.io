[Home](../../../../index.md) >> [Articles](../../../index.md) >> [Java Articles](../../index.md) << [JVM Articles](../index.md) << JVM Tools

# Introduction

Java虚拟机性能调试和远程跟踪的工具

# Navigation

- [jps](jps.md)
    jps用于查看JVM进程相关的信息，包括LVMID、JVM启动参数、JVM启动类等。
    
- [jstat](jstat.md)
    jstat用于统计垃圾收集器信息以及垃圾收集相关的堆空间、Metaspace空间等信息
    
- [jstatd](jstatd.md)
    jstatd是一个基于RMI（Remove Method Invocation）的服务程序。

- [jsadebugd](jsadebugd.md)
    jsadebugd命令是一个基于RMI（Remove Method Invocation）的Debug服务，该服务程序挂载到一个JVM进程或一个Java Core文件，从而jstack、jmap、jinfo等远程客户端工具可以通过RIM挂载到该Debug服务，对远程JVM进程或Java Core文件的信息进行查看。

- [jinfo](jinfo.md)
    jinfo是一个查看JVM所有属性以及被设置过的JVM参数的工具
    
- [jmap](jmap.md)
    jmap用于查看、导出JVM的堆信息，并可以对堆中的对象图进行分析。

- [jhat](jhat.md)
    JVM堆Dump分析工具jat（JVM Heap Analysis Tool）介绍。
    
- [jstack](jstack.md)    
    JVM线程快照生成工具jstack介绍。

- [jdb](jdb.md)
    jdb（Java Debugger）是一个可以对JVM执行断点跟踪调试的命令行工具。该命令行工具可以设置断点、查看断点处对象或变量的值。

# 问题
## jinfo命令中的executable core是指什么？怎么生成executable core文件？

