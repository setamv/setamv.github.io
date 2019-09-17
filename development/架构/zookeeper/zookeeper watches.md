# Zookeeper Watches

## Zookeeper Watches的特性
+ 只触发一次
    当Zookeeper的节点发生变化时，watch事件只会给客户端发送一次，如果客户端需要继续监听watch事件，必须重新设置一个新的watch监听。
    注意：
    - 在客户端收到一个watch事件和重新设置一个新的watch监听之间，会存在一定的时间间隔，在这个时间间隔内，Zookeeper的节点可能已经发生了变化，此时，该变化将不会通知客户端，因为客户端还未重新设置watch监听。
    - 只触发一次，还意味着，当客户端同时设置了多种watch监听的时候，如果Zookeeper的节点发生的变化会触发多种watch的监听，也只会通知客户端一次。例如：客户端通过exists和getData设置了节点的watch监听，当删除该节点时，即便exists和getData都会设置节点的Delete事件的监听，服务器也只会对该客户端触发一次Delete事件。
+ 异步通知客户端
+ 

### 疑问
下面一段是Zookeeper官网的原文，地址为：http://zookeeper.apache.org/doc/r3.5.5/zookeeperProgrammers.html#sc_zkStatStructure 下 "Zookeeper Watches"一节
```
Watches are maintained locally at the ZooKeeper server to which the client is connected. This allows watches to be lightweight to set, maintain, and dispatch. When a client connects to a new server, the watch will be triggered for any session events. Watches will not be received while disconnected from a server. When a client reconnects, any previously registered watches will be reregistered and triggered if needed. In general this all occurs transparently. There is one case where a watch may be missed: a watch for the existence of a znode not yet created will be missed if the znode is created and deleted while disconnected.
```
疑问：
+ `Watches are maintained locally at the ZooKeeper server to which the client is connected` 这句话的意思是指Zookeeper集群中，只会在客户端当前连接的服务器上维护Zookeeper Watches，集群中其他服务器不会维护这些Zookeeper Watches吗？
+ `When a client connects to a new server, the watch will be triggered for any session events` 这句话是什么意思？
+ `When a client reconnects, any previously registered watches will be reregistered and triggered if needed` 
    这里是指当客户端断开重连后，以前注册的watches会自动重新注册并被触发吗？如果是集群，客户端断开后重连，需要连接到之前的同一台服务器吗？`triggered if needed`需要的情况下才触发watches，这里需要的时候是指什么时候？