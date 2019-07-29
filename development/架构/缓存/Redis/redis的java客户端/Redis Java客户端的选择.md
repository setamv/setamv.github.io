# Redis Java客户端的选择
Redis的Java客户端很多，官方推荐的有三种：Jedis、Redisson和lettuce。
在这里对Jedis和Redisson进行对比介绍

## Jedis
+ 轻量，简洁，便于集成和改造
+ 支持连接池
+ 支持pipelining、事务、LUA Scripting、Redis Sentinel、Redis Cluster
+ 不支持读写分离，需要自己实现
+ 文档差（真的很差，几乎没有……）
+ github：https://github.com/xetorthio/jedis
+ 文档：https://github.com/xetorthio/jedis/wiki

# Redisson
+ 基于Netty实现，采用非阻塞IO，性能高
+ 支持异步请求
+ 支持连接池
+ 支持pipelining、LUA Scripting、Redis Sentinel、Redis Cluster
+ 不支持事务，官方建议以LUA Scripting代替事务
+ 支持在Redis Cluster架构下使用pipelining
+ 支持读写分离，支持读负载均衡，在主从复制和Redis Cluster架构下都可以使用
+ 内建Tomcat Session Manager，为Tomcat 6/7/8提供了会话共享功能
+ 可以与Spring Session集成，实现基于Redis的会话共享
+ 文档较丰富，有中文文档
+ github：https://github.com/redisson/redisson
+ 文档：https://github.com/redisson/redisson/wiki

对于Jedis和Redisson的选择，同样应遵循前述的原理，尽管Jedis比起Redisson有各种各样的不足，但也应该在需要使用Redisson的高级特性时再选用Redisson，避免造成不必要的程序复杂度提升。