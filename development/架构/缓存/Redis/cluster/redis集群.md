# redis集群

## redis集群的特征
### 无法保证强一致性
redis集群无法保证强一致性，这意味着在特定的场景下，集群可能丢失客户端写入的数据。
redis集群无法保证强一致性的原因是：
1. 异步复制
    redis集群中，主从节点之间的复制是异步的，在客户端请求写操作的过程中，master节点接受客户端的写请求并写成功后，回复OK给客户端，然后异步分发写入的数据给从节点。如果在分发数据之前，主节点宕机了，然后一个从节点被提升为主节点，此时，客户端写入的数据就丢失了，因为这部分数据还未同步给从节点。
2. 可能存在的分区
    因为存在分区的可能性，如果客户端连接到了分区中的少数节点并写入数据，数据也可能会丢失。例如：有A、B、C三个主节点，A1、B1、C1三个从节点，当分区发生时，假设整个集群被分为2个区，其中一个区包含节点A、C、A1、B1、C1，另一个区包含节点B，并且客户端连接到了节点B上。
    此时，如果客户端写入数据到节点B，并且分区没能及时恢复，将导致在多数节点的分区中，从节点B1被提升为主节点，而写入节点B的数据将丢失。

### 集群节点的通信
All the cluster nodes are connected using a TCP bus and a binary protocol, called the Redis Cluster Bus. Every node is connected to every other node in the cluster using the cluster bus. Nodes use a gossip protocol to propagate information about the cluster in order to discover new nodes, to send ping packets to make sure all the other nodes are working properly, and to send cluster messages needed to signal specific conditions

### 自动发现集群中的其他节点

### 发现集群中FAIL的节点

### 将slave节点提升为master节点

### 使用集群中的slave节点扩展读操作
通常，集群中的slave节点收到master节点所分配的slot中的key相关命令时，将返回重定向到master节点的信息，而不是直接在slave节点执行命令。
但是，可以通过给slave节点发送`READONLY`命令，使得slave节点直接执行请求的命令。`READONLY`命令告诉slave节点，当前连接的客户端可以忍受可能读取到过时的数据，并且当前客户端不会有写请求。
如下面的例子所示：
```
127.0.0.1:7000> cluster nodes
933f9af709e70db7be012bfa1e4f11af09336f4e 127.0.0.1:8000@18000 slave 9a4569be70f1deb8cad134c236a4b733c1ea35c9 0 1565047998000 1 connected
f2317f063290be3cb9727af7904f228682c27be0 127.0.0.1:8001@18001 slave b7e6cdfefe99503661274eb1c6e2f50cd8adb5ba 0 1565047998531 2 connected
9a4569be70f1deb8cad134c236a4b733c1ea35c9 127.0.0.1:7000@17000 myself,master - 0 1565047997000 1 connected 0-5460
93b80829e5cf56f9f94768baaeeba4ca71a0975a 127.0.0.1:7002@17002 master - 0 1565047997126 3 connected 10923-16383
b7e6cdfefe99503661274eb1c6e2f50cd8adb5ba 127.0.0.1:7001@17001 master - 0 1565047997528 2 connected 5461-10922
e3b4e1ac96c1ae4a9a1313b6e980aeeaa0d12eef 127.0.0.1:8002@18002 slave 93b80829e5cf56f9f94768baaeeba4ca71a0975a 0 1565047998029 3 connected
[注解]：7000节点为一个master节点，8000节点是7000节点的一个slave节点，

127.0.0.1:7000> keys *
1) "{3560}a"
2) "{3560}b"
127.0.0.1:8000> get {3560}a
(error) MOVED 0 127.0.0.1:7000
127.0.0.1:8000> readonly
OK
127.0.0.1:8000> get {3560}a
"hello"
[注解]：直接在8000节点上查询key “a”的值，返回的是重定向到master节点；发送`READONLY`命令后，在8000节点上就可以查询到key “a”的值了
```