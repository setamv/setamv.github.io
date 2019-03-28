# 定制zookeeper日志文件的输出目录
在zookeeper安装目录的conf文件夹下，有个log4j.properties文件，用于对输出日志进行配置
但是，zookeeper默认的会将日志全部输出到conf目录下，名称为 zookeeper.out
即便修改了log4j.properties文件，也不起作用。

## 解决办法
1. 修改log4j.properties配置文件，指定日志输出目录 
    zookeeper.root.logger=INFO, ROLLINGFILE
    zookeeper.console.threshold=INFO
    zookeeper.log.dir=/var/logs/zookeeper
    zookeeper.log.file=zookeeper.log
    zookeeper.log.threshold=DEBUG
    zookeeper.tracelog.dir=/var/logs/zookeeper
    zookeeper.tracelog.file=zookeeper_trace.log

2. 修改bin/zkEvn.sh文件