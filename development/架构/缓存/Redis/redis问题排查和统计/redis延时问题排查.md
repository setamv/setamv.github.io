# redis延时问题排查

## 测试redis服务的延时
+ 使用`redis-cli --intrinsic-latency`测试redis服务意外的环境（如虚拟机、操作系统等）所产生的延时
    参考[`redis-cli --intrinsic-latency`命令选项](../redis-cli命令选项/redis-cli命令选项.md)一节
+ 使用`redis-cli --latency`测试redis客户端到redis服务端的网络延时
    参考[`redis-cli --latency`命令选项](../redis-cli命令选项/redis-cli命令选项.md)一节


## 查看Linux内核是否禁用了 transparent_hugepage 特性
当Linux内核开启transparent_hugepage特性时，redis在调用fork进行持久化的时候可能遭受很大的延时，大的内存分页是导致延时的原因。
例如，在Linux/AMD64系统中，内存被分为4KB大小一页，每一页有一个虚拟地址。为了保存每一页的虚拟地址信息，在一个有24GB内存的机器中，将需要耗费 24GB / 4KB * 8 = 48MB的内存。
在执行fork的过程中：
1. Fork is called, two processes with shared huge pages are created.
    redis持久化时，会调用fork来创建一个子进程，用于在后台做持久化，子进程和redis主进程将共享huge pages（即实际的物理内存）。
2. In a busy instance, a few event loops runs will cause commands to target a few thousand of pages, causing the copy on write of almost the whole process memory.
    上面的意思是说当redis实例很忙的时候，在很短的时间内可能会改变好几千个内存分页中的数据，因为在未改变数据之前，主进程和子进程是共享物理内存的，改变之后，子进程会复制一份数据发生了改变的内存分页（原理参见下面的COW-写时拷贝技术），在开启transparent_hugepage时，一个内存分页的大小为2M，几千个分页将变为好几G的内存复制。
3. This will result in big latency and big memory usage.
    第2点将导致在fork的过程中产生很大的延时和很大的内存消耗。

可以禁用该特性。方法是执行以下的命令：
```
# echo never > /sys/kernel/mm/transparent_hugepage/enabled
```

### 什么是 Huge pages(标准大页) 和 Transparent Huge pages(透明大页)
在 Linux 中大页分为两种： Huge pages (标准大页) 和  Transparent Huge pages(透明大页) 。
内存是以块即页的方式进行管理的，当前大部分系统默认的页大小为 4096 bytes 即 4K 。 1MB 内存等于 256 页； 1GB 内存等于 256000 页。
CPU 拥有内置的内存管理单元，包含这些页面的列表，每个页面通过页表条目引用。当内存越来越大的时候，CPU 需要管理这些内存页的成本也就越高

Huge Pages
Huge pages  是从 Linux Kernel 2.6 后被引入的，目的是通过使用大页内存来取代传统的 4kb 内存页面， 以适应越来越大的系统内存，让操作系统可以支持现代硬件架构的大页面容量功能。
Huge pages  有两种格式大小：2MB 和 1GB ， 2MB 页块大小适合用于GB大小的内存， 1GB 页块大小适合用于TB级别的内存；2MB 是默认的页大小。
Transparent Huge Pages

Transparent Huge Pages  缩写  THP ，这个是 RHEL 6 开始引入的一个功能，在 Linux6 上透明大页是默认启用的。
由于 Huge pages 很难手动管理，而且通常需要对代码进行重大的更改才能有效的使用，因此 RHEL 6 开始引入了 Transparent Huge Pages （ THP ）， THP 是一个抽象层，能够自动创建、管理和使用传统大页。
THP 为系统管理员和开发人员减少了很多使用传统大页的复杂性 ,  因为 THP 的目标是改进性能 ,  因此其它开发人员  ( 来自社区和红帽 )  已在各种系统、配置、应用程序和负载中对  THP  进行了测试和优化。这样可让  THP  的默认设置改进大多数系统配置性能。但是 ,  不建议对数据库工作负载使用  THP 。

这两者最大的区别在于 :  标准大页管理是预分配的方式，而透明大页管理则是动态分配的方式。


### 什么是 COW（copy on write，写时拷贝技术）
写入时复制（英语：Copy-on-write，简称COW）是一种计算机程序设计领域的优化策略。其核心思想是，如果有多个调用者（callers）同时请求相同资源（如内存或磁盘上的数据存储），他们会共同获取相同的指针指向相同的资源，直到某个调用者试图修改资源的内容时，系统才会真正复制一份专用副本（private copy）给该调用者，而其他调用者所见到的最初的资源仍然保持不变。这过程对其他的调用者都是透明的（transparently）。


例如：
现在有一个父进程P1，在其虚拟地址空间（有相应的数据结构表示）上有：正文段，数据段，堆，栈这四个部分，相应的，内核要为这四个部分分配各自的物理块。即：正文段块，数据段块，堆块，栈块。
+ 代码段:顾名思义，就是存放了程序代码的数据，假如机器中有数个进程运行相同的一个程序，那么它们就以使用相同的代码段。
+ 数据段：则存放程序的全局变量，常数以及动态数据分配的数据空间（比如用malloc之类的函数取得的空间）
+ 堆栈段:存放的就是子程序的返回地址、子程序的参数以及程序的局部变量。

现在P1用fork()函数为进程创建一个子进程P2，内核将只为新生成的P2子进程创建虚拟空间结构，它们来复制于父进程的虚拟空间结构，但是不为这些段分配物理内存，它们共享父进程的物理空间，当父子进程中有更改相应段的行为发生时，再为子进程相应的段分配物理空间。

### 当COW(写时拷贝技术)遇到Transparent Huge pages(透明大页)
以redis为例，在执行`save`或`bgrewriteaof`时，将调用fork()创建子进程（后台进程）用于持久化。此时就存在两个进程：主进程，用于处理redis客户端的请求；子进程，用于在后台持久化。
因为应用了COW(写时拷贝技术)，主进程和子进程在fork后的那一刻将共享物理内存，直到主进程因为接收新的redis客户端请求而改变了数据的值，这将导致子进程会复制一份内存页，这块内存页大小在未启用Transparent Huge pages(透明大页)时是4KB，如果开启了Transparent Huge pages(透明大页)，将变为2M。

设想，如果子进程持久化的时间持续6秒钟（数据占用约1G的内存），而此时redis的TPS为100，假设最坏的情况，即每个请求改变的值都不一样，则整个过程中将发生 6 * 100 个值的改变，如果每个值都散列在不同的内存分页中，将发生内存复制的大小分别为：
1）未开启Transparent Huge pages为 6 * 100 * 4KB = 2400KB；
2）开启Transparent Huge pages为 6 * 100 * 2M = 1200M 

影响：
1. 在复制这部分内存的过程中，redis服务将阻塞从而导致延时
2. 因为COW(写时拷贝技术)将导致子进程持久化的过程中内存占用突然飙升，如上例，开启Transparent Huge pages时内存占用将增加1200M。 


### 查看redis进程使用的Transparent Huge pages
通过"/proc/meminfo"文件中的`AnonHugePages`项可以查看到整个系统所使用的Transparent Huge pages大小，如下所示：
```
# cat /proc/meminfo 
MemTotal:        1882768 kB
MemFree:         1208956 kB
MemAvailable:    1545272 kB
...
AnonHugePages:     69632 kB         [注解]Transparent Huge pages被使用的大小
CmaTotal:              0 kB
CmaFree:               0 kB
HugePages_Total:       0
HugePages_Free:        0
HugePages_Rsvd:        0
HugePages_Surp:        0
Hugepagesize:       2048 kB
DirectMap4k:       65408 kB
DirectMap2M:     2031616 kB
DirectMap1G:           0 kB
```

通过"/proc/pid/smaps"文件（pid为进程的ID）中的`AnonHugePages`项可以查看指定进程所使用的Transparent Huge pages记录，如下所示：
```
# cat /proc/1919/smaps | grep AnonHugePages
...
AnonHugePages:         0 kB
AnonHugePages:         0 kB
AnonHugePages:         0 kB
AnonHugePages:      4096 kB
AnonHugePages:         0 kB
```

## 启用redis的延时监控
启用redis的延时监控，监控redis的延时信息。参考[latency相关命令](../命令/latency相关命令.md)。
1. 启用延时监控：`> config set latency-monitor-threshold 100`（阈值设置为100毫秒）
2. 查看延时统计信息：`> latency graph command` 或 `> latency doctor` 或 `> latency latest` ...

## 查看持久化设置
以下的持久化设置下，延时的可能性依次变小：
1. AOF + fsync always
    即设置配置项`appendfsync always`，redis将在执行每个命令后立即将改变写入磁盘。这种情况下redis的性能是最差的。
2. AOF + fsync every second
    即设置配置项`appendfsync everysec`，redis将每秒写入一次磁盘。这种设置既可以保证比较好的数据安全性，又有较好的性能
3. AOF + fsync every second + no-appendfsync-on-rewrite
    即设置配置项`appendfsync everysec`以及`no-appendfsync-on-rewrite yes`
    `no-appendfsync-on-rewrite yes`意味着，在fork一个子进程执行`BGSAVE`或`BGREWRITEAOF`的过程中，将不会执行fsync，从而减轻执行`BGSAVE`或`BGREWRITEAOF`的过程中磁盘的压力。
    这种配置比第2中配置的发生延时的可能性更低
4. AOF + fsync never
    即设置配置项`appendfsync no`，redis将不会主动执行fsync，而是将刷新输出缓冲区的任务交给操作系统（刷新输出缓冲区意味着将输出缓冲区的内容写入磁盘）。因为操作系统刷新
5. RDB
    即不启用AOF，只是用RDB进行持久化。


## 查看fork产生的延时
redis在持久化的过程中（生成RDB或重写AOF文件），redis会fork一个子进程（后台进程）。fork操作本身会产生延时，可以从`info`命令的统计信息中查看fork产生的延时信息。
```
127.0.0.1:6379> info stats                  
# Stats
total_connections_received:0
...
latest_fork_usec:0                  [注解]redis服务刚启动时，因为还未执行任何命令，所以没有fork操作
...

127.0.0.1:6379> save                [注解]使用save命令迫使redis生成RDB文件，此时会进行fork操作
OK

127.0.0.1:6379> info stats
# Stats
total_connections_received:1
... 
latest_fork_usec:1187               [注解]fork操作的时间是1187微秒（usec是微秒microseconds的缩写）
... 
```

一般机器内存越大，fork的时间会越长。
下面是redis官网中对不同机器配置下fork的时长统计：
+ Linux beefy VM on VMware 6.0GB RSS forked in 77 milliseconds (12.8 milliseconds per GB).
+ Linux running on physical machine (Unknown HW) 6.1GB RSS forked in 80 milliseconds (13.1 milliseconds per GB)
+ Linux running on physical machine (Xeon @ 2.27Ghz) 6.9GB RSS forked into 62 milliseconds (9 milliseconds per GB).
+ Linux VM on 6sync (KVM) 360 MB RSS forked in 8.2 milliseconds (23.3 milliseconds per GB).
+ Linux VM on EC2, old instance types (Xen) 6.1GB RSS forked in 1460 milliseconds (239.3 milliseconds per GB).
+ Linux VM on EC2, new instance types (Xen) 1GB RSS forked in 10 milliseconds (10 milliseconds per GB).
+ Linux VM on Linode (Xen) 0.9GBRSS forked into 382 milliseconds (424 milliseconds per GB).


## swapping(虚拟内存)导致的延时
Linux和Windows都支持虚拟内存，即按一定策略将内存中的数据交换到磁盘中，并在应用程序需要使用这些数据的时候再从磁盘加载到内存中，从而使得系统可以加载比实际内存大小更多的数据。
当redis的数据被交换到磁盘中后，再次命中这部分数据时，需要先从磁盘中加载这部分数据，从而导致更多的延时。
导致redis的数据被交换到磁盘的原因可能有：
1. 系统的内存压力过大，比如开启的应用过多，或redis本身比预想占用的内存更多
2. redis中的部分数据闲置时间过长，操作系统内核可能将这部分长时间未被访问的内存交换到磁盘中去
3. 部分进程产生大量的I/O操作。因为文件通常会被缓存，大量的I/O可能导致内核增加文件的缓存，从而导致内存的占用飙升。redis的持久化(RDB和AOF)也有可能导致这种情况发生。

如果是因为swapping导致redis的延时，只能通过增加内存或分离其他耗用内存比较大的程序到其他服务器中。

Linux系统提供了很多工具可以用来追踪swapping信息，比如 smaps文件、iostat命令查看I/O统计。

### smaps
`/proc/pid/smaps`文件（pid为进程ID）中记录了进程所占用物理内存的使用情况。如下所示：
```
# redis-cli info | grep process_id
process_id:1919

# cat /proc/1919/smaps
...
7f6dbb014000-7f6dbb214000 ---p 00002000 fd:01 1059508                    /usr/lib64/libdl-2.17.so
Size:               2048 kB
Rss:                   0 kB
Pss:                   0 kB
Shared_Clean:          0 kB
Shared_Dirty:          0 kB
Private_Clean:         0 kB
Private_Dirty:         0 kB
Referenced:            0 kB
Anonymous:             0 kB
AnonHugePages:         0 kB
Swap:                  0 kB
KernelPageSize:        4 kB
MMUPageSize:           4 kB
Locked:                0 kB
VmFlags: mr mw me sd 
7fff95e4f000-7fff95e51000 r-xp 00000000 00:00 0                          [vdso]
Size:                  8 kB
Rss:                   4 kB
Pss:                   0 kB
Shared_Clean:          4 kB
Shared_Dirty:          0 kB
Private_Clean:         0 kB
Private_Dirty:         0 kB
Referenced:            4 kB
Anonymous:             0 kB
AnonHugePages:         0 kB
Swap:                  0 kB
KernelPageSize:        4 kB
MMUPageSize:           4 kB
Locked:                0 kB
VmFlags: rd ex mr mw me de sd 
```

`/proc/pid/smaps`文件中被分为了很多段，每一段表示进程虚拟内存空间中一块连续的区域。其中：
+ 第一行从左到右依次表示地址范围、权限标识、映射文件偏移、设备号、inode、文件路径。详细解释可以参见[understanding-linux-proc-id-maps](http://stackoverflow.com/questions/1401359/understanding-linux-proc-id-maps)
+ Size：表示该映射区域在虚拟内存空间中的大小。
+ Rss：表示该映射区域当前在物理内存中占用了多少空间　
+ Pss：该虚拟内存区域平摊计算后使用的物理内存大小(有些内存会和其他进程共享，例如mmap进来的)。比如该区域所映射的物理内存部分同时也被另一个进程映射了，且该部分物理内存的大小为1000KB，那么该进程分摊其中一半的内存，即Pss=500KB。　　　　　
+ Shared_Clean：和其他进程共享的未被改写的page的大小
+ Shared_Dirty： 和其他进程共享的被改写的page的大小
+ Private_Clean：未被改写的私有页面的大小。
+ Private_Dirty： 已被改写的私有页面的大小。
+ Swap：表示非mmap内存（也叫anonymous memory，比如malloc动态分配出来的内存）由于物理内存不足被swap到交换空间的大小。

下面列出了redis进程使用到的所有虚拟内存大小以及swap内存，可以看到，基本上swap都是0，表明很少发生了内存的swap：
```
# cat /proc/1919/smaps | egrep '^(Swap|Size)'
Size:               1564 kB
Swap:                  0 kB
Size:                  8 kB
Swap:                  0 kB
Size:                 24 kB
Swap:                  0 kB
Size:               2200 kB
Swap:                  0 kB
Size:                132 kB
Swap:                  0 kB
Size:               5632 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:               8192 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:               8192 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:               8192 kB
Swap:                  0 kB
Size:             103588 kB
Swap:                  0 kB
Size:               8192 kB
Swap:                  0 kB
Size:               1804 kB
Swap:                  0 kB
Size:               2044 kB
Swap:                  0 kB
Size:                 16 kB
Swap:                  0 kB
Size:                  8 kB
Swap:                  0 kB
Size:                 20 kB
Swap:                  0 kB
Size:                 92 kB
Swap:                  0 kB
Size:               2044 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                 16 kB
Swap:                  0 kB
Size:                 28 kB
Swap:                  0 kB
Size:               2044 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  8 kB
Swap:                  0 kB
Size:               2048 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:               1028 kB
Swap:                  0 kB
Size:               2044 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                136 kB
Swap:                  0 kB
Size:                 20 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
Size:                132 kB
Swap:                  0 kB
Size:                  8 kB
Swap:                  0 kB
Size:                  4 kB
Swap:                  0 kB
```

### iostat命令
iostat命令可以查看系统的I/O统计信息，如下所示：
```
# iostat -xk 1
Linux 3.10.0-862.2.3.el7.x86_64 (izwz95n8068u7u1zz5oihcz)       08/31/2019      _x86_64_        (1 CPU)

avg-cpu:  %user   %nice %system %iowait  %steal   %idle
           1.04    0.00    0.52    0.05    0.00   98.38

Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
vda               0.00     0.43    0.01    1.55     0.51    10.38    13.98     0.02   10.66    7.55   10.67   0.67   0.10
```


## AOF持久化导致的延时
当配置了AOF的持久化方式，redis通过两个系统调用完成数据写入磁盘：
+ write(2)
    write(2)将数据写入AOF文件的缓冲区。write(2)可能因为系统级的同步或者因为文件缓冲区满了从而导致延时。
+ fdatasync(2)
    fdatasync(2)用于将文件缓冲区的数据写入磁盘。取决于磁盘I/O的性能，fdatasync(2)可能因为系统I/O瓶颈而导致延时。fdatasync(2)是最有可能导致延时的操作，特别是其他进程也在执行I/O操作的时候。
Linux下可以使用`strace`命令来追踪redis进程的，`strace`命令的用法参考[strace命令](/linux/命令工具/strace.md)
下面通过`strace`命令拦截和记录redis调用系统的fdatasync和write命令进行持久化
```
# strace -f -p 27757 -T -e trace=fdatasync,write
strace: Process 27757 attached with 4 threads
[pid 27757] write(8, "*2\r\n$6\r\nSELECT\r\n$1\r\n0\r\n*3\r\n$3\r\ns"..., 56) = 56 <0.000118>
[pid 27757] write(9, "+OK\r\n", 5)      = 5 <0.000195>
[pid 27759] fdatasync(8)                = 0 <0.002662>
```
上面的"<0.000118>"表示系统调用消耗的时间，单位为秒。

## key过期导致的延时
redis检查key过期的策略分为两种：
+ 其一是当过期的key被访问到时，删除key；
+ 另一种是redis服务端周期性的（10次/秒）抽查一批被设置了过期时间的key，如果发现有过期的key，将删除key
    这种策略下，当抽查的key中，有超过25%的key都过期了，redis将继续抽查下一批，直到抽查的key中过期的比例不超过25%。
如果redis中发生大规模的key在同一时刻过期，可能导致redis被阻塞。所以在批量设置key的过期时间时，最好在过期时间上增加一部分随机时间，避免过期时间过于集中。

## 慢执行日志
redis可以记录慢执行日志。通过配置项`slowlog-log-slower-than <times-in-usec>`和`slowlog-max-len <len>`进行设置。
`slowlog-log-slower-than <times-in-micro>`配置项中的参数`<times-in-micro>`用于指定慢执行的阈值，单位为毫秒。当值为0时，将记录所有执行的日志。当值为负数时，表示禁止记录慢执行日志
`slowlog-max-len <len>`配置项中的参数`<len>`用于指定记录慢执行日志的最大数量，redis中的慢执行日志都是记录在内存中，不会写到文件中，所以，当慢执行日志达到最大数量时，加入新的慢执行日志时，将丢弃一部分最老的慢执行日志。
可以通过`slowlog get <n>`查看慢执行日志记录（参数n可选，为一次查看的记录数量）；
通过`slowlog reset`命令清空之前的慢执行日志记录；
通过`slowlog len`命令查看当前记录的慢执行日志的数量；
如下所示：
```
# redis-cli config set slowlog-log-slower-than 500
OK
# redis-cli config set slowlog-max-len 2
OK
# redis-cli debug sleep 400
OK
# redis-cli debug sleep 500
OK
# redis-cli debug sleep 600
OK
# redis-cli slowlog get
1) 1) (integer) 4                   [注解]该值为慢执行日志的序列号，每记录一次慢执行日志，该值加1
   2) (integer) 1567241772          [注解]该值为慢执行发生的时间的Unix时间戳
   3) (integer) 463376              [注解]该值为操作执行的时间，单位为微秒（？为什么时间与sleep的时间不一样）
   4) 1) "debug"                    [注解]该值及后面的子列表为慢执行操作的命令和参数
      2) "sleep"
      3) "600"
   5) "127.0.0.1:38202"             [注解]该值为慢执行操作的客户端地址
   6) ""                            [注解]该值为慢执行操作的客户端名称，可以通过`CLIENT SETNAME`进行设置
2) 1) (integer) 3
   2) (integer) 1567241763
   3) (integer) 423255
   4) 1) "debug"
      2) "sleep"
      3) "500"
   5) "127.0.0.1:38198"
   6) ""
```

## 开启redis的watchdog对延时进行监控
redis2.6引入了watchdog可用于监控redis的延时操作。
redis官方建议在生产环境下开启watchdog要慎重，因为它可能以非预期的方式干涉redis的普通操作的执行，所有开启之前最好做好数据备份。另外，只有在尝试过所有方法后都无法排查到延时问题的前提下，才开启watchdog作为最后的排查手段。
开启watchdog的监控的过程为：
1. 通过`config set watchdog-period 500`开启watchdog，其中500为监控延时的阈值，单位为毫秒。最小的阈值为200毫秒。所以设置的值必须>=200毫秒
    当阈值设置为0时，表示关闭watchdog
2. redis开始不断的监控自己的操作
3. 当redis检测到操作被阻塞超过指定的延时阈值，将输出操作相关的报告信息到redis日志文件
4. 将延时的报告信息提交给redis开发人员，因为报告信息并不是通俗易懂的内容

注意：无法通过配置文件对watchdog进行设置，只能在运行时通过命令`config set watchdog-period 500`开启watchdog

以下通过`debug sleep`调试工具模拟延时：
```
# redis-cli config set watchdog-period 500
OK
# redis-cli debug sleep 1000                        [注解]让命令等待1秒钟，超过延时的阈值500毫秒
OK
# tail -n 25 /var/log/redis/redis-6379.log          [注解]查看redis最后输出的日志
27757:M 31 Aug 2019 15:33:58.117 * Background saving started by pid 29435
29435:C 31 Aug 2019 15:33:58.126 * DB saved on disk
29435:C 31 Aug 2019 15:33:58.129 * RDB: 0 MB of memory used by copy-on-write
27757:M 31 Aug 2019 15:33:58.219 * Background saving terminated with success
27757:signal-handler (1567240660) 
--- WATCHDOG TIMER EXPIRED ---
EIP:
/lib64/libpthread.so.0(__nanosleep+0x2d)[0x7f7e02fbff3d]

Backtrace:
./src/redis-server 127.0.0.1:6379(logStackTrace+0x29)[0x471049]
./src/redis-server 127.0.0.1:6379(watchdogSignalHandler+0x1b)[0x4710fb]
/lib64/libpthread.so.0(+0xf6d0)[0x7f7e02fc06d0]
/lib64/libpthread.so.0(__nanosleep+0x2d)[0x7f7e02fbff3d]
./src/redis-server 127.0.0.1:6379(debugCommand+0xc07)[0x470ae7]
./src/redis-server 127.0.0.1:6379(call+0xa7)[0x430a17]
./src/redis-server 127.0.0.1:6379(processCommand+0x33f)[0x431caf]
./src/redis-server 127.0.0.1:6379(processInputBuffer+0x175)[0x4409a5]
./src/redis-server 127.0.0.1:6379(aeProcessEvents+0x2a0)[0x42aed0]
./src/redis-server 127.0.0.1:6379(aeMain+0x2b)[0x42b19b]
./src/redis-server 127.0.0.1:6379(main+0x4af)[0x42804f]
/lib64/libc.so.6(__libc_start_main+0xf5)[0x7f7e02c06445]
./src/redis-server 127.0.0.1:6379[0x42829a]
27757:signal-handler (1567240660) --------
```