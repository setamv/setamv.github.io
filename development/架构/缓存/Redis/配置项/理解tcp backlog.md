# 理解tcp backlog
在linux 2.2以前，backlog大小包括了半连接状态和全连接状态两种队列大小。
linux 2.2以后，分离为两个backlog来分别限制半连接SYN_RCVD状态的未完成连接队列大小跟全连接ESTABLISHED状态的已完成连接队列大小。互联网上常见的TCP SYN FLOOD恶意DOS攻击方式就是用/proc/sys/net/ipv4/tcp_max_syn_backlog来控制的，可参见《TCP洪水攻击（SYN Flood）的诊断和处理》。

在使用listen函数时，内核会根据传入参数的backlog跟系统配置参数/proc/sys/net/core/somaxconn中，二者取最小值，作为“ESTABLISHED状态之后，完成TCP连接，等待服务程序ACCEPT”的队列大小。在kernel 2.4.25之前，是写死在代码常量SOMAXCONN，默认值是128。在kernel 2.4.25之后，在配置文件/proc/sys/net/core/somaxconn (即 /etc/sysctl.conf 之内 )中可以修改。我稍微整理了流程图，如下:
![tcp-sync-queue-and-accept-queue-small.jpg](images/tcp-sync-queue-and-accept-queue-small.jpg)

在How TCP backlog works in linux一文中，作者给出了比较详细的分析：

第一种实现方式在底层维护一个由backlog指定大小的队列。服务端收到SYN后，返回一个SYN/ACK，并把连接放入队列中，此时这个连接的状态是SYN_RECEIVED。当客户端返回ACK后，此连接的状态变为ESTABLISHED。队列中只有ESTABLISHED状态的连接能够交由应用处理。第一种实现方式可以简单概括为：一个队列，两种状态。

第二种实现方式在底层维护一个SYN_RECEIVED队列和一个ESTABLISHED队列，当SYN_RECEIVED队列中的连接返回ACK后，将被移动到ESTABLISHED队列中。backlog指的是ESTABLISHED队列的大小。

传统的基于BSD的tcp实现第一种方式，在linux2.2之前，内核也实现第一种方式。当队列满了以后，服务端再收到SYN时，将不会返回SYN/ACK。比较优雅的处理方法就是不处理这条连接，不返回RST，让客户端重试。

在linux2.2后，选择第二种方式实现，SYN_RECEIVED队列的大小由proc/sys/net/ipv4/tcp_max_syn_backlog系统参数指定，ESTABLISHED队列由backlog和/proc/sys/net/core/somaxconn中较小的指定。

但是在windows server中，底层选择winsock API实现，backlog的定义是represents the maximum length of the queue of pending connections for the listener(这是一个比较模糊的定义……来源于BSD)，当队列满了后，将会返回RST。

if (ESTABLISHED is full) {SYN.req -> ESTABLISHED?}
考虑这样一种情况，当ESTABLISHED队列满了，此时收到一个连接的ACK，需要将此连接从SYN队列移到ESTABLISHED队列中，会发生什么？

linux底层的关键代码是:

listen_overflow:
if (!sysctl_tcp_abort_on_overflow) {
inet_rsk(req)->acked = 1;
return NULL;
}
除非系统的tcp_abort_on_overflow指定为1（将返回RST），否则底层将不会做任何事情……这是一种委婉的退让策略，在服务端处理不过来时，让客户端误以为ACK丢失，继续重新发送ACK。这样，当服务端的处理能力恢复时，这条连接又可以重新被移动到ESTABLISHED队列中去。