# redis debug相关命令
redis debug相关命令用于debug，特别是集群的debug，其下子命令包括：
```
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7002
127.0.0.1:7002> debug help
 1) DEBUG <subcommand> arg arg ... arg. Subcommands are:
 2) ASSERT -- Crash by assertion failed.
 3) CHANGE-REPL-ID -- Change the replication IDs of the instance. Dangerous, should be used only for testing the replication subsystem.
 4) CRASH-AND-RECOVER <milliseconds> -- Hard crash and restart after <milliseconds> delay.
 5) DIGEST -- Output a hex signature representing the current DB content.
 6) DIGEST-VALUE <key-1> ... <key-N>-- Output a hex signature of the values of all the specified keys.
 7) ERROR <string> -- Return a Redis protocol error with <string> as message. Useful for clients unit tests to simulate Redis errors.
 8) LOG <message> -- write message to the server log.
 9) HTSTATS <dbid> -- Return hash table statistics of the specified Redis database.
10) HTSTATS-KEY <key> -- Like htstats but for the hash table stored as key's value.
11) LOADAOF -- Flush the AOF buffers on disk and reload the AOF in memory.
12) LUA-ALWAYS-REPLICATE-COMMANDS <0|1> -- Setting it to 1 makes Lua replication defaulting to replicating single commands, without the script having to enable effects replication.
13) OBJECT <key> -- Show low level info about key and associated value.
14) PANIC -- Crash the server simulating a panic.
15) POPULATE <count> [prefix] [size] -- Create <count> string keys named key:<num>. If a prefix is specified is used instead of the 'key' prefix.
16) RELOAD -- Save the RDB on disk and reload it back in memory.
17) RESTART -- Graceful restart: save config, db, restart.
18) SDSLEN <key> -- Show low level SDS string info representing key and value.
19) SEGFAULT -- Crash the server with sigsegv.
20) SET-ACTIVE-EXPIRE <0|1> -- Setting it to 0 disables expiring keys in background when they are not accessed (otherwise the Redis behavior). Setting it to 1 reenables back the default.
21) SLEEP <seconds> -- Stop the server for <seconds>. Decimals allowed.
22) STRUCTSIZE -- Return the size of different Redis core C structures.
23) ZIPLIST <key> -- Show low level info about the ziplist encoding.
24) STRINGMATCH-TEST -- Run a fuzz tester against the stringmatchlen() function.
```

## DEBUG ASSERT
`DEBUG ASSERT`命令用于断言，当断言失败时，接收命令的节点将__crash__。
[注解]:
+ 如果断言失败，__crash__的结果将导致redis进程将退出。这一点和`DEBUG CRASH-AND-RECOVER`命令有一点区别

### 命令格式
```
> DEBUG ASSERT --
```

### 疑问
    怎么使用呢？

## DEBUG CHANGE-REPL-ID


## DEBUG CRASH-AND-RECOVER
`DEBUG CRASH-AND-RECOVER`命令用于模拟redis实例__crash__并在指定的时间后恢复。
[注解]：
+ 该命令只会使得redis进程不可访问，但进程不会退出，这点和`DEBUG ASSERT`命令断言失败有一些区别。
### 命令格式
```
> DEBUG CRASH-AND-RECOVER <milliseconds>
```
说明：
+ `<milliseconds>` 为redis实例__crash__的时间（单位毫秒），在该时间过去以后，redish实例将自动恢复。

## DEBUG DIGEST
`DEBUG DIGEST`命令用于对整个redis实例的数据，产生一个摘要，可用于验证两个redis数据库数据是否一致。
[注解]：
+ redis集群中不同master节点，摘要的值不相同（因为存储的内容不相同）
+ redis集群中master节点和它的slave节点的摘要在slave节点完整复制master节点数据的情况下是一致的

### 命令格式
```
> DEBUG DIGEST
```

## DEBUG DIGEST-VALUE
`DEBUG DIGEST-VALUE`命令用于对指定的key，产生一个摘要，可用于验证多个key的值是否一致
[注解]：

### 命令格式
```
> DEBUG DIGEST-VALUE <key-1> ... <key-N>
```
说明：
+ `<key-1> ... <key-N>` 
    计算摘要的key列表，如果指定了多个key，每个key都会输出一个对应的摘要

### 示例
计算key a 和 b的摘要
```
127.0.0.1:6379> debug digest-value a b
1) ed3966c92d61c522a8260b5c16c9469b328b5a95
2) b78559cd22bce6e6df712eb99ce7f3b08e8f8f1f
```

## DEBUG ERROR
`DEBUG ERROR`命令将返回redis错误，错误信息为命令后的参数值。该命令在客户端模拟一条命令执行失败的情况下非常有用，发送debug error，redis直接会返回一个错误应答
### 命令格式
```
> DEBUG ERROR <string>
```
其中：
+ `<string>` 为需要redis返回的错误信息，执行该命令后，redis将返回“(error) <string>”

### 示例
模拟返回一个错误，错误信息是：“hello, redis error”
```
> debug error "hello, redis error"
(error) hello, redis error
```

## DEBUG LOG
`DEBUG LOG`命令用于向redis的日志中写入一些内容。
### 命令格式
```
> debug log <message>
```
其中：
+ `<message>` 为需要写入日志的内容

### 示例
往日志中写入内容
```
127.0.0.1:7000> debug log "hello, this is the log from manual input"
OK

[root@izwz95n8068u7u1zz5oihcz redis-cluster]# tail /var/log/redis/redis-7000.log -n 1
29879:M 08 Aug 2019 07:52:38.718 # DEBUG LOG: hello, this is the log from manual input
```

## DEBUG HTSTATS 
该命令用于统计redis实例中指定db的hash table的统计信息
### 命令格式
```
> DEBUG HTSTATS  <dbid>
```

### 示例
统计db 0中的hash table信息
```
127.0.0.1:6379> debug htstats 0
[Dictionary HT]
Hash table 0 stats (main hash table):
 table size: 1048576
 number of elements: 1000002
 different slots: 644391
 max chain length: 9
 avg chain length (counted): 1.55
 avg chain length (computed): 1.55
 Chain length distribution:
   0: 404185 (38.55%)
   1: 384878 (36.70%)
   2: 184088 (17.56%)
   3: 58453 (5.57%)
   4: 13864 (1.32%)
   5: 2594 (0.25%)
   6: 450 (0.04%)
   7: 51 (0.00%)
   8: 11 (0.00%)
   9: 2 (0.00%)
[Expires HT]
No stats available for empty dictionaries
```
说明：


## DEBUG HTSTATS-KEY <key>
该命令用于统计redis实例中指定db的hash table的统计信息
### 命令格式
```
> DEBUG HTSTATS  <dbid>
```

### 示例
统计db 0中的hash table信息

## DEBUG OBJECT
该命令用于查看一个key内部信息，比如refcount、encoding、serializedlength等，结果如下：
```
127.0.0.1:7000> debug object key:373
Value at:0x7f7b290170c0 refcount:1 encoding:embstr serializedlength:10 lru:4815615 lru_seconds_idle:209069
```
[注解]：
+ 该命令可用于集群中，如果指定的key不在当前redis节点上，将返回“(error) ERR no such key”

### 命令格式
```
> DEBUG OBJECT  <key>
```

## DEBUG LOADAOF
该命令用于清空当前数据库，重新从aof文件里加载数据库，相当于： emptyDb(); loadAppendOnlyFile();


### 命令格式
```
> DEBUG LOADAOF
```

## DEBUG PANIC
该命令用于模拟当前redis进程经历一段不稳定后崩溃的情形。
该命令将使得redis进程退出。


### 命令格式
```
> DEBUG PANIC
```

## DEBUG POPULATE
`DEBUG POPULATE <count> [prefix] [size]`用于批量的创建count个string类型的key
[注解]：
+ 该命令批量创建key的规则是：prefix:n，其中，n为从0到count-1。如果prefix:x在数据库中已经存在，不会覆盖已存在的值。如果未指定`<prefix>`，则默认为key:n
+ 在redis集群中执行该命令时，如果创建的key所处的slot不属于当前redis节点，该key将被丢弃，而总共创建的key的数量将减少。

### 命令格式
```
> DEBUG POPULATE <count> [prefix] [size]
```
其中：
+ `<count>` 必须的参数，指定创建key的数量
+ `[prefix]` 可选的参数，指定创建key的前缀。如果未指定，默认为“key”
+ `[size]` 指定创建的值的长度。

### 示例
```
127.0.0.1:6379> debug populate 2 setamv 10
OK
127.0.0.1:6379> keys setamv*
1) "setamv:1"
2) "setamv:0"
127.0.0.1:6379> get setamv:0
"value:0\x00\x00\x00"

127.0.0.1:6379> debug populate 2
OK
127.0.0.1:6379> keys key*
1) "key:1"
2) "key:0"
127.0.0.1:6379> get key:0
"value:0"
```

## DEBUG RELOAD
该命令将保存当前redis的数据到磁盘的RDB文件，并清空内存中的数据，最后重新从磁盘加载到内存。冲新加载过程中只能服务部分只读请求（比如info、ping等）

### 命令格式
```
> DEBUG RELOAD
```

## DEBUG RESTART
该命令将重启当前redis实例，在重启之前，会先保存配置文件和数据到磁盘。

### 命令格式
```
> DEBUG RESTART
```

## DEBUG SDSLEN <key>
该命令用于查看一个key的SDS长度信息，返回的数据如下所示：
```
127.0.0.1:6379> get key:0
"value:0"
127.0.0.1:6379> debug sdslen key:0
key_sds_len:5, key_sds_avail:0, key_zmalloc: 8, val_sds_len:7, val_sds_avail:0, val_zmalloc: 16
```

### 什么是SDS
`SDS`是指Redis中的简单动态字符串，`SDS`的数据结构与API相关文件是：sds.h, sds.c。
`SDS`本质上就是char *，因为有了表头sdshdr结构的存在，所以`SDS`比传统C字符串在某些方面更加优秀，并且能够兼容传统C字符串。
`SDS`在Redis中是实现字符串对象的工具，并且完全取代char*。`SDS`是二进制安全的，它可以存储任意二进制数据，不像C语言字符串那样以‘\0’来标识字符串结束，
因为传统C字符串符合ASCII编码，这种编码的操作的特点就是：遇零则止 。即，当读一个字符串时，只要遇到’\0’结尾，就认为到达末尾，就忽略’\0’结尾以后的所有字符。因此，如果传统字符串保存图片，视频等二进制文件，操作文件时就被截断了。
`SDS`表头的buf被定义为字节数组，因为判断是否到达字符串结尾的依据则是表头的len成员，这意味着它可以存放任何二进制的数据和文本数据，包括’\0’

### 命令格式
```
> DEBUG SDSLEN <key>
```

## DEBUG SEGFAULT
该命令将使得当前redis进程退出，并发送一个`sigsegv`信号

### 疑问
+ 什么是`sigsegv`信号？

### 命令格式
```
> DEBUG SEGFAULT
```

## DEBUG SLEEP <seconds>
该命令将使得当前redis进程停止`<seconds>`所指定的秒数，在这期间，redis进程将不接受任何服务。
[注解]
+ 在集群的master节点上执行`DEBUG SLEEP`命令可能使得master节点变为fail状态（但仍然是 connected 状态），从而使得它的slave节点被提升为master节点来接替它。 

### 命令格式
```
> DEBUG SLEEP <seconds>
```