# 单个服务器上启动zookeeper集群
如果只是用于测试和试验，可以在单台服务器上启动zookeeper的集群。
下面描述在单台服务器上启动3个节点的zookeeper集群。假设已经将zookeeper的bin目录加入了环境变量（因此可以在任意目录直接执行zkServer.sh和zkCli.sh命令）
文件目录结构如下所示（加号开始的表示文件目录，-号开始的表示文件）：
```
+ /opt/workspace/zookeeper
    + server-2666
        + data
            - myid
        - log4j.properties
        - zoo.cfg
    + server-2777        
        + data
            - myid
        - log4j.properties
        - zoo.cfg
    + server-2888
        + data
            - myid
        - log4j.properties
        - zoo.cfg
```
其中：
+ log4j.properties文件从zookeeper安装目录的conf/log4j.properties拷贝过来
    拷贝过来的日志文件需要进行部分修改，如下所示：
    ```
    zookeeper.root.logger=INFO, ROLLINGFILE             [注解]此处修改为ROLLINGFILE
    ...
    log4j.appender.ROLLINGFILE=org.apache.log4j.RollingFileAppender
    log4j.appender.ROLLINGFILE.Threshold=${zookeeper.log.threshold}
    log4j.appender.ROLLINGFILE.File=/var/log/zookeeper/server_2666.log          [注解]此处需要修改为日志文件的路径，并创建目录。不同集群节点的日志文件名称不同
    log4j.appender.ROLLINGFILE.MaxFileSize=${zookeeper.log.maxfilesize}
    ...
    ```
+ server-2666/zoo.cfg的内容
    ```
    tickTime=2000
    dataDir=/opt/workspace/zookeeper/server-2666/data
    clientPort=2186
    initLimit=5
    syncLimit=2
    server.1=localhost:2666:3666
    server.2=localhost:2777:3777
    server.3=localhost:2888:3888
    admin.serverPort=8080
    ```
+ server-2777/zoo.cfg的内容
    ```
    tickTime=2000
    dataDir=/opt/workspace/zookeeper/server-2777/data
    clientPort=2187
    initLimit=5
    syncLimit=2
    server.1=localhost:2666:3666
    server.2=localhost:2777:3777
    server.3=localhost:2888:3888
    admin.serverPort=8081
    ```
+ server-2888/zoo.cfg的内容
    ```
    tickTime=2000
    dataDir=/opt/workspace/zookeeper/server-2888/data
    clientPort=2188
    initLimit=5
    syncLimit=2
    server.1=localhost:2666:3666
    server.2=localhost:2777:3777
    server.3=localhost:2888:3888
    admin.serverPort=8082
    ```
+ myid文件的内容
    每个节点的data目录下必须创建文件"myid"，内容为节点的id。对应zoo.cfg中"server.n"中的n
+ 启动集群
    ```
    # cd /opt/workspace/zookeeper
    # zkServer.sh --config ./server-2666 start
    ZOOBINDIR = /opt/software/apache-zookeeper-3.5.5-bin/bin
    ZooKeeper JMX enabled by default
    Using config: ./server-2666/zoo.cfg
    Starting zookeeper ... STARTED

    # zkServer.sh --config ./server-2777 start
    ZOOBINDIR = /opt/software/apache-zookeeper-3.5.5-bin/bin
    ZooKeeper JMX enabled by default
    Using config: ./server-2777/zoo.cfg
    Starting zookeeper ... STARTED

    # zkServer.sh --config ./server-2888 start
    ZOOBINDIR = /opt/software/apache-zookeeper-3.5.5-bin/bin
    ZooKeeper JMX enabled by default
    Using config: ./server-2888/zoo.cfg
    Starting zookeeper ... STARTED

    # zkCli.sh -server localhost:2186
    Connecting to localhost:2186
    Welcome to ZooKeeper!
    JLine support is enabled

    WATCHER::

    WatchedEvent state:SyncConnected type:None path:null
    [zk: localhost:2186(CONNECTED) 0] ls /
    [zookeeper]
    ```