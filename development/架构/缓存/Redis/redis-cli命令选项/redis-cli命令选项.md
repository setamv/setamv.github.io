# redis-cli命令选项

## -p -h -n -a选项
`redis-cli`连接redis服务端的选项包括-p、-h、-n、-a。其中：
+ `-h` 选项
    指定redis服务端的IP。默认为 localhost
+ `-p` 选项
    指定redis服务端的端口。默认为6379
+ `-n`选项
    指定连接到的redis数据库。一个redis实例默认有16个数据库，编号为0...15之间。`-n`参数默认是连接到编号为0的数据库
+ `-a`选项
    指定连接redis服务端的密码。

### tags
`连接redis服务端` `连接redis实例` `指定连接选项` `指定连接参数`

### 示例
```
# redis-cli -p 6380 -h 192.168.0.199 -n 2 -a 123456
```
上述示例中，将连接到IP为192.168.0.199，端口为6380的redis实例。并且默认选择编号为2的数据库。连接的密码为123456


## 将其他程序获取输入
### -x选项指定从其他命令获取输入
`redis-cli`可以从其他命令获取输入（例如标准输入流）。当使用`-x`选项时，`redis-cli`可以指定最后一个参数为其他程序的输入内容（如从一个文件输入）。如下所示：
```
# cat data
bar
ok another line
# redis-cli -x set foo < data
OK
# redis-cli get foo
"bar\nok another line\n"
```
[说明]
+ `redis-cli`指定-x选项后，最后一个参数为一个文件重定向到标准输入，结果是该文件的全部内容将作为redis-cli命令的最后一个参数
+ 文件中可以指定多行内容

### 通过管道符号"|"从其他命令获取输入
`redis-cli`可以通过管道符号"|"从其他程序获取输入。例如：
```
# echo "set foo bar" | redis-cli
OK
# redis-cli get foo
"bar"
```
下面的示例是通过`cat`命令将整个文件的所有命令输入redis-cli批量执行：
```
# cat commands
hmset family papa will mama susie girl candy boy andy
hkeys family
hmget family papa mama
hmget family girl boy

# cat commands | redis-cli
OK
1) "papa"
2) "mama"
3) "girl"
4) "boy"
1) "will"
2) "susie"
1) "candy"
2) "andy"
```
可以看到，文件commands中的所有命令都被执行了。

## -r选项连续重复执行命令
`redis-cli`命令通过指定选型"-r"和"-i"来连续的重复执行同一个命令。
```
# redis-cli -r <count> -i <delay>
```
其中：
+ `-r <count>` 指定重复执行命令count次
+ `-i <delay>` 表示每次重复执行命令时，间隔的时间，单位为秒，可以指定一个小数，如：`-i 0.1`表示间隔0.1秒，即100毫秒。

### 应用场景
连续监控某一个值的变化

## --csv 选项
--csv选项可以将指定命令的输出结果导出为csv文件的格式（注意，只是导出为csv格式，而不是csv文件）。
```
# redis-cli --csv commands
```
### 示例
将所有的key按csv文件格式导出，并将命令执行结果重定向写入一个csv文件中
```
# redis-cli keys "*"
1) "foo"
2) "family"
3) "product"
# redis-cli --csv keys "*" > keys.csv
# cat keys.csv 
"foo","family","product"
```
### tags
`CSV文件` `导出` `redis数据导出`


## --eval选项
`--eval`选项可以让`redis-cli`命令运行一个Lua脚本文件。
```
# redis-cli --eval lua_script_file key0 ... keyn, arg0 ... argn
```
其中：`lua_script_file`为需要运行的Lua脚本文件；`key0 ... keyn`为传入Lua脚本文件的key值列表，在Lua脚本中可以通过KEY[n]来引用；`arg0 ... argn`为传入Lua脚本文件的参数列表，在Lua脚本中可以通过ARGV[n]来引用
更详细的内容参考：[eval和evalsha命令](../命令/eval和evalsha命令.md)

## --latency 选项
`redis-cli --latency`命令选项可以用于检测redis客户端到redis服务端的网络延时。该命令选项将使redis客户端每秒发送100次`PING`到redis服务端，然后打印出平均延时的统计信息，如下所示：
```
# redis-cli --latency
min: 0, max: 1, avg: 0.12 (555 samples)
```
延时时间的单位为毫秒。

## --latency-history 选项
`redis-cli --latency-history`命令选项与`redis-cli --latency`命令选项类似，只不过它是每15秒（可以通过`-i <interval>`对间隔时间进行指定）对延时信息做一次取样，每次取样信息都会单独打印，如下所示：
```
# redis-cli --latency-history -i 5
min: 0, max: 1, avg: 0.11 (489 samples) -- 5.01 seconds range
min: 0, max: 1, avg: 0.11 (489 samples) -- 5.01 seconds range
min: 0, max: 1, avg: 0.10 (488 samples) -- 5.00 seconds range
min: 0, max: 2, avg: 0.11 (488 samples) -- 5.01 seconds range
```

## --intrinsic-latency 选项
`redis-cli --intrinsic-latency`选项用于测试redis命令执行过程中，redis以外的环境相关因素导致的延迟时长，如操作系统、虚拟机等。该命令选项必须在redis服务器上执行才有意义。
该命令选项需要指定一个测试的时长，单位为秒，如下所示，测试20秒内来自环境相关因素导致的延迟时长：
```
D:\DevTools\redis 3.2>redis-cli.exe -n 2 -a 123456 --intrinsic-latency 20
Max latency so far: 1 microseconds.
Max latency so far: 2 microseconds.
Max latency so far: 19 microseconds.
Max latency so far: 21 microseconds.
Max latency so far: 23 microseconds.
Max latency so far: 43 microseconds.
Max latency so far: 66 microseconds.
Max latency so far: 76 microseconds.
Max latency so far: 102 microseconds.
Max latency so far: 108 microseconds.
Max latency so far: 118 microseconds.
Max latency so far: 282 microseconds.
Max latency so far: 298 microseconds.       [注解]目前发生的最大延时为298微秒

488188362 total runs (avg latency: 0.0410 microseconds / 40.97 nanoseconds per run).
Worst run took 7274x longer than the average latency.
```
上面最后的结果表明，平均每个操作因redis意外的环境相关因素导致的延迟时长为 0.0410微秒。并且最大的延时是平均延时的7274倍，即：7274 * 0.041 = 298
