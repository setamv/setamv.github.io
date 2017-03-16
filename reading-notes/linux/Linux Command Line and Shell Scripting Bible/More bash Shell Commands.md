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
    
        | **Parameter** |                         Description                          |
        |---------------|--------------------------------------------------------------|
        | -A            | Show all processes.                                          |
        | -N            | Show the opposite of the specified parameters                |
        | -a            | Select all processes except both session leaders             |
        |               | and processes not associated with a terminal                 |
        | -d            | Show all processes except session leaders                    |
        | -e            | Show all processes.                                          |
        | -C cmslist    | Show processes contained in the list cmdlist                 |
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
        |               |                                                              |

