[Back](index.md)

# Schedule Jobs In Linux

# Introduction

This article will introduce ways of sheduling jobs in linux.

# Content Catalog <a id='≡'>≡</a>

- [Linux定制执行计划概述](#OOSJIL)
- [`at` command制定计划任务](#AC)
- [Cron计划任务](#CT)
    + [使用`crontab`命令编辑crontab文件](#CT-TAG1)
    + [crontab文件](#CT-TAG2)
        * [crontab文件的格式](#CT-TAG3)
        * [crontab命令格式示例](#CT-TAG3-1)
- [Anacron计划任务](#ACT)
    + [anacrontab配置文件](#ACT-CF)
    + [anacron程序](#ACT-P)
- [各种定制计划任务方式的之间区别](#D)
    + [cron和anacron的区别](#D-CA)


# Content

## Linux定制执行计划概述  <a id='OOSJIL'>[≡](#≡)</a>

Linux里面定制执行计划的方式有好几种:

- `at`命令
    `at`命令适用于制定在某个时间点一次性执行的任务。

- Cron
    Cron适用于制定重复性执行的任务，重复的方式可以非常灵活的指定。

- Anacron
    Anacron适用于制定重复性执行的任务，重复的最小单位为天。

##`at` command制定计划任务 <a id='AC'>[≡](#≡)</a>

Please refer to [Scheduling a job using the `at` command](../../reading-notes/linux/Linux Command Line and Shell Scripting Bible/Script Control.md#RLC-SAJUTAC)

## Cron计划任务 <a id='CT'>[≡](#≡)</a>

使用Cron定制计划任务涉及到几个方面：

1. crontab文件
    crontab文件保存了已创建的计划任务列表。
    该文件位于/var/spool/cron目录下，每个用户对应的crontab文件名称与用户名相同。

2. cron deamon服务(crond)
    cron deamon服务是执行crontab文件中的计划任务的deamon程序。该程序每分钟检查一次crontab文件中的计划任务信息，查看是否有需要执行的计划任务，如果有，就执行计划任务。

3. `crontab`命令
    `crontab`命令用于编辑crontab文件信息，包括添加一条计划任务、删除一条计划任务等。

Cron计划任务总体来说就是：先使用crontab命令将需要制定的计划任务写入crontab文件中，然后cron deamon服务（该服务的进程名称为crond）定时检查crontab文件以查看是否有满足条件可执行的任务了。

cron deamon服务（即crond进程）在执行任务计划时，其本身所产生的日志默认是保存到 /var/log/目录下，文件名为"cron-日期"的格式，如"cron-20170412"。


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
`* * * * * cat %"hello, cat"`      
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


## Anacron计划任务 <a id='ACT'>[≡](#≡)</a>

使用Anacron定制计划任务涉及到几个方面：

1. anacrontab配置文件
    anacrontab配置文件保存了已制定的计划任务列表。
    该文件位置是/etc/anacrontab。

2. anacron程序
    anacron程序用于周期性的执行anacrontab配置文件中的计划任务。

Anacron计划任务简要的说就是：先将计划任务信息编辑到anacrontab配置文件中，然后anacron程序将周期性的执行anacrontab配置文件中的计划任务。

### anacrontab配置文件 <a id='ACT-CF'>[≡](#≡)</a>

anacrontab配置文件保存了anacron程序执行的计划任务信息。该配置文件包含三种类型行内容：计划任务信息行；环境参数设置行；空行。

- 计划任务信息行

    每一行计划任务信息都包含如下的格式：   
    ```
    'period in days'  'delay in minutes'  'job-identifier'   command
    ```

    + 'period in days' 
        用于指定计划任务执行的频率，单位为"天"，表示多少天执行1次。   
        其值可以是一个正整数，如: '1'表示1天执行一次；'30'表示30天执行一次；
        其值也可以是一个宏(macro)，包括：@daily, @weekly, @monthly，分别表示1天执行一次，7执行一次，30执行一次。
        **_注意_**：   
        1. **正确理解频率**。比如当频率设置为每5天执行一次，则anacron程序会在运行时，检查当前时间往前数5天以内，该计划任务是否执行过，如果还未执行过，则执行该任务，否则，不执行。
        2. **正确理解@weekly和@monthly**。 这里的@weekly和@monthly并非日历上的周和月，只是数字7和30的另一种语义表达方式，例如：一个 @monthly 的计划任务，anacron程序在运行时，会检查该计划任务在30天以内是否执行过，如果还未执行过，则执行该任务，否则，不执行。这里并不是日历上的每个月份执行一次的意思。
    + 'delay in minutes'
        用于指定计划任务在被执行之前延迟的时间，单位为"分钟"。
        **_注意_**：这里的延迟有两层意思：
        1. 一个是anacron程序启动以后该将该执行计划标记为ready状态，等待执行的时间；
        2. 其二是在anacron程序启动时指定了`-s`参数（表示多个执行计划按顺序执行）的情况下，如果第一个任务Job1的延时为5分钟，Job2的延时为6分钟，但是Job1需要执行2分钟才能完成任务，等Job1执行完成时，Job2的第一个6分钟延时已过，Job2必须等到第二个6分钟延时（就是第12分钟）再检查Job1是否完成，如果Job1已经完成，这时候Job2才开始执行。
    + 'job-identifier'
        用于为计划任务指定一个唯一的名称，主要用于日志记录。
    + 'command'
        指定计划任务执行的命令或脚本（如果是脚本必须写全路径）

    示例：

    + `1 5 job1 command` 表示每天执行1次命令`command`，且延迟时间为5分钟。
    + `@monthly 0 job2 command` 表示每30天

- 环境参数设置行

    环境参数设置行的格式如下所示：   
    ```
    VAR=VALUE
    ```
    其中，VAR为环境参数名称；VALUE为参数值。这里有几个需要注意的地方：  

    + 对空格的处理：如果VAR两边有空格，将被忽略；如果VALUE两边有空格，这些空格将作为参数值的一部分。如：` HOME  =  /home/setamv   `中，参数名称为"HOME"，参数值为"  /home/setamv   "
    + 参数作用域：从设置参数的下一行开始一直到文件的末尾或该参数的下一个赋值行。
    + 'START_HOURS_RANGE' 环境参数用于指定计划任务可以执行的时间段，比如：`START_HOURS_RANGE=6-8`，则计划任务只会在一天的6点到8点之间执行，如果这段时间刚好系统宕机了，这些计划任务在这一天就不会被执行了。

- 空行
    
    空行可以是没有任何内容（或只有空白字符，如空格、Tab）的行，或以'#'开始的注释内容，'#'之前可以有空白字符。

注意，如果一行内容太长，可以将其分成两行，并在第一行的末尾加上'\'字符。

### anacron程序 <a id='ACT-P'>[≡](#≡)</a>

anacron程序用于周期性的执行anacrontab配置文件中的计划任务。这些计划任务的重复频率都是以天为单位。

anacron程序在启动时会读取/etc/anacrontab配置文件中的计划任务列表，并检查每一个计划任务在前n天时间内是否执行过（这里的n是计划任务的执行频率），如果没有执行过，就执行该计划任务，否则跳过该计划任务，如果计划任务指定了延时参数，anacron程序会等待延时时间之后才开始执行计划任务（请参见[anacrontab配置文件](。

anacron程序在执行完每一个计划任务后，都会将该计划任务的执行日期信息保存到/var/spool/anacron目录下（一般是生成一个名称与任务计划的identifier相同的空文件），等到下次启动时，anacron程序就将该信息作为计划任务的上一次执行日期，从而推算出当前是否需要再次执行该计划任务。在比较历史执行时间和计划任务时，anacron程序使用计划任务的

anacron程序在执行完所有计划任务后会退出。

如果计划任务在执行期间有信息输出到标准输出或标准错误输出，anacron程序将通过邮件的方式将这些信息发送给执行anacron程序的用户（一般是root用户）。也可以通过`MAILTO`环境变量进行修改。

anacron程序会为每个启动的计划任务打开最多2个文件描述符，如果并发的计划任务很多，打开的文件描述符将有可能达到上限。可以通过`$ echo $(($(ulimit -n) / 2))`来查看当前可打开的文件描述符个数。

anacron程序启动时可以指定一些选项，如下表所示：  

|  **Option** |                            **Description**                            |
|-------------|-----------------------------------------------------------------------|
| -f          | Forces execution of all jobs, ignoring any timestamps.                |
|             | ？没搞明白`-f`选项的真正作用，测试过几次，                            |
|             | 都是生成了/var/spool/anacron日期文件，但没有发送输出信息到邮箱        |
| -u          | Updates the timestamps of all jobs to the current date,               |
|             | but does not run any.                                                 |
| -s          | Serializes execution of jobs.  Anacron does not start                 |
|             | a new job before the previous one finished.                           |
| -n          | Runs jobs immediately and ignores the specified delays                |
|             | in the /etc/anacrontab file.  This options implies -s.                |
| -d          | Does not fork Anacron to the background                               |
| -T          | Anacrontab testing. Tests the /etc/anacrontab configuration file      |
|             | for validity. If there is an error in the file, it is shown on the    |
|             | standard output and Anacron returns the value of 1.                   |
|             | Valid anacrontabs return the value of 0.                              |
| -S spooldir | Uses the specified spooldir to store timestamps in.                   |
|             | This option is required for users who wish to run anacron themselves. |
|             |                                                                       |


#### anacron程序遗留问题

1. man anacron帮助中的这句话该如何理解？

    Anacron only considers jobs whose identifier, as specified in anacrontab(5), matches any of the job  command-line  arguments.   The job  command-line  arguments  can  be  represented by shell wildcard patterns (be sure to protect them from your shell with adequate quoting).  Specifying no job command-line arguments is equivalent to specifying "*"  (that is, all jobs are considered by Anacron).

2. anacron程序产生的日志到哪里去看？帮助文档上说的是发送到"syslogd"或者"rsyslogd"了，该如何查看呢？




## 各种定制计划任务方式的之间区别   <a id='D'>[≡](#≡)</a>

### cron和anacron的区别  <a id='D-CA'>[≡](#≡)</a>

cron是假定系统在一天24小时连续运行的情况下，进行计划任务的执行，如果其中系统宕机了，在宕机期间本来要执行的计划任务将被错过。

而anacron并未假定系统会一天24小时连续的运行，因为anacron的计划的最小重复粒度为天，所以只要机器在一天中的任何时间运行，计划都可以得到执行。这类似于指定任务在一段时间内运行一次，，但并不强制要求任务必须在该段时间内的某个特定时间点执行，而cron无法做到这一点。
