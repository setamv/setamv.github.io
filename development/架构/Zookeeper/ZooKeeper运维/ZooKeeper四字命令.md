# ZooKeeper运维

## 四字命令
ZooKeeper有一些命令可以用来查看服务器的状态和配置信息等，这些命令被称为四字命令，因为它们都是由4个英文字母组成。比如：`stat`、`conf`等。
ZooKeeper对四字命令有一个whitelist，只有在whitelist中的四字命令才会被允许执行，否则将直接返回类似"stat is not executed because it is not in the whitelist"的错误信息。

### 设置四字命令的whitelist
要设置四字命令到whitelist中，可以在启动ZooKeeper的脚本（如zkServer.sh）中的以下位置添加如下内容：
```
else
    echo "JMX disabled by user request" >&2
    ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
fi
# 在这里添加四字命令的whitelist，其值可以是"*"或一个用逗号分隔的四字命令列表，如"stat,conf"，"*"表示所有命令都加入whitelist中。
ZOOMAIN="-Dzookeeper.4lw.commands.whitelist=* ${ZOOMAIN}"
```

#### 通过ZooKeeper源码分析四字命令的whitelist
ZooKeeper服务器对四字命令的检查，源码位于`org.apache.zookeeper.server.NettyServerCnxn#checkFourLetterWord`，代码如下：
```
private boolean checkFourLetterWord(final Channel channel, ByteBuf message, final int len) {
    // We take advantage of the limited size of the length to look
    // for cmds. They are all 4-bytes which fits inside of an int
    if (!FourLetterCommands.isKnown(len)) {
        return false;
    }

    String cmd = FourLetterCommands.getCommandString(len);

    ....

    // ZOOKEEPER-2693: don't execute 4lw if it's not enabled.
    if (!FourLetterCommands.isEnabled(cmd)) {
        LOG.debug("Command {} is not executed because it is not in the whitelist.", cmd);
        NopCommand nopCmd = new NopCommand(
            pwriter,
            this,
            cmd + " is not executed because it is not in the whitelist.");
        nopCmd.start();
        return true;
    }
    ....
}    
```
而`org.apache.zookeeper.server.command.FourLetterCommands.isEnabled(String command)`的源码如下所示：
```
private static final String ZOOKEEPER_4LW_COMMANDS_WHITELIST = "zookeeper.4lw.commands.whitelist";

public static synchronized boolean isEnabled(String command) {
    if (whiteListInitialized) {
        return whiteListedCommands.contains(command);
    }

    // 根据下面这几行代码，便可以看出，这些４字指令，是配置在ＶＭ变量内的，而key值是ZOOKEEPER_4LW_COMMANDS_WHITELIST，其实它的常量定义在最上面，这个常量的值为：zookeeper.4lw.commands.whitelist
    //看下面的代码便知道，４字指令的格式是用逗号（,）分隔，也可以直接用*，则会把cmd2String里已经缓存的所有指令，迭代的添加到whiteListedCommands这个白名单里
    String commands = System.getProperty(ZOOKEEPER_4LW_COMMANDS_WHITELIST);
    if (commands != null) {
        String[] list = commands.split(",");
        for (String cmd : list) {
            if (cmd.trim().equals("*")) {
                for (Map.Entry<Integer, String> entry : cmd2String.entrySet()) {
                    whiteListedCommands.add(entry.getValue());
                }
                break;
            }
            if (!cmd.trim().isEmpty()) {
                whiteListedCommands.add(cmd.trim());
            }
        }
    }

    ...
    whiteListInitialized = true;
    return whiteListedCommands.contains(command);
}
```
从上面可以，ZooKeeper是通过系统属性"zookeeper.4lw.commands.whitelist"来对whitelist进行设置的。其值可以是一个用逗号分隔的四字命令列表，也可以是"*"，如果是"*"，表示添加所有四字指令到whitelist中。

### 执行四字命令
有两种方式执行四字命令，其一是通过`telnet`命令执行；其二是通过`nc`命令执行。

#### 使用telnet执行四字命令
首先使用telnet客户端登录ZooKeeper的对外服务端口（默认为2181），然后直接输入四字命令即可。如下所示：
```
[root@192]# telnet localhost 2181
Trying ::1...
Connected to localhost.
Escape character is '^]'.
stat            【说明】这是输入的四字命令，下面是四字命令的返回结果
Zookeeper version: 3.5.5-390fe37ea45dee01bf87dc1c042b5e3dcce88653, built on 05/03/2019 12:07 GMT
Clients:
 /0:0:0:0:0:0:0:1:45372[0](queued=0,recved=1,sent=0)

Latency min/avg/max: 0/0/0
Received: 2
Sent: 1
Connections: 1
Outstanding: 0
Zxid: 0x233
Mode: standalone
Node count: 53
Connection closed by foreign host.
```

#### 使用nc执行四字命令
nc是netcat的简写，有着网络界的瑞士军刀美誉。因为它短小精悍、功能实用，被设计为一个简单、可靠的网络工具。linux系统没有自带该工具，需要自行安装。
使用nc执行四字命令的方法如下：
```
# echo conf | nc localhost 2181
clientPort=2181
secureClientPort=-1
dataDir=/opt/workspace/zookeeper/data/version-2
dataDirSize=268453033
dataLogDir=/opt/workspace/zookeeper/data/version-2
dataLogSize=268453033
tickTime=2000
maxClientCnxns=60
minSessionTimeout=4000
maxSessionTimeout=40000
serverId=0
```