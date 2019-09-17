# redis运行时统计
本文将讲述各种统计redis实例的性能、当前实例的运行状况的方法。

## 持续性查看redis实例的概况
`redis-cli --stat -i <interval>`可以相linux中的top命令那样，连续实时的查看redis实例的运行状况。
该命令将每隔一段时间输出一次redis实例的运行状况。间隔时间可以通过`<interval>`参数执行，单位为秒，可以为小数，如0.5表示每半秒。
该命令的输出内容如下所示：
```
# redis-cli --stat
------- data ------ --------------------- load -------------------- - child -
keys       mem      clients blocked requests            connections          
3          835.23K  1       0       102095 (+0)         43          
3          835.23K  1       0       102096 (+1)         43          
3          835.23K  1       0       102097 (+1)         43          
3          835.23K  1       0       102098 (+1)         43          
3          835.23K  1       0       102099 (+1)         43  
...
```

## 扫描big key
`redis-cli --bigkeys -i <sleep-interval>`可以分析redis实例中的big key，并输出。参数`-i <sleep-interval>`用于指定分析的过程中每扫描100个key之后sleep的时间。单位为秒。
注意：
+ 该命令只会对当前的数据库进行分析，而不是整个redis实例的所有数据库。所以，可以通过`-n`选项来指定需要分析的数据库

其输出内容如下所示：
```
# redis-cli --bigkeys

Warning: Using a password with '-a' option on the command line interface may not be safe.

# Scanning the entire keyspace to find biggest keys as well as
# average sizes per key type.  You can use -i 0.1 to sleep 0.1 sec
# per 100 SCAN commands (not usually needed).

[00.00%] Biggest hash   found so far 'pm_product:128' with 3 fields
[00.00%] Biggest hash   found so far 'pm_product_barcode:117' with 141 fields
[00.00%] Biggest string found so far 'sys_config:wms_token_valid_time' with 2 bytes
[08.33%] Biggest string found so far 'notify_message_last_update_time:123:171' with 19 bytes
[08.33%] Biggest list   found so far 'sys_privilege_id_list:2' with 360 items
[24.24%] Biggest hash   found so far 'pm_product:116' with 221 fields
[55.30%] Biggest hash   found so far 'pm_product:136' with 230 fields
[55.30%] Biggest hash   found so far 'sys_privilege' with 389 fields
[62.88%] Biggest string found so far 'sys_config:wms.domain' with 24 bytes
[78.79%] Biggest string found so far 'sys_config:resource.file.home' with 27 bytes
[注解]：上面会出现多个Biggest hash是因为每次扫描一个key是与上一次对应类型的key的最大值进行比较，如果更大，将输出。所以，同类型的key越往下，key越大。

-------- summary -------

Sampled 132 keys in the keyspace!
Total key length in bytes is 2880 (avg len 21.82)

Biggest string found 'sys_config:resource.file.home' has 27 bytes
Biggest   list found 'sys_privilege_id_list:2' has 360 items
Biggest   hash found 'sys_privilege' has 389 fields

29 strings with 452 bytes (21.97% of keys, avg size 15.59)
3 lists with 389 items (02.27% of keys, avg size 129.67)
0 sets with 0 members (00.00% of keys, avg size 0.00)
100 hashs with 3221 fields (75.76% of keys, avg size 32.21)
0 zsets with 0 members (00.00% of keys, avg size 0.00)
0 streams with 0 entries (00.00% of keys, avg size 0.00)
```

## 使用info命令查看redis服务的统计信息
`> info`命令可以查看redis实例当前的状态以及统计信息。可以通过`> info`一次性输出所有统计信息，也可以每次只输出一类统计信息（如：`> info clients`）。统计信息包括以下几个方面：
### Server    
redis 服务端的一些信息，如下图所示。
```
# Server
redis_version:5.0.5
redis_git_sha1:00000000
redis_git_dirty:0
redis_build_id:5854306b54493a31
redis_mode:standalone
os:Linux 3.10.0-862.2.3.el7.x86_64 x86_64
arch_bits:64
multiplexing_api:epoll
atomicvar_api:atomic-builtin
gcc_version:4.8.5
process_id:11604
run_id:d4911fffdc9d0a19278b810c01973cb0eaaf33c2
tcp_port:6379
uptime_in_seconds:380562
uptime_in_days:4
hz:10                                   [注解]该值表示后台任务的执行频率，参考redis.conf中hz配置项的说明
configured_hz:10
lru_clock:6318609
executable:/opt/software/redis-5.0.5/./src/redis-server
config_file:/opt/software/redis-5.0.5/./redis.conf
```

### Clients
redis客户端连接的统计信息，如下图所示：
```
# Clients
connected_clients:1                     [注解]当前客户端的连接数。不包括slave节点的连接
client_recent_max_input_buffer:2        [注解]redis服务端会分别为每个连接的客户端设置了输入缓冲区和输出缓冲区，这里为连接的客户端中的最大输入缓冲区的值。
client_recent_max_output_buffer:20504   [注解]连接的客户端中的最大输出缓冲区的值
blocked_clients:0                       [注解]被挂起的客户端数量（客户端挂起的原因是因为执行了阻塞性的命令，如BLPOP、BRPOP等）
```

### Memory
redis实例内存消耗相关的统计信息
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

### Persistence
redis持久化相关的信息
```
# Persistence
loading:0
rdb_changes_since_last_save:0
rdb_bgsave_in_progress:0
rdb_last_save_time:1566565522
rdb_last_bgsave_status:ok
rdb_last_bgsave_time_sec:9
rdb_current_bgsave_time_sec:-1
rdb_last_cow_size:208896
aof_enabled:0
aof_rewrite_in_progress:0
aof_rewrite_scheduled:0
aof_last_rewrite_time_sec:-1
aof_current_rewrite_time_sec:-1
aof_last_bgrewrite_status:ok
aof_last_write_status:ok
aof_last_cow_size:0
```

### Stats
其他的一些统计信息
```
# Stats
total_connections_received:71
total_commands_processed:10108073
instantaneous_ops_per_sec:0
total_net_input_bytes:601430542
total_net_output_bytes:40930854
instantaneous_input_kbps:0.00
instantaneous_output_kbps:0.00
rejected_connections:0
sync_full:0
sync_partial_ok:0
sync_partial_err:0
expired_keys:0
expired_stale_perc:0.00
expired_time_cap_reached_count:0
evicted_keys:0
keyspace_hits:55
keyspace_misses:1
pubsub_channels:0
pubsub_patterns:0
latest_fork_usec:8534
migrate_cached_sockets:0
slave_expires_tracked_keys:0
active_defrag_hits:0
active_defrag_misses:0
active_defrag_key_hits:0
active_defrag_key_misses:0
```

### Replication
主从复制相关的统计信息
```
# Replication
role:master
connected_slaves:0
master_replid:740082704e3025c6daf8e176af6a00eba4b937a3
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:0
second_repl_offset:-1
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

### CPU
CPU耗用统计信息
```
# CPU
used_cpu_sys:164.429926
used_cpu_user:306.711098
used_cpu_sys_children:0.414882
used_cpu_user_children:6.560565
```

### Cluster
是否开启集群的信息
```
# Cluster
cluster_enabled:1
```

### Keyspace
```
# Keyspace
db0:keys=1,expires=0,avg_ttl=0
```


## 实时监控redis执行的命令
`monitor`命令可以实时打印redis执行的每一个命令。

## 监控redis的延时
`redis-cli --latency`命令选项可以用于检测redis客户端到redis服务端的网络延时。该命令选项将使redis客户端每秒发送100次`PING`到redis服务端，然后打印出延时的统计信息，如下所示：
```
# redis-cli --latency
min: 0, max: 1, avg: 0.12 (555 samples)
```
延时时间的单位为毫秒。

`redis-cli --latency-history`命令选项与`redis-cli --latency`命令选项类似，只不过它是每15秒（可以通过`-i <interval>`对间隔时间进行指定）对延时信息做一次取样，每次取样信息都会单独打印，如下所示：
```
# redis-cli --latency-history -i 5
min: 0, max: 1, avg: 0.11 (489 samples) -- 5.01 seconds range
min: 0, max: 1, avg: 0.11 (489 samples) -- 5.01 seconds range
min: 0, max: 1, avg: 0.10 (488 samples) -- 5.00 seconds range
min: 0, max: 2, avg: 0.11 (488 samples) -- 5.01 seconds range
```