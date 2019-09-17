# ZooKeeper

## ZooKeeper数据模型
ZooKeeper由一个树状的节点结构组成，每个节点被称为*znode*。就像文件系统中的文件夹和文件一样，ZooKeeper中每个节点由规范的斜杠分隔的路径表示，子节点的路径为父节点路径+"/"+子节点的名称。如："/parent"为父节点路径，"/parent/child1"为子节点路径。
*znode*的路径不支持相对路径，只支持绝对路径，每个节点都可以包含数据。
*znode*的路径可以包含除以下字符以外的所有unicode字符：
+ The null character (\u0000) 
+ The following characters can't be used because they don't display well, or render in confusing ways: \u0001 - \u001F and \u007F
+ \u009F.
+ The following characters are not allowed: \ud800 - uF8FF, \uFFF0 - uFFFF.
+ 字符"."可以为节点名称的一部分，但是不能单独作为一个节点的名称，如："/zk/my.node"是可以的，但是"/zk/./mynode"是不允许的。