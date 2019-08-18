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

## CLUSTER FAILOVER
Sometimes it is useful to force a failover without actually causing any problem on a master. For example in order to upgrade the Redis process of one of the master nodes it is a good idea to failover it in order to turn it into a slave with minimal impact on availability.

Manual failovers are supported by Redis Cluster using the `CLUSTER FAILOVER` command, that __must be executed in one of the slaves of the master__ you want to failover.

Manual failovers are special and are safer compared to failovers resulting from actual master failures, since they occur in a way that avoid data loss in the process, by switching clients from the original master to the new master only when the system is sure that the new master processed all the replication stream from the old one.
命令格式：
```
> CLUSTER FAILOVER [force|takeover]
```
注意：  
+ 该命令必须在slave节点上执行。执行成功后，当前的slave将被提升为master，原master节点将切换为新master的slave节点。

下面是在slave节点上执行`# ./redis-cli -p 8002 CLUSTER FAILOVER`命令的日志信息（其中 8002 为slave节点的端口，master节点的端口为7001）：
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
1. 在执行`CLUSTER FAILOVER`命令后，master节点发送了它的`replication offset`给slave，并等待slave接收所有的replication stream，完成后才会开始failover的过程。
2. 开始新的master选举
3. 选举完成后，原来的master（7001端口）变成了slave节点（日志中的 Replica 192.168.199.130:7001 asks for synchronization）

## CLUSTER REPLICATE
`CLUSTER REPLICATE`命令用于将__当前节点__重新设置为另一个master节点的slave。
命令格式：
```
> CLUSTER REPLICATE <node-id>
```
其中：
+ `node-id` 为cluster中一个master节点的ID，命令执行成功后，当前节点将变为它的slave。

[注解]
+ `CLUSTER REPLICATE`命令不会导致其他slave节点重新分配master
    在使用`# redis-cli --cluster add-node`命令新增一个slave节点，并指定到一个master时，有可能导致其他slave节点被重新设置master（参考[--cluster选项=>--cluster add-node 一节中的第二点注解](../redis-cli命令选项/--cluster选项.md)
    使用`CLUSTER REPLICATE`命令不会发生其他slave节点重新分配master的情况。
+ 在master节点上执行`CLUSTER REPLICATE`命令的结果
    - 如果master上已经分配了slots
        在当前master上执行`CLUSTER REPLICATE`命令将导致报错，如下所示：
        ```
        192.168.199.130:7000> CLUSTER REPLICATE fe00ec489c2446232de3770fea1a9a7717079b79
        (error) ERR To set a master the node must be empty and without assigned slots.
        ```
    - 如果master上未分配任何slots
        在当前master上执行`CLUSTER REPLICATE`命令的结果将是：
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
        执行`CLUSTER REPLICATE`命令：
        ```
        192.168.199.130:7003> CLUSTER REPLICATE 78d288b7644411bb1f10ad3547406859e8727daf
        OK
        ```
        执行`CLUSTER REPLICATE`命令之后的节点信息，可以看到，节点`192.168.199.130:7003`、`192.168.199.130:8003`和`192.168.199.130:8000`都变为`192.168.199.130:7000`的slave节点了：
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
192.168.199.130:8000> CLUSTER REPLICATE 00597cbc4c94eabbabd199336785405ced501064
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

## CLUSTER BUMPEPOCH
`CLUSTER BUMPEPOCH`命令用于提升当前节点的`config epoch`（同时，整个集群的`cluster_current_epoch`也会提升）。
可以使用命令`CLUSTER NODES`查看当前cluster中各节点的`config epoch`（在输出结果的第7列，connected前一列），使用命令`CLUSTER INFO`查看当前节点的`config epoch`和整个集群的`cluster_current_epoch`。
如下所示：
```
# ./redis-cli -p 7000 cluster nodes
b71fe2f70905269210485e69152cd8424a2d4e85 127.0.0.1:7003@17003 master - 0 1564617894551 8 connected
34abc0f94de16ad49126a3ad336d6549d64d9374 127.0.0.1:7002@17002 master - 0 1564617893045 3 connected 10923-16383
15154e277b79b6ac98beb47e89a0224c7866092d 127.0.0.1:9001@19001 slave e50c2f8c7095e4bcac2d1ceb12a5c523d8049d8f 0 1564617892506 2 connected
57ee24d3b23d175c2ec74e79176f867cc79cced7 127.0.0.1:8000@18000 slave 7a1d9d4ff98aee7d5a71d4b055cd4ee424696973 0 1564617894448 12 connected
d5df3eb0e12ab2fd9cf5563fa90cc3dc2bd158d3 127.0.0.1:9000@19000 slave 7a1d9d4ff98aee7d5a71d4b055cd4ee424696973 0 1564617893000 12 connected
e50c2f8c7095e4bcac2d1ceb12a5c523d8049d8f 127.0.0.1:7001@17001 master - 0 1564617893000 2 connected 5461-10922
12afee6a56dd89b1185ab89d1a2be201edfcc9ed 127.0.0.1:8002@18002 slave 34abc0f94de16ad49126a3ad336d6549d64d9374 0 1564617894000 3 connected
d88e39e405aef23d0bd7f56ba19b62763484a2b8 127.0.0.1:8003@18003 slave b71fe2f70905269210485e69152cd8424a2d4e85 0 1564617893447 8 connected
7a1d9d4ff98aee7d5a71d4b055cd4ee424696973 127.0.0.1:7000@17000 myself,master - 0 1564617893000 12 connected 0-5460
8b65a80064d122e07fa4c67b7733a301be6459b6 127.0.0.1:8001@18001 slave e50c2f8c7095e4bcac2d1ceb12a5c523d8049d8f 0 1564617894047 4 connected
[注解]：上面输出结果中第7列就是节点的config epoch，slave节点的config epoch和它的master节点相同

# ./redis-cli -p 7000 cluster info | grep epoch
cluster_current_epoch:12
cluster_my_epoch:12
[注解]：cluster_current_epoch是整个集群的epoch，cluster_my_epoch是当前节点的epoch

# ./redis-cli -p 7001 cluster bumpepoch
BUMPED 13
[注解]：切换到7001端口的节点执行 CLUSTER BUMPEPOCH，节点的config epoch被提升为13

# ./redis-cli -p 7001 cluster nodes | grep master
7a1d9d4ff98aee7d5a71d4b055cd4ee424696973 127.0.0.1:7000@17000 master - 0 1564618134581 12 connected 0-5460
e50c2f8c7095e4bcac2d1ceb12a5c523d8049d8f 127.0.0.1:7001@17001 myself,master - 0 1564618134000 13 connected 5461-10922
34abc0f94de16ad49126a3ad336d6549d64d9374 127.0.0.1:7002@17002 master - 0 1564618134000 3 connected 10923-16383
b71fe2f70905269210485e69152cd8424a2d4e85 127.0.0.1:7003@17003 master - 0 1564618133000 8 connected
[注解]：可以看到，7001端口的节点，config epoch变为13了

# ./redis-cli -p 7001 cluster info | grep epoch
cluster_current_epoch:13
cluster_my_epoch:13
[注解]：可以看到，整个集群的epoch（cluster_current_epoch）也变为13了

# ./redis-cli -p 7001 cluster bumpepoch
STILL 13
[注解]：再次执行CLUSTER BUMPEPOCH，提示epoch仍然是13，没有变化

# ./redis-cli -p 7000 cluster info | grep epoch
cluster_current_epoch:13
cluster_my_epoch:12
[注解]：整个集群的epoch（即cluster_current_epoch）也变成了13

# ./redis-cli -p 8001 cluster bumpepoch
BUMPED 14
# ./redis-cli -p 7001 cluster nodes | grep 8001
8b65a80064d122e07fa4c67b7733a301be6459b6 127.0.0.1:8001@18001 slave e50c2f8c7095e4bcac2d1ceb12a5c523d8049d8f 0 1564618430065 13 connected
# ./redis-cli -p 7001 cluster info | grep epoch
cluster_current_epoch:14
cluster_my_epoch:13
[注解]：在slave节点（端口8001）上执行 CLUSTER BUMPEPOCH，提示epoch到了14，但是该slave节点的config epoch仍然是13，而整个集群的epoch（cluster_current_epoch）已经变为14了。
```
[注解]：
+ slave节点的`config epoch`是保存它最后一次与master交换信息时master的`config epoch`值。
+ 在slave节点上执行`CLUSTER BUMPEPOCH`命令，只会提升整个集群的epoch(`cluster_current_epoch`)，节点`config epoch`不受影响
    整个集群的epoch比集群中所有节点的`config epoch`都不会小，只要有节点的`config epoch`发生变化，它会更新为集群所有节点中`config epoch`最大的值。
+ 连续两次执行`CLUSTER BUMPEPOCH`命令，在其他节点的`config epoch`没有发生改变的前提下，当前节点的`config epoch`只会提升一次
    `CLUSTER BUMPEPOCH`命令提升节点的`config epoch`的逻辑是：如果当前节点的`config epoch`比整个集群的`cluster_current_epoch`小，则将当前节点的`config epoch`提升为`cluster_current_epoch` + 1，否则，当前节点的`config epoch`保持不变（因为它已经是集群中最大的`config epoch`了）
    例如，上面第一次执行`CLUSTER BUMPEPOCH`命令后，输出结果为`BUMPED 7`，节点的`config epoch`从1提升为7了。


## CLUSTER ADDSLOTS
`CLUSTER ADDSLOTS`命令用于将指定的slots分配到当前登录的redis节点。
命令格式：
```
> CLUSTER ADDSLOTS <slot> [slot ...]
```
其中：
+ `<slot> [slot ...]`

```
[root@SAAS redis-cluster]# ./redis-cli -c -p 7003 cluster addslots 10383
(error) ERR Slot 10383 is already busy
```

### 示例
#### 批量分配slot
下面是一个批量分配slot的脚本，脚本内容如下所示：
```
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# vi batch_add_slots.sh 
#!/bin/bash

# 将一段连续的slots分配到指定的节点
# 入参：
# $0 = 分配slots的节点的端口号
# $1 = 起始slot
# $2 = 结束slot

if [ $# -lt 3 ];
then
    echo "请输入三个参数: $0=分配slots的节点的端口号, $1=起始slot, $2=结束slot"
    exit
fi

BASE_DIR=/opt/software/redis-cluster
port=$1
slot_snum=$2
slot_enum=$3

for ((i=$slot_snum; i<=$slot_enum; i++));
do
    $BASE_DIR/redis-cli -p $port cluster addslots $i
done
```

## CLUSTER DELSLOTS
`CLUSTER DELSLOTS`命令使得当前节点遗忘(forget)指定的slots所分配的master。
命令格式：
```
> CLUSTER DELSLOTS <slot> [slot ...]
```
其中：
+ `<slot> [slot ...]` 
    为指定需要当前节点forget的slot列表

注意：
+ 该命令只会让__当前节点__遗忘指定的slots属于哪个master，集群中的其他节点仍然记录了该slots所属master的信息。
+ 如果需要将slot从master A节点通过`CLUSTER ADDSLOTS`重新分配到master B节点上，必须在master A节点和master B节点执行`CLUSTER DELSLOTS`命令该slot forget，否则，在master B节点上执行`CLUSTER ADDSLOTS`命令将报如下错误：
    ```
    (error) ERR Slot 0 is already busy
    ```

### 示例
#### 将集群中的slot 0从端口为7000的节点删除，并加入到端口为7001的节点
```
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7000 cluster nodes
085a7822af3c2b8f2f7310f4a59dd36af0631677 127.0.0.1:7002@17002 master - 0 1564704450017 3 connected 8192-12287
9720b9ce48bd04e0259ccbba4d4ca85dad910b2a 127.0.0.1:8001@18001 slave ef3d88c5f1934c2676d2dede6a319b650698e4cf 0 1564704451521 2 connected
428a74963be86be451469a4d078db9e71da43010 127.0.0.1:7000@17000 myself,master - 0 1564704448000 1 connected 0-4095
360f95db72dff868de0a948f804162e8aa2cfadd 127.0.0.1:8000@18000 slave 428a74963be86be451469a4d078db9e71da43010 0 1564704450519 1 connected
ad33758b255f013e71f8283d846d97de6b02d38f 127.0.0.1:8002@18002 slave 085a7822af3c2b8f2f7310f4a59dd36af0631677 0 1564704450000 3 connected
ef3d88c5f1934c2676d2dede6a319b650698e4cf 127.0.0.1:7001@17001 master - 0 1564704450519 2 connected 4096-8191
7e12bbc1bb6ff1d13608aeb725c7cd8cba5b3ae2 127.0.0.1:8003@18003 slave dab20ec4b69d316b27f2dd0e4c2faa2d97d6daed 0 1564704450519 4 connected
dab20ec4b69d316b27f2dd0e4c2faa2d97d6daed 127.0.0.1:7003@17003 master - 0 1564704450017 4 connected 12288-16383
[注解]：
1）当前集群由4个master节点（端口号分别是7000、7001、7002、7003），每个master节点都有一个slave节点。
2）当前 slot 0 分配在端口7000的master节点上。

[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7001 cluster delslots 0
OK
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7001 cluster addslots 0
(error) ERR Slot 0 is already busy
[注解]：因为slot 0所属的master节点（端口7000）还没有forget该slot，所以，通过 ADDSLOTS 重新分配将报错

[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7000 cluster delslots 0
OK
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7001 cluster delslots 0
OK
[注解]：这里再次从7001端口的master节点forget slot 0，是因为之前没有将该slot从7000端口的master forget，可能期间通过heartbeat，7001的master又从7000的master那里获取了slot 0所属的master信息
[root@izwz95n8068u7u1zz5oihcz redis-cluster]# ./redis-cli -p 7001 cluster addslots 0
OK
[注解]：从slot 0所属的master节点（端口7000）forget该slot后，通过 ADDSLOTS 重新分配成功
```
    
## CLUSTER FLUSHSLOTS
`CLUSTER FLUSHSLOTS`命令用于清空当前节点已分配的slots，起作用相当于在当前节点执行`CLUSSTER DELSLOTS slot1 ... slotn`命令，其中`slot1 ... slotn`为当前节点已分配的所有slot列表。
注意：和`CLUSSTER DELSLOTS`命令的效果类似，其他节点仍然保留了该节点已分配的slot信息，所以，如果需要将这些清空的slot重新分配给其他节点，必须在其他节点使用命令`CLUSSTER DELSLOTS`命令forget掉其记录的slot分配信息，参见`CLUSSTER DELSLOTS`一节。
命令格式：
```
> CLUSTER FLUSHSLOTS
```

## CLUSTER SETSLOT
`CLUSTER SETSLOT`命令用于设置当前节点中slot的状态，根据子命令的不同，可以设置不同的状态，包括如下子命令：
1. `MIGRATING`子命令：用于设置slot处于migrating状态（即slot正在迁移到其他集群中的节点）
2. `IMPORTING`子命令：用于设置slot处于importing状态（即slot正从集群中的其他节点导入当前节点）
3. `STABLE`子命令：清除slot的importing / migrating状态
4. `NODE`子命令：用于将slot重新分配到集群中的一个新的节点（即：指定的slot分配到指定的NODE）。所以，NODE后面跟目标节点ID

命令格式：
```
> CLUSTER SETSLOT <slot> (importing|migrating|stable|node <node-id>)
```
其中：
+ `<slot>`
    集群中的指定slot
+ `<node-id>` 
    指定集群中的节点ID
+ `importing|migrating|stable|node`
    子命令，一次指定一个子命令，各个子命令的说明，参考下面对应小节

### CLUSTER SETSLOT <slot> IMPORTING <destination-node-id>
`CLUSTER SETSLOT <slot> IMPORTING`子命令用于设置指定的slot处于migrating状态，该命令执行成功后，表示当前节点已经准备好将key迁移到destination-node-id表示的目标节点上。
该命令必须在slot被分配的master节点上执行。
当一个slot被设置为migrating状态时，其被分配的master节点的行为将发生以下改变：
1. 如果在当前节点上执行的命令中的key仍然在当前节点上，该命令将正常执行。
    什么情况下一个key可能不在当前节点上呢？
    比如：当前slot被设置为migrating状态，然后slot中的一个key使用命令`migrate`迁移到其他节点了，而该slot可能仍然在该节点上，只是处于migrating状态。这时候，如果执行`get key`，将返回“(error) ASK”（参见下面的示例：将一个slot以及slot中的所有数据迁移到另一个节点）
2. 如果在当前节点上执行的命令中的key不在当前节点上，将返回“(error) ASK”重定向，提示客户端到重定向的目标节点重试该命令。
    返回“(error) ASK”重定向，并不是说明key已经在另一个节点可以访问了，目标节点可能处于“importing”状态，或者虽然key已经迁移到目标节点了，但slot还未分配到目标节点上（参见下面的示例：将一个slot以及slot中的所有数据迁移到另一个节点）
3. 如果在当前节点上执行的命令中包含多个key，其中如果所有的key都在当前节点上，将和第1点一样；如果所有的key都不在当前节点上，将和第2点一样；否则，将返回TRYAGAIN错误信息。

### CLUSTER SETSLOT <slot> MIGRATING <source-node-id>
`CLUSTER SETSLOT <slot> MIGRATING`子命令和`IMPORTING`子命令相反，该命令执行成功后，表示当前节点已经准备好从source-node-id节点导入key。
该命令必须在非`<slot>`被分配的master节点上执行。
当一个slot被设置为importing状态时，当前节点的行为将发生以下改变：
1. Commands about this hash slot are refused and a MOVED redirection is generated as usually, but in the case the command follows an ASKING command, in this case the command is executed.
    官网的这段描述中，“Commands about this hash slot”是指的哪些命令？是和key相关的命令吗？比如：`get key`

In this way when a node in migrating state generates an ASK redirection, the client contacts the target node, sends ASKING, and immediately after sends the command. This way commands about non-existing keys in the old node or keys already migrated to the target node are executed in the target node, so that:
1. New keys are always created in the target node. During a hash slot migration we'll have to move only old keys, not new ones.
2. Commands about keys already migrated are correctly processed in the context of the node which is the target of the migration, the new hash slot owner, in order to guarantee consistency.
3. Without ASKING the behavior is the same as usually. This guarantees that clients with a broken hash slots mapping will not write for error in the target node, creating a new version of a key that has yet to be migrated.

上面这一段官网的描述没有看明白！！！！

### CLUSTER SETSLOT <slot> STABLE
该子命令将清除slot的migrating 或 importing状态，该子命令主要用于修复因为使用`# redis-cli fix`命令导致集群陷入错误状态的问题。
正常情况，migrating 和 importing状态将在slot迁移的最后阶段使用`CLUSTER SETSLOT <slot> NODE`命令时被自动清除。
#### 使用场景
当使用`CLUSTER SETSLOT IMPORTING`和`CLUSTER SETSLOT MIGRATING`进行迁移的过程中，想退回之前的迁移操作（即退回到执行IMPORINT和MIGRATING之前的状态），也可以使用该命令

### CLUSTER SETSLOT <slot> NODE <destination-node-id>
该命令的影响取决于当前slot处于迁移过程中的状态，下面是在不同前提条件下该命令的影响（以下称“源节点”为slot迁移的来源节点，“目标节点”为slot迁移的目标节点）：
1. 如果是在slot被分配的master节点上执行该命令（即源节点上），该命令将使得slot被分配到目标节点（只是在源节点的slot-node-mapping中分配，实际的slot仍然在源节点上）
    如果该slot上还有key属于源节点（即key还未被迁移到目标节点），该命令将报错。
2. 如果slot处于migrating状态（即在源节点上），migrating状态将被清除，并且slot被分配到目标节点。（这里的分配只是在slot-node-mapping中记录，实际的slot仍然在源节点上）
3. 如果slot处于importing状态（即在目标节点上），该命令将使得slot被分配到目标节点上，并且：
    + importing状态将被清除
    + 如果目标节点的config epoch不是集群中最大的config epoch，目标节点的config epoch将被设置为比集群中所有config epoch都大的一个值
        拥有集群中最大的config epoch，可以确保目标节点在与集群中其它节点协商的过程中，让其他节点统一目标节点广播的信息是集群中最新的，迫使集群中其他节点都接受目标节点的广播信息（包括slot的resharding信息等)。
        这样的意义是：当slot从源节点resharding到目标节点后，目标节点将广播它所拥有的slots，并迫使其他节点都同意目标节点所拥有的slots。从而达到迁移后的slot分配信息在整个集群都得到认可。
        参考redis官网“Redis Cluster Specification”一章中的[Configuration handling, propagation, and failovers](https://redis.io/topics/cluster-spec)

### redis集群中slot的重新分配方法
`CLUSTER SETSLOT`命令主要用于将slot中的所有key从一个节点（下面简称为“源节点”）迁移到另一个节点（下面简称为“目标节点”）。迁移的步骤为：
1. 通过命令`CLUSTER SETSLOT <slot> IMPORTING <source-node-id>`将目标节点的slot设置为importing状态
2. 通过命令`CLUSTER SETSLOT <slot> MIGRATING <destination-node-id>`将源节点的slot设置为migrating状态
3. 通过命令`CLUSTER GETKYESINSLOT`获取源节点的slot中的所有key，然后将所有的key通过命令`migrate`迁移到目标节点
    当一个处于MIGRATING状态的slot中的key从源节点迁移到目标节点后，此时，在源节点查询该key，将返回“(error) ASK 目标节点IP:端口”；
    对于ASK类型的重定向，必须在重定向节点上先执行`ASKING`命令，然后再执行查询，否则重定向节点将返回“(error) MOVED”错误信息。`ASKING`命令将在重定向节点上设置一个一次性标志，强制重定向节点查询一个处于IMPRTING状态的slot。
    所以，如果直接在目标节点查询该key，将返回“(error) MOVED 源节点ID:端口”。
    必须现在目标节点执行`ASKING`命令，然后再执行查询命令。
    往源节点写入一个新的key，将返回“(error) ASK 目标节点IP:端口”；往目标节点写入一个新的key，如果先执行`ASKING`命令，是可以成功写入的；否则，目标节点将返回“error) MOVED”
    
4. 在源节点或目标节点上执行`CLUSTER SETSLOT <slot> NODE <destination-node-id>`
    实际操作的时候，需要在源节点和目标节点上分别执行`CLUSTER SETSLOT <slot> NODE <destination-node-id>`和`CLUSTER SETSLOT <slot> NODE <destination-node-id>`才行。
    注意：源节点和目标节点上执行`CLUSTER SETSLOT <slot> NODE <destination-node-id>`命令时，都是指定目标节点的ID。

### 示例
#### 将一个slot以及slot中的所有数据迁移到另一个节点
假设有端口分别为7000、7001、7002的三个master节点，现在需要将节点7000中的slot 3300迁移到节点7002。
迁移的过程为：
1. 在节点7002上执行`CLUSTER SETSLOT importing`命令设置importing flag
2. 然后节点7000上执行`CLUSTER SETSLOT migrating`命令设置migrating flag,
3. 在节点7000上通过`migrate`命令迁移所有slot 3000上的数据到节点7002
4. 在7000节点上执行`CLUSTER SETSLOT node <destination-node-id>`命令消除migrating flag
5. 在7002节点上执行`CLUSTER SETSLOT node <destination-node-id>`命令消除importing flag

下面将分别从7000节点和7002节点两个视角查看命令执行后的集群信息（这里的“视角”是指在从节点上执行命令的结果来看）：
```
127.0.0.1:7001> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564833591899 3 connected 10923-16383
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564833590896 1 connected 0-5460
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 myself,master - 0 1564833590000 2 connected 5461-10922

127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564835180360 1 connected 0-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564835178000 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835179358 2 connected 5461-10922
[注解]：上面分别从7000节点和7002节点两个视角查看集群节点信息，集群中有7000、7001、7002三个master节点。且slot 3300当前分配在7000节点中。


127.0.0.1:7000> cluster getkeysinslot 3300 3
1) "b"
127.0.0.1:7002> cluster getkeysinslot 3300 3
(empty list or set)
127.0.0.1:7000> get b
"hello migrating"
127.0.0.1:7002> get b
(error) MOVED 3300 127.0.0.1:7000
[注解]：确认slot 3中存在key "b"，并且是位于节点7000中


127.0.0.1:7002> cluster setslot 3300 importing 392957d7d28b618b27f53e93d0cec211491dd2ad
OK
127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564835637604 1 connected 0-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564835636000 3 connected 10923-16383 [3300-<-392957d7d28b618b27f53e93d0cec211491dd2ad]
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835636601 2 connected 5461-10922
127.0.0.1:7000> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564835676707 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835675704 2 connected 5461-10922
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 myself,master - 0 1564835674000 1 connected 0-5460 [3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]
127.0.0.1:7000> get b
"hello migrating"
127.0.0.1:7002> get b
(error) MOVED 3300 127.0.0.1:7000
[注解]：将7002节点设置importing flag，此时：
        从7000节点的视角看到slot 3300被标记为migrating到节点7002（id=16013d36a6ddeb19b29ccf65ae6f3c82e3289d47)（即：[3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]）
        从7002节点的视角看到slot 3300被标记为从节点7000（id=392957d7d28b618b27f53e93d0cec211491dd2ad）importing过来（即：[3300-<-392957d7d28b618b27f53e93d0cec211491dd2ad]）


127.0.0.1:7000> cluster setslot 3300 migrating 16013d36a6ddeb19b29ccf65ae6f3c82e3289d47
OK
127.0.0.1:7000> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564835522285 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835523288 2 connected 5461-10922
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 myself,master - 0 1564835523000 1 connected 0-5460 [3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]
127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564835544343 1 connected 0-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564835545000 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835545345 2 connected 5461-10922
[注解]：将7000节点设置migrating flag，此时：
       从7000节点的视角看到slot 3300被标记为migrating到16013d36a6ddeb19b29ccf65ae6f3c82e3289d47（即：[3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]）
       从7002节点的视角看还未发生任何变化


127.0.0.1:7000> cluster setslot 3300 node e589bc2ea2196f0987e190aeeb448c307d33c465
(error) ERR Can't assign hashslot 3300 to a different node while I still hold keys for this hash slot.
[注解]：当slot 3300上还有key没有迁移到7002节点时，执行`cluster setslot 3300 node`会报错


127.0.0.1:7000> migrate 127.0.0.1 7002 b 0 5000
OK
127.0.0.1:7000> cluster getkeysinslot 3300 3
(empty list or set)
127.0.0.1:7002> cluster getkeysinslot 3300 3
1) "b"
127.0.0.1:7000> keys *
(empty list or set)
127.0.0.1:7002> keys *
1) "b"
127.0.0.1:7000> get b
(error) ASK 3300 127.0.0.1:7002
127.0.0.1:7002> get b
(error) MOVED 3300 127.0.0.1:7000
127.0.0.1:7002> ASKING
127.0.0.1:7002> get b
"hello migrating"
[注解]：将key"b"从节点7000迁移到节点7002。此时：
        1）从命令`cluster getkeysinslot 3300 3`和命令`keys *`分别在7000和7002节点执行的结果可以看到，key "b"已经被迁移到7002节点了。
        2）从命令`get b`分别在7000和7002节点执行的结果可以看到，此时在两个节点上访问key "b"，都无法获取到对应的值，只是分别返回“(error) ASK 3300 127.0.0.1:7002”和“(error) MOVED 3300 127.0.0.1:7000”。参考 `CLUSTER SETSLOT <slot> MIGRATING`和`CLUSTER SETSLOT <slot> IMPORTING`命令。
           对于ASK类型的重定向，正确的访问方法是，在重定向节点先执行`ASKING`命令，在查询相关的key。


127.0.0.1:7000> set 3560 hello
(error) ASK 0 127.0.0.1:7002
127.0.0.1:7002> set 3560 hello
(error) MOVED 0 127.0.0.1:7000
127.0.0.1:7002> ASKING
OK
127.0.0.1:7002> set 3560 hello
OK
[注解]：当源节点和目标节点分别处于migrating和importing状态时，写入一个新的key和读取一个已迁移到目标节点的key逻辑类似，在源节点返回“(error) ASK”，在目标节点需要先执行`ASKING`指令后，在执行写操作才能成功。


127.0.0.1:7000> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564835676707 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835675704 2 connected 5461-10922
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 myself,master - 0 1564835674000 1 connected 0-5460 [3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]
127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564835637604 1 connected 0-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564835636000 3 connected 10923-16383 [3300-<-392957d7d28b618b27f53e93d0cec211491dd2ad]
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564835636601 2 connected 5461-10922
[注解]：迁移完key "b"后，集群中节点的信息未发生改变。仍然和迁移之前的一致。



127.0.0.1:7002> cluster setslot 3300 node 16013d36a6ddeb19b29ccf65ae6f3c82e3289d47
OK
127.0.0.1:7000> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564837572946 3 connected 3300 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564837571943 2 connected 5461-10922
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 myself,master - 0 1564835674000 1 connected 0-5460 [3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]
127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564837561914 1 connected 0-3299 3301-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564837560000 3 connected 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564837560910 2 connected 5461-10922
127.0.0.1:7000> get b
(error) MOVED 3300 127.0.0.1:7002
127.0.0.1:7002> get b
"hello migrating"
[注解]：在节点7002执行`cluster setslot 3300 node`命令，清除importing flag，执行完后集群信息变化如下：  
        1）从节点7002的视角看，slot 3300已经分配在7002节点了（上一次是“[3300-<-392957d7d28b618b27f53e93d0cec211491dd2ad]”）
        2）从节点7002执行命令`get b`，可以正确的取到值了，而不是“（error）MOVED”
    

127.0.0.1:7000> cluster setslot 3300 node 16013d36a6ddeb19b29ccf65ae6f3c82e3289d47
OK
127.0.0.1:7000> cluster nodes
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 master - 0 1564836608297 3 connected 3300 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564836609300 2 connected 5461-10922
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 myself,master - 0 1564836608000 1 connected 0-3299 3301-5460
127.0.0.1:7002> cluster nodes
392957d7d28b618b27f53e93d0cec211491dd2ad 127.0.0.1:7000@17000 master - 0 1564836622332 1 connected 0-5460
16013d36a6ddeb19b29ccf65ae6f3c82e3289d47 127.0.0.1:7002@17002 myself,master - 0 1564836621000 3 connected 3300 10923-16383
61907c2b4fa056dadb3f8438597fa6cb750e0472 127.0.0.1:7001@17001 master - 0 1564836623335 2 connected 5461-10922
127.0.0.1:7000> get b
(error) MOVED 3300 127.0.0.1:7002
127.0.0.1:7002> get b
"hello migrating"
[注解]：在节点7000执行`cluster setslot 3300 node`命令，清除migrating flag，执行完后集群信息变化如下：
       1）从节点7000的视角看，slot 3300已经分配到节点7002了，而不是之前的“[3300->-16013d36a6ddeb19b29ccf65ae6f3c82e3289d47]”（即已经不是migrating状态了）
```


## CLUSTER FORGET
`CLUSTER FORGET`命令用于将指定的节点从当前节点的nodes table中移除。集群中每个节点保存的nodes table中记录了当前集群中的所有节点清单（包括slave节点），如果从nodes table中移除一个节点，当前节点认为被移除的节点已经不在集群中了。
假设有A、B、C、D四个节点，如果要将节点D从集群中移除，需要在A、B、C三个节点上分贝执行`CLUSTER FORGET`命令，这样整个集群中所有节点都会将D从nodes table中移除。但是，如果在A节点执行`CLUSTER FORGET`命令后，B节点给A节点发送了heartbeat包，其中携带了节点D的信息，节点A是否又会将D加入nodes table呢？
redis在实现`CLUSTER FORGET`命令的时候，会增加一个`ban-list`，记录在`ban-list`中的节点，将在处理heartbeat包信息的过程中被忽略掉。同时，被移除的节点加入`ban-list`中只在1分钟内有效。
所以，执行`CLUSTER FORGET`命令后，redis所做的事情包括：
1. 命令指定的节点将从当前节点的nodes table中移除；
2. 被移除的节点ID被记录到当前节点的`ban-list`中，有效期为1分钟；
3. 当前节点在处理heartbeat包的gossip sections时，将忽略`ban-list`中的节点。

命令格式：
```
> CLUSTER FORGET <node-id>
```
其中：
+ `<node-id>`
    被移除节点的ID

以下条件将导致命令执行失败：
1. 被移除节点的ID在当前节点的nodes table中不存在；
2. 被移除节点是一个master节点，并且执行`CLUSTER FORGET`命令的节点为它的slave节点
3. 被移除节点为当前执行`CLUSTER FORGET`命令的节点（即不能自己forget自己）

### 示例
#### 将一个节点从集群中移除
假设当前集群有4个节点，IP都是127.0.0.1，端口号分别为：7000、7001、7002、7003，下面将端口为7003的节点从集群中移除
```
127.0.0.1:7000> cluster nodes
be970e9abcee233bad6361c2186b6aa40f7b4c7b 127.0.0.1:7001@17001 master - 0 1564829162000 2 connected 4096-8191
a4d24b5a0dd2676e8f05ffb470a865815bcaf591 127.0.0.1:7002@17002 master - 0 1564829162365 3 connected 8192-12287
753b60cc113bc983e607664dcf09fa568f28cbb8 127.0.0.1:7003@17003 master - 0 1564829161000 4 connected 12288-16383
485814e8f86709cc1f0208661555f7bd54d7fa0a 127.0.0.1:7000@17000 myself,master - 0 1564829161000 1 connected 0-4095
[注解]：查看集群中的节点清单，共4个master节点

127.0.0.1:7003> cluster forget 753b60cc113bc983e607664dcf09fa568f28cbb8
(error) ERR I tried hard but I can't forget myself...
[注解]：不能自己遗忘自己

127.0.0.1:7000> cluster forget 753b60cc113bc983e607664dcf09fa568f28cbb8
OK
127.0.0.1:7001> cluster forget 753b60cc113bc983e607664dcf09fa568f28cbb8
OK
127.0.0.1:7002> cluster forget 753b60cc113bc983e607664dcf09fa568f28cbb8
OK

127.0.0.1:7002> cluster info
cluster_state:fail
127.0.0.1:7002> set a "hello"
(error) CLUSTERDOWN Hash slot not served
[注解]：因为7003节点分配了slots，且7003节点已经从7002节点的nodes table中移除了，所以整个集群中缺少了7003节点的那部分slots。此时，集群的状态是FAIL。所以此时集群处于不可用状态。

127.0.0.1:7003> cluster flushslots
OK
[注解]：清空7003节点已分配的slots

# ./batch_add_slots.sh 7000 12288 16383
ok
...
[注解]：批量的将slots[12288 ~ 16383]分配给端口7000的节点。

127.0.0.1:7002> cluster nodes
485814e8f86709cc1f0208661555f7bd54d7fa0a 127.0.0.1:7000@17000 master - 0 1564830795588 1 connected 0-4095 12288-16383
a4d24b5a0dd2676e8f05ffb470a865815bcaf591 127.0.0.1:7002@17002 myself,master - 0 1564830794000 3 connected 8192-12287
be970e9abcee233bad6361c2186b6aa40f7b4c7b 127.0.0.1:7001@17001 master - 0 1564830796616 2 connected 4096-8191

127.0.0.1:7002> cluster info
cluster_state:ok
[注解]：再看集群节点信息，12288-16383区间的slot已经分配到7000节点了，并且整个集群的状态也变为OK了。

127.0.0.1:7003> cluster reset hard
OK
[注解]：重置7003节点，清除所有之前的集群信息。这样，7003节点就彻底的从之前的集群中移除了。
```
    

## CLUSTER RESET
`CLUSTER RESET`命令用于重置集群中的一个节点。重置分为两种方式：soft和hard。重置的效果为：
1. 所有集群中的其他节点都将被当前节点遗忘（即从当前节点的nodes table中移除）
2. 所有的slots分配信息都被清空（即slots-to-nodes映射被全部清除）
3. 如果当前节点是一个slave节点，执行命令后，将变为一个空的master节点，它的数据将被全部清空
4. 仅应用于hard方式：当前节点生成一个新的node id
5. 仅应用于hard方式：当前节点记录的current epoch和config epoch变为0
6. 新的配置信息被保存到集群配置文件中（即redis.conf中设置的`cluster-config-file`文件）

#### 疑问
1. 在节点上执行`CLUSTER RESET`后，怎么将该节点从集群中其他节点的node table中移除？难道要到每个节点执行`CLUSTER FORGET`吗？
    正确，就是在其他节点上都执行`CLUSTER FORGET`命令。