# ZooKeeper的数据模型
ZooKeeper由一个树状的节点结构组成，每个节点被称为*znode*。就像文件系统中的文件夹和文件一样，ZooKeeper中每个节点由规范的斜杠分隔的路径表示，子节点的路径为父节点路径+"/"+子节点的名称。如："/parent"为父节点路径，"/parent/child1"为子节点路径。
*znode*的路径不支持相对路径，只支持绝对路径，每个节点都可以包含数据。
*znode*的路径必须满足一下规则（参见ZooKeeper源码中的org.apache.zookeeper.common.PathUtils#validatePath(String)）：
+ path != null
+ path.length != 0
+ path必须以"/"开始
+ path不能以"/"结尾
+ path中不能包含连续的两个"/"
+ path中不能包含"/../"或"/.."（代表相对路径）
+ path中不允许包含"/./"或以"/."结尾
+ 不能包含以下字符c：
    c > '\u0000' && c <= '\u001f'
                || c >= '\u007f' && c <= '\u009F'
                || c >= '\ud800' && c <= '\uf8ff'
                || c >= '\ufff0' && c <= '\uffff'

## 节点状态信息
ZooKeeper节点的状态信息可通过命令`# zkCli.sh -server localhost:2818 get -s /node-path`查看，其中，node-path为节点的路径。如下所示：
```
# zkCli.sh -server localhost:2186 get -s /parent
...

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
parent-node            
cZxid = 0x200000024
ctime = Mon Sep 09 06:52:16 CST 2019
mZxid = 0xf00000391
mtime = Sun Sep 22 07:51:51 CST 2019
pZxid = 0x200000027
cversion = 2
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 11
numChildren = 2
```
说明：
+ parent-node : 为节点的内容
+ cZxid : 为创建节点的事务ID
+ ctime : 创建节点的时间戳
+ mZxid : 节点内容最后一次被修改的事务ID
    mZxid在每一次节点内容被修改时都会更新
+ mtime : 节点内容最后一次被修改的时间戳
+ pZxid : 节点的子节点列表最后一次变更时的事务ID
    pZxid的更新规则：
    - 只有子节点列表发生变更时，pZxid才会更新，子节点内容变更不会更新pZxid的值。所以，pZxid = Max(子节点的cZxid)
    - 孙子节点列表发生变更不会更新pZxid的值
+ cversion : 子节点列表变更的版本号
    cversion的更新规则：
    - 新增、删除子节点时，节点的cversion会加1
    - 变更子节点的内容时，节点的cversion不会更新
    - 新增、删除孙子节点时，节点的cversion不会更新
+ dataVersion : 节点内容的版本号
    dataVersion的更新规则：
    - 节点内容每次变更时都会加1，注意，这里的变更仅仅指对节点的数据进行设置，即便设置后节点的内容未发生任何改变也是有效的。
        如：节点内容在变更前为"hello"，使用命令`set /node-path hello`设置节点的内容，这里新的内容和节点已有的内容一样，这种情况下cversion的值也会加1     
+ aclVersion : 节点ACL的版本号        
+ ephemeralOwner : 临时节点所属客户端会话的会话ID，非临时节点该值为0
  
