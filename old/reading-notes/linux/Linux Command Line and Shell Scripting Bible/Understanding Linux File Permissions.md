[Back](index.md)

### Reading Notes : "Understanding Linux File Permissionss"

### Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 6: Understanding Linux File Permissionss
- Pages: {177, 120}
- Reading Time: 27/03/2017 19:30 ~ 23:00

<span id="chapterList"></span>
### Chapter List
- [Linux Security](#LinuxSecurity)
- [Using Linux Groups](#UsingLinuxGroups)
- [Decoding File Permissions](#DecodingFilePermissions)
- [Changing Security Settings](#ChangingSecuritySettings)
- [Sharing Files](#SharingFiles)

<span id="LinuxSecurity">[Chapter List](#chapterList)</span>
#### Linux Security

- Description

    The core of the Linux security system is the user account. 
    User permissions are tracked using a user ID (often called a UID), which is assigned to an account when it’s created. The UID is a numerical value, unique for each user.

- The /etc/passwd File

    The Linux system uses a special file to match the login name to a corresponding UID value. This file is the /etc/passwd file. The /etc/passwd file contains several pieces of information about the user as follows:
    ```
    $ cat /etc/passwd
    root:x:0:0:root:/root:/bin/bash
    bin:x:1:1:bin:/bin:/sbin/nologin
    ....
    ```
    The fields above contain the following information:
        1. The login username
        2. The password for the user
        3. The numerical UID of the user account
        4. The numerical group ID (GID) of the user account
        5. A text description of the user account (called the comment field)
        6. The location of the HOME directory for the user
        7. The default shell for the user
    
    Linux system creates lots of user accounts for various functions that aren’t actual users. These are called system accounts.
    A system account is a special account that services running on the system use to gain access to resources on the system. All services that run in background mode need to be logged in to the Linux system under a system user account.

- The /etc/shadow file

    The /etc/shadow file provides more control over how the Linux system manages passwords. Only the root user has access to the /etc/shadow file

    The /etc/shadow file contains one record for each user account on the system. A record looks like this:
        `rich:$1$.FfcK0ns$f1UgiyHQ25wrB/hykCn020:11627:0:99999:7:::`
    There are nine fields in each /etc/shadow file record:
        1. The login name corresponding to the login name in the /etc/passwd file
        2. The encrypted password
        3. The number of days since January 1, 1970 that the password was last changed
        4. The minimum number of days before the password can be changed
        5. The number of days before the password must be changed
        6. The number of days before password expiration that the user is warned to change the password
        7. The number of days after a password expires before the account will be disabled
        8. The date (stored as the number of days since January 1, 1970) since the user account was disabled
        9. A field reserved for future use

- Adding a new user

    The primary tool used to add new users to your Linux system is `useradd`.
    The `useradd` command uses a combination of system default values and command line parameters to define a user account, To see the system default values used on your Linux distribution, enter the `useradd` command with the -D parameter:
        ```
        $ useradd -D
        GROUP=100
        HOME=/home
        INACTIVE=-1
        EXPIRE=
        SHELL=/bin/bash
        SKEL=/etc/skel
        CREATE_MAIL_SPOOL=yes
        ```
    This example shows the following default values:
        1. The new user will be added to a common group with group ID 100.
        2. The new user will have a HOME account created in the directory /home/loginname
        3. The account will not be disabled when the password expires.
        4. The new account will not be set to expire at a set date.
        5. The new account will use the bash shell as the default shell.
        6. The system will copy the contents of the /etc/skel directory to the user’s HOME directory.
        7. The system will create a file in the mail directory for the user account to receive mail

    If you want to override a default value when creating a new user, you can do that with command line parameters. These are shown in Table bellow:        

    |  **Parameter**   |                     **Description**                      |
    |------------------|----------------------------------------------------------|
    | -c comment       | Add text to the new user’s comment field                 |
    | -d home_dir      | Specify a different name for the home directory other    |
    |                  | than the login name.                                     |
    | -e expire_date   | Specify a date, in YYYY-MM-DD format,                    |
    |                  | when the account will expire                             |
    | -f inactive_days | Specify the number of days after a password expires      |
    |                  | when the account will be disabled. A value of 0 disables |
    |                  | the account as soon as the password expires;             |
    |                  | a value of -1 disables this feature                      |
    | -g initial_group | Specify the group name or GID of the user’s login group  |
    | -G group. . .    | Specify one or more supplementary groups the user        |
    |                  | belongs to                                               |
    | -k               | Copy the /etc/skel directory contents into the user’s    |
    |                  | HOME directory (must use -m as well)                     |
    | -m               | Create the user’s HOME directory                         |
    | -M               | Don’t create a user’s HOME directory                     |
    |                  | (used if the default setting is to create one).          |
    | -n               | Create a new group using the same name as the            |
    |                  | user’s login name                                        |
    | -r               | Create a system account                                  |
    | -p passwd        | Specify a default password for the user account          |
    | -s shell         | Specify the default login shell.                         |
    | -u uid           | Specify a unique UID for the account                     |
    |                  |                                                          |

    example: add a user named "susie" and set her home directory as "/home/susie-home"
        `$ useradd -d /home/susie-home -p susiepwd susie`

    if you find yourself having to override a value all the time, you can change the system default new user values by using the -D parameter, along with a parameter representing the value you need to change, These parameters are shown as follows:

    |   **Parameter**    |                    **Description                     |
    |--------------------|------------------------------------------------------|
    | -b default_home    | Change the location of where users’ HOME directories |
    |                    | are created.                                         |
    | -e expiration_date | Change the expiration date on new accounts           |
    | -f inactive        | Change the number of days after a password has       |
    |                    | expired before the account is disabled               |
    | -g group           | Change the default group name or GID used            |
    | -s shell           | Change the default login shell                       |
    |                    |                                                      |

    example, change the default shell command as follows:
        `$ useradd -D -s /bin/tsch`


- Removing a user

    The `userdel` command is used to remove a user from the system, By default, the userdel command only removes the user information from the /etc/passwd file. It doesn’t remove any files the account owns on the system.

    If you use the -r parameter, userdel will remove the user’s HOME directory, along with the user’s mail directory. However, there may still be other files owned by the deleted user account on the system. This can be a problem in some environments.

- Modifying a user

    Linux provides a few different utilities for modifying the information for existing user accounts. The following table shows these utilities.
    | **Command** |                       **Description**                        |
    |-------------|--------------------------------------------------------------|
    | usermod     | Edits user account fields, as well as specifying primary and |
    |             | secondary group membership                                   |
    | passwd      | Changes the password for an existing user                    |
    | chpasswd    | Reads a file of login name and password pairs,               |
    |             | and updates the passwords                                    |
    | chage       | Changes the password’s expiration date                       |
    | chfn        | Changes the user account’s comment information               |
    | chsh        | Changes the user account’s default shell                     |
    |             |                                                              |

    + `usermod` Command

        The `usermod' command provides options for changing most of the fields in the /etc/passwd file. The parameters are mostly the same as the useradd parameters.

        However, there are a couple of additional parameters that might come in handy:
            * -l: to change the login name of the user account
            * -L: to lock the account so the user can’t log in
            * -p: to change the password for the account
            * -U: to unlock the account so that the user can log in
            
        example: lock a user
            `$ usermod -L setamv`

    + `passwd` Command
    
        If you just use the passwd command by itself, it’ll change your own password. Any user in the system can change their own password, but only the root user can change someone else’s password.

        The table bellow are some useful options can be used in `passwd` command:

        | **Option** |                        **Description**                         |
        |------------|----------------------------------------------------------------|
        | -l         | This  option is used to lock the password of specified account |
        |            | and it is available to root only                               |
        | -u         | This is the reverse of the -l option                           |
        |            | it will unlock the account password                            |
        | -d         | This is a quick way to delete a password for an account.       |
        |            | It will set the named account passwordless.                    |
        |            | Available to root only.                                        |
        |            | 注意，有一些SSH终端（如SecureCRT）并不支持无密码登录。         |
        | -e         | This is a quick way to expire a password for an account. The   |
        |            | user will be forced to change  the  password  during  the      |
        |            | next login attempt                                             |
        | -S         | Output a short information about the status of the             |
        |            | password for a given account                                   |
        |            |                                                                |

- `chpasswd` Command

    The chpasswd command reads a list of login name and password pairs (separated by a colon) from the standard input, and automatically encrypts the password and sets it for the user account.

    1. Example: change a user's password
        `$ echo setamv:setamvpwd | chpasswd`

    2. Example: change a list of users' password by a file.
    The content of file "passwdlst.txt" is as follows:
        ```
        susie:susiepwd
        hong:hongpwd
        setamv:setamvpwd
        ```
    Now, you can use the file "passwdlst.txt" as standard input to change the password of users "susie", "hong" and "setamv" at once:
        `$ chpasswd < passwdlst.txt`


<span id="UsingLinuxGroups">[Chapter List](#chapterList)</span>
#### Using Linux Groups

- Description

    User accounts are great for controlling security for individual users, but they aren’t so good at allowing groups of users to share resources. To accomplish this, the Linux system uses another security concept, called groups.

    Group permissions allow multiple users to share a common set of permissions for an object on the system, such as a file, directory, or device.

- The /etc/group file

    The /etc/group file contains information about each group used on the system. Here are a few examples from the /etc/group file on my Linux system:
        ```
        root:x:0:root
        bin:x:1:root,bin,daemon
        daemon:x:2:root,bin,daemon
        sys:x:3:root,bin,admin
        mysql:x:27:
        ....
        ```
    The /etc/group file uses four fields:
        1. The group name
        2. The group password
        3. The GID
        4. The list of user accounts that belong to the group
    
    The group password allows a non-group member to temporarily become a member of the group by using the password. This feature is not used all that commonly, but it does exist.

    There are several groups in the list that don’t have any users listed. This isn’t because they don’t have any members. When a user account uses a group as the default group in the /etc/passwd file, the user account doesn’t appear in the /etc/group file as a member.

- Creating new groups

    The `groupadd` command allows you to create new groups on your system:
    The command syntax is:
        `groupadd [options] groupName`
    The most useful options are:

    |  **Option** |                         **Description**                          |
    |-------------|------------------------------------------------------------------|
    | -g GID      | The numerical value of the group's ID. This value must be unique |
    | -p PASSWORD | The encrypted password                                           |
    | -r          | Create a system group                                            |
    |             |                                                                  |

- Modifying groups

    The groupmod command allows you to change the GID (using the -g parameter) or the group name (using the -n parameter) of an existing group

- `gpasswd` Command
    
    The gpasswd command is used to administer /etc/group, and /etc/gshadow. Every group can have administrators, members and a password.

    System administrators can use the -A option to define group administrator(s) and the -M option to define members. They have all rights of group administrators and members.

    `gpasswd` called by a group administrator with a group name only prompts for the new password of the group.

    The format of `gpasswd` is:
        `gpasswd [option] group`
    the group field can be either the group name or gropu UID.

    + `gpasswd` command options

        |  **Option** |           **Description**            |
        |-------------|--------------------------------------|
        | -a user     | Add the user to the named group      |
        | -d user     | Remove the user from the named group |
        | -A user,... | Set the list of administrative users |
        | -M user,... | Set the list of group members.       |
        |             |                                      |


<span id="DecodingFilePermissions">[Chapter List](#chapterList)</span>
#### Decoding File Permissions

- Using file permission symbols

    the ls command allows us to see the file permissions for files, directories, and devices on the Linux system:
        ```
        $ ls -l
        total 68
        -rw-rw-r-- 1 rich rich 50 2007-09-13 07:49 file1.gz
        -rw-rw-r-- 1 rich rich 23 2007-09-13 07:50 file2
        -rw-rw-r-- 1 rich rich 48 2007-09-13 07:56 file3
        .....
        ```
    The first field in the output listing is a code that describes the permissions for the files and directories. The first character in the field defines the type of the object:
        + -: for files
        + d: for directories
        + l: for links
        + c: for character devices
        + b: for block devices
        + n: for network devices
    After that, there are three sets of three characters. Each set of three characters defines an access permission triplet:
        + r: for read permission for the object
        + w: for write permission for the object
        + x: for execute permission for the object
    If a permission is denied, a dash appears in the location
    The three sets relate the three levels of security for the object:
        1. The owner of the object
        2. The group that owns the object
        3. Everyone else on the system

- Default file permissions

    The `umask` command used to shows and sets the default permissions for any file or directory you create:
        ```
        $ umask
        0022
        ```
    The output of `umask` means:
        1. The first digit represents a special security feature called the sticky bit. see 
        2. The next three digits represent the octal values of the umask for a file or directory. see section "The _Octal_ mode"
    
    The umask value is just that, a mask. It masks out the permissions you don’t want to give to the security level.

    The umask value is subtracted from the full permission set for an object. The full permission for a file is mode 666 (read/write permission for all), but for a directory it’s 777 (read/write/execute permission for all).

    When i `touch` a new file, the file starts out with permissions 666, and the umask of 022 is applied, leaving a file permission of 644.

    The umask value is normally set in the /etc/profile startup file (see Chapter 5). You can specify a different default umask setting using the umask command as follows:
        `$ umask 026`
    
    + The _Octal_ mode

        Octal mode security settings take the three rwx permission values and convert them into a 3-bit binary value, represented by a single octal value. In the binary representation, each position is a binary bit. Thus, if the read permission is the only permission set, the value becomes r--, relating to a binary value of 100, the following table shows the possible combinations:

        | **Permission** | **Binary** | **Octal** |        **Description**        |
        |----------------|------------|-----------|-------------------------------|
        | ---            |        000 |         0 | No permissions                |
        | --x            |        001 |         1 | Execute-only permission       |
        | -w-            |        010 |         2 | Write-only permission         |
        | -wx            |        011 |         3 | Write and execute permissions |
        | r--            |        100 |         4 | Read-only permission          |
        | r-x            |        101 |         5 | Read and execute permissions  |
        | rw-            |        110 |         6 | Read and write permissions    |
        | rwx            |        111 |         7 | all Permissions               |
        |                |            |           |                               |

<span id="ChangingSecuritySettings">[Chapter List](#chapterList)</span>
#### Changing Security Settings

- Changing permissions

    The `chmod` command allows you to change the security settings for files and directories. The format of the chmod command is:

        `chmod options mode file`

    The mode parameter allows you to set the security settings using either octal or symbolic mode.

    + Using _Octal_ mode settings
    
        The octal mode settings are pretty straightforward; just use the standard three-digit octal code you want the file to have:

            `$ chmod 760 newfile`

    + Using normal string of three sets of three characters
    
        The format for specifying a permission in symbolic mode is:

            `$ chomod [ugoa...][[+-=][rwxXstugo...]`

        * The first group of characters defines to whom the new permissions apply:
            * u: for the user 
            * g: for the group
            * o: for others (everyone else)
            * a: for all of the above
        
        * Next, a symbol is used to indicate whether you want to:
            * +: add the permission to the existing permissions 
            * -: subtract the permission from the existing permission 
            * =: set the permissions to the value.
        
        * Finally, the third symbol is the permission used for the setting, You may notice that there are more than the normal rwx values here. The additional settings are:
            * X: to assign execute permissions only if the object is a directory or if it already had execute permissions
            * s: to set the UID or GID on execution
            * t: to save program text
            * u: to set the permissions to the owner’s permissions
            * g: to set the permissions to the group’s permissions
            * o: to set the permissions to the other’s permissions
        
        * Example: Set the permission for group to the same as the owner.
            ```
            $ ll
            -rw-r--r--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            $ chmod g=u passwdlst.txt
            $ ll
            -rw-rw-r--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            ```

        * Example: Set the group permission to read and write:
            ```
            $ ll
            -rw-r--r--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            $ chmod g=rw passwdlst.txt
            $ll
            -rw-rw-r--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            ```

        * Example: Add executable permission:
            ```
            $ ll
            -rw-r--r--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            $ chmod g+x passwdlst.txt
            $ll
            -rw-r-xr--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            ```

- Changing ownership

    + `chown` Command

        The `chown` command used to change the owner of a file, and the chgrp command allows you to change the default group of a file.

        The format of the chown command is:

            `$ chown options owner[.group] file`

            1. You can specify either the login name or the numeric UID for the new owner of the file.
            2. The chown command also allows you to change both the user and group of a file.
            3. Finally, if your Linux system uses individual group names that match user login names, you can change both with just one entry: `$ chown setamv. newfile`
            4. Only the root user can change the owner of a file. Any user can change the default group of a file, but the user must be a member of the groups the file is changed from and to

        Example:
            ```
            $ ll
            -rw-r-xr--. 1 root root   42 Mar 27 05:59 passwdlst.txt
            $ chown setamv.setamv passwdlst.txt
            $ ll
            -rw-r-xr--. 1 setamv setamv   42 Mar 27 05:59 passwdlst.txt
            ```

    + `chgrp` Command

        The chgrp command provides an easy way to change just the default group for a file or directory:
            ```
            $ chgrp shared newfile
            $ ls -l newfile
            -rw-rw-r-- 1 rich shared 0 Sep 20 19:16 newfile*
            ```
        Now any member in the shared group can write to the file. This is one way to share files on a Linux system.

<span id="SharingFiles">[Chapter List](#chapterList)</span>
#### Sharing Files

- Description

    When you create a new file, Linux assigns the file permissions of the new file using your default UID and GID. To allow others access to the file, you need to either change the security permissions for the everyone security group or assign the file a different default group that contains other users.
    This can be a pain in a large environment if you want to create and share documents among several people. Fortunately, there’s a simple solution for how to solve this problem. 
    There are three additional bits of information that Linux stores for each file and directory:
        + **The set user id (SUID)**
            When a file is executed by a user, the program runs under the permissions of the file owner.
        + **The set group id (SGID)**
            For a file, the program runs under the permissions of the file group. For a directory, new files created in the directory use the directory group as the default group.
        + **The sticky bit**
            The file remains (sticks) in memory after the process ends.

    The SGID bit is important for sharing files. By enabling the SGID bit, you can force all new files created in a shared directory to be owned by the directory’s group and now the individual user’s group.

    The SGID is set using the chmod command. It’s added to the beginning of the standard threedigit octal value (making a four-digit octal value), or you can use the symbol `s` in symbolic mode.

    If you’re using octal mode, you’ll need to know the arrangement of the bits, shown in the following table:

    | **Binary** | **Octal** |         **Description**          |
    |------------|-----------|----------------------------------|
    |        000 |         0 | All bits are cleared             |
    |        001 |         1 | The sticky bit is set            |
    |        010 |         2 | The SGID bit is set              |
    |        011 |         3 | The SGID and sticky bits are set |
    |        100 |         4 | The SUID bit is set.             |
    |        101 |         5 | The SUID and sticky bits are set |
    |        110 |         6 | The SUID and SGID bits are set   |
    |        111 |         7 | All bits are set                 |
    |            |           |                                  |

    So, to create a shared directory that always sets the directory group for all new files, all you need to do is set the SGID bit for the directory as follows:
        ```
        $ mkdir testdir
        $ ll
        drwxr-xr-x. 2 root   root      6 Mar 27 07:57 testdir
        $ chgrp shared testdir
        $ ll
        drwxr-xr-x. 2 root   shared    6 Mar 27 07:57 testdir
        $ chmod g+s testdir
        drwxr-sr-x. 2 root   shared    6 Mar 27 07:57 testdir
        $ umask 002
        $ cd testdir
        $ touch testfile
        $ ll
        -rw-rw-r--. 1 root shared 0 Mar 27 08:01 testfile
        ```

    注意：当临时将一个用户加入'shared'用户组且在加入用户组之前，该用户已经登录，这种情况下，该用户必须先退出Bash环境后再重新登录，才能修改'shared'用户组拥有写权限的文件。





