# expire命令

## expire命令概述
`expire`命令用于对一个key设置过期时间。当过期时间到了，key将会被删除。重复对一个key执行`expire`命令，效果相当于更新key的过期时间。当设置成功时，redis将返回1；否则，返回0（比如key不存在）.

当对key执行以下命令时，过期时间的设置将被清除：SET、DEL、GETSET 以及所有以 STORE 结尾的命令
所有改变key的值（而非替换）都不会改变key的过期时间，例如：INCR、LPUSH、HSET等

可以使用`PERSIST`命令手动清除一个key的过期时间设置。
当使用`RENAME`命令修改key的名称时，原key的过期时间设置同样会转移到新的key上。

当`expire`/`pexpire`命令指定一个非正数的过期时间时，key将被删除（而不是过期）
当`expireat`/`pexpireat`命令指定一个过去的时间时，key将被删除（而不是过期）

可以使用`ttl`命令查看一个key的剩余存活时间

## 命令格式化
```
> expire key seconds
```
其中，key为需要设置过期时间的key；seconds为存活时间，单位为秒

## expire的精确度
redis 2.4之前，过期时间的精确度在1秒以内（即实际的过期时间可能会比预期的时间晚1秒钟）。在redis2.6以后，过期时间的精确度在1毫秒以内。

## 过期的逻辑
当使用`expire`命令设置过期时间时，redis是将过期时间转换为一个绝对的Unix时间戳（到毫秒），并且会持久化到磁盘。所以redis实例重启后过期时间的设置仍然有效。

## redis检查key过期的策略
redis检查key过期的策略有两种：
1. 当客户端访问一个key的时候，会检查key是否过期，如果过期了，将删除key
2. redis服务端周期性的抽查一批被设置了过期时间的key，如果发现有过期的key，将删除key
    抽查的频次是10次/秒，每次抽查时：
    1. 抽查设置了过期时间的key的数量为20个
    2. 删除所有被发现过期的key
    3. 如果超过25%的key都过期了，将重新从第1步开始

# pexpire命令
`pexpire`命令和`expire`命令不一样的地方是存活时间的单位，`pexpire`命令的存活时间单位为毫秒，而`expiry`命令的存活时间单位为秒

# expireat命令
`expireat`命令和`expire`命令类似，和`expire`命令不一样的是，它用于设置一个绝对的过期时间，其命令格式如下所示：
```
> expireat key timestamp
```
其中，key为需要设置过期时间的key；timestamp为过期的时间点，其值为到秒的Unix时间戳


# pexpireat命令
`pexpireat`命令和`pexpire`命令不一样的地方是过期时间的单位，`pexpire`命令的过期时间单位为秒，而`pexpireat`命令的过期时间单位为毫秒



# ttl 和 pttl命令
`ttl`和`pttl`命令用于查看指定key的剩余存活时间。他们的区别是：`ttl`返回结果的单位为秒，而`pttl`返回结果的单位为毫秒