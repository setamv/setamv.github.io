[Back](index.md)

# Schedule Jobs In Linux

# Introduction

This article will introduce ways of sheduling jobs in linux.

# Content Catalog <a id='≡'>≡</a>

- [Overview of Scheduling Jobs in Linux](#OOSJIL)
- [`at` command制定计划任务](#AC)
- [Cron计划任务](#CT)
    + [使用`crontab`命令编辑crontab文件](#CT-TAG1)
    + [crontab文件](#CT-TAG2)
        * [crontab文件的格式](#CT-TAG3)
        * [crontab命令格式示例](#CT-TAG3-1)


# Content

## Overview of Scheduling Jobs in Linux <a id='OOSJIL'>[≡](#≡)</a>

There are multiple ways of scheduling jobs in linux:

- `at` command制定计划任务
    `at`命令适用于schedule一次性执行的任务。

- Use cron table
    Cron table适用于schedule重复性执行的任务，重复的方式可以非常灵活的指定。

##`at` command制定计划任务 <a id='AC'>[≡](#≡)</a>

Please refer to [Scheduling a job using the `at` command](../../reading-notes/linux/Linux Command Line and Shell Scripting Bible/Script Control.md#RLC-SAJUTAC)

## Cron计划任务 <a id='CT'>[≡](#≡)</a>

使用Cron定制计划任务涉及到几个方面：

1. crontab文件
    crontab文件保存了已创建的计划任务列表。
    该文件位于/var/spool/cron目录下，每个用户对应的cron table文件名称与用户名相同。

2. cron deamon服务
    cron deamon服务是执行crontab文件中的计划任务的deamon程序。该程序每分钟检查一次crontab文件中的计划任务信息，查看是否有需要执行的计划任务，如果有，就执行计划任务。

3. `crontab`命令
    `crontab`命令用于编辑crontab文件信息，包括添加一条计划任务、删除一条计划任务等。

Cron计划任务总体来说就是：先使用crontab命令将需要制定的计划任务写入crontab文件中，然后crond服务定时检查cron table文件以查看是否有满足条件可执行的任务了。


### 使用`crontab`命令编辑crontab文件 <a id='CT-TAG1'>[≡](#≡)</a>

官方文档请参考 `man cron`      

`crontab`命令位于/usr/bin/crontab，该命令的主要用于管理crontab文件内的计划任务信息。   

Linux系统上的每一个用户都拥有与自己用户名同名的crontab文件，用于保存该用户的计划任务信息。

系统在初始状态下时，/usr/bin/crontab目录下是没有任何crontab文件的，当第一次使用`crontab`命令时，该命令默认会创建一个与当前有效用户名同名的crontab文件。此处有效用户名是指，当为`crontab`命令指定了`-u user`选项时，有效用户名为`-u`选项后跟的用户名；如果没有`-u`选项，则为当前执行命令的用户。

`crontab`命令的格式为：        
```
crontab [-u user] file
crontab [-u user] [-l | -r | -e] [-i] [-s]
crontab -n [ hostname ]
crontab -c
```

`crontab`命令的选项如下表所示：  

| **Options** |                         **Description**                         |
|-------------|-----------------------------------------------------------------|
| -u user     | 用于指定用户名，即编辑该用户的crontab文件                       |
| -l          | 列出 _当前crontab文件_ 中的计划任务信息。                       |
| -r          | 删除 _当前crontab文件_ 中的所有计划任务信息                     |
| -e          | 编辑 _当前crontab文件_ 中的计划任务信息，默认使用vi编辑该文件 |

**_注释_**：上面Table中所有的 _当前crontab文件_ 是指：

1. 如果指定了-u选项，则当前crontab文件是指-u选项后跟的用户的crontab文件；
2. 如果未指定-u选项，则当前crontab文件是指当前的有效连接用户的crontab文件。



### crontab文件 <a id='CT-TAG2'>[≡](#≡)</a>

官方文档请参考 `man 5 crontab`

crontab文件中保存了cron计划任务信息，并且每个用户都有它自己的crontab文件。每个crontab文件中的计划任务都是在该crontab文件所属用户下执行的。

crontab文件的格式：  

1. Blank lines, leading spaces, and tabs are ignored。
2. Lines whose first non-white space character is a pound-sign (#)  are  comments。Note that comments are not allowed on the same line as cron commands。
3. An active line in a crontab is either an environment setting or a cron command.
4. An environment setting is of the form:
    `name = value`

    where the white spaces around the equal-sign (=) are optional, and any subsequent non-leading white spaces in value is  a  part  of the  value  assigned  to  name.   The  value string may be placed in quotes (single or double, but matching) to  preserve leading or trailing white spaces.

    Several environment variables are set up automatically by the cron daemon. `SHELL` is set to /bin/sh, and `LOGNAME`(即用户名)  and `HOME`  are set  from  the  /etc/passwd  line of the crontab´s owner.  `HOME` and `SHELL` can be overridden by settings in the crontab; `LOGNAME` can not.

    The `CRON_TZ` variable specifies the time zone specific for the cron table.  The user should enter a time according to the specified time zone into the table. The time used for writing into a log file is taken from the local time zone, where the daemon is run‐ning.

#### crontab文件的格式  <a id='CT-TAG3'>[≡](#≡)</a>

crontab文件的每一行执行计划信息都包含五个表示时间和日期的值域，后面跟一个可选的用户名（如果当前的crontab文件时系统crotab文件），最后再跟着执行计划要执行的命令，其格式如下：      
```
* * * * * [user] command
```
上面的五个星号，每一个都代表一个和时间或日期相关的值域，它们依次为如下表所示的值域：   

| **Field**    | **allowed values**                   |
| minute       | 0-59                                 |
| hour         | 0-23                                 |
| day of month | 1-31                                 |
| month        | 1-12                                 |
| day of week  | 0-7 (0 or 7 is Sunday, or use names) |

每一个和时间或日期相关的值域都可以包含:    

1. an asterisk (*)
    星号(*)表示一个位置所允许的所有值。比如，当一个星号在'minute'位置上时，表示一个小时中的任何一分钟。

2. a hyphen (-)
    连字符(-)表示一个范围，当两个数字用一个连字符(-)连接，表示从第一个数字到第二个数字的一个时间范围（两端的数字包括在内），如：当'1-10'在'minute'位置上时，表示一个小时中的0到10分钟之间。 
3. commas (,)
    逗号(,)表示一个列表，用于分割多个值或范围。如：当'1,5,10'在'minute'位置上时，表示一个小时中的第0分钟、第5分钟和第10分钟。而'0-4,8-12'在'minute'位置上表示一个小时中的第0到第4分钟以及第8到第12分钟。

4. step values (/)
    反斜杠(/)用于表示一个范围的步长。步长总是和一个范围(-)或星号(*)在一起使用，如：当'0-30/5'在'minute'位置上时，表示0-30分钟之间，每5分钟执行一次；当'*/5'在'minute'位置上时，表示0-59分钟之间，每5分钟执行一次。

'day of month'值和'day of week'值可以使用三字母缩写(如1月可以使用Feb、feb或FEB），并且字母大小写都可以。但是，这种方式不允许使用在范围(-)或列表(,)中，如不能使用'FEB-DEC'表示一月份到十二月份。

**_注意_**如果同时指定了'day of month'和'day of week'，则在任何一个条件满足的情况下，计划任务都会被执行。如："30 4 1,15 * 5"将会在每个月的1号和15号以及每周星期五的上午4:30执行。

`command`部分，表示执行计划将要执行的命令，它也可以是一个脚本文件，如果是脚本文件，这里需要将脚本文件的全路径都写上。

从`command`部分开始一直到一行结束或遇到第一个非转义的百分号(%)之间的所有内容，都将视为执行计划将要执行的命令内容。如果遇到一个非转义的百分号(%)，该百分号后面的所有内容都将作为命令的 标准输入(STDIN) 来对待。如：   
`* * * * * cat % "hello, cat"`      
百分号(%)后面的内容"hello, cat"都将作为标准输入(STDIN)被`cat`命令接收，其结果是`cat`命令将他们原样输出。

#### crontab命令格式示例  <a id='CT-TAG3-1'>[≡](#≡)</a>

- `0 6 * * *` 表示每天早上6点        
- `0 */2 * * *` 表示每2小时
- `0 23-7/2,8 * * *` 表示晚上11点到早上8点之间每两个小时，早上八点
- `0 11 4 * 1-3` 表示每个月的4号和每个礼拜的礼拜一到礼拜三的早上11点


### 遗留问题

在Linux的CRONTAB(5)的帮助页（使用`$ man 5 crontab`进行查看）最后，提到了一些crontab格式的扩展（EXTENSIONS），其中有一行是这样写的：
@yearly    :    Run once a year, ie.  "0 0 1 1 *".
我理解星号(*)代表当前值域的所有有效值的集合，那么"0 0 1 1 *"中的星号(*)应该匹配一个星期中的任何一天，所以整个"0 0 1 1 *"岂不是会匹配1月份的任何一个星期的星期1到星期日的0点0份吗？这跟 @yearly表示的 “Run once a year”不是矛盾吗？
因为一年才执行一次，不是很好验证，求知道的朋友帮忙解答一下，是不是我哪里理解错了。非常感谢。

