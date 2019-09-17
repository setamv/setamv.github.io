# redlock 分布式锁

## 疑问
redlock是否存在以下失败的可能：
假设有5个master节点，分别为A、B、C、D、E。现在有两个客户端client1、client2获取redlock，假设以下的时序：
1. client1请求A、B、C、D、E获取锁
2. client1成功获得A、B、C节点的锁，而D、E因为某些原因，未能获取到锁
3. 节点C在同步锁到slave节点之前，突然宕机，并且salve节点被提升为master节点（假设为F节点）
4. client2请求A、B、D、E、F获取锁
5. client2成功获得D、E、F节点的锁，而此时如果client1持有的锁还未释放，这种情况下，将违反锁的互斥性约束。

如果将slave节点提升为master的时间设置的足够长（比如大于锁的最大存货时间）是否就可以避免上述问题了？但是，如果如果这样可以避免该问题的发生，那单master+slave的情况下应该也可以避免，因为单master+slave环境下，主要的问题也是因为master同步到slave是异步的，客户端在master成功获取到锁后，如果master宕机当还未同步到slave，slave被提升为master后可能存在其他客户端同时获取到锁的风险。