[Back](index.md)

### Reading Notes : "More bash Shell Commands"

### Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 4: More bash Shell Commands
- Pages: {121, 120}
- Reading Time: 16/03/2017 21:08 ~ 16/03/2017 23:31

### Notes

#### Monitoring Programs

#### Basic knowledge about process
- session       
　　当一个用户登录到主机，Linux将会建立了一个session，但是该session的维系是基于连接的，其维系的方式有两种：     
    1. 本地连接：就是说用户是在主机本机上进行的登录，直接通过键盘和屏幕和主机进行交互。
    2. 远程连接：用户通过互联网进行连接，比如基于ssh，连接都是经过加密的。
　　session是一个或多个进程组的集合。
 
 - session leader    
    1. 用户登录就是一个session的开始，登录之后，用户会得到一个与终端相关联的进程，该进程就是该session的leader，session的id就是该进程的id。
    2. 在程序中调用pid_t setsid（void），如果调用此函数的进程不是一个进程组的组长，则此函数就会创建一个新的session，它将做以下三件事：
        1. 该进程是新会话的session leader，也是该会话中唯一的进程；
        2. 该进程成为一个新进程组的组长进程，新进程组id是该进程id；
        3. 该进程是没有控制终端的。如果该进程原来是有一个控制终端的，但是这种联系也会被打断。

##### Peeking at the processes

- `ps` Command
 　　The GNU ps command that’s used in Linux systems supports three different types of command line parameters:
    * Unix-style parameters, which are preceded by a dash
    * BSD-style parameters, which are not preceded by a dash
    * GNU long parameters, which are preceded by a double dash
    
    - Unix-style parameters of `ps` command   
    　　注意，下表中所有选项后跟的 xxlist 参数列表，表示的是一个由多个部分使用逗号“,”连接的列表，如 `-p pidlist` 可以写成 `-p 2457,2578`，其中，2457和2478分别为两个进程的ID号。
    
        | **Parameter** |                       **Description**                        |
        |---------------|--------------------------------------------------------------|
        | -A            | Show all processes.                                          |
        | -N            | Show the opposite of the specified parameters                |
        | -a            | Select all processes except both session leaders             |
        |               | and processes not associated with a terminal                 |
        | -d            | Show all processes except session leaders                    |
        | -e            | Show all processes.                                          |
        | -C cmdlist    | Show processes contained in the list cmdlist                 |
        | -G grplist    | Show processes with a group ID listed in grplist             |
        | -U userlist   | Show processes owned by a userid listed in userlist          |
        | -g grplist    | Show processes by session or by groupid contained in grplist |
        | -p pidlist    | Show processes with PIDs in the list pidlist                 |
        | -s sesslist   | Show processes with session ID in the list sesslist          |
        | -t ttylist    | Show processes with terminal ID in the list ttylist          |
        | -u userlist   | Show processes by effective userid in the list userlist      |
        | -F            | Use extra full output                                        |
        | -O format     | Display specific columns in the list format ,                |
        |               | along with the default columns                               |
        | -M            | Display security information about the process               |
        | -c            | Show additional scheduler information about the process      |
        | -f            | Display a full format listing                                |
        | -j            | Show job information                                         |
        | -l            | Display a long listing                                       |
        | -o format     | Display only specific columns listed in format               |
        | -y            | Don’t show process flags                                     |
        | -Z            | Display the security context information, the same with -M   |
        | -H            | Display processes in a hierarchical format                   |
        |               | (showing parent processes)                                   |
        | -n namelist   | Define the values to display in the WCHAN column             |
        | -w            | Use wide output format, for unlimited width displays         |
        | -L            | Show process threads                                         |
        | --Headers     | Repeat header lines, one per page of output.                 |
        |               |                                                              |

    - `ps` Command Result Columns
    
        | **Column Name** |                       **Description**                       |
        |-----------------|-------------------------------------------------------------|
        | F               | System flags assigned to the process by the kernel.         |
        |                 | 1 = forked but didn't exec;                                 |
        |                 | 4 = used super-user privileges                              |
        | S               | The state of the process                                    |
        |                 | O = running on processor;                                   |
        |                 | S = sleeping;                                               |
        |                 | R = runnable, waiting to run;                               |
        |                 | Z = zombie, process terminated but parent not available;    |
        |                 | T = process stopped                                         |
        | UID             | The user responsible for launching the process              |
        | PID             | The process ID of the process                               |
        | PPID            | The PID of the parent process                               |
        |                 | (if a process is started by another process)                |
        | C               | processor utilization. Currently, this is the integer value |
        |                 | of the percent usage over the lifetime of the process.      |
        |                 | (see %cpu)                                                  |
        | %cpu            | cpu utilization of the process in "##.#" format.            |
        |                 | Currently, it is the CPU time used divided by the time      |
        |                 | the process has been running                                |
        |                 | (cputime/realtime ratio), expressed as a percentage.        |
        | PRI             | The priority of the process                                 |
        |                 | (higher numbers mean lower priority)                        |
        | STIME           | starting time or date of the process. Only the year will    |
        |                 | be displayed if the process was not started the same year   |
        |                 | ps was invoked, or "MmmDD" if it was not started            |
        |                 | the same day, or "HH:MM" otherwise                          |
        | TTY             | The terminal device from which the process was launched     |
        | TIME            | The cumulative CPU time required to run the process         |
        | CMD             | The name of the program that was started                    |
        | NI              | The nice value, which is used for determining priorities    |
        | ADDR            | The memory address of the process                           |
        | SZ              | Approximate amount of swap space required                   |
        |                 | if the process was swapped out                              |
        | WCHAN           | Address of the kernel function                              |
        |                 | where the process is sleeping                               |
        |                 |                                                             |


    
        * example 1        
            ```
            $ ps -ef
            UID PID PPID C STIME TTY TIME CMD
            root 1 0 0 11:29 ? 00:00:01 init [5]
            root 2 0 0 11:29 ? 00:00:00 [kthreadd]
            root 3 2 0 11:29 ? 00:00:00 [migration/0]
            ......
            ```
        
        * example 2
            ```
            $ ps -l
            F S UID PID PPID C PRI NI ADDR SZ WCHAN TTY TIME CMD
            0 S 500 3081 3080 0 80 0 - 1173 wait pts/0 00:00:00 bash
            0 R 500 4463 3081 1 80 0 - 1116 - pts/0 00:00:00 ps
            ```

        * example 3 - organizes the processes in a hierarchical format
            ```
            $ ps -efH
            UID         PID   PPID  C STIME TTY          TIME       CMD
            root       1393      1  0 06:14 ?        00:00:00   /usr/sbin/sshd
            root       2147   1393  0 06:14 ?        00:00:01     sshd: root@pts
            root       2151   2147  0 06:14 pts/0    00:00:00       -bash
            root       2270   2237  0 06:14 pts/1    00:00:00         su - setamv
            setamv     2271   2270  0 06:14 pts/1    00:00:00           -bash
            setamv     2409   2271  0 07:08 pts/1    00:00:00             ps -efH
            ```
            **explain**
            Notice the shifting in the CMD column output. This shows the hierarchy of the processes that are running. 
            1. First, the sshd process started by the root user (this is the Secure Shell (SSH) server session, which listens for remote SSH connections). 
            2. Next, when I connected from a remote terminal to the system, the main SSH process spawned a terminal process (pts), which in turn spawned a bash shell.
            3. From there, I executed the `su - setamv` command, which appears as a child process from the bash process. and it spawned a new bash shell.
            4. Finally, I executed the current `ps -efH` command from the bash shell.
        
##### Real-time process monitoring

- `top` Command
    
    The `top` command displays process information similarly to the `ps` command, but it does it in real-time mode, and by contrast, `ps` command can only display information for a specific point in time.

    * `top` Command Options
    
        |  **Option**  |                      **Description**                      |
        |--------------|-----------------------------------------------------------|
        | -i           | Starts top with the last remembered `i' state reversed.   |
        |              | When this toggle is Off, tasks that have not used any CPU |
        |              | since the last update will not be displayed.              |
        | -n number    | Specifies the maximum number of iterations,               |
        |              | or frames, top should produce before ending               |
        | -o filedname | Specifies the name of the field on which tasks will be    |
        |              | sorted. prepend a `+` to the field name will              |
        |              | force sorting high to low,                                |
        |              | whereas a `-' will  ensure  a  low  to  high ordering.    |
        | -p pidlist   | Monitor  only  processes  with  specified  process IDs    |
        | -u user      | Display  only  processes with a user id                   |
        |              | or user name matching that given                          |
        |              |                                                           |

    * `top` Command Outputs
        `top` command outputs like the followings:
        ```
        top - 07:43:09 up  1:29,  2 users,  load average: 0.00, 0.01, 0.05
        Tasks: 167 total,   1 running, 166 sleeping,   0 stopped,   0 zombie
        %Cpu(s):  0.0 us,  0.1 sy,  0.0 ni, 99.8 id,  0.1 wa,  0.0 hi,  0.0 si,  0.0 st
        KiB Mem :  1867292 total,  1498912 free,   145140 used,   223240 buff/cache
        KiB Swap:  2098172 total,  2098172 free,        0 used.  1516748 avail Mem 

        PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND                                                                 
        2199 setamv    20   0  116040   2800   1692 S   0.0  0.1   0:00.03 bash                                                                    
        2271 setamv    20   0  116040   2804   1692 S   0.0  0.2   0:00.19 bash                                                                 
        .....
        ```
        
        There are two areas of the outputs:

        1. Summary Display Area
            The summary display area is on the top of outputs, it shows the summary of the current system. The summary area can ben divided into three parts:

            1. UPTIME and LOAD Averages
                This portion consists of a single line containing: 
                + top:   program or window name, depending on display mode
                + 07:43:09 up  1:29:    current time and length of time since last boot
                + 2 users:  total number of users
                + load average: system load avg over the last 1, 5 and 15 minutes
        
            2. TASK and CPU States
                This portion consists of a minimum of two lines.  
                + Line 1 shows total tasks or threads, depending on the state of the Threads-mode toggle.  That total is further classified as: running; sleeping; stopped; zombie
                + Line 2 shows CPU state percentages based on the interval since the last refresh. They are:
                    - us: time running un-niced user processes
                    - sy: time running kernel processes
                    - ni: time running niced user processes
                    *id: time spent in the kernel idle handler
                    *wa: time waiting for I/O completion
                    - hi: time spent servicing hardware interrupts
                    - si: time spent servicing software interrupts
                    - st: time stolen from this vm by the hypervisor
        
            3. MEMORY Usage
                This portion consists of two lines which may express values in kibibytes(KiB) through exbibytes (EiB) depending on the scaling factor enforced with the `E' interactive command.
                + Line 1
                    Line 1 reflects physical memory, classified as: total, free, used and buff/cache
                + Line 2 
                    Line 2 reflects mostly virtual memory, classified as: total, free, used and avail (which is physical memory).
                    The avail number is an estimation of physical  memory available for starting new applications, without swapping. Unlike the free field, it attempts to account for readily reclaimable page cache and memory slabs.

        2. Output Columns 

            | **Column Name** |                    **Description**                     |
            |-----------------|--------------------------------------------------------|
            | %CPU            | The task's share of the elapsed CPU time since the     |
            |                 | last screen update, expressed as a                     |
            |                 | percentage of total CPU time                           |
            | %MEM            | A task's currently used share of available             |
            |                 | physical memory.                                       |
            | PR              | The priority of the process                            |
            | NI              | The nice value of the process                          |
            | VIRT            | The total amount of virtual memory used by the process |
            | RES             | The amount of physical memory the process is using     |
            | SHR             | The amount of memory the process is sharing            |
            |                 | with other processes                                   |
            | S               | The process status                                     |
            |                 | D = interruptible sleep,                               |
            |                 | R = running,                                           |
            |                 | S = sleeping,                                          |
            |                 | T = traced or stopped,                                 |
            |                 | Z = zombie)                                            |
            | TIME+           | The total CPU time the process has used since starting |
            | COMMAND         | The command line name of the process (program started) |
            |                 |                                                        |

#### Stopping processes

- Signals

    In Linux, processes communicate between each other using signals. A process signal is a predefined message that processes recognize and may choose to ignore or act on. The developers program how a process handles signals. Most well-written applications have the ability to receive and act on the standard Unix process signals. These signals are shown in Table

- Linux Process Signals
    
    | **Signal** | **Name** |                  **Description**                  |
    |------------|----------|---------------------------------------------------|
    |          1 | HUP      | Hang up.                                          |
    |          2 | INT      | Interrupt.                                        |
    |          3 | QUIT     | Stop running.                                     |
    |          9 | KILL     | Unconditionally terminate.                        |
    |         11 | SEGV     | Segment violation.                                |
    |         15 | TERM     | Terminate if possible.                            |
    |         17 | STOP     | Stop unconditionally, but don’t terminate.        |
    |         18 | TSTP     | Stop or pause, but continue to run in background. |
    |         19 | CONT     | Resume execution after STOP or TSTP.              |
    |            |          |                                                   |

- The `kill` Command

    The kill command allows you to send signals to processes based on their process ID (PID).

    By default the kill command sends a TERM signal to all the PIDs listed on the command line. But sometimes the TERM signal will be ignored by the process.

    ```
    $ kill 10,3923    # this will send TERM signal to process with PID 10 and PID 3923
    ```

    If you need to get forceful, the -s parameter allows you to specify other signals (either using their name or signal number).
    The generally accepted procedure is:
    1. try the TERM signal. If the process ignores that, then 
    2. try the INT or HUP signals. If the program recognizes these signals, it’ll try to gracefully stop doing what it was doing before shutting down. 
    3. The most forceful signal is the KILL signal. When a process receives this signal, it immediately stops running. This can lead to corrupt files.

    To send a process signal, you must either be the owner of the process or be logged in as the root user.

- The 'killall' Command

    * The `killall` command is a powerful way to stop processes by using their names rather than the PID numbers since `kill` command can only use their PID numbers. 
    * The killall command allows you to use wildcard characters as well, making it a very useful tool when you’ve got a system that’s gone awry. Like following:
    ```
    $ killall tomcat*   # will kill all processes whoes name start with tomcat.
    ```