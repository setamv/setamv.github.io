[Back](index.md)

# Introduction

Java虚拟机的常用参数信息记录

# Catalogue <a id="≡">≡</a>
- [HotSpot JVM Parameters](#jp)
    * [-Xmx 和 -Xms](#jp-xmx)

# HotSpot JVM Parameters <a id="jp">[≡](#≡)</a>

  |     **Parameter**      | **Initial Value**    |            **Keywords**             |
  | ---------------------- | --- | ----------------------------------- |
  | -XX:+PrintFlagsInitial |     | 打印JVM参数、JVM参数初始值、JVM参数 |
  |                        |     |                                     |

## -XX:+PrintFlagsFinal <a id="pff">[≡](#≡)</a>
- 参数说明
    打印JVM的所有-XX参数的最终值（当参数未被指定为特定值，则最终值为参数的初始值；否则为指定的值）。
- 参数输出结果
    设置该参数后，JVM启动后将打印所有-XX类型的参数，每个参数占一行，每一行的格式相同，包括5列，如下所示：      
    `bool PrintFlagsFinal      := true            {product}`
    其中：
    * 第1列 参数值的类型。包括：bool、uintx、intx、double、ccstrlist、ccstr、uint64_t
    * 第2列 参数名称
    * 第3列 表达式运算符。包括：`=`、`:=`，其中`=`表示第四列是参数的默认值；`:=`表示第四列被用户或者JVM赋值了（即不是默认值）
    * 第4列 参数的值。该值必须符合第1列中值类型。
    * 第5列 参数的类别     

## -XX:+PrintFlagsInitial
- 参数说明
    打印JVM的所有-XX参数的初始值。
- 参数输出结果
    请参考[-XX:+PrintFlagsFinal](#pff)

## -Xmn <a id="jp-xmn">[≡](#≡)</a>
- 参数说明
    指定JVM堆中新生代的大小
    
## -XX:SurvivorRatio
- 参数说明
    指定新生代中Eden区与Survivor区的容量比值。
- 取值
    `-XX:SurvivorRatio=N`，'N'为一个大于0的整数。表示Eden区与Survivor区的容量比值为:N:1。  
    默认值为8，表示 Eden:Survivor=8:1，存在两个Survivor区（一个From，一个To）。如果新生代大小为10M，则Eden区大小为8M，两个Survivor区大小都为1M。
    
## -Xmx 和 -Xms <a id="jp-xmx">[≡](#≡)</a>
- 参数说明
    设置JVM的堆大小，其中，-Xmx用于设置可分配的最大堆存储空间；-Xms用于设置堆最小的堆存储空间。
- 取值  
    `-XmxN`，'N'为存储空间大小和单位，如'20m'、'10240k'等。    
- 示例
    `-Xmx20m`，`-Xms20480K` 分别将最大堆存储空间和最小堆存储空间分别设置为20M和20480K。

## -XX:PretenureSizeThreshold
- 参数说明
    直接晋升到老年代的对象大小。设置这个参数后，大于这个参数值的对象将直接晋升到老年代分配。    
    注意，只有当发生GC时才会有晋升的机会，并非对象已创建就直接进入老年代中。
- 取值
    `-XX:PretenureSizeThreshold=10m`
    
## -XX:MaxTenuringThreshold
- 参数说明
    晋升到老年代的年龄。
    虚拟机给每个对象定义了一个对象年龄（Age）计数器。如果对象在Eden出生并经过第一次Minor GC后仍然存活，并且能被Survivor容纳的话，将被移动到Survivor空间中，并且对象年龄设为1。对象在Survivor区中每“熬过”一次Minor GC，年龄就增加1岁，当它的年龄达到MaxTenuringThreshold设定的值时，就将会被晋升到老年代中。
- 取值
    `-XX:MaxTenuringThreshold=1` 表示对象晋升到老年代的年龄为1.即只要对象经历过1次Minor GC后任然存活并且能被Survivor所容纳，该对象就会晋升到老年代.
    默认值为15.

## -XX:+UseSerialGC 
- 参数说明
    指定使用Serial(新生代) + Serial Old（老年代）的收集器组合。    
    此模式为虚拟机运行在Client模式下的默认值。
    
## -XX:+UseParNewGC
- 参数说明
    指定使用ParNew(新生代) + Serial Old（老年代）的收集器组合。

## -XX:+UseConcMarkSweepGC
- 参数说明
    指定使用ParNew(新生代) + CMS(老年代) + Serial Old(CMS收集器出现Concurrent Mode Failure失败后的后备收集器)的收集器组合

## -XX:+UseParallelGC
- 参数说明
    指定使用Parallel Scavenge(新生代) + Serial Old（老年代）的收集器组合。    
    此模式为虚拟机运行在Server模式下的默认值。  

## -XX:+UseParallelOldGC
- 参数说明
    指定使用Parallel Scavenge(新生代) + Parallel Old（老年代）的收集器组合。

## -XX:+UseG1GC
- 参数说明
    指定使用G1收集器。

## -XX:ParallelGCThreads
- 参数说明
    设置GC执行垃圾收集时同时开启的线程数。如果未指定该参数，默认的线程数与CPU的数量相同。
- 适用的收集器
    ParNew
- 示例
    `-XX:ParallelGCThreads=3` 设置GC执行垃圾收集时同时开启3个线程。

## -XX:MaxGCPauseMillis
- 参数说明
    设置最大垃圾收集停顿时间
- 适用的收集器
    Parallel Scavenge
- 示例
    `-XX:MaxGCPauseMillis=100` 设置GC最大垃圾收集停顿时间为100ms

## -XX:GCTimeRatio    
- 参数说明
    设置收集器的吞吐量大小。    
    吞吐量=运行用户代码时间/（运行用户代码时间+垃圾收集时间）
- 取值
    `-XX:GCTimeRatio=N`，其中值'N'的说明：1/(N+1)\*100% 为垃圾收集时间占总时间（即运行用户代码时间+垃圾收集时间）的比值（后面简称'垃圾收集时间占比'）。如当'N'为19时，垃圾收集时间占比为 1/(19+1)\*100% = 5%，即表示5%的时间用于垃圾收集。      
    'N'的默认值为99，此时垃圾收集时间占比为：1/(1+99)\*100% = 1%
- 适用的收集器
    Parallel Scavenge
- 示例
    `-XX:GCTimeRatio=99` 设置GC的垃圾收集时间占比为：1/(1+99)\*100% = 1%

## -XX:+UseAdaptiveSizePolicy
- 参数说明
    开关参数，打开改参数后，就不需要手工指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX:SurvivorRatio）、晋升老年代对象年龄（-XX:PretenureSizeThreshold）等细节参数了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量，这种调节方式称为GC自适应的调节策略（GC Ergonomics）
- 适用的收集器
    Parallel Scavenge

## -XX:CMSInitiatingOccupancyFraction
- 参数说明
    设置CMS收集器在老年代空间被使用多少时后触发垃圾收集。
- 取值
    `-XX:CMSInitiatingOccupancyFraction=N`，'N'为一个百分比数值，表示老年代空间剩余的百分比。如 `-XX:CMSInitiatingOccupancyFraction=50` 表示当老年代空间剩余50%时触发垃圾收集。    
    默认值为68
- 适用的收集器    
    CMS    
    
## -XX:+UseCMSCompactAtFullCollection
- 参数说明
    设置CMS收集器在完成垃圾收集后是否要进行一次内存碎片整理。
- 适用的收集器    
    CMS

## -XX:CMSFullGCsBeforeCompaction
- 参数说明
    设置CMS收集器在进行若干次垃圾收集后，再启动一次内存碎片整理。
- 取值
    `-XX:CMSFullGCsBeforeCompaction=N`，N表示执行垃圾收集的次数。默认值为0，表示每次进入垃圾收集时都进行碎片整理
- 适用的收集器    
    CMS

## -XX:+PrintGC
- 参数说明
    开启输出GC日志的功能。

## -XX:+PrintGCDetails
- 参数说明
    虚拟机在发生垃圾收集行为时打印内存回收日志，并且在进程退出的时候输出当前的内存各区域分配情况

## -XX:+PrintGCTimeStamps
- 参数说明
    输出GC的时间戳（以基准时间的形式）
    
## -XX:+PrintGCDateStamps
- 参数说明
    输出GC的时间戳（以日期的形式，如 2013-05-04T21:53:59.234+0800）
    
## -XX:+PrintHeapAtGC
- 参数说明
    在进行GC的前后打印出堆的信息
    
## -Xloggc:../logs/gc.log  
- 参数说明
    指定GC日志输出到日志文件，并指定日志文件的输出路径。该路径是相对当前Main类所在根目录的相对路径。

## -verbose:gc
- 参数说明
    开启打印GC日志。

## -Xss
- 参数说明
    设置每个线程的栈(Stack Space)大小。
    
    Stack Space用于方法的递归调用时压入Stack Frame。所以当递归调用太深的时候，就有可能耗尽Stack Space，爆出StackOverflowError的错误
- 参数设置
    在参数后面加上需要设置的栈大小即可。
- 示例
    `-Xss512K` 设置每个线程的栈大小为512K。
    
## -XX:PermSize 和 -XX:MaxPermSize
- 参数说明
    于设置虚拟机的方法区内存大小。其中，`-XX:PermSize`用于设置方法区的初始大小；`-XX:MaxPermSize` 用于设置方法区的最大可占用内存大小。
- 示例
    `-XX:PermSize=10M` 将方法区的初始大小设置为10M；`-XX:MaxPermSize=50M` 将方法区的最大可占用内存设置为50M 
- 版本支持
    JDK8已经不支持该参数，请参考 `MaxMetaspaceSize` 参数。

## -XX:MaxMetaspaceSize
- 参数说明
    设置虚拟机方法区的最大内存空间。只适用于JDK8以后的版本。
- 示例
    `-XX:MaxMetaspaceSize=10M` 将方法区的最大可以内存空间设置为10M；
- 版本支持
    JDK8以后（包括JDK8）的版本才开始支持，用于替换JDK8以前版本的`MaxPermSize`参数

## -XX:MaxDirectMemorySize
- 参数说明
    设置直接内存的最大可用内存大小。其默认值与`-Xmx`的值一样
    
## -XX:+HeapDumpOnOutOfMemoryError
- 参数说明
    开启虚拟机遇到OutOfMemoryError时将堆信息Dump。
    
## -XX:+HeapDumpPath 
- 参数说明
    设置虚拟机导出堆Dump文件时文件保存的路径。可以和'HeapDumpBeforeFullGC'、'HeapDumpAfterFullGC'配合使用。
    
## -XX:+HeapDumpBeforeFullGC   
- 参数说明
    开启虚拟机Full GC前导出堆Dump文件

## -XX:+HeapDumpAfterFullGC   
- 参数说明
    开启虚拟机Full GC后导出堆Dump文件   
    
## -XX:+HeapDumpOnCtrlBreak
- 参数说明
    开启虚拟机遇到Ctrl+Break组合键时，生成堆Dump文件。
    
## -Xnoclassgc
- 参数说明
    设置GC是否对方法区中已加载的类进行回收
    
## -verbose:class    
- 参数说明
    启用类加载日志打印
    
## -XX:+TraceClassLoading   
- 参数说明
    查看类加载信息
    
## -XX:+TraceClassUnLoading    
- 参数说明
    查看类卸载信息
    
## -XX:+HandlePromotionFailure
- 参数说明
    是否允许分配担保失败，即老年代的剩余空间不足以应付新生代的整个Eden和Survivor区的所有对象都存活的极端情况。
    
## -Dcom.sun.management.jmxremote
- 参数说明
    开启JMX管理功能。JDK1.5（包括1.5）之前默认是未开启的，JDK1.6之后默认都是开启的。

## -Xshare
- 参数说明
    开启或关闭类共享优化功能，类共享是一个在多虚拟机进程中共享rt.jar中类数据以提高加载速度和节省内存的优化。
    注意：根据相关Bug报告的反映，VisualVM的Profiler功能可能会因为类共享而导致被监视的应用程序崩溃，所以读者进行Profiling前，最好在被监视程序中使用-Xshare：off参数来关闭类共享优化
- 取值
    `-Xshare:off` 关闭类共享功能；`-Xshare:on` 开启类共享功能。JDK1.5以后，在Client模式下默认为开启。

