[Back](index.md)

#Introduction 

JVM统计信息监视工具jstat介绍。

# Catalogue <a id="≡">≡</a>
- [概述](#o)
- [jps命令格式](#cf)
- [示例](#ex)
- [jstat命令输出](#outputoption) 
    * [-class output](#classoutput)
    * [-compiler](#compileroutput)
    * [-gc](#gcoutput)
    * [-gccapacity](#gccapacityoutput)
    * [-gccause](#gccauseoutput)
    * [-gcnew](#gcnewoutput)
    * [-gcnewcapacity](#gcnewcapacityoutput)
    * [-gcold](#gcoldoutput)
    * [-gcoldcapacity](#gcoldcapacityoutput)
    * [-gcmetacapacity](#gcmetacapacityoutput)
    * [-gcutil](#gcutiloutput)
    * [-printcompilation](#printcompilationoutput)
- [参考文档](#r)

# Content

## 概述 <a id="o">[≡](#≡)</a>

jstat（JVM Statistics Monitoring Tool）是用于监视虚拟机各种运行状态信息的命令行工具。它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据，在没有GUI图形界面，只提供了纯文本控制台环境的服务器上，它将是运行期定位虚拟机性能问题的首选工具。

如果是监视远程虚拟机进程，需要远程主机提供RMI支持，Sun提供的jstatd工具可以很方便地建立远程RMI服务器。

## jstat命令格式 <a id="cf">[≡](#≡)</a>

`jstat [ generalOption | outputOption vmid [interval[s|ms] [count]] `

其中，generalOption和outputOption两种选项必需至少指定一项，且不能同时指定generalOption和outputOption选项。

### generalOption
generalOption选项不能和outputOption选项或其他参数一起使用，即如果指定了generalOption，就不要指定其他任何参数了。generalOption包括：
- -help 
    打印帮助信息
- -options
    打印jstat支持的outputOption选项列表。

### outputOption
outputOption选项用于指定jstat命令输出哪些内容以及输出内容的格式。

#### 指定输出内容格式的选项    
- -h n
    每隔n行展示一次列标题，n为正整数，默认值为0，表示在内容的第一行展示列标题。
- -t
    在内容的第一列展示自JVM启动以来所经过的时间信息，单位为秒。
- -JjavaOption
    给jps命令本身调用的Java启动器传递JVM参数，例如：'-J-Xms48m'用于设置虚拟机启动的堆内存为48M。
    
#### 指定输出哪些内容的选项
jstat命令主要可以通过以下这些选项指定需要统计JVM的哪些信息：    
- -class    统计类加载相关的信息。输出内容格式见[-class output](#classoutput)
- -compiler 统计JIT编译器的相关信息。输出内容格式见[-compiler output](#compileroutput)
- -gc       统计GC收集器的垃圾收集行为以及堆栈相关信息。输出内容格式见[-gc output](#gcoutput)
- -gccapacity 统计各个分代的容量以及对应的存储空间信息。输出内容格式见[-gccapacity output](#gccapacityoutput)
- -gccause  统计垃圾收集器的垃圾收集信息，它和-gcutil选项的区别是：多了两项统计信息，分别是导致上一次和本次垃圾收集事件发生的原因。输出内容格式见[-gccause output](#gccauseoutput)
- -gcnew    统计新生代的垃圾收集相关的信息。输出内容格式见[-gcnew output](#gcnewoutput)
- -gcnewcapacity 统计新生代存储空间的相关信息。输出内容格式见[-gcnewcapacity output](#gcnewcapacityoutput)
- -gcold    统计老年代和永久带（metaspace）的垃圾收集相关的信息。输出内容格式见[-gcold output](#gcoldoutput)
- -gcoldcapacity 统计来年代存储空间的相关信息。输出内容格式见[-gcoldcapacity output](#gcoldcapacityoutput)
- -gcmetacapacity 统计Metaspace存储空间的相关信息。输出内容格式见[-gcmetacapacity output](#gcmetacapacityoutput)
- -gcutil   统计垃圾收集相关的信息。输出内容格式见[-gcutil output](#gcutiloutput)
- -printcompilation 统计HotSpot虚拟机的编译器信息。输出内容格式见[-printcompilation output](#printcompilationoutput)

### vmid
Virtual machine identifier, which is a string that indicates the target JVM. The general syntax is the following:    
`[protocol:][//]lvmid[@hostname[:port]/servername]`    
其中各个部分请参考[jps hostid](jps.md/#hostid)，而lvmid是虚拟机进程的ID，参见[jps](jps.md)

### interval[s|ms]
Sampling interval in the specified units, seconds (s) or milliseconds (ms). Default units are milliseconds. Must be a positive integer. When specified, the jstat command produces its output at each interval.

### count
Number of samples to display. The default value is infinity which causes the jstat command to display statistics until the target JVM terminates or the jstat command is terminated. This value must be a positive integer.

## 示例 <a id="ex">[≡](#≡)</a>
- `$ jstat -gcutil 6544 1s 5` 
    表示每间隔1秒统计一次gcutil内容，总共统计5次。   
- `$ jstat -gcutil rmi://4504@192.168.1.100` 
    表示统计主机192.168.1.100上虚拟机的lvmid为4504的gcutil内容。其中，主机192.168.1.100上启动了jstatd进程，参见[jstatd命令格式](jstatd.md/#cf)。

## jstat命令输出 <a id="outputoption">[≡](#≡)</a>
jstat命令根据outputOption部分所指定的选项不同，其输出内容也不一样，下面针对每一种outputOption详细描述每种类型的输出结果字段。

### -class output <a id="classoutput">[≡](#≡)</a>
#### 示例内容
```
Loaded  Bytes  Unloaded  Bytes     Time
19951 40399.6        0     0.0      43.66
```
#### 列说明
* Loaded: 已加载的类数量
* Bytes: 已加载的字节数，单位为 kBs
* Unloaded: 以卸载的类数量
* Bytes: 以卸载的字节数，单位为 kBs
* Time: 类加载和卸载所耗费的时间。单位为毫秒。

### -compiler output <a id="compileroutput">[≡](#≡)</a>
#### 示例内容
```
Compiled Failed Invalid   Time   FailedType FailedMethod
 13557      2       0    42.57          1 org/eclipse/osgi/internal/loader/BundleLoader findClassInternal
```
#### 列说明
* Compiled: 已执行的编译任务次数。
* Failed: 失败的编译任务次数。
* Invalid: 无效的编译任务次数。
* Time: 编译任务所花费的时间，单位毫秒
* FailedType: 最后一次失败的编译任务的编译类型。
* FailedMethod: 最后一次失败的编译任务的类名和方法    

### -gc output <a id="gcoutput">[≡](#≡)</a>
#### 示例内容
```
S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    
CCSC      CCSU   YGC     YGCT    FGC    FGCT     GCT
0.0   5120.0  0.0   5120.0 116736.0 79872.0   140288.0   109490.3  131196.0 120163.2
18044.0 14769.4   22      0.453   0      0.000    0.453
```
#### 列说明
* S0C: Current survivor space 0 capacity (kB).
* S1C: Current survivor space 1 capacity (kB).
* S0U: Survivor space 0 utilization (kB).
* S1U: Survivor space 1 utilization (kB).
* EC: Current eden space capacity (kB).
* EU: Eden space utilization (kB).
* OC: Current old space capacity (kB).
* OU: Old space utilization (kB).
* MC: Metaspace capacity (kB). 
* MU: Metacspace utilization (kB).
* CCSC: Compressed class space capacity (kB).
* CCSU: Compressed class space used (kB).
* YGC: Number of young generation garbage collection events.
* YGCT: Young generation garbage collection time.
* FGC: Number of full GC events.
* FGCT: Full garbage collection time.
* GCT: Total garbage collection time.
    
### -gccapacity output <a id="gccapacityoutput">[≡](#≡)</a>
#### 示例内容
示例JVM参数设置：-XX:+UseSerialGC -Xmx20m -Xms20m -Xmn10m -XX:SurvivorRatio=8
```    
NGCMN    NGCMX     NGC     S0C   S1C       EC      OGCMN      OGCMX       OGC         OC       MCMN     MCMX      MC     CCSMN    CCSMX     CCSC    YGC    FGC
10240.0  10240.0  10240.0 1024.0 1024.0   8192.0    10240.0    10240.0    10240.0    10240.0   0.0    1056768.0  4864.0   0.0   1048576.0   512.0    2     0
```
#### 列说明
* NGCMN: Minimum new generation capacity (kB).
* NGCMX: Maximum new generation capacity (kB).
* NGC: Current new generation capacity (kB).
* S0C: Current survivor space 0 capacity (kB). 
* S1C: Current survivor space 1 capacity (kB).
* EC: Current eden space capacity (kB).
* OGCMN: Minimum old generation capacity (kB).
* OGCMX: Maximum old generation capacity (kB).
* OGC: Current old generation capacity (kB).
* OC: Current old space capacity (kB).
* MCMN: Minimum metaspace capacity (kB).
* MCMX: Maximum metaspace capacity (kB).
* MC: Metaspace capacity (kB).
* CCSMN: Compressed class space minimum capacity (kB).
* CCSMX: Compressed class space maximum capacity (kB).
* CCSC: Compressed class space capacity (kB).
* YGC: Number of young generation GC events.
* FGC: Number of full GC events.

### -gccause output <a id="gccauseoutput">[≡](#≡)</a>
#### 示例内容
```
S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT    LGCC                 GCC
0.00   0.00  45.33  45.87  54.49  55.74    2    0.003     0    0.000    0.003 Allocation Failure   No GC
```
#### 列说明
This option displays the same summary of garbage collection statistics as the -gcutil option, but includes the causes of the last garbage collection event and (when applicable) the current garbage collection event. In addition to the columns listed for -gcutil, this option adds the following columns:
* LGCC: Cause of last garbage collection
* GCC: Cause of current garbage collection

### -gcnew output <a id="gcnewoutput">[≡](#≡)</a>
#### 示例内容
```
S0C    S1C    S0U    S1U   TT MTT  DSS      EC       EU     YGC     YGCT
1024.0 1024.0  0.0   0.0   15  15  512.0   8192.0   3713.8      2    0.003
```
#### 列说明
* S0C: Current survivor space 0 capacity (kB).
* S1C: Current survivor space 1 capacity (kB).
* S0U: Survivor space 0 utilization (kB).
* S1U: Survivor space 1 utilization (kB).
* TT: Tenuring threshold.
* MTT: Maximum tenuring threshold.
* DSS: Desired survivor size (kB).
* EC: Current eden space capacity (kB).
* EU: Eden space utilization (kB).
* YGC: Number of young generation GC events.
* YGCT: Young generation garbage collection time.
   
### -gcnewcapacity output <a id="gcnewcapacityoutput">[≡](#≡)</a>
#### 示例内容
```
NGCMN      NGCMX       NGC      S0CMX     S0C     S1CMX     S1C       ECMX        EC      YGC   FGC
10240.0    10240.0    10240.0   1024.0   1024.0   1024.0   1024.0     8192.0     8192.0     2     0
```
#### 列说明
* NGCMN: Minimum new generation capacity (kB).
* NGCMX: Maximum new generation capacity (kB).
* NGC: Current new generation capacity (kB).
* S0CMX: Maximum survivor space 0 capacity (kB).
* S0C: Current survivor space 0 capacity (kB).
* S1CMX: Maximum survivor space 1 capacity (kB).
* S1C: Current survivor space 1 capacity (kB).
* ECMX: Maximum eden space capacity (kB).
* EC: Current eden space capacity (kB).
* YGC: Number of young generation GC events.
* FGC: Number of full GC events.
       
### -gcold output <a id="gcoldoutput">[≡](#≡)</a>
#### 示例内容
```
MC       MU      CCSC     CCSU       OC          OU       YGC    FGC    FGCT     GCT
4864.0   2649.7    512.0    285.4     10240.0      4697.5      2     0    0.000    0.003
```
#### 列说明
* MC: Metaspace capacity (kB).
* MU: Metaspace utilization (kB).
* CCSC: Compressed class space capacity (kB).
* CCSU: Compressed class space used (kB).
* OC: Current old space capacity (kB).
* OU: Old space utilization (kB).
* YGC: Number of young generation GC events.
* FGC: Number of full GC events.
* FGCT: Full garbage collection time.
* GCT: Total garbage collection time.

### -gcoldcapacity output <a id="gcoldcapacityoutput">[≡](#≡)</a>
#### 示例内容
```
OGCMN       OGCMX        OGC         OC       YGC   FGC    FGCT     GCT
10240.0     10240.0     10240.0     10240.0     2     0    0.000    0.003
```
#### 列说明
* OGCMN: Minimum old generation capacity (kB).
* OGCMX: Maximum old generation capacity (kB).
* OGC: Current old generation capacity (kB).
* OC: Current old space capacity (kB).
* YGC: Number of young generation GC events.
* FGC: Number of full GC events.
* FGCT: Full garbage collection time.
* GCT: Total garbage collection time.

### -gcmetacapacity output <a id="gcmetacapacityoutput">[≡](#≡)</a>
#### 示例内容
```
MCMN       MCMX        MC       CCSMN      CCSMX       CCSC     YGC   FGC    FGCT     GCT
0.0  1056768.0     4864.0        0.0  1048576.0       512.0      2     0    0.000   0.003
```
#### 列说明
* MCMN: Minimum metaspace capacity (kB).
* MCMX: Maximum metaspace capacity (kB).
* MC: Metaspace capacity (kB).
* CCSMN: Compressed class space minimum capacity (kB).
* CCSMX: Compressed class space maximum capacity (kB).
* YGC: Number of young generation GC events.
* FGC: Number of full GC events.
* FGCT: Full garbage collection time.
* GCT: Total garbage collection time.

### -gcutil output <a id="gcutiloutput">[≡](#≡)</a>
#### 示例内容
```
S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT  
0.00   0.00  45.33  45.87  54.48  55.74      2    0.003     0    0.000    0.003
```
#### 列说明   
* S0: Survivor space 0 utilization as a percentage of the space's current capacity.
* S1: Survivor space 1 utilization as a percentage of the space's current capacity.
* E: Eden space utilization as a percentage of the space's current capacity.
* O: Old space utilization as a percentage of the space's current capacity.
* M: Metaspace utilization as a percentage of the space's current capacity.
* CCS: Compressed class space utilization as a percentage.
* YGC: Number of young generation GC events.
* YGCT: Young generation garbage collection time.
* FGC: Number of full GC events.
* FGCT: Full garbage collection time.
* GCT: Total garbage collection time.

### -printcompilation output <a id="printcompilationoutput">[≡](#≡)</a>
#### 示例内容
```
Compiled  Size  Type Method
 151     51    1 jdk/internal/org/objectweb/asm/ByteVector enlarge
```
#### 列说明
* Compiled: Number of compilation tasks performed by the most recently compiled method.
* Size: Number of bytes of byte code of the most recently compiled method.
* Type: Compilation type of the most recently compiled method.
* Method
    Class name and method name identifying the most recently compiled method. Class name uses slash (/) instead of dot (.) as a name space separator. Method name is the method within the specified class. The format for these two fields is consistent   with the HotSpot -XX:+PrintCompilation option.


## 参考文档 <a id="r">[≡](#≡)</a>     

### 官方文档

http://docs.oracle.com/javase/7/docs/technotes/tools/share/jstat.html