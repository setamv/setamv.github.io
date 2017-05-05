[Back](index.md)

# Introduction

Java虚拟机的常用参数信息记录

# Catalogue <a id="≡">≡</a>
- [HotSpot JVM Parameters](#jp)
    * [-Xmx 和 -Xms](#jp-xmx)

# HotSpot JVM Parameters <a id="jp">[≡](#≡)</a>

## -XX:+PrintFlagsInitial
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

## -Xmn <a id="jp-xmn">[≡](#≡)</a>
- 参数说明
    指定JVM堆中新生代的大小
    
## -XX:SurvivorRatio
- 参数说明
    指定新生代中Eden区与Survivor区的容量比值。
- 取值
    `-XX:SurvivorRatio=N`，'N'为一个大于0的整数。表示Eden区与Survivor区的容量比值为:N:1。  
    默认值为8，表示 Eden:Survivor=8:1
    
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
    晋升到老年代的年龄。每个对象在坚持过一次Minor GC而不被GC回收时，其年龄就增加1，当对象在某次GC的过程中年龄超过该参数值时，就晋升到老年代。
- 取值

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

## -XX:+PrintGCDetails
- 参数说明
    用于开启Java虚拟机的GC日志打印功能。加入该参数后，Java虚拟机将记录并打印垃圾收集器（GC）的垃圾收集操作日志。

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

    

