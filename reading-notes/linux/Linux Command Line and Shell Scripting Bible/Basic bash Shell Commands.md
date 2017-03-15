### Reading Notes : "Basic bash Shell Commands"

### Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 3: Basic bash Shell Commands
- Pages: {89, 120}
- Reading Time: 15/03/2017 19:08 ~ 15/03/2017 23:31

### Notes

#### The Shell Prompt      
　　There are two environment variables that control the format of the command line prompt:       
    - **PS1**: Controls the format of the default command line prompt       
    - **PS2**: Controls the format of the second-tier command line prompt       

　　The following table shows the special characters that you can use in the prompt string:　　　    　　

| **Character** |                         **Description**                         |
|---------------|-----------------------------------------------------------------|
| \a            | The bell character                                              |
| \d            | The date in the format ‘‘Day Month Date’’                       |
| \e            | The ASCII escape character                                      |
| \h            | The local hostname                                              |
| \H            | The fully qualified domain hostname                             |
| \j            | The number of jobs currently managed by the shell               |
| \l            | The basename of the shell’s terminal device name                |
| \n            | The ASCII newline character                                     |
| \r            | The ASCII carriage return                                       |
| \s            | The name of the shell                                           |
| \t            | The current time in 24-hour HH:MM:SS format                     |
| \T            | The current time in 12-hour HH:MM:SS format                     |
| \@            | The current time in 12-hour am/pm format                        |
| \u            | The username of the current user                                |
| \v            | The version of the bash shell                                   |
| \V            | The release level of the bash shell                             |
| \w            | The current working directory                                   |
| \W            | The basename of the current working directory                   |
| \!            | The bash shell history number of this command                   |
| \#            | The command number of this command                              |
| \$            | A dollar sign if a normal user or a pound sign if the root user |
| \nnn          | ASCII character corresponding to the octal value nnn            |
|               | like space character is \040. (reference ASCII table)           |
| \\            | A backslash                                                     |
| \[            | Begins a control code sequence                                  |
| \]            | Ends a control code sequence                                    |

    For example, the following command will set the shell prompt to `[-bash noaa]$`
    ```
    $ PS1=[\\s\\040\\W]\\$\\040
    ```

#### Filesystem Navigation
- The Root Driver And Mount Point       
　　The first hard drive installed in a Linux PC is called the root drive. The root drive contains the core of the virtual directory. Everything else builds from there.
　　On the root drive, Linux creates special directories called mount points. Mount points are directories　in the virtual directory where you assign additional storage devices.      
　　The virtual directory causes files and directories to appear within these mount point directories, even though they are physically stored on a different drive.   
　　Often the system files are physically stored on the root drive, while user files are stored on a　different drive．     

- Common Linux Directory Names

| **Directory** |                         **Usage**                         |
|---------------|-----------------------------------------------------------|
| /             | The root of the virtual directory.                        |
|               | Normally, no files are placed here.                       |
| /bin          | The binary directory,                                     |
|               | where many GNU user-level utilities are stored.           |
| /boot         | The boot directory, where boot files are stored.          |
| /dev          | The device directory, where Linux creates device nodes.   |
| /etc          | The system configuration files directory.                 |
| /home         | The home directory,                                       |
|               | where Linux creates user directories.                     |
| /lib          | The library directory,                                    |
|               | where system and application library files are stored.    |
| /media        | The media directory,                                      |
|               | a common place for mount points used for removable media. |
| /mnt          | The mount directory, another common place                 |
|               | for mount points used for removable media.                |
| /opt          | The optional directory,                                   |
|               | often used to store optional software packages.           |
| /root         | The root home directory.                                  |
| /sbin         | The system binary directory,                              |
|               | where many GNU admin-level utilities are stored.          |
| /tmp          | The temporary directory,                                  |
|               | where temporary work files can be created and destroyed.  |
| /usr          | The user-installed software directory.                    |
| /var          | The variable directory,                                   |
|               | for files that change frequently, such as log files.      |
               
- ls Command
    - Command Options

    | **Option** |                    Description                     |
    |------------|----------------------------------------------------|
    | -a         | Don’t ignore entries starting with a period.       |
    | -A         | Don’t list the . and .. files.                     |
    | -b         | Calculate the block sizes using size-byte blocks.  |
    | -B         | Don’t list entries ends with the tilde (~) symbol  |
    |            | (used to denote backup copies).                    |
    | -c         | with -lt: sort by, ctime;                          |
    |            | with -l: show ctime and sort by name;              |
    |            | otherwise:  sort  by ctime, newest first           |
    | -h         | with -l, print sizes in human readable format      |
    | -i         | Display the index number (inode) of each file.     |
    | -I PATTERN | do not list implied entries matching shell PATTERN |
    | -r         | reverse order while sorting                        |
    | -R         | list subdirectories recursively                    |
    | -S         | sort by file size                                  |
    | -u         | with -lt: sort by, and show, access time;          |
    |            | with -l: show access time and sort by name;        |
    |            | otherwise:  sort by access time                    |
    | -X         | sort alphabetically by entry extension             |

    - 文件列表过滤        
    ls命令的最后可以跟一个字符串，用于过滤文件和目录列表，并支持通配符规则，如：`$ ls -al *.sh` 将只会列出以".sh"结尾的文件和目录。     

    - ls输出结果中的列说明
    Command `$ ls -alihb` will list files and directories as follows:           
    `102343818 -rw-rw-r--. 1 setamv setamv  255 Mar  9 07:13 testgz.sh`     
    1. The first column in the listing is the file or directory inode number. 
    2. The second column is a diagram of the type of file, along with the
file’s permissions. 
    3. the third column is the number of hard links to the file 
    4. the fourth column is the owner of the file
    5. the fifth column is the group the file belongs to
    6. the sixth column is the size of the file
    7. the seventh column is a timestamp showing the last modification time by default
    8. the eighth column is the actual filename
    　
    　
#### File Handling

- cp Command Options        
    
    | Option |                        Description                         |
    |--------|------------------------------------------------------------|
    | -f     | Force the overwriting of existing destination              |
    |        | files without prompting                                    |
    | -b     | Create a backup of each existing destination               |
    |        | file instead of overwriting it                             |
    | -i     | Prompt before overwriting destination files.               |
    | -l     | Create a file link instead of copying the files            |
    | -r     | Copy files recursively                                     |
    | -s     | Create a symbolic link instead of copying the file.        |
    |        | 注意：如果是赋值符号链接到其他目录，目标路径必须写全路径。 |
    | -u     | Copy the source file only if it has a newer date           |
    |        | and time than the destination (update).                    |

- Linking Files         
    There are two different types of file links in Linux:            
    + A symbolic, or soft, link         
        The inode number of origin file and symbolic are not identical, indicating that the Linux system treats symbolic file as a separate file
    + A hard link
        The hard link creates a separate file that contains information about the original file and where to locate it. When you reference the hard link file, it’s just as if you’re referencing the original file, the file's infomation are the same under `ls` command, and inode number also identical。     
        When a hard link file is created, the hard link numbers of the origin file and hard link files's will add 1 (the third column of `ll` command result.)      

    You can also use `ln` command to link file. default is hard link, add `-s` option will create symbolic link.


#### Viewing File Content

- Viewing file statistics
    The `stat` command provides a complete rundown of the status of a file on the filesystem as follows:
    ```
    File: "test10"
    Size: 6 Blocks: 8 Regular File
    Device: 306h/774d Inode: 1954891 Links: 2
    Access: (0644/-rw-r--r--) Uid: ( 501/ rich) Gid: ( 501/ rich)
    Access: Sat Sep 1 12:10:25 2007
    Modify: Sat Sep 1 12:11:17 2007
    Change: Sat Sep 1 12:16:42 2007
    ```

- Viewing the file type
    The `file` command can get the type of the file. The `file` command classifies files into three categories:     
    * Text files: Files that contain printable characters       
    * Executable files: Files that you can run on the system        
    * Data files: Files that contain nonprintable binary characters, but that you can’t run on the system
    　

- Viewing the whole file
    * The `cat` Command
    * The `more` Command
    * The `less` Command
    * The `tail` Command
    * The `head` Command

 
 
 
 
 
 
 
 
 
 
 