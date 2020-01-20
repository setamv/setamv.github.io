# zkCli客户端命令

## create命令
ZooKeeper的*create*命令用于创建*ZNode*节点
#### 命令格式
`# create [-s] [-e] [-c] [-t ttl] path [data] [acl]`
+ `-s`：将创建顺序节点
+ `-e`：将创建ephemeral类型的节点，即客户端会话断开后将自动删除的节点
+ `-c`：将创建容器节点（container node），容器节点在所有子节点都被删除后，将被自动删除
+ `-t ttl`: 创建有过期时间的节点，超时被删除。ZooKeeper服务器默认情况下未开启ttl模式，必须在配置项中增加:TODO

## delete命令
ZooKeeper的*delete*命令用于删除*ZNode*节点
#### 命令格式
`# delete [-v version] path`
其中：
+ `-v version`：指定版本号删除。如果指定的版本号与被删除节点的版本号(dataVersion)不一致，将删除失败。该功能实现了类似乐观锁的特性。
#### 注意事项
+ 如果被删除节点还有子节点，是不能删除的。此时可以使用`deleteall`命令删除。

## deleteall命令
ZooKeeper的*deleteall*命令用于删除指定的*ZNode*节点以及该节点下的所有子孙节点
#### 命令格式
`# deleteall path`

## setquota命令
ZooKeeper的*setquota*命令用于设置节点的配额（即最大容量），包括两方面：1）节点数据的配额；2）节点的子节点数配额。当节点的配额被超过时只会打印WARN级别的日志提醒而不是直接让超出配额的操作失败。
#### 命令格式
`# setquota -n|-b val path`
其中：
+ `-n|-b val`: 表示设置配额的类型。`-n`表示设置当前节点的子节点数配额；`-b`表示设置当前节点数据的配额；
    - `-n val`用于设置子节点数的配额。这里的子节点包括所有的子孙节点，并且配额数包含了当前节点自身。例如`-n 3`表示当前节点下最多可以创建2个子孙节点，超过2个就会打印WARN级别的日志，如下所示：
        ```
        2019-11-15 18:02:52,458 [myid:] - WARN  [SyncThread:0:DataTree@340] - Quota exceeded: /qu count=4 limit=3
        2019-11-15 18:02:57,594 [myid:] - WARN  [SyncThread:0:DataTree@340] - Quota exceeded: /qu count=5 limit=3
        ```
    - `-b val`用于设置节点数据配额。`val`为配额的字节数。当节点的数据超过配额数时，将打印WARNING日志。
#### 注意事项
1. 不能对一个节点同时设置`-n`和`-b`，即即限制节点数据的配额又限制节点的子节点数配额。
    例如，第一次设置配额`# setquota -n 3 /qu`，再次设置配额`# setquota -b 3 /qu`时将报错："org.apache.commons.cli.AlreadySelectedException: The option 'b' was specified but an option from this group has already been selected: 'n'"

## delquota命令
ZooKeeper的*delquota*命令删除节点的配额设置
#### 命令格式
`# delquota -n|-b path`



## stat命令
ZooKeeper的*stat*命令用于查看*ZNode*的统计信息
#### 命令格式
`# stat node-path`
其中，*node-path*为*ZNode*节点的路径
#### 示例
```
# zkCli.sh -server localhost:2186 stat /family
ZOOBINDIR = /opt/software/apache-zookeeper-3.5.5-bin/bin
ZOOCFGDIR = 
ZOOCFG = 
ZOO_LOG_DIR = 
ZOO_LOG4j_PROP = 
Connecting to localhost:2186

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
cZxid = 0x100000002
ctime = Wed Sep 04 07:30:08 CST 2019
mZxid = 0x100000002
mtime = Wed Sep 04 07:30:08 CST 2019
pZxid = 0x100000002
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 13
numChildren = 0
```    

## conf命令
ZooKeeper的*conf*命令用于输出ZooKeeper服务器运行时使用的基本配置信息，包括*clientPort*、*dataDir*、*tickTime*等。
*conf*命令会根据ZooKeeper当前的运行模式来决定输出的信息，如果是单击模式，就不会输出诸如*initLimit*、*syncLimit*、*electionAlg*等只有在集群模式下才有效的设置

#### 命令格式
`# conf`

### 示例
```

```


