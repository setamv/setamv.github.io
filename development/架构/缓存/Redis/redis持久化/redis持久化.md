# redis持久化
redis的持久化方式有三种：
+ RDB方式
    RDB的方式可以保存redis在任意时点的快照
+ AOF（Append Only File）方式
    AOF的方式下，redis实例将收到的写操作记录日志，并通过回放的方式恢复数据。

## RDB方式
当使用RDB的方式进行持久化时，redis实例将以既定的周期将redis实例当前数据的快照写入rdb文件（一般以.rdb为后缀）。

### RDB方式的配置
redis配置文件中对RDB方式的配置为：
```
save n m
```
上面的参数表示一种生成快照的策略。表示在n指定的时间（单位为秒）内，当超过m个key发生变化时，将导出数据到rdb文件，生成一份快照。redis导出数据到rdb文件是以覆盖的方式，即这一次导出的数据文件（rdb文件）将覆盖上一次导出的数据文件。
例如：
```
save 60 1000
```
表示如果60秒内有1000个key发生了变化，就会导出数据到rdb文件。redis可以同时指定多个生成快照的策略，如：
```
save 60 1000
save 30 200
save 1 10000
```
上面表示，当60秒内有1000个key发生了变化，或者30秒内有200个key发生了变化，或者1秒内有10000个key发生了变化，都会生成一份快照。

### 使用命令生成RDB文件
可以使用命令`> SAVE` 或 `> BGSAVE`手动触发生成一份快照。他们的区别是：`SAVE`命令是以同步的方式生成快照，期间，redis实例将阻塞所有其他客户端的请求。`BGSAVE`命令将fork一个child进程专门用于导出数据生成快照。期间redis实例仍然可以处理其他客户端的请求。


## AOF方式
当使用AOF的方式进行持久化时，redis实例将收到的所有写操作记录日志，并在下次启动的时候对日志进行回放，从而恢复之前的数据。
记录日志的格式与Redis protocol相同（参见[大批量数据导入](../工具/大批量数据导入.md)）。

### 重写AOF日志文件
AOF的方式下，因为需要对所有写操作记录日志，随着redis实例的写操作越多，日志将变得越来越大。
例如，有一个100次的循环，每循环一次，将一个key的值增加1，如果全部记录日志，将记录100条写操作的日志。
这种情况下，redis实例可以通过重写日志的方式减小日志文件的大小。Redis支持以后台进程的方式重写AOF日志文件，期间不会阻塞其他客户端的请求。
使用`BGREWRITEAOF`命令可以强制redis重写AOF日志。例如：
```
127.0.0.1:6379> set papa will
OK
127.0.0.1:6379> set mama susie
OK
127.0.0.1:6379> del papa
(integer) 1

# cat appendonly.aof 
*2
$6
SELECT
$1
0
*3
$3
set
$4
papa
$4
will
*3
$3
set
$4
mama
$5
susie
*2
$3
del
$4
papa
```
可以看到，上面的AOF日志文件完整的记录了`set papa will`、`set mama susie`、`del papa`三个命令的日志。但是，如果从结果来看，备份数据完全可以只记录`set mama susie`一条命令即可，因为key "papa"最后被删除了，所以下次恢复的时候不会有该key。
此时，执行`BGREWRITEAOF`命令，效果如下图所示：
```
127.0.0.1:6379> bgrewriteaof
Background append only file rewriting started

# cat appendonly.aof 
*2
$6
SELECT
$1
0
*3
$3
SET
$4
mama
$5
susie
```
从上面可以看到，有关操作key "papa"的命令日志全部没有了。

### aof-use-rdb-preamble选项
`aof-use-rdb-preamble`选项的值如果为"yes"，AOF日志文件将以混合RDB格式和Redis protocol格式的形式记录。
在重写AOF日志的时候，当前redis实例中的数据将以RDB格式写在AOF文件的最前面。后续redis实例接收的写操作将以Redis protocol的格式追加到日志文件后面。
如下所示：
```
127.0.0.1:6379> keys *
1) "mama"

127.0.0.1:6379> config get aof-use-rdb-preamble     [注解]此时aof-use-rdb-preamble设置项打开了
1) "aof-use-rdb-preamble"
2) "yes"

127.0.0.1:6379> bgrewriteaof        [注解]手动触发AOF日志的重写
Background append only file rewriting started

# vi appendonly.aof                 [注解]查看AOF日志文件的内容，是一串乱码，不是redis protocol格式的数据。这是因为打开了aof-use-rdb-preamble设置项
REDIS0009ú      redis-ver^E5.0.5ú
redis-bitsÀ@ú^EctimeÂ=Õc]ú^Hused-memÂ¸<82>^M^@ú^Laof-preambleÀ^Aþ^@û^A^@^@^Dmama^Esusieÿ^R^R^EO5dXÕ

127.0.0.1:6379> set papa will       [注解]此处往redis实例写入一个数据，因为redis配置了appendfsync everysec，所以1秒后该操作将写入AOF日志文件
OK

# vi appendonly.aof                 [注解]此时查看AOF日志文件，可以看到set papa will命令被以redis protocol的格式追加到了日志末尾
REDIS0009ú      redis-ver^E5.0.5ú
redis-bitsÀ@ú^EctimeÂ=Õc]ú^Hused-memÂ¸<82>^M^@ú^Laof-preambleÀ^Aþ^@û^A^@^@^Dmama^Esusieÿ^R^R^EO5dXÕ*2^M
$6^M
SELECT^M
$1^M
0^M
*3^M
$3^M
set^M
$4^M
papa^M
$4^M
will^M
```

### redis重写日志的过程
redis使用copy-on-write（一边写一遍拷贝）的技巧进行日志的重写，其过程如下所示：
1. redis实例fork一个子进程
2. 子进程开始重写日志文件到一个临时文件中（重写的过程类似于RDB导出，将redis实例当前的全部数据导出到AOF文件）
3. 父进程将后续所有导致key变化的操作写入内存的缓冲区，同时将这些操作追加到旧的AOF文件末尾
4. 当子进程完成重写后，父进程将得到子进程完成重写的信号，并将内存缓冲区中的内容追加到子进程重写的AOF文件末尾
5. redis将子进程重写的AOF文件重命名，后续所有的日志都追加到新的AOF文件末尾


### RDB和AOF两种方式的比较
+ RDB更紧凑，AOF文件更大
    RDB可以保存redis数据库在任意时点的快照，保存的数据紧凑，非常适合用做数据的备份，包括将备份的数据传输到其他主机。
    在数据相同的情况下，RDB文件比AOF文件更小。
    例如：一个拥有10000001个key的redis数据库，它的内存信息如下所示：
    ```
    127.0.0.1:6379> INFO memory
    # Memory
    used_memory:695116720
    used_memory_human:662.91M
    ...
    ```
    分别使用`SAVE`和`BGREWRITEAOF`（未开启aof-use-rdb-preamble的情况下）导出后，大小分别为：
    ```
    # ll -h
    -rw-r--r--  1 root root 345M Aug 26 21:58 appendonly.aof
    ...
    -rw-r--r--  1 root root 227M Aug 26 21:58 dump.rdb
    ```
    可以看到，AOF文件大小为345M，而RDB文件的大小为227M
+ RDB持久化的方式可以最大化redis的性能。
    因为RDB的方式下，主进程只需要fork一个子进程用于保存快照信息，主进程不需要执行I/O相关的操作
+ 相比于AOF的方式，RDB在大数据量的情况下，redis实例的启动速度更快（即加载数据的过程耗时更短）
    例如：一个拥有10000001个key的redis数据库，在分别使用RDB和AOF恢复的情况下，加载数据的时间如下所示：
    ```
    # ll -h
    -rw-r--r--  1 root root 345M Aug 26 21:58 appendonly.aof
    ...
    -rw-r--r--  1 root root 227M Aug 26 21:58 dump.rdb                                      [注解]RDB文件和AOF文件的大小分别是227M和345M

    [注解]以下是关闭appendonly的情况下的redis启动日志（即设置 "appendonly no"）：
    ....
    19462:M 26 Aug 2019 22:41:27.672 # Server initialized
    19462:M 26 Aug 2019 22:41:27.672 # WARNING overcommit_memory is set to 0! .....
    19462:M 26 Aug 2019 22:41:36.029 * DB loaded from disk: 8.357 seconds                   [注解]RDB文件的方式加载时间是8.357秒
    19595:M 26 Aug 2019 22:43:29.980 * Ready to accept connections

    [注解]以下是打开appendonly的情况下的redis启动日志（即设置 "appendonly yes"）：
    ....
    19595:M 26 Aug 2019 22:43:09.871 # Server initialized
    19595:M 26 Aug 2019 22:43:09.872 # WARNING overcommit_memory is set to 0! .....
    19595:M 26 Aug 2019 22:43:29.980 * DB loaded from append only file: 20.109 seconds      [注解]AOF文件的方式加载时间是20.109秒
    19595:M 26 Aug 2019 22:43:29.980 * Ready to accept connections
    ...
    ```
+ 在发生异常的情况下，RDB方式可能丢失更多的数据。
    因为RDB是备份redis数据的全量快照，所以设置的备份周期相较于AOF方式来说更长（通常为5分钟），这意味着最坏的情况下将丢失5分钟以内写入的所有数据。
    而AOF是增量的形式进行备份，一般每秒钟写入一次，所以最坏的情况下也只会丢失1秒钟以内写入的数据。
+ RDB方式备份的时候，主进程需要fork一个子进程用于将数据持久化到磁盘。如果数据量非常大，fork将是一个比较耗时的操作（短的几毫秒，长的可能到秒），这期间，可能导致redis阻塞其他客户端的请求。
+ AOF方式备份的数据更易读
    因为AOF方式写入的日志符合redis protocol格式，一个命令接着一个命令往后写，所以可以很容易的看出redis执行了哪些命令。如果需要将备份数据中的指定命令删除，也是很简单的事情。
+ redis支持重写AOF文件
+ AOF方式的性能比RDB方式更慢。因为AOF方式会频繁的触发fsync，而在fsync期间，redis的主进程是无法写入数据的
