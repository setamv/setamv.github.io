# Linux目录的用途说明


## /bin
用户二进制文件所在的目录
包含二进制可执行文件。
在单用户模式下，你需要使用的常见Linux命令都位于此目录下。系统的所有用户使用的命令都设在这里。
例如：ps、ls、ping、grep、cp

## /sbin
系统二进制文件
就像/bin，/sbin同样也包含二进制可执行文件。
但是，在这个目录下的linux命令通常由系统管理员使用，对系统进行维护。例如：iptables、reboot、fdisk、ifconfig、swapon命令

## /etc
配置文件
包含所有程序所需的配置文件。
也包含了用于启动/停止单个程序的启动和关闭shell脚本。例如：/etc/resolv.conf、/etc/logrotate.conf

## /dev
设备文件
包含设备文件。
这些包括终端设备、USB或连接到系统的任何设备。例如：/dev/tty1、/dev/usbmon0

## /proc 
进程信息
包含系统进程的相关信息。
这是一个虚拟的文件系统，包含有关正在运行的进程的信息。例如：/proc/{pid}目录中包含的与特定pid相关的信息。
这是一个虚拟的文件系统，系统资源以文本信息形式存在。例如：/proc/uptime

## /var 
变量文件
var代表变量文件。
这个目录下可以找到内容可能增长的文件。
这包括 - 系统日志文件（/var/log）;包和数据库文件（/var/lib）;电子邮件（/var/mail）;打印队列（/var/spool）;锁文件（/var/lock）;多次重新启动需要的临时文件（/var/tmp）;

## /tmp 
临时文件
包含系统和用户创建的临时文件。
当系统重新启动时，这个目录下的文件都将被删除。

## /opt
这里主要存放那些可选的程序。你想尝试最新的firefox测试版吗?那就装到/opt目录下吧，这样，当你尝试完，想删掉firefox的时候，你就可 以直接删除它，而不影响系统其他任何设置。安装到/opt目录下的程序，它所有的数据、库文件等等都是放在同个目录下面。

举个例子：刚才装的测试版firefox，就可以装到/opt/firefox_beta目录下，/opt/firefox_beta目录下面就包含了运 行firefox所需要的所有文件、库、数据等等。要删除firefox的时候，你只需删除/opt/firefox_beta目录即可，非常简单。

## /usr 
用户程序
包含二进制文件、库文件、文档和二级程序的源代码。

### /usr/bin
包含用户程序的二进制文件。如果你在/bin中找不到用户二进制文件，到/usr/bin目录看看。例如：at、awk、cc、less、scp。

### /usr/sbin
包含系统管理员的二进制文件。如果你在/sbin中找不到系统二进制文件，到/usr/sbin目录看看。例如：atd、cron、sshd、useradd、userdel。

### /usr/lib
包含了/usr/bin和/usr/sbin用到的库。

### /usr/local
包含了从源安装的用户程序。例如，当你从源安装Apache，它会在/usr/local/apache2中。
这里主要存放那些手动安装的软件，即 不是通过“新立得”或apt-get安装的软件 。 它和/usr目录具有相类似的目录结构 。让软件包管理器来管理/usr目录，而把自定义的脚本(scripts)放到/usr/local目录下面，我想这应该是个不错的主意。
注意这个目录与/opt目录的区别