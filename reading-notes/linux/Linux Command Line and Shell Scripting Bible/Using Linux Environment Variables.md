[Back](index.md)

### Reading Notes : "Using Linux Environment Variables"

### Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 5: Using Linux Environment Variables
- Pages: {153, 176}
- Reading Time: 27/03/2017 6:40 ~ 8:00

<span id="chapterList"></span>
### Chapter List
- [What Are Environment Variables](#WhatAreEnvironmentVariables)
- [Variable Arrays](#VariableArrays)
- [Using Command Aliases](#UsingCommandAliases)

<span id="WhatAreEnvironmentVariables">[Chapter List](#chapterList)</span>
#### What Are Environment Variables?

- What Are environment Variables?

    The bash shell uses a feature called environment variables to store information about the shell session and the working environment (thus the name environment variables). This feature also allows you to store data in memory that can be easily accessed by any program or script running from the shell.

    There are two types of environment variables in the bash shell:
    + Global variables
    + Local variables

- Global environment variables

    Global environment variables are visible from the shell session, and any child processes that the shell spawns. Local variables are only available in the shell that creates them. 

    The system environment variables always use all capital letters to differentiate them from normal user environment variables.

    To view the global environment variables, use the `printenv` command:
        `$ pringenv`

    To display the value of an individual environment variable, use the `echo` command, When referencing environment variable, you must place a dollar sign before the environment variable name:
        `$ echo $HOME`

- Local environment variables

    Local environment variables, as their name implies, can be seen only in the local process in which they are defined.

    There isn’t a command that displays only local environment variables. The `set`command displays all of the environment variables set for a specific process. However, this also includes the global environment variables:
        `$ set`

- Setting Environment Variables
    
    + Setting local environment variables
    
        Once you set a local environment variable, it’s available for use anywhere within your shell process. However, if you spawn another shell, it’s not available in the child shell.

        设置本地环境变量的方式如下：
        ```
        $ test=testing
        $ echo $test
        testing
        ```
        注意：
        1. 等号"="两边不能有空格
        2. If you need to assign a string value that contains spaces, you’ll need to use a single quotation mark to delineate the beginning and the end of the string. like `$ test='testing a long string'`

    + Setting global environment variables

        The method used to create a global environment variable is to create a local environment variable, then export it to the global environment.

        ```
        $ test='another testing'
        $ export test
        $ bash
        $ echo $test
        another testing
        ```
        **_Notice_** that when exporting a local environment variable, you don’t use the dollar sign to reference the variable’s name.
        
- Removing Environment Variables

    you can also remove an existing environment variable. This is done by using the `unset` command:
        `$ unset test`
    When referencing the environment variable in the unset command, remember not to use the dollar sign

- Default Shell Environment Variables

    There are specific environment variables that the bash shell uses by default to define the system environment. The table bellow shows some important default environment variables:

    | **Variable** |                         **Description**                         |
    |--------------|-----------------------------------------------------------------|
    | CDPATH       | A colon-separated list of directories used as a search path for |
    |              | the cd command                                                  |
    | HOME         | The current user’s home directory                               |
    | MAIL         | The filename for the current user’s mailbox.                    |
    | PATH         | A colon-separated list of directories where the shell looks     |
    |              | for commands                                                    |
    | PS1          | The primary shell command line interface prompt string          |
    | PS2          | The secondary shell command line interface prompt string        |
    | BASH         | The full pathname to execute the current instance of the        |
    |              | bash shell                                                      |
    | BASH VERSION | The version number of the current instance of the bash shell    |
    | COLUMNS      | the width of the terminal used for the current instance of      |
    |              | the bash shell                                                  |
    |              |                                                                 |


- Locating System Environment Variables

    When you start a bash shell by logging in to the Linux system, by default bash checks several files for commands. These files are called startup files. The startup files bash processes depend on the method you use to start the bash shell. There are three ways of starting a bash shell:
    1) As a default login shell at login time
    2) As an interactive shell that is not the login shell
    3) As a non-interactive shell to run a script
    
    + Login shell

        When you log in to the Linux system, the bash shell starts as a login shell. The login shell looks for four different startup files to process commands from. The order in which the bash shell processes the files is:
        1) /etc/profile
        2) $HOME/.bash_profile
        3) $HOME/.bash_login
        4) $HOME/.profile

        * The /etc/profile file

            The /etc/profile file is the main default startup file for the bash shell. Whenever you log in to the Linux system, bash executes the commands in the /etc/profile startup file.

            In /etc/profile's content, there’s a `for` statement that iterates through any files located in the /etc/profile.d directory. This provides a place for the Linux system to place application-specific startup files that will be executed by the shell when you log in.


        * The $HOME startup files

            The remaining three startup files are all used for the same function — to provide a user-specific startup file for defining user-specific environment variables. Most Linux distributions use only one of these three startup files: 
            1) $HOME/.bash_profile
            2) $HOME/.bash_login
            3) $HOME/.profile
            Since they are in the user’s HOME directory, each user can edit the files add his or her own environment variables that are active for every bash shell session they start.

    + Interactive shell

        If you start a bash shell without logging into a system, you start what’s called an interactive shell. The interactive shell doesn’t act like the login shell, but it still provides a CLI prompt for you to enter commands.

        If bash is started as an interactive shell, it doesn’t process the /etc/profile file. Instead, it checks for the .bashrc file in the user’s HOME directory.

        The .bashrc file does two things. First, it checks for a common bashrc file in the /etc directory. Second, it provides a place for the user to enter personal aliases

    + Non-interactive shell

        Non-interactive shell is the shell that the system starts to execute a shell script. This is different in that there isn’t a CLI prompt to worry about. However, there may still be specific startup commands you want to run each time you start a script on your system.


<span id="VariableArrays">[Chapter List](#chapterList)</span>
#### Variable Arrays

- What's Variable Arrays

    Environment variables can be used as arrays. An array is a variable that can hold multiple values. Values can be referenced either individually or as a whole for the entire array.

- Define Variable Arrays
 
    To set multiple values for an environment variable, just list them in parentheses, with each value separated by a space:
        `$ mytest=(var1 var2 var3 var4 var5)`

- Reference Variable Arrays

    + Reference an individual array element
        To reference an individual array element, you must use a numerical index value, which represents its place in the array. The numeric value is enclosed in square brackets:
            ```
            $ echo ${mytest[2]}
            var3
            $ echo $mytest
            var1
            $ echo ${mytest[*]}
            var1 var2 var3 var4 var5
            ```
        Note that: 
        1) Reference the whole array will only display the first value.  
        2) Environment variable arrays start with an index value of zero.
        3) To display an entire array variable, you use the asterisk wildcard character as the index value

    + Change the value of an individual array element    
        You can also change the value of an individual index position:
            ```
            $ mytest[1]=changed
            ```
    
    + Remove an individual value within the array
        Use the `unset` command to remove an individual value within the array:
            ```
            $ unset mytest[1]
            ```

    + Remove the entire array
        You can remove the entire array just by using the array name in the `unset` command:
            ```
            $ unset mytest
            ```


<span id="UsingCommandAliases">[Chapter List](#chapterList)</span>
#### Using Command Aliases        

- 