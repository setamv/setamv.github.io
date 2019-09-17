# redis内存使用统计
redis的内存使用统计，可以用`INFO memory`、`MEMORY stats`以及`MEMORY doctor`查看和分析

## INFO memory
`INFO memory`统计的内存使用情况如下所示：
```
# Memory
used_memory:762177800                   [注解]redis实例实际分配的内存
used_memory_human:726.87M       
used_memory_rss:783060992               [注解]从操作系统的视角，redis实例分配的内存（和top命令中的结果相同）
used_memory_rss_human:746.79M
used_memory_peak:762217600              [注解]redis实例分配的内存的峰值
used_memory_peak_human:726.91M
used_memory_peak_perc:99.99%            [注解]used_memory_peak / used_memory 的比例  [疑问]这里比例不是超过100%了吗？
used_memory_overhead:841214             [注解]redis实例用于内部数据存储所使用的内存（非用户的key）        
used_memory_startup:791448              [注解]redis启动的时候占用的内存（即redis实例进程等占用的内存，不包含任何数据所占用的内存）
used_memory_dataset:761336586           [注解]redis中存储数据所占用的内存，= used_memory - used_memory_overhead
used_memory_dataset_perc:99.99%         [注解]used_memory_dataset / (used_memory - used_memory_startup)
allocator_allocated:762209472           
allocator_active:762552320
allocator_resident:784982016
total_system_memory:1927954432
total_system_memory_human:1.80G
used_memory_lua:37888                   [注解]Lua引擎占用的内存
used_memory_lua_human:37.00K
used_memory_scripts:0
used_memory_scripts_human:0B
number_of_cached_scripts:0
maxmemory:0                             [注解]maxmemory配置项的值
maxmemory_human:0B
maxmemory_policy:noeviction             [注解]maxmemory_policy配置项的值
allocator_frag_ratio:1.00               
allocator_frag_bytes:342848
allocator_rss_ratio:1.03
allocator_rss_bytes:22429696
rss_overhead_ratio:1.00
rss_overhead_bytes:-1921024
mem_fragmentation_ratio:1.03            [注解]used_memory_rss / used_memory的比例
mem_fragmentation_bytes:20946328        
mem_not_counted_for_evict:0
mem_replication_backlog:0
mem_clients_slaves:0
mem_clients_normal:49694
mem_aof_buffer:0
mem_allocator:jemalloc-5.1.0
active_defrag_running:0                 [注解]标识当前内存碎片整理程序是否正在运行
lazyfree_pending_objects:0              [注解]当前等待被释放的对象数。（一般是在unlink、flushdb、flushall等命令异步执行后）
```
[注解]：
+ `used_memory_rss` 和 `used_memory`
    正常情况下`used_memory_rss`比`used_memory`要大一点点，如果出现`used_memory_rss`远大于`used_memory`，意味着存在内存碎片，这种情况下，`mem_fragmentation_ratio`的值将比较大。
    反过来，如果`used_memory`远大于`used_memory_rss`，意味着有部分redis的内存被操作系统换出了（如被换出到虚拟内存的分页文件中了），这种情况下，redis可能会存在比较大的延时。
    `used_memory_rss` 和 `used_memory` 之间的差值还有可能是redis已经将内存还给内存的allocator了，但是allocator还未将该内存还给操作系统，这部分也可能引起他们的差值。
+ 还可以使用`memory stats`命令和`memory doctor`命令查看更多内存的信息

## MEMORY stats
使用`MEMORY stats`统计的数据如下所示：
```
127.0.0.1:6379> memory stats
 1) "peak.allocated"                    [注解]和INFO memory中的used_memory_peak相同
 2) (integer) 809247224
 3) "total.allocated"                   [注解]和INFO memory中的used_memory相同
 4) (integer) 762299464
 5) "startup.allocated"                 [注解]和INFO memory中的used_memory_startup相同
 6) (integer) 791448
 7) "replication.backlog"               [注解]主从复制时backlog占用的内存。和INFO replication中的repl_backlog_size相同
 8) (integer) 0
 9) "clients.slaves"                    [注解]主节点上管理所有从服务器相关信息所使用的内存（包括主从复制的输出缓冲区、连接上下文等）
10) (integer) 0
11) "clients.normal"                    [注解]所有客户端相关所使用的内存，包括客户端的输出缓冲区、客户端连接上下文等
12) (integer) 66616
13) "aof.buffer"                        [注解]AOF缓冲区所占用的内存
14) (integer) 0
15) "lua.caches"                        [注解]Lua
16) (integer) 0
17) "db.0"                              [注解]数据库0的管理字典数据所使用的内存（主要是hashtable）
18) 1) "overhead.hashtable.main"
    2) (integer) 232
    3) "overhead.hashtable.expires"
    4) (integer) 0
19) "overhead.total"                    [注解]redis杂项所使用的内存，包括startup.allocated、replication.backlog, clients.slaves, clients.normal, aof.buffer以及redis管理keyspace所使用的数据结构的内存消耗等。
20) (integer) 858296
21) "keys.count"                        [注解]redis实例的所有数据库的key的总数
22) (integer) 5
23) "keys.bytes-per-key"                [注解]单个key占用内存的均值。该值 = (total.allocated - startup.allocated) / keys.count
24) (integer) 152301603 
25) "dataset.bytes"                     [注解]数据占用的存储空间。该值 = total.allocated - overhead.total
26) (integer) 761441168
27) "dataset.percentage"                [注解] 该值 = dataset.bytes / (total.allocated - startup.allocated)
28) "99.991218566894531"
29) "peak.percentage"                   [注解] 该值 = peak.allocated / total.allocated
30) "94.198585510253906"
31) "allocator.allocated"
32) (integer) 762746224
33) "allocator.active"
34) (integer) 763363328
35) "allocator.resident"
36) (integer) 787050496
37) "allocator-fragmentation.ratio"
38) "1.0008090734481812"
39) "allocator-fragmentation.bytes"
40) (integer) 617104
41) "allocator-rss.ratio"
42) "1.0310300588607788"
43) "allocator-rss.bytes"
44) (integer) 23687168
45) "rss-overhead.ratio"
46) "0.99367684125900269"
47) "rss-overhead.bytes"
48) (integer) -4976640
49) "fragmentation"
50) "1.0259957313537598"
51) "fragmentation.bytes"
52) (integer) 19815440
```

## 查看内存问题诊断
`MEMORY doctor`命令可以输出redis客户端认为内存出现了问题的线索。

## Linux内核参数overcommit_memory 
在启动redis的过程中，redis的启动日志中可能会输出如下内容：
```
[13223] 17 Mar 13:18:02.207 # WARNING overcommit_memory is set to 0! Background save may fail under low memory condition. To fix this issue add 'vm.overcommit_memory = 1' to /etc/sysctl.conf and then reboot or run the command 'sysctl vm.overcommit_memory=1' for this to take effect.
```
上面的`overcommit_memory`是Linux的内存分配策略，可选的值为：0、1、2。其中：
+ 0 
    表示内核将检查是否有足够的可用内存供应用进程使用；如果有足够的可用内存，内存申请允许；否则，内存申请失败，并把错误返回给应用进程。
+ 1
    表示内核允许分配所有的物理内存，而不管当前的内存状态如何。
+ 2
    表示内核允许分配超过所有物理内存和交换空间总和的内存

Linux对大部分申请内存的请求都回复"yes"，以便能跑更多更大的程序。因为申请内存后，并不会马上使用内存。这种技术叫做 Overcommit。当linux发现内存不足时，会发生OOM killer(OOM=out-of-memory)。它会选择杀死一些进程(用户态进程，不是内核线程)，以便释放内存。
当oom-killer发生时，linux会选择杀死哪些进程？选择进程的函数是oom_badness函数(在mm/oom_kill.c中)，该函数会计算每个进程的点数(0~1000)。点数越高，这个进程越有可能被杀死。每个进程的点数跟oom_score_adj有关，而且oom_score_adj可以被设置(-1000最低，1000最高)。

修改overcommit_memory的方法：
1. 编辑/etc/sysctl.conf，设置vm.overcommit_memory=1，然后`# sysctl -p`使配置文件生效
2. 直接使用命令修改：`# sysctl vm.overcommit_memory=1`
3. 通过命令`echo 1 > /proc/sys/vm/overcommit_memory`

### overcommit_memory设置为0可能引发的问题
1. 当内存不够的时候，执行`> save`或`> bgsave`导出rdb文件时可能会失败
