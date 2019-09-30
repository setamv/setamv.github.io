# zkCli客户端命令

## stat命令
ZooKeeper的*stat*命令用于查看*ZNode*的统计信息
#### 命令格式
`# zkCli.sh node-path`
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
