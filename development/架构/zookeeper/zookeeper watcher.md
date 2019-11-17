# ZooKeeper watcher

## ZooKeeper Watcher事件
ZooKeeper的Watcher事件包含KeeperState和EventType两个属性，如下所示：
| KeeperState       | EventType              | 触发条件                                       | 说明                                                            |
|-------------------|------------------------|------------------------------------------------|-----------------------------------------------------------------|
| SynchConnected(3) | None(-1)               | 客户端与服务器成功建立会话                     | 此时客户端和服务器处于连接状态                                  |
|                   | NodeCreated(1)         | Watcher监听的对应ZNode被创建                   |                                                                 |
|                   | NodeDeleted(2)         | Watcher监听到对应的ZNode被删除                 |                                                                 |
|                   | NodeDataChanged(3)     | Watcher监听到对应的ZNode的数据内容发生变化     |                                                                 |
|                   | NodeChildrenChanged(4) | Watcher监听的ZNode的子节点列表发生变更。注意： |                                                                 |
|                   |                        | ..1）子节点的数据变更不会触发该Watcher事件；   |                                                                 |
|                   |                        | ..2）孙子节点列表变更不会触发该Watcher事件；   |                                                                 |
| Disconnected(0)   | None(-1)               | 客户端与ZooKeeper服务器断开连接                | 此时客户端和服务器处于断开连接状态，但会话不一定失效，重连后仍然可以使用之前的会话                              |
| Expired(-112)     | None(-1)               | 会话超时                                       | 此时客户端会话失效，通常同时也会收到SessionExpiredException异常 |
| AuthFailed(4)     | None(-1)               | 通常有两种情况：                               | 通常同时也会收到AuthFailedException异常                         |
|                   |                        | ..1）使用错误的scheme进行权限检查；            |                                                                 |
|                   |                        | ..2）SASL权限检查失败                          |                                                                 |


## ZooKeeper Watcher事件触发
ZooKeeper可以通过三个方法注册Watcher事件的监听，这三个方法分别为：exists(nodePath)、getData(nodePath)、getChildren(nodePath)，其中参数nodePath为节点路径，以下以targetNode指代路径为nodePath的节点。当监听的节点（或其子节点）发生某种变更时，注册的Watcher将被触发。
下表列出这三个方法注册的Watcher事件在何时会被触发：

| 方法                  | 触发的事件             | 触发的时机                     |
|-----------------------|------------------------|--------------------------------|
| exists(nodePath)      | NodeCreated(1)         | targetNode被创建时             |
|                       | NodeDataChanged(3)     | targetNode的数据发生变更时。数据发生变更也包括节点的数据版本变化 |
|                       | NodeDeleted(2)         | targetNode被删除时             |
| getData(nodePath)     | NodeDataChanged(3)     | targetNode的数据内容发生变更时 |                                                                                   
|                       | NodeDeleted(2)         | targetNode被删除时             |
| getChildren(nodePath) | NodeChildrenChanged(4) | 创建或删除targetNode的子节点时 |
|                       | NodeDeleted(2)         | targetNode被删除时             |

ZooKeeper注册的Watcher事件在被触发后，将移除Watcher事件的注册，如果需要下次再被触发，需要重新注册。 
  
针对*NodeDataChanged*，数据发生变更不仅仅是节点的数据内容变更了，也包括节点的数据版本发生变化。例如，节点的当前数据为"abc"，如果通过setData设置节点的数据为"abc"，尽管设置前后的数据相同，但是设置完成后，节点的数据版本将发生变化，所以也会触发*NodeDataChanged(3)*

## ZooKeeper Watcher事件
ZooKeeper客户端收到Watcher事件时，会按收到Watcher事件的顺序同步消费。