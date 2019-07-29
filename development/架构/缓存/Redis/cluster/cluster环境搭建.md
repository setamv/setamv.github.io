# cluster 设置
当前文档以示例讲述如何配置一个redis cluster，该cluster将拥有3个master节点，3个slave节点。每个master节点拥有一个slave节点。最终的配置情况如下所示：
| 节点类型 | IP:PORT              | 所属master           |
|----------|----------------------|----------------------|
| master   | 192.168.199.130:7000 |                      |
| master   | 192.168.199.130:7001 |                      |
| master   | 192.168.199.130:7002 |                      |
| slave    | 192.168.199.130:8000 | 192.168.199.130:7000 |
| slave    | 192.168.199.130:8001 | 192.168.199.130:7001 |
| slave    | 192.168.199.130:8002 | 192.168.199.130:7002 |

## 设置步骤
1. 创建如下目录结构（其中，`-`开头的为文件；`+`开头的为文件夹：
    ```
    + /opt/redis-cluster
        - redis-server
        - redis-cli
        + node-7000
            - redis.conf
        + node-7001
            - redis.conf
        + node-7002
            - redis.conf
        + node-8000
            - redis.conf
        + node-8001
            - redis.conf
        + node-8002
            - redis.conf
    ```
    其中:
    + redis-server和redis-cli文件是从`redis安装目录/src`下拷贝过来的同名文件
    + redis.conf中的内容如下所示：
        ```
        bind 192.168.199.130 127.0.0.1
        port 7000
        daemonize yes
        logfile /var/log/redis/redis-7000.log
        cluster-enabled yes
        cluster-config-file nodes-7000.conf
        cluster-announce-ip 192.168.199.130
        cluster-node-timeout 5000
        appendonly yes
        ```
        以上是节点`192.168.199.130:7000`的配置，其他节点只需要将端口号改为对应的即可。
2. 启动所有的节点
    ```
    # cd /opt/redis-cluster
    # ./redis-server ./node-7000/redis.conf
    # ./redis-server ./node-7001/redis.conf
    # ./redis-server ./node-7002/redis.conf
    # ./redis-server ./node-8000/redis.conf
    # ./redis-server ./node-8001/redis.conf
    # ./redis-server ./node-8002/redis.conf
    ```
3. 使用命令`cluster create`将6个节点创建为一个cluster，并指定每个master拥有一个slave
    ```
    # ./redis-cli --cluster create 192.168.199.130:7000 192.168.199.130:7001 192.168.199.130:7002 192.168.199.130:8002 192.168.199.130:8000 192.168.199.130:8001 --cluster-replicas 1
    [注解]以下都是命令执行后的输出内容。
    >>> Performing hash slots allocation on 6 nodes...
    Master[0] -> Slots 0 - 5460
    Master[1] -> Slots 5461 - 10922
    Master[2] -> Slots 10923 - 16383
    Adding replica 192.168.199.130:8000 to 192.168.199.130:7000
    Adding replica 192.168.199.130:8001 to 192.168.199.130:7001
    Adding replica 192.168.199.130:8002 to 192.168.199.130:7002
    >>> Trying to optimize slaves allocation for anti-affinity
    [WARNING] Some slaves are in the same host as their master
    M: b0848720c2c3e60a1f8a80751ebb64f722297e2b 192.168.199.130:7000
    slots:[0-5460] (5461 slots) master
    M: e0ac3e0c44145cedc41e7dc4f82138da43ab348b 192.168.199.130:7001
    slots:[5461-10922] (5462 slots) master
    M: 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5 192.168.199.130:7002
    slots:[10923-16383] (5461 slots) master
    S: f5660c1a0bb8a0feb1f92fa006a352245f44d7c6 192.168.199.130:8002
    replicates 6d989010ae4a50b8bc4ffd8b0305f48e743a0ec5
    S: 2f26800902409c19fa1b93437b9140cfe01367eb 192.168.199.130:8000
    replicates b0848720c2c3e60a1f8a80751ebb64f722297e2b
    S: 2f3354d8dfec8709070e85bcf0b6a5927780d10b 192.168.199.130:8001
    replicates e0ac3e0c44145cedc41e7dc4f82138da43ab348b
    Can I set the above configuration? (type 'yes' to accept): yes
    >>> Nodes configuration updated
    >>> Assign a different config epoch to each node
    >>> Sending CLUSTER MEET messages to join the cluster
    Waiting for the cluster to join
    ..
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
    ```
    从以上的输出日志可以看到，3个slave节点`192.168.199.130:8000`、`192.168.199.130:8001`、`192.168.199.130:8002`分别属于master节点`192.168.199.130:7000`、`192.168.199.130:7001`、`192.168.199.130:7002`
