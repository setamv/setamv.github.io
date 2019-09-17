# strace
`strace`命令用于跟踪系统调用和信号（signals）。
`strace`命令通过运行指定的命令来拦截和记录指定进程发起的系统调用或指定进程收到的信号量。
进程发起的每个系统调用的名称、参数以及返回值都将可以打印出来或通过`-o`选项输出到指定的文件中。

## 命令格式
```
# strace [options] command args
```
其中，command为本次跟踪的命令，如启动一个应用的命令。args为命令command的参数。例如跟踪redis的启动：`# strace [options] ./src/redis-server ./redis.conf`

## 命令选项

+ `-f` 选项
    当指定了`-f`选项，`strace`将跟踪由当前进程通过fork(2)、vfork(2)、clone(2)创建的子进程。
    当同时指定`-p PID -f`时，将跟踪指定的进程以及该进程的所有线程。
+ `-t`、`-tt`、`-ttt`选项
    `-t`、`-tt`、`-ttt`选项用于指定系统调用所发生的时间的打印格式:
    - `-t`将打印如"15:24:33"的时间格式；
    - `-tt`将打印如"15:26:07.012859"的时间格式，其中"012859"部分为精确到微秒的时间。
    - `-ttt`将打印如"1567236408.587650"的时间格式，其中"1567236408"为Unix时间戳（单位为秒），"587650"部分为精确到微秒的时间。

    时间信息将打印在一行的前面，如下面三行所示，他们分别是指定`-t`、`-tt`、`-ttt`选项的打印结果：
    ```
    # strace -f -p 27757 -t -T -e trace=fdatasync,write
    strace: Process 27757 attached with 4 threads
    [pid 27757] 15:24:33 write(8, "*3\r\n$3\r\nset\r\n$3\r\nboy\r\n$4\r\nandy\r\n", 32) = 32 <0.000131>

    # strace -f -p 27757 -tt -T -e trace=fdatasync,write
    strace: Process 27757 attached with 4 threads
    [pid 27757] 15:26:07.012859 write(8, "*3\r\n$3\r\nset\r\n$3\r\nboy\r\n$4\r\nandy\r\n", 32) = 32 <0.000279>

    # strace -f -p 27757 -ttt -T -e trace=fdatasync,write
    strace: Process 27757 attached with 4 threads
    [pid 27757] 1567236408.587650 write(8, "*3\r\n$3\r\nset\r\n$3\r\nboy\r\n$4\r\nandy\r\n", 32) = 32 <0.000193>
    ```
+ `-T` 选项
    当指定了`-T`选项，`strace`命令将打印系统调用执行的时间（即从系统调用开始到系统调用结束之间消耗的时间），以秒为单位。如下所示：
    ```
    # strace -f -p 27757 -t -T -e trace=fdatasync,write
    strace: Process 27757 attached with 4 threads
    [pid 27757] 15:24:33 write(8, "*3\r\n$3\r\nset\r\n$3\r\nboy\r\n$4\r\nandy\r\n", 32) = 32 <0.000131>
    ```
    其中，系统调用`write`执行时间花费了0.000131秒。
+ `-e expr` 选项
    `-e expr`选项用于指定需要跟踪的事件。`expr`的格式为`[qualifier=][!]value1[,value2]...`，其中：
    `qualifier`可以是trace, abbrev, verbose, raw, signal, read, write中的一个。`value`的值根据`qualifier`的不同可以有不同的值。下面分别对不同的`qualifier`进行说明。
+ `-e trace=set`
    该选项用于指定跟踪系统调用的范围。参数`set`用于指定需要跟踪的系统调用集合。如：指定`-e trace=read,write,close,open`时，将只跟踪系统调用`read`、`write`、`close`、`open`。
    默认值为`trace=all`，即跟踪所有的系统调用。
    `-e trace=set`选项包含一系列简写形式，即特定的`set`值将等价于一系列的系统调用，包括：
    - `-e trace=file`
        指定只跟踪参数中包含文件名的系统调用，相当于`-e trace=open,stat,chmod,unlink...`的简写形式，即所有对文件执行操作的系统调用
    - `-e trace=process`
        跟踪包含进程管理的系统调用，相当于`-e trace=fork,wait,exec...`
    - `-e trace=network`
        跟踪网络相关的系统调用
    - `-e trace=signal`
        跟踪信号量相关的系统调用
    - `-e trace=memory`
        跟踪内存映射相关的系统调用
+ `-o filename` 选项
    将跟踪信息输出到指定的文件，而不是当前的stderr。
+ `-p pid`
    将当前的跟踪附加到指定的进程上。可以同时指定多个`-p pid`选项，用于同时跟踪多个进程。如`-p 21212 -p 3323`。
    如果指定了`-p pid`选项，`strace`命令可以不指定command部分。
+ `-P path`
    指定只跟踪访问指定路径的系统调用。

## 示例

### 通过`strace`命令跟踪应用启动过程中的所有系统调用
通过`strace`命令跟踪应用启动过程中的所有系统调用，当应用启动失败的时候，可以非常方便的定位到错误的细节，如下面所示，使用`strace`命令启动redis服务器：
```
# strace -f -tt ./src/redis-server ./redis.conf 
15:04:53.238249 execve("./src/redis-server", ["./src/redis-server", "./redis.conf"], [/* 23 vars */]) = 0
15:04:53.239019 brk(NULL)               = 0xf12000
15:04:53.239201 mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fb7b2cd8000
15:04:53.239375 access("/etc/ld.so.preload", R_OK) = -1 ENOENT (No such file or directory)
15:04:53.239567 open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
15:04:53.239745 fstat(3, {st_mode=S_IFREG|0644, st_size=31318, ...}) = 0
15:04:53.239908 mmap(NULL, 31318, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7fb7b2cd0000
...
```


### 通过strace命令拦截和记录系统运行过程中的指定系统调用
下面通过`strace`命令拦截和记录redis调用系统的fdatasync和write命令进行持久化
```
# strace -f -p 27757 -T -e trace=fdatasync,write
strace: Process 27757 attached with 4 threads
[pid 27757] write(8, "*2\r\n$6\r\nSELECT\r\n$1\r\n0\r\n*3\r\n$3\r\ns"..., 56) = 56 <0.000118>
[pid 27757] write(9, "+OK\r\n", 5)      = 5 <0.000195>
[pid 27759] fdatasync(8)                = 0 <0.002662>
```
上面<0.000118>中的值为当前系统调用消耗的时间，单位为微秒。