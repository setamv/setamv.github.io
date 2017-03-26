[Back](index.md)

### Reading Notes : "More bash Shell Commands"

### Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 4: More bash Shell Commands
- Pages: {121, 120}
- Reading Time: 16/03/2017 21:08 ~ 26/03/2017 23:31

<span id="chapterList"></span>
### Chapter List
- [Monitoring Programs](#monitoringPrograms)
- [Monitoring Disk Space](#monitoringDiskSpace)
- [Working with Data Files](#workingWithDataFiles) 

<span id="monitoringPrograms">[Chapter List](#chapterList)</span>
#### Monitoring Programs 

##### Basic knowledge about process
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
        | -f            | Display a full format listing                                |
        | -l            | Display a long listing                                       |
        | -H            | Display processes in a hierarchical format                   |
        |               | (showing parent processes)                                   |
        | -n namelist   | Define the values to display in the WCHAN column             |
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

##### Stopping processes

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

<span id="monitoringDiskSpace">[Chapter List](#chapterList)</span>
#### Monitoring Disk Space

##### Mounting media

- What is the mounting?
    the Linux filesystem combines all media disks into a single virtual directory. Before you can use a new media disk on your system, you need to place it in the virtual directory. This task is called mounting.

- The `mount` command

    * General Description

        The `mount` command is used to mount media (It's means to place the media in the virtual directory).

        By default, the mount command displays a list of media devices currently mounted on the system: 

        ```
        $ mount
        /dev/mapper/VolGroup00-LogVol00 on / type ext3 (rw)
        proc on /proc type proc (rw)
        sysfs on /sys type sysfs (rw)
        devpts on /dev/pts type devpts (rw,gid=5,mode=620)
        /dev/sda1 on /boot type ext3 (rw)
        ```

        There are four pieces of information the mount command result provides:
        1. The device location of the media (It's not the virtual directory, but the real location of hardware, hard disk's location is as '/dev/sda1'). 
        2. The mount point in the virtual directory where the media is mounted. (This virtual directory can be explorered by `cd` command)
        3. The filesystem type. There are lots of types. If you share removable media devices with your Windows PCs, the types are most likely to be as followings:
            - vfat: Windows long filesystem
            - ntfs: Windows advanced filesystem used in Windows NT, XP, and Vista.
            - iso9660: The standard CD-ROM filesystem.
        Most USB memory sticks and floppies are formatted using the vfat filesystem
        4. The access status of the mounted media

        To manually mount a media device in the virtual directory, you’ll need to be logged in as the root user. 
        The basic command for manually mounting a media device is:
            `mount -t type device directory`

        + Example: Manually mount a usb memory device.        
            1. 首先，如果已经插入USB存储设备，可以使用命令 `$ fdisk -l` 来查看USB存储设备的位置信息（即上面的 device location），下面是命令的结果示例：
            ```
            [root@localhost ~]# fdisk -l

            Disk /dev/sda: 42.9 GB, 42949672960 bytes, 83886080 sectors
            ...

               Device Boot      Start         End      Blocks   Id  System
            /dev/sda1   *        2048      616447      307200   83  Linux
            ...

            Disk /dev/sdb: 8527 MB, 8527020032 bytes, 16654336 sectors
            Units = sectors of 1 * 512 = 512 bytes
            Sector size (logical/physical): 512 bytes / 512 bytes
            I/O size (minimum/optimal): 512 bytes / 512 bytes
            Disk label type: dos
            Disk identifier: 0xcad4ebea

               Device Boot      Start         End      Blocks   Id  System
            /dev/sdb4   *         256    16654335     8327040    b  W95 FAT32
            ```
            看到上面的最后一行信息 “/dev/sdb4”，这个就是USB存储设备的 device location（USB存储设备的名称都是以sdb开头）。
            2. 使用mount命令将USB存储设备挂载到(mount to)虚拟目录(Virture directory) “/media/usb”，命令如下：
                `$ mount -t vfat /dev/sdb4 /medial/usb1`
            3. 此时，USB存储设备已经挂载完成，挂载的虚拟目录就像普通的文件目录一样，可以使用命令 `cd`、`ls`、`cp`等进行浏览、拷贝等操作了。
    
    * `mount` Command options
    
        | **Parameter** |                     **Description**                     |
        |---------------|---------------------------------------------------------|
        | -a            | Mount all filesystems specified in the /etc/fstab file  |
        | -f            | Causes the mount command to simulate mounting a device, |
        |               | but not actually mount it                               |
        | -t vfstype    | The argument following the -t is used to indicate       |
        |               | the filesystem type, The filesystem types which are     |
        |               | currently  supported refer to man help.                 |
        | -F            | When used with the -a parameter,                        |
        |               | mounts all filesystems at the same time                 |
        | -v            | Verbose mode, explains all the steps                    |
        |               | required to mount the device                            |
        | -I            | Don’t use any filesystem helper files                   |
        |               | under /sbin/mount.filesystem                            |
        | -l            | Add the filesystem labels automatically                 |
        |               | for ext2, ext3, or XFS filesystems                      |
        | -n            | Mount the device without registering                    |
        |               | it in the /etc/mstab mounted device file                |
        | -p num        | For encrypted mounting, read the passphrase             |
        |               | from the file descriptor num                            |
        | -s            | Ignore mount options not supported by the filesystem    |
        | -r            | Mount the device as read-only                           |
        | -w            | Mount the device as read-write (the default)            |
        | -L label      | Mount the device with the specified label               |
        | -U uuid       | Mount the device with the specified uuid                |
        | -O            | When used with the -a parameter,                        |
        |               | limits the set of filesystems applied                   |
        | -o            | Add specific options to the filesytem,                  |
        |               | The popular options to use are:                         |
        |               | ro: Mount as read-only                                  |
        |               | rw: Mount as read-write.                                |
        |               | user: Allow an ordinary user to mount the filesystem    |
        |               | check=none: Mount the filesystem without                |
        |               | performing an integrity check.                          |
        |               | loop: Mount a file (see "use `mount` command to mount   |
        |               | a iso image file with `-o` option" bellow)              |
        |               |                                                         |

        
    * use `mount` command to mount a iso image file with `-o` option.
    
        The .iso file is a complete image of the CD in a single file. Most CD-burning software packages can create a new CD based on the .iso file. A feature of the mount command is that you can mount a .iso file, directly to your Linux virtual directory without having to burn it onto a CD. This is accomplished using the -o parameter with the loop option.

        The following example use `mount` command to mount a iso image file from directory "/home/setamv/setups/CentOS-7-x86_64-DVD-1611.iso":
        ```
        $ mount -t iso9660 -o loop /home/setamv/setups/CentOS-7-x86_64-DVD-1611.iso /mnt/CentOS
        mount: /dev/loop0 is write-protected, mounting read-only
        $ ls -acl /mnt/CentOS
        total 657
        drwxr-xr-x. 8 root root   2048 Dec  5 05:47 .
        drwxr-xr-x. 3 root root     20 Mar 20 07:05 ..
        -rw-r--r--. 1 root root     14 Dec  5 05:47 CentOS_BuildTag
        -rw-r--r--. 1 root root     29 Dec  5 05:47 .discinfo
        drwxr-xr-x. 3 root root   2048 Dec  5 05:47 EFI
        -rw-r--r--. 1 root root    215 Dec  5 05:47 EULA
        -rw-r--r--. 1 root root  18009 Dec  5 05:47 GPL
        drwxr-xr-x. 3 root root   2048 Dec  5 05:47 images
        drwxr-xr-x. 2 root root   2048 Dec  5 05:47 isolinux
        drwxr-xr-x. 2 root root   2048 Dec  5 05:47 LiveOS
        drwxrwxr-x. 2 root root 630784 Dec  5 05:47 Packages
        drwxrwxr-x. 2 root root   4096 Dec  5 05:47 repodata
        -rw-r--r--. 1 root root   1690 Dec  5 05:47 RPM-GPG-KEY-CentOS-7
        -rw-r--r--. 1 root root   1690 Dec  5 05:47 RPM-GPG-KEY-CentOS-Testing-7
        -r--r--r--. 1 root root   2883 Dec  5 05:55 TRANS.TBL
        -rw-r--r--. 1 root root    366 Dec  5 05:47 .treeinfo
        ```


- The `umount` command、

    To remove a removable media device, you should never just remove it from the system. Instead, you should always unmount it first.

    The command used to unmount devices is `umount` (yes, there’s no ‘‘n’’ in the command, which gets confusing sometimes). The format for the umount command is pretty simple:
        `umount [directory | device ]`
    If there are any open files contained on the device, the system won’t let you unmount it.

- The `df` command

    The `df` command shows each mounted filesystem that contains data. The command outputs are as follows:

    ```
    $ df
    Filesystem     1K-blocks     Used Available Use% Mounted on
    /dev/sda3       39517336 19887132  19630204  51% /
    devtmpfs          923780        0    923780   0% /dev
    tmpfs             933644        0    933644   0% /dev/shm
    tmpfs             933644     8804    924840   1% /run
    tmpfs             933644        0    933644   0% /sys/fs/cgroup
    /dev/sda1         303780   121904    181876  41% /boot
    tmpfs             186732        0    186732   0% /run/user/0
    tmpfs             186732        0    186732   0% /run/user/1000
    /dev/sdb4        8318720  3841888   4476832  47% /mnt/usb1
    ```
    The displayed columns are:
    1. Filesystem: The device location of the device
    2. 1K-blocks: How many 1024-byte blocks of data it can hold
    3. Used: How many 1024-byte blocks are used
    4. Available: How many 1024-byte blocks are available
    5. Use%: The amount of used space as a percentage
    6. Mounted on: The mount point where the device is mounted
    
    * `df` Command options
    
        | **Option** |                     **Description**                     |
        |------------|---------------------------------------------------------|
        | -h         | print sizes in human readable format (e.g., 1K 234M 2G) |
        | -B SIZE    | scale sizes by SIZE before printing them;               |
        |            | SIZE is an integer and optional unit.                   |
        |            | example: 10M is 10*1024*1024,                           |
        |            | Units are K, M, G, T, P, E, Z, Y (powers of 1024)       |
        |            | or KB, MB, ... (powers of 1000)                         |
        |            | The default is 1K                                       |
        |            |                                                         |

- The the `du` command

    The du command shows the disk usage for a specific directory (by default, the current directory).

    - `du` command options
        
        |  **Option** |                       **Description**                        |
        |-------------|--------------------------------------------------------------|
        | -a          | write counts for all files, not just directories.            |
        |             | this option will list all file size and directory sizes.     |
        | -B SIZE     | scale sizes by SIZE before printing them;                    |
        |             | SIZE is an integer and optional unit.                        |
        |             | example: 10M is 10*1024*1024,                                |
        |             | Units are K, M, G, T, P, E, Z, Y (powers of 1024)            |
        |             | or KB, MB, ... (powers of 1000)                              |
        |             | The default is 1K                                            |
        | -c          | produce a grand total, which will list only the child        |
        |             | directories' disk usage.                                     |
        | -d n        | print the total for a directory (or file, with --all)        |
        |             | only if it's depth in directory of the command line argument |
        |             | is less equal than n. `-d 0` is the same as `-s`             |
        | -h          | print sizes in human readable format (e.g., 1K 234M 2G)      |
        | -s          | display only a total for each argument, for example:         |
        |             | `$ du -sh *` will list all files and child directories'      |
        |             | size in current directory as argument '*' match everything.  |
        | -t SIZE     | exclude entries smaller than SIZE if positive,               |
        |             | or entries greater than SIZE if negative                     |
        | --time      | show time of the last modification of any file               |
        |             | in the directory, or any of its subdirectories               |
        | --time=WORD | show time as WORD instead of modification time:              |
        |             | atime, access, use, ctime or status                          |
        | --exclude   | exclude files that match PATTERN, for example:               |
        | =PATTERN    | `$ du -a --exclude=file*` will filter file                   |
        |             | whose name start with "file"                                 |
        |             |                                                              |

    - example 1: list all files and child directories size in current directory.
        ```
        $ du -sh *
        476K    bin
        1.4M    bins
        4.0K    cat.output
        4.0K    cat.output.bak
        4.0K    cat.output.cp
        4.0K    col.ex
        4.0K    cp1
        ...
        ```
        The command executed above list all files in current directory, if with star, no file will list.
    

<span id="workingWithDataFiles">[Chapter List](#chapterList)</span>
#### Working with Data Files 

##### Sorting Data

- `sort` Command

    By default, the sort command sorts the data lines in a text file using standard sorting rules for the language you specify as the default for the session。

    The default rule to sort is take the whole line as a key to compare with. and if you specify `-k` option, the `sort` command will split the whole line into fields(or columns) by seperators which default is _blank(s)_(whitespaces or tabs) or the seperators specified by `-t` option. 

    The `sort` can be used as a pipeline command.

    + `sort` command options
    
        | **Option** |                      **Description**                       |
        |------------|------------------------------------------------------------|
        | -b         | Ignore leading blanks when sorting.                        |
        | -c         | Don’t sort, but check if the input data is already sorted. |
        |            | Report if not sorted.                                      |
        | -f         | By default, sort orders capitalized letters first.         |
        |            | This parameter ignores case                                |
        | -g         | Use general numerical value to sort.                       |
        | -k KEYDEF  | sort via a key; KEYDEF gives location and type.            |
        |            | refered to [KEYDEF](#KEYDEF)                               |
        | -M         | Sort by month order using three-character month names.     |
        | -m         | Merge two already sorted data files                        |
        | -n         | Sort by string numerical value                             |
        | -o file    | Write results to file specified.                           |
        | -r         | Reverse the sort order (descending instead of ascending.   |
        | -t         | Specify the character used to distinguish key positions.   |
        |            |                                                            |

        * KEYDEF <a id="KEYDEF"></a>
            KEYDEF is `F[.C][OPTS][,F[.C][OPTS]]` for start and stop position, where `F` is a field number and `C` is a character position in the field; both are origin 1, and the stop position defaults to the line's end.  If neither -t nor -b is in effect, characters in a field are counted from the beginning of the preceding whitespace.  OPTS is one or more single-letter ordering options [bdfgiMhnRrV], which override global ordering options for that key.  If no key is given, use the entire line as the key.
            
        * example: 
            The content of file _sort.data_ is:
            ```
            setamv  32  male 78 Nov
            susie   28  female 86 Aug
            hong    1   male 95 Mar
            angel   4   female 94 Aug
            ```
            There is a _Tab_ between the first column(setamv) and second column(32). and a whitespace between the third column(male) and fourth column(78)

            1. the default sort result
                ```
                $ sort sort.data
                angel   4   female 94 Aug
                hong    1   male 95 Mar
                setamv  32  male 78 Nov
                susie   28  female 86 Aug
                ```

            2. with `-k` option
                ```
                $ sort -k 4 sort.data
                hong    1   male 95 Mar
                susie   28  female 86 Aug
                setamv  32  male 78 Nov
                angel   4   female 94 Aug
                ```
                使用`-k`选项时, 每一行数据都以空白符（空格或Tab）为分割符进行分割成多列（上文中使用field指代列的意思），`-k`选项的参数“2”表示按分割后的第2列进行排序，所以最终结果是以“1,28,32,4”这几个Field为依据排序（默认按字典排序）。

            3. with advanced '-k' option
                ```
                $ sort -k 4.3 sort.data
                angel   4   female 94 Aug
                hong    1   male 95 Mar
                susie   28  female 86 Aug
                setamv  32  male 78 Nov
                ```
                这个例子，和上一个例子的唯一区别是，`-k`选项的参数更复杂一些，其参数“4.3”表示，根据分割后的第4列中的第3个字符以后的部分排序(即 4,5,6,8这4个数字，这里第3个字符需要将前面的空白分割符也算在内）

            4. sort by three-character month names
                ```
                $ sort -M -k 5 sort.data
                hong    1   male 95 Mar
                angel   4   female 94 Aug
                susie   28  female 86 Aug
                setamv  32  male 78 Nov
                ```
                这个例子是按三字母表示法的月份来排序的

##### Searching for data

- `grep` Command
    
    `grep [OPTIONS] PATTERN [FILE...]`

    The grep command searches either the input or the file you specify for lines that contain characters that match the specified pattern. The output from grep is the lines that contain the matching pattern.     

    The `grep` command can be used as a pipline command.

    By default, the grep command uses basic Unix-style regular expressions to match patterns. A Unix-style regular expression uses special characters to define how to look for matching patterns。

    The `egrep` command is an offshoot of `grep`, which allows you to specify POSIX extended regular expressions, which contain more characters for specifying the matching pattern.

    The `fgrep` command is another version that allows you to specify matching patterns as a list of fixed-string values, separated by newline characters. This allows you to place a list of strings in a file, then use that list in the fgrep command to search for the strings in a larger file.

    Question: How to combine regular expressions to `grep` command?

    + `grep` Command Options
    
        | **Option** |                         **Description**                          |
        |------------|------------------------------------------------------------------|
        | -e PATTERN | Use PATTERN as the pattern.                                      |
        |            | This can be used to specify multiple search patterns,            |
        |            | or to protect a pattern beginning with a hyphen (-)              |
        |            | The output is lines matches any of the PATTERN                   |
        | -v         | Invert the sense of matching, to select non-matching lines       |
        | -i         | Ignore case distinctions in both the PATTERN and the input files |
        | -w         | Select lines containing matches that form whole words            |
        | -x         | Select only those matches that exactly match the whole line      |
        | -c         | Suppress normal output;                                          |
        |            | instead print a count of matching lines for each input file      |
        | -l         | Only print file names who contain matched line(s)                |
        | -L         | On the contrary to the option `-l`, this option will only        |
        |            | print file names who don't contain matched line                  |
        | -m NUM     | Stop reading a file after NUM matching lines                     |
        | -q         | Quiet;  do not write anything to standard output.  Exit          |
        |            | immediately with zero status if any match is found,              |
        |            | even if an error was detected。See example 3 below.              |
        | -s         | Suppress error messages about nonexistent or unreadable files    |
        | -n         | Prefix each line of output with the 1-based line number          |
        |            | within its input file                                            |
        | -A NUM     | Print NUM lines of trailing context after matching lines         |
        | -B NUM     | Print NUM lines of leading context before matching lines         |
        | -C NUM     | means `-A NUM -B NUM`, print both trailing and leading context   |
        | -r         | Read  all  files  under each directory, recursively,             |
        |            | following symbolic links only if they are on the command line    |
        | -R         | Read all files under each directory, recursively.                |
        |            | Follow all symbolic links, unlike -r                             |
        | -f         | Obtain  patterns  from  FILE,  one  per line.  The empty file    |
        |            | contains zero patterns, and therefore matches nothing            |

        **examples**
        
        The following examples are base on file `grep.log` with content as followings:
        ```
        MANDATORY_MANPATH           /usr/share/man
        MANDATORY_MANPATH           /usr/local/share/man
        MANPATH_MAP /bin            /usr/share/man
        MANPATH_MAP /usr/bin/X11        /usr/X11R6/man
        MANPATH_MAP /opt/bin        /opt/man
        MANPATH_MAP /opt/sbin       /opt/man
        MANDB_MAP   /usr/X11R6/man      /var/cache/man/X11R6
        MANDB_MAP   /opt/man        /var/cache/man/opt
        ```

        * Example 1: `-e` Option 
            
            ```
            $ grep -e MANDB_MAP -e X11R6 grep.log
            MANPATH_MAP /usr/bin/X11        /usr/X11R6/man
            MANDB_MAP   /usr/X11R6/man      /var/cache/man/X11R6
            MANDB_MAP   /opt/man        /var/cache/man/opt
            ```
            Pay attention to outputs, the first line only match the second pattern "X11R6", it's also joined to the output.

        * Example 2: `-w` Option
            
            ```
            $ grep -w MANDB_MAP grep.log
            MANDB_MAP   /usr/X11R6/man      /var/cache/man/X11R6
            MANDB_MAP   /opt/man        /var/cache/man/opt

            $ grep -w MANDB grep.log

            ```
            The second command has no output, as pattern "MANDB" don't match any word.

        * Example 3: `-q` and `-s` Options
        
            ```
            $ grep -q MAN grep.log
            $ echo $?
            0

            $ grep -q MAN grep.log1
            grep: grep.log1: No such file or directory
            $ echo $?
            2

            $ grep -qs MAN grep.log1
            $ echo $?
            2
            ```
            可以看到：
            1. 第一段的执行结果为0，表示有匹配的结果；
            2. 第二段，因为`grep.log1`文件不存在，所以打印了错误信息，同时返回结果为2
            3. 第三段，因为加了`-s`选项，错误信息被压制了，但返回结果还是2
        
##### Compressing data

- Linux File Compression Utilities

    This is a table about the file compression utilities in linux:

    | **Utilities** | **File extension** |             **Description**             |
    |---------------|--------------------|-----------------------------------------|
    | bzip2         | .bz2               | Uses the Burrows-Wheeler block          |
    |               |                    | sorting text compression algorithm      |
    |               |                    | and Huffman coding                      |
    | compress      | .Z                 | Original Unix file compression utility; |
    |               |                    | starting to fade away into obscurity    |
    | gzip          | .gz                | The GNU Project’s compression utility;  |
    |               |                    | uses Lempel-Ziv coding                  |
    | zip           | .zip               | The Unix version of the PKZIP program   |
    |               |                    | for Windows                             |
    |               |                    |                                         |

- The bzip2 utility
    
    The utilities in the bzip2 package are：

    | **Utility**  |                   **Description**                    |
    |--------------|------------------------------------------------------|
    | bzip2        | for compressing files                                |
    | bzcat        | for displaying the contents of compressed text files |
    | bunzip2      | for uncompressing compressed .bz2 files              |
    | bzip2recover | for attempting to recover damaged compressed files   |
    |              |                                                      |    

    其实，`bzip2`、`bzcat`、`bunzip2`这3个命令是执行的同一个程序，只是根据命令的名称，默认执行的参数选项不一样。你也可以使用`bunzip2`命令加上`-z`选项来执行压缩，而不是解压。所以，以下的命令选项对它们三个命令都适用：

    | **Option** |                         **Description**                         |
    |------------|-----------------------------------------------------------------|
    | -c         | Compress or decompress to standard output                       |
    | -d         | Force decompression. bzip2, bunzip2 and bzcat are really        |
    |            | the same program, and the decision about what actions to  take  |
    |            | is done on the basis of which name is used. This flag           |
    |            | overrides that mechanism, and forces bzip2 to decompress        |
    | -z         | The complement to -d: forces compression, regardless of the     |
    |            | invocation name                                                 |
    | -t         | Check  integrity of the specified file(s), but don't decompress |
    |            | them.  This really performs a trial decompression and           |
    |            | throws away the result.                                         |
    | -f         | Force overwrite of output files.  Normally, bzip2 will not      |
    |            | overwrite existing output files                                 |
    | -k         | Keep (don't delete) input files during compression              |
    |            | or decompression                                                |
    |            |                                                                 |

    + `bizp2` Command
    
        By default, the bzip2 command attempts to compress the original file, and **_replaces it with the compressed file_**, using the same filename with a .bz2 extension

        Return  values:  
        * 0 for a normal exit, 
        * 1 for environmental problems (file not found, invalid flags, I/O errors, &c), 
        * 2 to indicate a corrupt compressed file, 
        * 3 for an internal consistency error (eg, bug) which caused bzip2 to panic
    
    + `bunzip2` Command

        Supplying no filenames to `bunzip2` will causes decompression from standard input to standard output.
        for example:
            `$ cat some_zipped_file.bz2 | bunzip2`
        will decompress the content of file "some_zipped_file.bz2" and then print the uncompressed content to standard output.


- `tar` Command to archive data

    The tar command was originally used to write files to a tape device for archiving. However, it can also write the output to a file.
    The format of the tar command is:
        `tar function [options] object1 object2 ...`
    The function parameter defines what the tar command should do, as shown in Table bellow:

    | **Function** | **Description**                                               |
    | -A           | Append an existing tar archive file to another existing       |
    |              | tar archive file                                              |
    | -c           | Create a new tar archive file                                 |
    | -d           | Check the differences between a tar archive file and          |
    |              | the filesystem.                                               |
    | -r           | Append files to the end of an existing tar archive file       |
    | -t           | List the contents of an existing tar archive file.            |
    | -u           | Append files to an existing tar archive file that are newer   |
    |              | than a file with the same name in the existing archive.       |
    | -x           | Extract files from an existing archive file.                  |
    | -f file      | Output results to file (or device) file. 当使用`-c`选项进行   |
    |              | 打包时，file指定了打包文件的保存路径；当使用`-c`选项进行      |
    |              | 解包时，file指定了要解包的tar文件路径。                       |
    | -j           | Redirect output to the bzip2 command for compression          |
    | -p           | Preserve all file permissions.                                |
    | -v           | List files as they are processed                              |
    | -z           | Redirect the output to the gzip command for compression       |
    | -C dir       | Change to the specified directory。当使用`-x`选项进行解包时， |
    |              | 默认是解包到当前目录下，如果要解包到其他目录，                |
    |              | 必须使用`-C dir`选项进行指定。                                |
