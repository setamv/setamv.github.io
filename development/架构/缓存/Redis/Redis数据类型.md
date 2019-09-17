# Redis数据类型

## String-字符串类型
### 操作命令
+ SET 设置值，如：`> set mykey somevalue`
+ GET 获取值，如：`> get mykey`
+ INCR 自增1（值必须是可转换为数值类型的字符串）
+ INCRBY 增加指定的值，比如下面counter的值最终为150：
    `> set counter 100`     
    `> incrby counter 50`
+ DECR 自减1（值必须是可转换为数值类型的字符串）
+ DECRBY 减少指定的值
+ MSET 批量设置多个值，如：`> mset a 10 b 20 c 30`
+ MGET 批量获取多个值，如：`> mget a b c`

## List-列表类型
### 概述
    Redis的List是基于链表实现的
### 操作命令
+ LPUSH 
    向List的左侧新增元素，可以一次性指定多个新增的元素。如：`> lpush mylist 1 2 3`，将以此向List的左侧新增"1"、"2"、"3"三个元素。List的值从左到右以此为：3, 2, 1
+ LPUSHX 
    如果Key对应的List存在，则和LPUSH相同，如果Key对应的List不存在，则不作任何操作。
+ RPUSH 
    向List的右侧新增元素，可以一次性指定多个新增的元素。
+ RPUSHX 
    如果Key对应的List存在，则和RPUSH相同，如果Key对应的List不存在，则不作任何操作。
+ LRANGE 
    从List的左侧开始获取指定起止位置的元素，如：`> lrange lista 0 2` 将获取列表lista从第0到2位置之间的三个元素。
+ LPOP 
    获取List左侧第一个元素，同时将该元素从List删除
+ RPOP 
    获取List右侧第一个元素，同时将该元素从List删除
+ LTRIM 
    将指定范围的元素作为List的新值，其他的元素将从List删除。如下面的命令最终使得mylist的元素只保留[0, 2]位置的“5 4 3”三个元素：
    `> lpush mylist 1 2 3 4 5`
    `> ltrim mylist 0 2`
+ BLPOP 和LPOP的区别是：当List为空时，将阻塞直到List不为空
+ BRPOP 和RPOP的区别是：当List为空时，将阻塞直到List不为空
+ RPOPLPUSH RPOP和LPUSH两个命令的合成
+ BRPOPLPUSH RPOPLPUSH命令的阻塞版本

+ LINDEX 获取List中指定索引位置的元素值
    `LINDEX key index`，获取index（从0开始）指定位置的元素值
+ LINSERT 
    `LINSERT key BEFORE|AFTER pivot value` 
    将元素value插入pivot（指定元素的值）的前面或后面
+ LLEN 获取List的长度    
+ LREM
    `LREM key count value`
    从List中移除值与value相同的元素。其中：
    - count > 0 : 从左到右从List中移除count个与value相同的元素
    - count < 0 : 从右到左从List中移除-count个与value相同的元素
    - count = 0 : 从List中移除所有与value相同的元素
+ LSET     
    `LSET key index value` 将List中位于index的元素的值设置为value

## Hash-哈希类型
### 概述
    
### 操作命令（以下值域是指Hash中存储的名称-值对）
+ HSET
    `HSET key field value`
    设置指定的值域到Hash中
+ HSETX 与HSET的区别是：当值域的名称在Hash中不存在时才会设置，否则，不做任何操作。
+ HMSET 
    `HMSET key field value [field value ...]`
    设置多个的值域到Hash中。
+ HGET 
    `HGET key field` 从Hash中获取指定的值
+ HMGET
    `HMGET key field [field ...]` 从Hash中获取多个值
+ HGETALL 从Hash中获取所有的值域
+ HKEYS 返回Hash中的所有值域的名称  
+ HVALS 返回Hash中的所有值域的值  
+ HLEN 返回Hash的长度
+ HDEL 
    `HDEL key field [field ...]` 删除指定的值域
+ HEXISTS 查看指定的值域是否存在    


## SET-哈希类型
### 概述
    
### 操作命令
+ SADD 
    `SADD key member [member ...]` 将元素加入集合中
+ SCARD 获取集合的大小（集合元素的个数）
+ SDIFF 
    `SDIFF key [key ...]` 获取第一个集合与余下所有集合并集的差集    
+ SDIFFSTORE
    `SDIFFSTORE destination key [key ...]` 该命令与SDIFF的区别是：该命令会将结果存入destination命令的集合中。如果destination集合已经存在，将覆盖已存在的集合。
+ SINTER `SINTER key [key ...]` 求多个集合的交集
+ SINTERSTORE `SINTERSTORE destination key [key ...]` 求多个集合的交集并将结果覆盖到destination的集合中
+ SISMEMBER `SISMEMBER key member` 查看指定元素是否存在于集合中
+ SMEMBERS 返回集合的所有元素
+ SMOVE `SMOVE source destination member` 将元素从集合source移到集合destination
+ SPOP `SPOP key [count]` 从集合中随机删除并返回count个元素
+ SRANDMEMBER `SRANDMEMBER key [count]` 从集合中随机返回count个元素（当count>0时）
+ SREM `SREM key member [member ...]` 从集合中移除指定的元素
+ SUNION `SREM key member [member ...]` 获取多个集合的并集
+ SUNIONSTORE `SUNIONSTORE destination key [key ...]` 获取多个集合的并集并将结果覆盖到destination指定的集合中         

## SORTED-SET-排序的集合
### 概述
有序集合的排序规则：
如果集合中两个元素的分值不相等，则分值小的排在前面；如果两个元素的分值相同，则两个元素按值字符串字典规则排序
### 操作命令
+ ZADD 
    `ZADD key [NX|XX] [CH] [INCR] score member [score member ...]`
    将元素按指定的分值加入有序集合中。集合中的元素将按分值排序。+inf和-inf表示正负无穷大
    - XX选项：表示仅当元素已经存在的时候才更新元素，如果元素不存在，不会新增；
    - NX选项：表示不会更新已存在的元素，只有当元素不存在的时候，做新增操作
    - CH选项：表示返回值将由原来的新增元素个数变成变更的元素个数（Changed）
    - INCR选项：表示将元素的分值增1
+ ZCARD 获取集合的大小
+ ZCOUNT `ZCOUNT key min max` 返回分值在[min, max]之间的元素
+ ZINCRBY `ZINCRBY key increment member` 将元素member的分值增加increment
+ ZINTERSTORE
    `ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]`
    获取多个集合的交集，并将交集的结果覆盖destination集合
    其中：
    - numkeys 表示参与求交集的集合个数，也就是后面key [key ...]的实际参数个数
    - WEIGHTS 表示参与求交集的每个集合的权重，该权重用于计算结果集中元素的分值，比如集合1和集合2中都有元素a，且分值分别为4和5。如果求交集时指定的权重分别为1和2，则结果集中元素a的分值为（下面按AGGREGATE指定的值计算）：
        * AGGREGATE = SUM时：分值 = 4 * 1 + 5 * 2 = 14
        * AGGREGATE = MIN是：分值 = MIN (4 * 1, 5 * 2) = MIN(4, 10) = 4
        * AGGREGATE = MAX是：分值 = MAX (4 * 1, 5 * 2) = MAX(4, 10) = 10
+ ZLEXCOUNT 
    `ZLEXCOUNT key min max`
    该命令与ZCOUNT类似，区别是，该命令只能应用于所有元素的分值score都一样的情况下，这种情况，所有元素将按字典顺序排列，并且该命令将返回按字典顺序排序位于min和max之间的所有元素。
    注意：这里的min和max与ZCOUNT中的不一样，这里必须指明开区间还是闭区间，如：`> zlexcount myzset [a (f` 为获取[a, f)之间的元素。
+ ZPOPMAX `ZPOPMAX key [count]` 删除并返回分值最高的count个元素
+ ZPOPMIN `ZPOPMIN key [count]` 删除并返回分值最低的count个元素   
+ ZRANGE `ZRANGE key start stop [WITHSCORES]` 获取按分值排序后的位于start到stop元素。WITHSCORES指定是否返回元素的分值
+ ZRANGEBYLEX 
    `ZRANGEBYLEX key min max [LIMIT offset count]`
    当sorted set中所有元素都有相同的score时，`ZRANGEBYLEX`命令将返回值位于min和max之间元素（使用Lexicographical方式比较），如果sorted set中的元素存在不同的score，返回的值将变得不确定。
    该命令在比较元素的值时，将使用Lexicographical方式进行比较，从低位开始到高位逐一比较，更长的值被认为分值更大。使用Lexicographical方式进行比较时，是比较的元素的值的二进制字节码，所以，对ASCII码表中的所有字符，字符的分值就是字符在ASCII码表中的位置（越往后分值越大），但是对非ASCII码字符来说就不是这样的了（比如utf-8编码的字符），这种情况下，分值取决于字符转换为二进制字节码后的值
    参数说明：
    - min max
        用于指定元素比较的最小和最大值，min和max必须以"("或"["开始，它们分别表示比较的开区间和闭区间。当min和max的值分别为"-"和"+"时，表示不限定最小值和不限定最大值。
    - LIMIT offset count
        和MySQL中的`LIMIT count offset`类似，用于限定返回的元素个数。当count为负数时，将返回所有匹配的元素
    参见redis官方文档：https://redis.io/commands/zrangebylex
+ ZRANGEBYSCORE 
    `ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]`    
    返回分值位于[min, max]之间的元素集合（默认按分值从小到大排序返回，如果分值相同，按字母顺序排序）
    参数说明：
    - min max
        min和max用于指定本次需要获取的元素的分值所处的区间。默认为闭区间（即包含min和max边界值）。但是可以通过增加"("前缀来指定开区间，如：`zrangebyscore key (1 5"将返回分值位于(1, 5]之间的元素。
        min的值可以指定为特殊的'-inf'，表示不限定最小的分值
        max的值可以指定为特殊的'+inf'，表示不限定最大的分值
        所以：`zrangebyscore key -inf +inf`将返回所有的元素
    - WITHSCORES
        如果加上该选项，将同时返回元素以及元素的分值；否则，将只返回元素
    - LIMIT offset count
        类似SQL中的`SELECT LIMIT offset, count`，count如果为负数，将返回offset后的所有元素
+ 。。。未完待续

## Bitmaps-位图类型

## HyperLogLogs-基数类型