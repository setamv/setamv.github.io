# 概述
tcpdump是一个用于截取网络分组，并输出分组内容的抓包工具，类似于window下的wireshark。tcpdump凭借强大的功能和灵活的截取策略，使其成为类UNIX系统下用于网络分析和问题排查的首选工具。 tcpdump存在于基本的Linux系统中，由于它需要将网络界面设置为混杂模式，普通用户不能正常执行，但具备root权限的用户可以直接执行它来获取网络上的信息。使用whereis命令查找是否安装tcpdump,并找到其命令的位置。

# 使用方法

# 举例
## 抓取访问redis（端口6379）的包
+ 命令
    `tcpdump -XX -nn -i enp2s0 tcp port 6379`
+ 参数说明
    * -XX
        When parsing and printing, in addition to printing the headers of each packet, print the data of each packet, including its link level header, in hex and ASCII.
        注意，这个选项不仅会打印16进制的抓包内容，还会打印ASCII格式的内容，便于阅读。
    * -i
        指定网络接口
    
+ 输出结果示例
    13:05:31.730727 IP (tos 0x0, ttl 64, id 27383, offset 0, flags [DF], proto TCP (6), length 73)
    192.168.130.230.6379 > 192.168.100.34.53949: tcp 33
        0x0000:  60da 836e d2b2 00e0 7024 fe70 0800 4500  `..n....p$.p..E.
        0x0010:  0049 6af7 4000 4006 675e c0a8 82e6 c0a8  .Ij.@.@.g^......
        0x0020:  6422 18eb d2bd 718b b176 a0f8 001d 5018  d"....q..v....P.
        0x0030:  00e5 e420 0000 2432 360d 0a68 7474 703a  ......$26..http:
        0x0040:  2f2f 3139 322e 3136 382e 3133 302e 3233  //192.168.130.23
        0x0050:  302f 7069 2f0d 0a                        0/pi/..


