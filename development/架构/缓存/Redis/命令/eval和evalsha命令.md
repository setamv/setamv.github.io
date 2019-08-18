# redis eval命令
redis的eval和evalsha命令用于执行Lua脚本。

## eval命令
redis的eval命令用于执行Lua脚本。其命令格式如下所示：
```
> eval lua_script <key-length> key1 ... keyn [arg1 ... argn]
```
其中：
+ lua_script
    为Lua脚本，使用双引号包裹
+ <key-length>
    指定后面参数中key的个数
+ key1 ... keyn
    指定脚本中可以使用的key，key的个数必须等于参数<key-length>，在Lua_script中可以使用`KEYS[i]`来引用参数中指定的第i个key
    注意：`KEYS[i]`中的KEYS必须是大写
+ arg1 ... argn
    指定脚本中可以使用的其他参数，和key类似，可以在lua_script中使用`ARGV[i]`来引用参数中指定的第i个参数
    注意：`ARGV[i]`中的KEYS必须是大写

[注解]
+ Lua脚本的执行具有原子性，即：在执行单个Lua脚本的过程中，redis不会执行其他的Lua脚本或命令。
    所以，eval命令不适合执行耗时比较长的Lua脚本
+ 集群中的使用限制
    - eval命令必须在master节点上执行，在slave节点执行将返回重定向
    - eval命令中的key必须全部落在当前master的slot中，否则，将返回重定向

### 示例
#### 返回参数
将指定的参数都返回
```
> eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
1) "key1"
2) "key2"
3) "first"
4) "second"
```

### redis.call()和redis.pcall()
在Lua脚本中，可以使用`redis.call()`和`redis.pcall()`来执行redis的命令。他们的参数为执行redis命令的所有参数列表（包括命令本身）。
例如：`> set foo bar`的命令可以使用：
```
> eval "return redis.call('set', 'foo', 'bar')" 0
OK
127.0.0.1:6379> eval "return redis.call('get', KEYS[1])" 1 foo
"bar"
```
[注解]：
+ 虽然可以在`redis.call()`命令中直接写key的值，但是redis官方推荐将key值写在eval命令的参数列表中，然后再通过KEYS[i]对参数进行引用。
    这样做有以下几点考虑：
    - 方便redis对key进行分析，特别是在集群中使用时这一点非常重要
        在命令执行之前，redis会分析该命令相关的key，如果要在使用eval命令的时候也能达到这种效果，必须将相关的key写在eval命令的参数列表中。在集群环境下，如果将key写到eval命令的参数中，redis可以在key不在当前节点所拥有的slot中时，返回一个重定向信息；否则，将无法做到这一点，例如：
        ```
        127.0.0.1:7000> cluster nodes
        e1e4f9c1f2bd756dcd7246b8d6d860937fe585a9 127.0.0.1:8001@18001 slave 14e961396c00058326974d9efbc90375be084f74 0 1565397307050 4 connected
        53e5158eb06d70b63e2afa5a9dae8fc6cbcb818d 127.0.0.1:7002@17002 master - 0 1565397307050 3 connected 10923-16383
        5baba2a87e61c0e3d39bb0c95ccd14ca2bf4a476 127.0.0.1:7000@17000 myself,master - 0 1565397307000 7 connected 0-5460
        14e961396c00058326974d9efbc90375be084f74 127.0.0.1:7001@17001 master - 0 1565397307050 2 connected 5461-10922
        ef2a789b2641a0d6c1984e73f110d72078984414 127.0.0.1:8002@18002 slave 53e5158eb06d70b63e2afa5a9dae8fc6cbcb818d 0 1565397307050 3 connected
        92074777def7a6b545e2aa2021e3733306de34ae 127.0.0.1:8000@18000 slave 5baba2a87e61c0e3d39bb0c95ccd14ca2bf4a476 0 1565397307000 7 connected
        127.0.0.1:7000> cluster keyslot foo
        (integer) 12182
        127.0.0.1:7000> eval "return redis.call('set', 'foo', 'bar')" 0
        (error) ERR Error running script (call to f_a0c38691e9fffe4563723c32ba77a34398e090e6): @user_script:1: @user_script: 1: Lua script attempted to access a non local key in a cluster node 
        127.0.0.1:7000> eval "return redis.call('set', KEYS[1], 'bar')" 1 foo
        (error) MOVED 12182 127.0.0.1:7002
        ```
        从上面`cluster keyslot foo`命令的执行结果可以知道，key“foo”落在了slot 12182上，该slot为7002节点拥有。所以，在7000节点上执行该key相关的命令会被重定向到7002节点。但是，直接在`redis.call()`中写key的值（而不是通过参数引用），返回的错误信息是“Lua script attempted to access a non local key in a cluster node”。
    

#### redis.call()和redis.pcall()的区别
redis.call() is similar to redis.pcall(), the only difference is that if a Redis command call will result in an error, redis.call() will raise a Lua error that in turn will force EVAL to return an error to the command caller, while redis.pcall will trap the error and return a Lua table representing the error.
例如，下面使用`get`命令获取一个HASH结构的值，返回的错误信息分别如下所示：
```
127.0.0.1:6379> eval "return redis.call('get', 'ht')" 0
(error) ERR Error running script (call to f_e9bb87f2121ada83daab62a8516f685e67889ee1): @user_script:1: WRONGTYPE Operation against a key holding the wrong kind of value 
127.0.0.1:6379> 
127.0.0.1:6379> eval "return redis.pcall('get', 'ht')" 0
(error) WRONGTYPE Operation against a key holding the wrong kind of value
```

#### redis.call()返回值的转换
在Lua脚本中使用`redis.call()`执行命令时，redis将`redis.call()`中执行命令的返回值转换为对应Lua脚本的数据类型，然后，再将Lua脚本的数据类型转换到redis的数据类型返回给客户端。
redis中的数据类型转换原则：当一个redis的数据类型转换为Lua脚本的数据类型，然后再转换为redis的数据类型，结果将和最初的redis数据类型保持一致。

##### redis的数据类型转换为Lua的数据类型
+ Redis integer reply -> Lua number
+ Redis bulk reply -> Lua string
+ Redis multi bulk reply -> Lua table (may have other Redis data types nested)
+ Redis status reply -> Lua table with a single ok field containing the status
+ Redis error reply -> Lua table with a single err field containing the error
+ Redis Nil bulk reply and Nil multi bulk reply -> Lua false boolean type

##### Lua的数据类型转换为redis的数据类型
+ Lua number -> Redis integer reply (the number is converted into an integer)
+ Lua string -> Redis bulk reply
+ Lua table (array) -> Redis multi bulk reply (truncated to the first nil inside the Lua array if any)
+ Lua table with a single ok field -> Redis status reply
+ Lua table with a single err field -> Redis error reply
+ Lua boolean false -> Redis Nil bulk reply.


## evalsha命令
`evalsha`命令是为了解决多次执行相同的Lua脚本导致消耗较多带宽的问题，其原理为：redis每次执行eval命令时，会将命令中的Lua脚本在编译后进行缓存，客户端可以通过`evalsha`命令发送Lua脚本的SHA1摘要信息，如果redis已经缓存了该SHA1对应的Luad脚本，将执行对应的脚本，否则，返回一个错误告诉客户端脚本还未缓存。
[注解]
+ redish只会缓存Lua脚本部分，参数部分不会缓存，所以，在执行`evalsha`命令时，必须指定参数（如果有参数）
+ redis将一直缓存所有的Lua脚本，除非以下两种情况发生：
    - 通过命令`SCRIPT FLUSH`将redis缓存的所有Lua脚本清除掉
    - 重启redis实例。redis进程退出后，存的所有Lua脚本也会被清除掉，不会持久化到磁盘中。
+ 可以通过`SCRIPT LOAD`命令一次性加载所有pipeline中的Lua脚本，从而使得所有的脚本都被redis缓存。
+ 有了`evalsha`命令后，相当于给一段脚本设置了一个简单的别名，后续都通过该别名就可以执行对应的脚本。
    这个有点像命令扩展的功能。

### 示例
使用Lua脚本和SHA1摘要获取相同key的值
```
127.0.0.1:7000> eval "return redis.call('get', KEYS[1])" 1 3560
"a"
127.0.0.1:7000> evalsha 4e6d8fc8bb01276962cce5371fa795a7763657ae 1 3560
"a"
```
[注解]：
+ 上述脚本`return redis.call('get', KEYS[1])`的SHA1的值为：4e6d8fc8bb01276962cce5371fa795a7763657ae

## SCRIPT命令
`SCRIPT`命令用于控制redis中的脚本。它下面包含几个子命令。

### SCRIPT FLUSH命令
该命令用于清除redis实例中所有缓存的脚本

### SCRIPT EXISTS <sha1> ... <shan>
该命令用于查看redis中是否缓存了指定SHA1摘要的脚本。
```
127.0.0.1:7000> script exists 4e6d8fc8bb01276962cce5371fa795a7763657ae 4e6d8fc8bb01276962cce5371fa795a7763657ab
1) (integer) 1
2) (integer) 0
```

### SCRIPT LOAD <script>
该命令用于加载并缓存指定的脚本。一次只能加载一个脚本

#### 示例
用sha1替代`hset`命令和`hget`命令
```
127.0.0.1:6379> script load "return redis.call('hset', KEYS[1], ARGV[1], ARGV[2])"
"3438b55821591797eb5646060b77fce104b42da6"
127.0.0.1:6379> script load "return redis.call('hget', KEYS[1], ARGV[1])"
"881a2828a2c9bdbe6e9aab5f48b66617185ddbc3"
127.0.0.1:6379> evalsha 3438b55821591797eb5646060b77fce104b42da6 1 family papa setamv
(integer) 1
127.0.0.1:6379> evalsha 881a2828a2c9bdbe6e9aab5f48b66617185ddbc3 1 family papa
"setamv"
```

### SCRIPT KILL
该命令用于中断一个执行了非常长时间的脚本
[注解]：
+ `SCRIPT KILL`命令只能用于脚本执行过程中还未修改redis中的数据的情况（只读类型的脚本显然在执行过程中的任何时点都是满足该条件的）

## Lua脚本的调试
redis实现了一种基于client/server模式的Lua脚本调试框架。使用方法参考官方文档：https://redis.io/topics/ldb

### 可视化debug插件
Intellij IDEA中有插件可以进行可视化调试，必须安装两个插件，分别是“EmmyLua”和“iedis”
+ EmmyLua插件用于支持Lua脚本语言的编写
+ iedis插件用于redis debugger，但该插件是收费的，破解参考：https://blog.csdn.net/qq_15071263/article/details/79759973

### 示例
#### 简单的示例调试
具体使用方法为：
1. 创建一个脚本文件，将需要调试的lua脚本写入文件中，假设为 test.lua，内容如下所示：
    ```
    [root@izwz95n8068u7u1zz5oihcz redis-cluster]# cat test.lua 
    return redis.call('hmget', KEYS[1], ARGV[1], ARGV[2])
    ```
2. 使用redis-cli以debugger模式连接redis服务器并debug
    debug的过程如下所示：
    ```
    # ./redis-cli -p 6379 --ldb --eval ./test.lua family , papa mama
    Lua debugging session started, please use:
    quit    -- End the session.
    restart -- Restart the script in debug mode again.
    help    -- Show Lua script debugging commands.

    * Stopped at 1, stop reason = step over
    -> 1   return redis.call('hmget', KEYS[1], ARGV[1], ARGV[2])

    lua debugger> help
    Redis Lua debugger help:
    [h]elp               Show this help.
    [s]tep               Run current line and stop again.
    [n]ext               Alias for step.
    [c]continue          Run till next breakpoint.
    [l]list              List source code around current line.
    [l]list [line]       List source code around [line].
                        line = 0 means: current position.
    [l]list [line] [ctx] In this form [ctx] specifies how many lines
                        to show before/after [line].
    [w]hole              List all source code. Alias for 'list 1 1000000'.
    [p]rint              Show all the local variables.
    [p]rint <var>        Show the value of the specified variable.
                        Can also show global vars KEYS and ARGV.
    [b]reak              Show all breakpoints.
    [b]reak <line>       Add a breakpoint to the specified line.
    [b]reak -<line>      Remove breakpoint from the specified line.
    [b]reak 0            Remove all breakpoints.
    [t]race              Show a backtrace.
    [e]eval <code>       Execute some Lua code (in a different callframe).
    [r]edis <cmd>        Execute a Redis command.
    [m]axlen [len]       Trim logged Redis replies and Lua var dumps to len.
                        Specifying zero as <len> means unlimited.
    [a]bort              Stop the execution of the script. In sync
                        mode dataset changes will be retained.

    Debugger functions you can call from Lua scripts:
    redis.debug()        Produce logs in the debugger console.
    redis.breakpoint()   Stop execution like if there was a breakpoing.
                        in the next line of code.

    lua debugger> n
    <redis> hmget family papa mama
    <reply> ["setamv","susie"]

    1) "setamv"
    2) "susie"

    (Lua debugging session ended -- dataset changes rolled back)
    ```
    说明：
    - debugger模式连接redis服务器时可以指定两种不同的方式：
        * --ldb 
            在这种方式下，debug脚本如果有写入数据，最后都将回滚，并且当前的debug过程不影响redis实例的其他连接操作。
        * --ldb-sync-mode
            这种方式成为“Synchronous mode”。在这种方式下，debug脚本写入的数据最终不会回滚，并且是以独占的方式连接到redis实例（即在调试过程中，其他客户端将无法连接到redis实例）。
    - 以debug模式连接server的命令格式为：
        ```
        # redis-cli --ldb --eval <script_file> key1 ... keyn , arg1 ... argn
        ```
        其中：
        * --eval 后面跟lua脚本文件的全路径
        * key1 ... keyn 为脚本中将使用到的key列表（即使用KEYS[i]引用的参数）
        * arg1 ... argn 为脚本中将使用到的参数列表（即使用ARGV[i]引用的参数）
    - 当连接上server后，客户端将显示`lua debugger>`，并等待用户输入后续的命令。可以直接输入help查看帮助信息。

    
#### 复杂的示例调试
下面使用Lua脚本求平均成绩。所有人的成绩都保存到了HSet中，如下所示（will、susie、candy、andy四个人的成绩分别是 85、90、95、100）：
```
127.0.0.1:6379> hmget scores will susie candy andy
1) "85"
2) "90"
3) "98"
4) "100"
```
求平均分的Lua脚本如下所示，脚本中需要接收一个参数KEYS[1]，用于设置求平均分的HSet的key：
```
local persons = redis.call('hkeys', KEYS[1])            -- 取得HSet的所有key，即每个人名，redis的hkeys返回一个列表，映射到Lua中为一个table
local totalScore = 0
local personCnt = 0
for i, v in ipairs(persons) do                          -- 循环每个人
  local score = redis.call('hget', KEYS[1], v)         -- 获取每个人的分值。因为redis
  totalScore = tonumber(totalScore) + tonumber(score)   -- 将每个人的分值加入到总分中
  personCnt = personCnt + 1
end
local avgScore = totalScore / personCnt                 -- 计算平均分
return avgScore
```
[注解]：
+ Lua脚本中不能有全局变量，只能使用局部变量（local声明）。有全局变量将报错，如下所示：
    ```
    (error) ERR Error running script (call to f_660054b928db5f7a17b15f7affc94f40a37c1e8b): @enable_strict_lua:8: user_script:2: Script attempted to create global variable 'persons' 
    ``` 

下面开始使用redis客户端调试Lua脚本（调试过程中可以使用"p"命令查看当前所有Lua变量的值）：
```
# ./src/redis-cli --ldb --eval /opt/workspace/lua/avgscore.lua scores
Lua debugging session started, please use:
quit    -- End the session.
restart -- Restart the script in debug mode again.
help    -- Show Lua script debugging commands.

* Stopped at 2, stop reason = step over
-> 2   local persons = redis.call('hkeys', KEYS[1])
lua debugger> n
<redis> hkeys scores
<reply> ["will","susie","candy","andy"]
* Stopped at 3, stop reason = step over
-> 3   local totalScore = 0
lua debugger> n
* Stopped at 4, stop reason = step over
-> 4   local personCnt = 0
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5   for i, v in ipairs(persons) do
lua debugger> n
* Stopped at 6, stop reason = step over
-> 6     local score = redis.call('hget', KEYS[1], v)
lua debugger> n
<redis> hget scores will
<reply> "85"
* Stopped at 7, stop reason = step over
-> 7     totalScore = tonumber(totalScore) + tonumber(score)
lua debugger> n
* Stopped at 8, stop reason = step over
-> 8     personCnt = personCnt + 1
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5   for i, v in ipairs(persons) do
lua debugger> n
* Stopped at 6, stop reason = step over
-> 6     local score = redis.call('hget', 'scores', v)
lua debugger> n
<redis> hget scores susie
<reply> "90"
* Stopped at 7, stop reason = step over
-> 7     totalScore = tonumber(totalScore) + tonumber(score)
lua debugger> n
* Stopped at 8, stop reason = step over
-> 8     personCnt = personCnt + 1
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5   for i, v in ipairs(persons) do
lua debugger> n
* Stopped at 6, stop reason = step over
-> 6     local score = redis.call('hget', 'scores', v)
lua debugger> n
<redis> hget scores candy
<reply> "98"
* Stopped at 7, stop reason = step over
-> 7     totalScore = tonumber(totalScore) + tonumber(score)
lua debugger> n
* Stopped at 8, stop reason = step over
-> 8     personCnt = personCnt + 1
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5   for i, v in ipairs(persons) do
lua debugger> n
* Stopped at 6, stop reason = step over
-> 6     local score = redis.call('hget', 'scores', v)
lua debugger> n
<redis> hget scores andy
<reply> "100"
* Stopped at 7, stop reason = step over
-> 7     totalScore = tonumber(totalScore) + tonumber(score)
lua debugger> n
* Stopped at 8, stop reason = step over
-> 8     personCnt = personCnt + 1
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5   for i, v in ipairs(persons) do
lua debugger> n
* Stopped at 10, stop reason = step over
-> 10  local avgScore = totalScore / personCnt
lua debugger> n
* Stopped at 11, stop reason = step over
-> 11  return avgScore
lua debugger> n

(integer) 93

(Lua debugging session ended -- dataset changes rolled back)

```

将Lua脚本使用unpack函数简化，如下所示：
```
# cat enhanced_avgscore.lua 
local persons = redis.call('hkeys', KEYS[1])
local totalScore = 0
local scores = redis.call('hmget', KEYS[1], unpack(persons))
for i, s in ipairs(scores) do
  totalScore = tonumber(totalScore) + tonumber(s)
end
local avgScore = totalScore / #scores
return avgScore
```

调试过程如下所示：
```
# ./src/redis-cli --ldb --eval /opt/workspace/lua/enhanced_avgscore.lua scores
Lua debugging session started, please use:
quit    -- End the session.
restart -- Restart the script in debug mode again.
help    -- Show Lua script debugging commands.

* Stopped at 1, stop reason = step over
-> 1   local persons = redis.call('hkeys', KEYS[1])
lua debugger> n
<redis> hkeys scores
<reply> ["will","susie","candy","andy"]
* Stopped at 2, stop reason = step over
-> 2   local totalScore = 0
lua debugger> n
* Stopped at 3, stop reason = step over
-> 3   local scores = redis.call('hmget', KEYS[1], unpack(persons))
lua debugger> n
<redis> hmget scores will susie candy andy
<reply> ["85","90","98","100"]
* Stopped at 4, stop reason = step over
-> 4   for i, s in ipairs(scores) do
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5     totalScore = tonumber(totalScore) + tonumber(s)
lua debugger> n
* Stopped at 4, stop reason = step over
-> 4   for i, s in ipairs(scores) do
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5     totalScore = tonumber(totalScore) + tonumber(s)
lua debugger> n
* Stopped at 4, stop reason = step over
-> 4   for i, s in ipairs(scores) do
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5     totalScore = tonumber(totalScore) + tonumber(s)
lua debugger> n
* Stopped at 4, stop reason = step over
-> 4   for i, s in ipairs(scores) do
lua debugger> n
* Stopped at 5, stop reason = step over
-> 5     totalScore = tonumber(totalScore) + tonumber(s)
lua debugger> n
* Stopped at 4, stop reason = step over
-> 4   for i, s in ipairs(scores) do
lua debugger> n
* Stopped at 7, stop reason = step over
-> 7   local avgScore = totalScore / #scores
lua debugger> n
* Stopped at 8, stop reason = step over
-> 8   return avgScore
lua debugger> n

(integer) 93

(Lua debugging session ended -- dataset changes rolled back)
```