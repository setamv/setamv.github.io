# redis cluster相关命令
以下所有的说明或示例代码都基于以下[cluster环境搭建.md](../cluster/cluster环境搭建.md)中的cluster节点分布：
| 节点类型 | IP:PORT              | 所属master           |
|----------|----------------------|----------------------|
| master   | 192.168.199.130:7000 |                      |
| master   | 192.168.199.130:7001 |                      |
| master   | 192.168.199.130:7002 |                      |
| slave    | 192.168.199.130:8000 | 192.168.199.130:7000 |
| slave    | 192.168.199.130:8001 | 192.168.199.130:7001 |
| slave    | 192.168.199.130:8002 | 192.168.199.130:7002 |

redis cluster相关的命令清单如下所示：
```
192.168.199.130:7001> cluster help
1) CLUSTER <subcommand> arg arg ... arg. Subcommands are:
2) ADDSLOTS <slot> [slot ...] -- Assign slots to current node.
3) BUMPEPOCH -- Advance the cluster config epoch.
4) COUNT-failure-reports <node-id> -- Return number of failure reports for <node-id>.
5) COUNTKEYSINSLOT <slot> - Return the number of keys in <slot>.
6) DELSLOTS <slot> [slot ...] -- Delete slots information from current node.
7) FAILOVER [force|takeover] -- Promote current replica node to being a master.
8) FORGET <node-id> -- Remove a node from the cluster.
9) GETKEYSINSLOT <slot> <count> -- Return key names stored by current node in a slot.
10) FLUSHSLOTS -- Delete current node own slots information.
11) INFO - Return onformation about the cluster.
12) KEYSLOT <key> -- Return the hash slot for <key>.
13) MEET <ip> <port> [bus-port] -- Connect nodes into a working cluster.
14) MYID -- Return the node id.
15) NODES -- Return cluster configuration seen by node. Output format:
16)     <id> <ip:port> <flags> <master> <pings> <pongs> <epoch> <link> <slot> ... <slot>
17) REPLICATE <node-id> -- Configure current node as replica to <node-id>.
18) RESET [hard|soft] -- Reset current node (default: soft).
19) SET-config-epoch <epoch> - Set config epoch of current node.
20) SETSLOT <slot> (importing|migrating|stable|node <node-id>) -- Set slot state.
21) REPLICAS <node-id> -- Return <node-id> replicas.
22) SLOTS -- Return information about slots range mappings. Each range is made of:
23)     start, end, master and replicas IP addresses, ports and ids
```

## cluster failover
Sometimes it is useful to force a failover without actually causing any problem on a master. For example in order to upgrade the Redis process of one of the master nodes it is a good idea to failover it in order to turn it into a slave with minimal impact on availability.

Manual failovers are supported by Redis Cluster using the `CLUSTER FAILOVER` command, that __must be executed in one of the slaves of the master__ you want to failover.

Manual failovers are special and are safer compared to failovers resulting from actual master failures, since they occur in a way that avoid data loss in the process, by switching clients from the original master to the new master only when the system is sure that the new master processed all the replication stream from the old one.
命令格式：
```
> cluster failover [force|takeover]
```
注意：  
+ 该命令必须在slave节点上执行。执行成功后，当前的slave将被提升为master，原master节点将切换为新master的slave节点。

下面是在slave节点上执行`# ./redis-cli -p 8002 cluster failover`命令的日志信息（其中 8002 为slave节点的端口，master节点的端口为7001）：
```
1648:S 28 Jul 2019 09:10:24.923 # Manual failover user request accepted.
1648:S 28 Jul 2019 09:10:25.003 # Received replication offset for paused master manual failover: 4760
1648:S 28 Jul 2019 09:10:25.103 # All master replication stream processed, manual failover can start.
1648:S 28 Jul 2019 09:10:25.103 # Start of election delayed for 0 milliseconds (rank #0, offset 4760).
1648:S 28 Jul 2019 09:10:25.103 # Starting a failover election for epoch 11.
1648:S 28 Jul 2019 09:10:25.133 # Failover election won: I'm the new master.
1648:S 28 Jul 2019 09:10:25.133 # configEpoch set to 11 after successful failover
1648:M 28 Jul 2019 09:10:25.133 # Setting secondary replication ID to 54b9ec53da83337072dc12f6867d15debb5be586, valid up to offset: 4761. New replication ID is 7419083e7a220da887cdd60aeb4e90b4d263f6ab
1648:M 28 Jul 2019 09:10:25.133 # Connection with master lost.
1648:M 28 Jul 2019 09:10:25.134 * Caching the disconnected master state.
1648:M 28 Jul 2019 09:10:25.134 * Discarding previously cached master state.
1648:M 28 Jul 2019 09:10:25.942 * Replica 192.168.199.130:7001 asks for synchronization
1648:M 28 Jul 2019 09:10:25.942 * Partial resynchronization request from 192.168.199.130:7001 accepted. Sending 0 bytes of backlog starting from offset 4761.
```
从上面可以看到：
1. 在执行`cluster failover`命令后，master节点发送了它的`replication offset`给slave，并等待slave接收所有的replication stream，完成后才会开始failover的过程。
2. 开始新的master选举
3. 选举完成后，原来的master（7001端口）变成了slave节点（日志中的 Replica 192.168.199.130:7001 asks for synchronization）

## cluster replicate
`cluster replicate`命令用于将__当前节点__重新设置为另一个master节点的slave。
命令格式：
```
> cluster replicate <node-id>
```
其中：
+ `node-id` 为cluster中一个master节点的ID，命令执行成功后，当前节点将变为它的slave。

[注解]
+ `cluster replicate`命令不会导致其他slave节点重新分配master
    在使用`# redis-cli --cluster add-node`命令新增一个slave节点，并指定到一个master时，有可能导致其他slave节点被重新设置master（参考[--cluster选项=>--cluster add-node 一节中的第二点注解](../redis-cli命令选项/--cluster选项.md)
    使用`cluster replicate`命令不会发生其他slave节点重新分配master的情况。
+ 在master节点上执行`cluster replicate`命令的结果
    - 如果master上已经分配了slots
        在当前master上执行`cluster replicate`命令将导致报错，如下所示：
        ```
        192.168.199.130:7000> cluster replicate fe00ec489c2446232de3770fea1a9a7717079b79
        (error) ERR To set a master the node must be empty and without assigned slots.
        ```
    - 如果master上未分配任何slots
        在当前master上执行`cluster replicate`命令的结果将是：
        1）当前master变为新指定master的slave节点
        2）如果当前master下有slave节点，这些slave节点也会变成新指定master的slave节点。
        例如：下面将master节点`192.168.199.130:7003`指定为新master`192.168.199.130:7000`的slave节点。
        这是执行命令之前的节点信息，节点`192.168.199.130:7003`为master节点，且其下有slave节点`192.168.199.130:8003`和`192.168.199.130:8000`：
        ```
        192.168.199.130:7000> cluster nodes
        6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001@18001 slave fe00ec489c2446232de3770fea1a9a7717079b79 0 1564299420639 4 connected
        78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000@17000 myself,master - 0 1564299419000 1 connected 0-5460
        55efa918bdbc70302a663f1d93eb0263402da2b9 192.168.199.130:8003@18003 slave 00597cbc4c94eabbabd199336785405ced501064 0 1564299420135 7 connected
        fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001@17001 master - 0 1564299419000 2 connected 5462-10922
        45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002@18002 slave 0e9f93246cbd0de72f3f216ba207c055ce36b51b 0 1564299419028 5 connected
        2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000@18000 slave 00597cbc4c94eabbabd199336785405ced501064 0 1564299419000 7 connected
        00597cbc4c94eabbabd199336785405ced501064 192.168.199.130:7003@17003 master - 0 1564299419632 7 connected 5461
        0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002@17002 master - 0 1564299420000 3 connected 10923-16383
        ```
        执行`cluster replicate`命令：
        ```
        192.168.199.130:7003> cluster replicate 78d288b7644411bb1f10ad3547406859e8727daf
        OK
        ```
        执行`cluster replicate`命令之后的节点信息，可以看到，节点`192.168.199.130:7003`、`192.168.199.130:8003`和`192.168.199.130:8000`都变为`192.168.199.130:7000`的slave节点了：
        ```
        192.168.199.130:7003> cluster nodes
        55efa918bdbc70302a663f1d93eb0263402da2b9 192.168.199.130:8003@18003 slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564299690000 8 connected
        00597cbc4c94eabbabd199336785405ced501064 192.168.199.130:7003@17003 myself,slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564299689000 7 connected
        2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000@18000 slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564299690969 8 connected
        78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000@17000 master - 0 1564299690567 8 connected 0-5461
        0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002@17002 master - 0 1564299691000 3 connected 10923-16383
        6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001@18001 slave fe00ec489c2446232de3770fea1a9a7717079b79 0 1564299690000 2 connected
        45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002@18002 slave 0e9f93246cbd0de72f3f216ba207c055ce36b51b 0 1564299689000 3 connected
        fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001@17001 master - 0 1564299690000 2 connected 5462-10922
        ```
        

### 示例
#### 重新设定master节点
将一个slave节点重新设置为另一个master的slave。
下面是重新设置之前master节点的统计情况：
```
[root@192 redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7000
192.168.199.130:7000 (78d288b7...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7001 (fe00ec48...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7003 (00597cbc...) -> 0 keys | 1 slots | 1 slaves.
192.168.199.130:7002 (0e9f9324...) -> 0 keys | 5461 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.
```

重新设置master
```
192.168.199.130:8000> cluster replicate 00597cbc4c94eabbabd199336785405ced501064
```

下面是重新设置之后master节点的统计情况：
```
[root@192 redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7000
192.168.199.130:7000 (78d288b7...) -> 0 keys | 5461 slots | 0 slaves.
192.168.199.130:7001 (fe00ec48...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7003 (00597cbc...) -> 0 keys | 1 slots | 2 slaves.
192.168.199.130:7002 (0e9f9324...) -> 0 keys | 5461 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.
```