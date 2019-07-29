# redis-cli --cluster命令选项
`# redis-cli --cluster`命令下有很多选项可用于对cluster进行设置，以下所有的说明或示例代码都基于以下[cluster环境搭建.md](cluster环境搭建.md)中的cluster节点分布：
| 节点类型 | IP:PORT              | 所属master           |
|----------|----------------------|----------------------|
| master   | 192.168.199.130:7000 |                      |
| master   | 192.168.199.130:7001 |                      |
| master   | 192.168.199.130:7002 |                      |
| slave    | 192.168.199.130:8000 | 192.168.199.130:7000 |
| slave    | 192.168.199.130:8001 | 192.168.199.130:7001 |
| slave    | 192.168.199.130:8002 | 192.168.199.130:7002 |

`# redis-cli --cluster`命令主要是对整个cluster进行设置。可以设置的命令选项如下所示：
```
[root@localhost redis-cluster]# ./redis-cli --cluster help
Cluster Manager Commands:
create         host1:port1 ... hostN:portN
                --cluster-replicas <arg>
check          host:port
                --cluster-search-multiple-owners
info           host:port
fix            host:port
                --cluster-search-multiple-owners
reshard        host:port
                --cluster-from <arg>
                --cluster-to <arg>
                --cluster-slots <arg>
                --cluster-yes
                --cluster-timeout <arg>
                --cluster-pipeline <arg>
                --cluster-replace
rebalance      host:port
                --cluster-weight <node1=w1...nodeN=wN>
                --cluster-use-empty-masters
                --cluster-timeout <arg>
                --cluster-simulate
                --cluster-pipeline <arg>
                --cluster-threshold <arg>
                --cluster-replace
add-node       new_host:new_port existing_host:existing_port
                --cluster-slave
                --cluster-master-id <arg>
del-node       host:port node_id
call           host:port command arg arg .. arg
set-timeout    host:port milliseconds
import         host:port
                --cluster-from <arg>
                --cluster-copy
                --cluster-replace
```

## --cluster create
`--cluster create`命令用于创建cluster。前提是用于创建cluster的所有节点实例已经启动。
命令格式：
```
# redis-cli create host1:port1 ... hostN:portN
                 --cluster-replicas <arg>
```
其中：
+ `host1:port1 ... hostN:portN`
    required, 创建cluster的节点列表。redis cluster要求至少有3个master节点。所以该参数中至少得有三个`host:port`列表
+ `--cluster-replicas <arg>`
    optional, 指定每个master的slave节点数，其中`<arg>`为每个master的slave节点个数。
    注意：
    - 如果未指定参数`--cluster-replicas <arg>`，则参数`host1:port1 ... hostN:portN`中的所有节点在cluster中都将是master节点
    - 如果指定了`--cluster-replicas <arg>`，表示每个master节点都至少要有`<arg>`参数所指定的slave节点数。
        例如：当指定`--cluster-replicas 1`时，表示所有master节点都至少要有1个slave节点，因为redis cluster要求至少有3个master节点，所以，`host1:port1 ... hostN:portN`参数中最少必须指定6个节点信息。
        当然，`host1:port1 ... hostN:portN`参数中指定7个节点也是可以的，多余的1个节点将随机分配给一个master节点作为slave。如果`host1:port1 ... hostN:portN`参数中
        当`host1:port1 ... hostN:portN`参数中指定了8个节点时，分配规则又有所不同，将出现4个master节点、4个slave节点。
        假设一共有n个节点（即`host1:port1 ... hostN:portN`有n个主机信息），且指定`--cluster-replicas m`（其中m为大于正整数），假设master节点数为mn，
        则有：mn * (m + 1) <= n < mn * (m + 2)，也就是：mn <= n / (m + 1) && mn > n / (m + 2)  
        也就是：mn = floor ( n / (m + 1))
### 示例
#### 将6个redis实例设置为一个cluster
```
[root@192 redis-cluster]# ./redis-cli --cluster create 192.168.199.130:7000 192.168.199.130:7001 192.168.199.130:7002 192.168.199.130:8001 192.168.199.130:8002 192.168.199.130:8000 --cluster-replicas 1
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 192.168.199.130:8002 to 192.168.199.130:7000
Adding replica 192.168.199.130:8000 to 192.168.199.130:7001
Adding replica 192.168.199.130:8001 to 192.168.199.130:7002
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: 78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000
   slots:[0-5460] (5461 slots) master
M: fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001
   slots:[5461-10922] (5462 slots) master
M: 0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002
   slots:[10923-16383] (5461 slots) master
S: 6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001
   replicates fe00ec489c2446232de3770fea1a9a7717079b79
S: 45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002
   replicates 0e9f93246cbd0de72f3f216ba207c055ce36b51b
S: 2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000
   replicates 78d288b7644411bb1f10ad3547406859e8727daf
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
....
>>> Performing Cluster Check (using node 192.168.199.130:7000)
M: 78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: 6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001
   slots: (0 slots) slave
   replicates fe00ec489c2446232de3770fea1a9a7717079b79
M: fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002
   slots: (0 slots) slave
   replicates 0e9f93246cbd0de72f3f216ba207c055ce36b51b
S: 2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000
   slots: (0 slots) slave
   replicates 78d288b7644411bb1f10ad3547406859e8727daf
M: 0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.


[root@192 redis-cluster]# ./redis-cli -c -h 192.168.199.130 -p 7000
192.168.199.130:7000> cluster nodes
6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001@18001 slave fe00ec489c2446232de3770fea1a9a7717079b79 0 1564296115576 4 connected
78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000@17000 myself,master - 0 1564296114000 1 connected 0-5460
fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001@17001 master - 0 1564296116081 2 connected 5461-10922
45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002@18002 slave 0e9f93246cbd0de72f3f216ba207c055ce36b51b 0 1564296116000 5 connected
2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000@18000 slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564296116000 6 connected
0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002@17002 master - 0 1564296115072 3 connected 10923-16383
```

## --cluster reshard
`--cluster reshard`命令用于对redis cluster中的slots进行重新分配。redis总共有16384个slots。
命令格式：
```
# redis-cli reshard host:port
                    --cluster-from <arg>
                    --cluster-to <arg>
                    --cluster-slots <arg>
                    --cluster-yes
                    --cluster-timeout <arg>
                    --cluster-pipeline <arg>
                    --cluster-replace
```
其中：
+ `host:port`   
    required，为redis cluster中任意一个节点的地址和端口
+ `--cluster-from <arg>`    
    required，指定slots从哪些节点reshard过来。参数`<arg>`的值可以是：
    - all 表示reshard的slots来自所有的节点
    - 单个或多个master节点的ID，多个节点ID之间使用逗号拼接，如：`b0848720c2c3e60a1f8a80751ebb64f722297e2b,6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5`。
        注意：必须是master节点的ID
+ `--cluster-to <arg>`      
    required，指定slots将要reshard到的master节点。参数`<arg>`的值为目标master节点的ID。注意，必须是master节点的ID，不可以是slave节点的ID。
+ `--cluster-slots <arg>`   
    required，指定需要reshard的slots数量。一个redis cluster总共有16384个slots，该数量必须为[1, 16384]。
+ `--cluster-yes`           
    optional，执行reshard之前是否需要在终端确认。
    如果不加该选项，在检查完成相关的参数后将提示`Do you want to proceed with the proposed reshard plan (yes/no)? `，必须输入`yes`才会最终执行reshard，否则，将放弃执行。
    如果加了该选项，将不需要在终端确认就直接执行reshard了。
+ `--cluster-timeout <arg>` 
    [未知]
+ `--cluster-pipeline <arg>` 
    [未知]
+ `--cluster-replace`
    [未知]

`--cluster reshard`命令重新分配的slots的算法是怎么的？是平均分配还是？

### 示例
将2个slots从节点`192.168.199.130:7000`和`192.168.199.130:7001`挪到`192.168.199.130:7002`。操作步骤：1）使用命令`> cluster nodes`查看各节点的ID；2）执行reshard命令
```
[root@localhost redis-cluster]# ./redis-cli -c -h 192.168.199.130 -p 7001
192.168.199.130:7001> cluster nodes
f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002@18002 slave 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 0 1564287136000 4 connected
2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000@18000 slave b0848720c2c3e60a1f8a80751ebb64f722297e2b 0 1564287137234 5 connected
e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001@17001 myself,master - 0 1564287135000 2 connected 5463-10922
b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000@17000 master - 0 1564287137234 1 connected 0-5460
b3bcfe2a97c640921f3daf8234e1882a44341a1a 192.168.199.130:8003@18003 slave a5968aa575ee671d7c186c01d9a717ba3435bb4c 0 1564287136125 7 connected
6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002@17002 master - 0 1564287135000 3 connected 10923-16383
a5968aa575ee671d7c186c01d9a717ba3435bb4c 192.168.199.130:7003@17003 master - 0 1564287136224 7 connected 5461-5462
2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001@18001 slave e0ac3e0c44145cedc41e7dc4f82138da43ab348b 0 1564287136527 2 connected

[注解]查看reshard之前各个节点的slots统计
[root@localhost redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7003
192.168.199.130:7003 (a5968aa5...) -> 0 keys | 0 slots | 0 slaves.
192.168.199.130:7001 (e0ac3e0c...) -> 0 keys | 5462 slots | 1 slaves.
192.168.199.130:7002 (6d989010...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7000 (b0848720...) -> 0 keys | 5461 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.

[root@localhost redis-cluster]# ./redis-cli --cluster reshard 192.168.199.130:7000 --cluster-from b0848720c2c3e60a1f8a80751ebb64f722297e2b,e0ac3e0c44145cedc41e7dc4f82138da43ab348b --cluster-to 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 --cluster-slots 4
>>> Performing Cluster Check (using node 192.168.199.130:7000)
M: b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: a5968aa575ee671d7c186c01d9a717ba3435bb4c 192.168.199.130:7003
   slots:[5461-5462] (2 slots) master
   1 additional replica(s)
S: b3bcfe2a97c640921f3daf8234e1882a44341a1a 192.168.199.130:8003
   slots: (0 slots) slave
   replicates a5968aa575ee671d7c186c01d9a717ba3435bb4c
S: 2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001
   slots: (0 slots) slave
   replicates e0ac3e0c44145cedc41e7dc4f82138da43ab348b
M: 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002
   slots: (0 slots) slave
   replicates 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5
M: e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001
   slots:[5463-10922] (5460 slots) master
   1 additional replica(s)
S: 2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000
   slots: (0 slots) slave
   replicates b0848720c2c3e60a1f8a80751ebb64f722297e2b
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.

Ready to move 4 slots.
  Source nodes:
    M: b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000
       slots:[0-5460] (5461 slots) master
       1 additional replica(s)
    M: e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001
       slots:[5463-10922] (5460 slots) master
       1 additional replica(s)
  Destination node:
    M: 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002
       slots:[10923-16383] (5461 slots) master
       1 additional replica(s)
  Resharding plan:
    Moving slot 0 from b0848720c2c3e60a1f8a80751ebb64f722297e2b
    Moving slot 1 from b0848720c2c3e60a1f8a80751ebb64f722297e2b
    Moving slot 2 from b0848720c2c3e60a1f8a80751ebb64f722297e2b
    Moving slot 5463 from e0ac3e0c44145cedc41e7dc4f82138da43ab348b
Do you want to proceed with the proposed reshard plan (yes/no)? yes
Moving slot 0 from 192.168.199.130:7000 to 192.168.199.130:7002: 
Moving slot 1 from 192.168.199.130:7000 to 192.168.199.130:7002: 
Moving slot 2 from 192.168.199.130:7000 to 192.168.199.130:7002: 
Moving slot 5463 from 192.168.199.130:7001 to 192.168.199.130:7002: 

[root@localhost redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7003
192.168.199.130:7001 (e0ac3e0c...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7002 (6d989010...) -> 0 keys | 5465 slots | 1 slaves.
192.168.199.130:7000 (b0848720...) -> 0 keys | 5458 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.
```

## --cluster add-node
用于向redis cluster添加一个新的节点。
命令格式：
```
# redis-cli --cluster add-node new_host:new_port existing_host:existing_port
                 --cluster-slave
                 --cluster-master-id <arg>
```
其中：
+ new_host:new_port     
    required，为新增节点的地址和端口
+ existing_host:existing_port   
    required, 为redis cluster中的任意一个节点的地址和端口
+ --cluster-slave       
    optional, 指定新增的节点作为一个slave节点
+ --cluster-master-id <arg>  
    optional, 如果指定了`--cluster-slave`选项，`--cluster-master-id <arg>`选项用于指定新增节点所属master节点的ID（arg参数对应master节点的ID值）。可使用`--cluster nodes`查看所有节点的ID。

[注解]：
+ 如果指定了`--cluster-slave`选项，但是未指定`--cluster-master-id <arg>`选项，新增的节点将自动选取一个master节点作为它的master。
    这里自动选取master节点的算法未知，但是会优先选择slave节点数少的master。
+ 如果是新增一个slave节点，有可能导致cluster中已有的slave节点重新分配master。
    参见本节示例中的[新增一个slave节点并导致cluster中其他slave节点重新分配master](新增一个slave节点并导致cluster中其他slave节点重新分配master)
+ 如果新增一个slave节点，并且该slave节点曾经加入过某个cluster，加入新的cluster之前，最好在启动该slave节点之前清除掉之前cluster conf文件（即redis.conf文件中设置项`cluster-config-file`对应的文件，该文件由redis实例生成并自动维护，里面记录了redis实例参与的cluster的信息）
    如果启动之前没有清楚cluster conf文件，在执行命令`--cluster add-node`最后，可能会遇到如下错误提示：
    ```
    [ERR] Node 192.168.199.130:8003 is not empty. Either the node already knows other nodes (check with CLUSTER NODES) or contains some key in database 0.
    ```


### 示例
#### 新增一个master节点
新增一个master节点的操作步骤为：1）启动新增的master节点；2）将master节点加入cluster中。
命令如下，其中，`192.168.199.130:7003`为新增的节点，`192.168.199.130 7000`为redis cluser中已存在的节点。
```
[root@localhost redis-cluster]# ./redis-server ./node-7003/redis.conf
[root@localhost redis-cluster]# ./redis-cli --cluster add-node 192.168.199.130:7003 192.168.199.130:7000
>>> Adding node 192.168.199.130:7003 to cluster 192.168.199.130:7000
>>> Performing Cluster Check (using node 192.168.199.130:7000)
M: b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: 2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001
   slots: (0 slots) slave
   replicates e0ac3e0c44145cedc41e7dc4f82138da43ab348b
M: 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002
   slots: (0 slots) slave
   replicates 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5
M: e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000
   slots: (0 slots) slave
   replicates b0848720c2c3e60a1f8a80751ebb64f722297e2b
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
>>> Send CLUSTER MEET to node 192.168.199.130:7003 to make it join the cluster.
[OK] New node added correctly.

[root@localhost redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7003
192.168.199.130:7003 (a5968aa5...) -> 0 keys | 0 slots | 0 slaves.
192.168.199.130:7001 (e0ac3e0c...) -> 0 keys | 5462 slots | 1 slaves.
192.168.199.130:7002 (6d989010...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7000 (b0848720...) -> 0 keys | 5461 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.

# ./redis-cli -c -p 7000
192.168.199.130:7000> cluster nodes
f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002@18002 slave 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 0 1564280904000 4 connected
2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000@18000 slave b0848720c2c3e60a1f8a80751ebb64f722297e2b 0 1564280903038 5 connected
e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001@17001 myself,master - 0 1564280903000 2 connected 5461-10922
b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000@17000 master - 0 1564280904000 1 connected 0-5460
6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002@17002 master - 0 1564280904553 3 connected 10923-16383
a5968aa575ee671d7c186c01d9a717ba3435bb4c 192.168.199.130:7003@17003 master - 0 1564280904000 0 connected
2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001@18001 slave e0ac3e0c44145cedc41e7dc4f82138da43ab348b 0 1564280904654 2 connected
```
从最后`# ./redis-cli --cluster info 192.168.199.130:7003`的结果可以看到，新增的节点`192.168.199.130:7003`为master类型的节点，并且未分配任何slots，且没有slave节点。后续可以通过`# ./redis-cli --cluster reshard`命令为期分配slots。也可以通过在slave节点上执行`> cluster REPLICATE`，将当前节点作为该master的slave节点。

#### 新增一个slave节点，并为其指定master节点
新增一个slave节点的操作步骤为：1）启动新增的slave节点；2）将slave节点加入cluster中并指定其所属的master节点。
命令如下，其中：`192.168.199.130:8003`为新增的节点，`a5968aa575ee671d7c186c01d9a717ba3435bb4c`为master节点`192.168.199.130 7003`的ID：
```
[root@localhost redis-cluster]# ./redis-server ./node-8003/redis.conf 
[root@localhost redis-cluster]# ./redis-cli --cluster add-node 192.168.199.130:8003 192.168.199.130:7000 --cluster-slave --cluster-master-id a5968aa575ee671d7c186c01d9a717ba3435bb4c
>>> Adding node 192.168.199.130:8003 to cluster 192.168.199.130:7000
>>> Performing Cluster Check (using node 192.168.199.130:7000)
M: b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: a5968aa575ee671d7c186c01d9a717ba3435bb4c 192.168.199.130:7003
   slots: (0 slots) master
S: 2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001
   slots: (0 slots) slave
   replicates e0ac3e0c44145cedc41e7dc4f82138da43ab348b
M: 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002
   slots: (0 slots) slave
   replicates 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5
M: e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000
   slots: (0 slots) slave
   replicates b0848720c2c3e60a1f8a80751ebb64f722297e2b
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
>>> Send CLUSTER MEET to node 192.168.199.130:8003 to make it join the cluster.
Waiting for the cluster to join

>>> Configure node as replica of 192.168.199.130:7003.
[OK] New node added correctly.

[root@localhost redis-cluster]# ./redis-cli --cluster info 192.168.199.130:8003
192.168.199.130:7002 (6d989010...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7001 (e0ac3e0c...) -> 0 keys | 5462 slots | 1 slaves.
192.168.199.130:7000 (b0848720...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7003 (a5968aa5...) -> 0 keys | 0 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.

192.168.199.130:7001> cluster nodes
f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002@18002 slave 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 0 1564281276543 4 connected
2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000@18000 slave b0848720c2c3e60a1f8a80751ebb64f722297e2b 0 1564281275000 5 connected
e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001@17001 myself,master - 0 1564281274000 2 connected 5461-10922
b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000@17000 master - 0 1564281275000 1 connected 0-5460
b3bcfe2a97c640921f3daf8234e1882a44341a1a 192.168.199.130:8003@18003 slave a5968aa575ee671d7c186c01d9a717ba3435bb4c 0 1564281274528 7 connected
6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002@17002 master - 0 1564281276343 3 connected 10923-16383
a5968aa575ee671d7c186c01d9a717ba3435bb4c 192.168.199.130:7003@17003 master - 0 1564281276000 7 connected
2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001@18001 slave e0ac3e0c44145cedc41e7dc4f82138da43ab348b 0 1564281276000 2 connected
```

#### 新增一个slave节点并导致cluster中其他slave节点重新分配master
当前cluster中节点的拓扑结构如下所示：
```
192.168.199.130:7000> cluster nodes
6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001@18001 slave fe00ec489c2446232de3770fea1a9a7717079b79 0 1564296327000 4 connected
78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000@17000 myself,master - 0 1564296327000 1 connected 0-5460
fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001@17001 master - 0 1564296327000 2 connected 5461-10922
45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002@18002 slave 0e9f93246cbd0de72f3f216ba207c055ce36b51b 0 1564296326640 5 connected
2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000@18000 slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564296326000 6 connected
00597cbc4c94eabbabd199336785405ced501064 192.168.199.130:7003@17003 master - 0 1564296327545 0 connected
0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002@17002 master - 0 1564296327647 3 connected 10923-16383
```

即有四个master节点：192.168.199.130:7000、192.168.199.130:7001、192.168.199.130:7002、192.168.199.130:7003。其中，192.168.199.130:7000、192.168.199.130:7001、192.168.199.130:7002 这3个master节点都有一个slave节点，分别是：192.168.199.130:8000、192.168.199.130:8001、192.168.199.130:8002，而192.168.199.130:7003下没有slave节点。

使用如下命令新增一个slave节点，并指定master为`192.168.199.130:7000`
```
[root@192 redis-cluster]# ./redis-cli --cluster add-node 192.168.199.130:8003 192.168.199.130:7000 --cluster-slave --cluster-master-id 78d288b7644411bb1f10ad3547406859e8727daf

[root@192 redis-cluster]# ./redis-cli --cluster info 192.168.199.130:7000
192.168.199.130:7000 (78d288b7...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7001 (fe00ec48...) -> 0 keys | 5461 slots | 1 slaves.
192.168.199.130:7003 (00597cbc...) -> 0 keys | 1 slots | 1 slaves.
192.168.199.130:7002 (0e9f9324...) -> 0 keys | 5461 slots | 1 slaves.
[OK] 0 keys in 4 masters.
0.00 keys per slot on average.

192.168.199.130:8003> cluster nodes
45820b1f1892af23600fafca6b13bde34364a376 192.168.199.130:8002@18002 slave 0e9f93246cbd0de72f3f216ba207c055ce36b51b 0 1564296775579 3 connected
78d288b7644411bb1f10ad3547406859e8727daf 192.168.199.130:7000@17000 master - 0 1564296773565 1 connected 0-5460
859fb3c4c18209b8875ff77bd24e57a18b548abf 192.168.199.130:8003@18003 myself,slave 78d288b7644411bb1f10ad3547406859e8727daf 0 1564296774000 0 connected
6d2fcaf621c3c42efadaf87830ce4dca4bcf87c8 192.168.199.130:8001@18001 slave fe00ec489c2446232de3770fea1a9a7717079b79 0 1564296773565 2 connected
2d08c34a251ca7a4ca263f9d17248da2a62a9a40 192.168.199.130:8000@18000 slave 00597cbc4c94eabbabd199336785405ced501064 0 1564296775000 7 connected
fe00ec489c2446232de3770fea1a9a7717079b79 192.168.199.130:7001@17001 master - 0 1564296774000 2 connected 5462-10922
00597cbc4c94eabbabd199336785405ced501064 192.168.199.130:7003@17003 master - 0 1564296775000 7 connected 5461
0e9f93246cbd0de72f3f216ba207c055ce36b51b 192.168.199.130:7002@17002 master - 0 1564296774571 3 connected 10923-16383
```
由上面最后的结果可以看到，原来属于master`192.168.199.130:7000`的slave节点`192.168.199.130:8000`被重新分配到了master`192.168.199.130:7003`下。重新分配后，每个master节点下都有一个slave节点。

## --cluster del-node
该命令用于从cluster中删除一个节点。
命令格式如下：
```
# redis-cli --cluster del-node host:port node_id
```
其中：
+ `host:port`
    cluster中任意一个节点信息
+ `node_id`
    本次要删除节点的ID

注意：
+ 当master节点中还有slots时，是不允许删除的（slave节点可以删除），执行删除命令将报如下错误信息：
    ```
    [root@localhost redis-cluster]# ./redis-cli --cluster del-node 192.168.199.130:7003 a5968aa575ee671d7c186c01d9a717ba3435bb4c
    >>> Removing node a5968aa575ee671d7c186c01d9a717ba3435bb4c from cluster 192.168.199.130:7003
    [ERR] Node 192.168.199.130:7003 is not empty! Reshard data away and try again.
    ```

### 示例
从cluster中删除ID为"b3bcfe2a97c640921f3daf8234e1882a44341a1a"的slave节点，命令如下：
```
[root@localhost redis-cluster]# ./redis-cli --cluster del-node 192.168.199.130:7000 b3bcfe2a97c640921f3daf8234e1882a44341a1a
>>> Removing node b3bcfe2a97c640921f3daf8234e1882a44341a1a from cluster 192.168.199.130:8003
>>> Sending CLUSTER FORGET messages to the cluster...
>>> SHUTDOWN the node.
```
此时，查看被删除slave节点的日志，内容如下所示：
```
2207:S 28 Jul 2019 13:32:04.647 # User requested shutdown...
2207:S 28 Jul 2019 13:32:04.647 * Calling fsync() on the AOF file.
2207:S 28 Jul 2019 13:32:04.647 * Removing the pid file.
2207:S 28 Jul 2019 13:32:04.648 # Redis is now ready to exit, bye bye...
```
说明节点从cluster中删除后，对应的redis进程也退出了，使用`ps -ef | grep redis`查看，确认对应的redis进程确实退出了。



## cluster failover
Sometimes it is useful to force a failover without actually causing any problem on a master. For example in order to upgrade the Redis process of one of the master nodes it is a good idea to failover it in order to turn it into a slave with minimal impact on availability.

Manual failovers are supported by Redis Cluster using the `CLUSTER FAILOVER` command, that __must be executed in one of the slaves of the master__ you want to failover.

Manual failovers are special and are safer compared to failovers resulting from actual master failures, since they occur in a way that avoid data loss in the process, by switching clients from the original master to the new master only when the system is sure that the new master processed all the replication stream from the old one.
命令格式：
```
> cluster failover [force|takeover]
```
注解：  
+ 该命令必须在slave节点上执行。执行成功后，对应的master节点将退出cluster，当前的slave将被提升为master。

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