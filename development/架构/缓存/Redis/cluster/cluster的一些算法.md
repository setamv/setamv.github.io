# cluster的一些算法

## cluster如何判定一个节点fail
1. 当一个节点（假设叫节点A）在配置的node timeout时间内，无法联系上另一个节点（假设叫节点B），则，节点A将标记节点B为`PFAIL`状态。
2. 节点A将`PFAIL`状态写入heartbeat packets中的gossip sections，通知集群中的其他节点
3. 其他节点接收heartbeat packets后，通过处理gossip sections，将得知节点B的`PFAIL`状态，并生成一份`failure reports`，从而记住节点A说节点B已经处于`PFAIL`状态。
4. 每一份`failure reports`都有一个生存周期，为node timeout的两倍
5. 当一个节点将节点B标记为`PFAIL`状态，并且收集到了占集群多数的master对节点B的`failure reports`（如果当前节点为一个master，它自己也算一个），该节点将提升节点B的`PFAIL`状态为`FAIL`状态，并广播强制其他节点也将节点B的状态提升为`FAIL`。