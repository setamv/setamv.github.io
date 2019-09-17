# latency相关命令
`latency`命令用于查看redis执行的命令中的延时统计信息。[官网参考文档](https://redis.io/topics/latency-monitor)
redis对延时信息，是通过监控操作执行的时间来采集的。当操作执行的时长超过了当前设置的延时阈值（通过`latency-monitor-threshold <n>`选项设置延时的阈值，参数n为阈值的时间，单位为毫秒，为0表示不监控延时），将采集当前操作的执行信息，采集的信息包括：1）延时操作发生的Unix时间戳；2）操作执行的时长。

redis将不同类型操作的延时信息记录到不同的时间序列中，并且每个时间序列中最多只会保留最近的160个采集信息。这些操作的类型包括：
+ command，即redis的命令执行；
+ fast-command，时间复杂度在O(1)到O(log N)之间的命令的执行；
+ fork，redis的fork操作。

`latency`命令下的子命令包括：
```
127.0.0.1:6379> latency help
1) LATENCY <subcommand> arg arg ... arg. Subcommands are:
2) DOCTOR              -- Returns a human readable latency analysis report.
3) GRAPH   <event>     -- Returns an ASCII latency graph for the event class.       [说明] <event>参数为操作类型，如：command、fork等。
4) HISTORY <event>     -- Returns time-latency samples for the event class.
5) LATEST              -- Returns the latest latency samples for all events.
6) RESET   [event ...] -- Resets latency data of one or more event classes.
7)                        (default: reset all data for all event classes)
8) HELP                -- Prints this help.
```

以下所有延时统计的信息，如果未特别说明，都取自以下命令的执行结果：
```
127.0.0.1:6379> config set latency-monitor-threshold 100        [注解]设置延时阈值为100毫秒，命令执行时长>=100毫秒的都将被采集到延时统计信息中
127.0.0.1:6379> debug sleep 0.1                                 [注解]使debug命令休眠0.1秒，即debug命令将执行100毫秒
OK
127.0.0.1:6379> debug sleep 0.2
OK
127.0.0.1:6379> debug sleep 0.3
OK
127.0.0.1:6379> debug sleep 0.4
OK
127.0.0.1:6379> debug sleep 0.5
OK
(0.50s)
127.0.0.1:6379> debug sleep 0.4
OK
127.0.0.1:6379> debug sleep 0.3
OK
127.0.0.1:6379> debug sleep 0.2
OK
127.0.0.1:6379> debug sleep 0.1
OK
127.0.0.1:6379> debug sleep 0.7
OK
(0.70s)
127.0.0.1:6379> debug sleep 0.05
OK
(0.05s)
```
上面将发生了10次延时（即10次命令的执行时长>=100毫秒）。

## 打开redis的延时监控
redis默认是关闭延时监控的，打开的方式是通过命令或在配置文件中设置选项`latency-monitor-threshold <n>`，如下所示：
```
127.0.0.1:6379> config set latency-monitor-threshold n
```
其中参数n为判断命令执行时间是否延时的阈值，单位为毫秒。比如当n为100时，如果命令执行时间>=100毫秒，将记录到延时统计信息中。n为0时表示关闭延时监控

## latency latest子命令
`latency latest`子命令用于查看最近一次的延时信息，如下所示：
```
127.0.0.1:6379> latency latest
1) 1) "command"
   2) (integer) 1567081764
   3) (integer) 700
   4) (integer) 700
```
输出信息说明：
+ 第一行输出信息"command"，表示延时操作的类型，是一个命令
+ 第二行输出信息"(integer) 1567081764"，表示延时操作发生的时间（一个Unix时间戳）
+ 第三行输出信息"(integer) 700"，表示延时的时长
+ 第四行输出信息"(integer) 700"，表示自开启延时监控（或执行了`latency reset`）以来的发生的最大延时。

## latency graph <event> 子命令
`latency graph <event>`子命令将延时统计信息以图形的形式展示，参数"event"为操作类型（如command、fork）。例如：
```
127.0.0.1:6379> latency graph command
command - high 700 ms, low 100 ms (all time high 700 ms)
--------------------------------------------------------------------------------
         #
   _#_   |
  o|||o  |
_#|||||#_|
          
3222221111
086531986s
sssssssss 
```
说明：
+ 上面结果中的第一行输出了三个统计值：
    - 最大的延时（700毫秒）
    - 最小的延时（100毫秒）
    - 自统计以来发生的最大延时（700毫秒），该统计值与第一个统计值的区别是：第一个统计值是在当前还保留的延时统计信息中的最大延时，因为redis最多只会保留160个延时采集信息。
+ 上面的图中，每一列表示采集到的一次延时信息
    每一列分为两部分：
    - 其一是上半部分的图形（由"_"、"#"、"o"、"|"组成），图形越高表示延时的时间越长；
    - 其二是下半部分的数字（竖向的），表示延时操作发生的时间距离当前时间的时长。比如第一列的30s表示延时操作发生在30秒前。


## latency reset
`latency reset`命令用于清除以前采集到的所有延时信息。

## latency history <event>
`latency history <event>`命令用于查看指定操作类型的延时的历史记录。如：
```
127.0.0.1:6379> latency history command
 1) 1) (integer) 1567081735
    2) (integer) 100
 2) 1) (integer) 1567081737
    2) (integer) 200
 3) 1) (integer) 1567081739
    2) (integer) 300
 4) 1) (integer) 1567081740
    2) (integer) 400
 5) 1) (integer) 1567081742
    2) (integer) 500
 6) 1) (integer) 1567081744
    2) (integer) 400
 7) 1) (integer) 1567081746
    2) (integer) 300
 8) 1) (integer) 1567081747
    2) (integer) 200
 9) 1) (integer) 1567081749
    2) (integer) 100
10) 1) (integer) 1567081764
    2) (integer) 700
```
上面每两行列出一个延时的采集信息，包括：1）延时操作发生的时间点（Unix时间戳）；2）延时的时长。

## latency doctor 命令
`latency doctor`命令用于延时的诊断，如下所示：
```
127.0.0.1:6379> latency doctor
Dave, I have observed latency spikes in this Redis instance. You don't mind talking about it, do you Dave?

1. command: 10 latency spikes (average 320ms, mean deviation 144ms, period 215.20 sec). Worst all time event 700ms.

I have a few advices for you:

- Check your Slow Log to understand what are the commands you are running which are too slow to execute. Please check http://redis.io/commands/slowlog for more information.
- Deleting, expiring or evicting (because of maxmemory policy) large objects is a blocking operation. If you have very large objects that are often deleted, expired, or evicted, try to fragment those objects into multiple smaller objects.
```