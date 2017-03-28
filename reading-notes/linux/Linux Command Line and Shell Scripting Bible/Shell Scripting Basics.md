[Back](index.md)

# Reading Notes ~ Shell Scripting Basics

## Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 8: Shell Scripting Basics
- Pages: {231, }
- Reading Time: 27/03/2017 19:30 ~ 23:00

## Content Navigation <a id="≡"></a>
- [Using Multiple Commands](#UMC)
- [Creating a Script File](#CS)
- [Displaying Messages](#DM)
- [Using Variables](#UA)
- [Redirecting Input and Output](#RIAO)
    + [Output Redirection](#RIAO-OR)
    + [Error Output Redirection](#RIAO-EOR)
    + [Input Redirection](#RIAO-IN)
    + [Discard Output](#RIAO-DO)
    + [Redirection to Same Destination](#RIAO-RTSD)
    + [Redirection Summary](#RIAO-RS)
- [Pipes](#P)
- [Performing Math](#PM)

## Reading Notes

### Using Multiple Commands <a id="UMC">[≡](#≡)</a>

  If you want to run two commands together, you can enter them on the same prompt line, separated with a semicolon, like: 
    ```
    $ date; who
    Tue Mar 28 06:19:44 PDT 2017
    root     pts/0        2017-03-28 06:19 (gateway)
    ```
  Using this technique, you can string together as many commands as you wish, up to the maximum command line character count of **255** characters.

### Creating a Script File <a id="CS">[≡](#≡)</a>

- The Step to Create a Script File

    1. Create a new file
        Use `touch` command to create a new file named "test.sh": `$ touch test.sh`
    2. Specify the shell
        Specify the shell you are using in the first line of the file. The format for this is: `#!/bin/bash`
    3. Put commands to the file
        You can use the semicolon and put both commands on the same line if you want to, but in a shell script, you can list commands on separate lines. The shell will process commands in the order in which they appear in the file.
    4. Add permission to execute the file
        Give myself permission to execute the file, using the chmod command: 
        `$ chmod u+x test.sh`


### Displaying Messages <a id="DM">[≡](#≡)</a>

  `echo`

### Using Variables <a id="UA">[≡](#≡)</a>

- Environment variables 
    
    You can reference environment variables within your scripts by:

    + Precede environment variable’s name by a dollar sign:
        `$ echo $HOME`
    + Place the environment variable name between the braces:
        `$ echo ${HOME}`
    + Place the environment variable within the double quotation marks:
        `$ echo "Your home dir is: $HOME."`

- User variables

    User variables can be any text string of up to 20 letters, digits, or an underscore character. User variables are case sensitive.

    Values are assigned to user variables using an equal sign. No spaces can appear between the variable, the equal sign, and the value.

        ```
        var1=10
        var2=$var1
        echo $var2
        ```

- The backtick

    The backtick allows you to assign the **output** of a shell command to a variable. You must surround the entire command line command with backtick characters:
        ```
        now=`date`
        echo "The date and time now is: $now"
        ```
    The shell runs the command within the backticks, and assigns the output to the variable now.
    
    Here’s a popular example of how the backtick is used to capture the current date and use it to create a unique filename in a script:
        ```
        #!/bin/bash
        # copy the /usr/bin directory listing to a log file
        today=`date +%y%m%d`
        ls /usr/bin -al > log.$today
        ```


### Redirecting Input and Output <a id="RIAO">[≡](#≡)</a>

- Output redirection <a id="RIAO-OR">[≡](#≡)</a>

    The bash shell uses the greater-than symbol `>` for output redirection, It' will send output from a command to a file or device instead of terminal:
        ```
        date > test.txt
        cat test.txt
        Tue Mar 28 07:08:42 PDT 2017
        ```
    The redirect operator `>` created the file test.txt (using the default umask settings) and redirected the standard output from the date command to the test.txt file. If the output file already exists, the redirect operator overwrites the existing file with the new file data.

    If you want to append output from a command to an existing file, you can use the double greater-than symbol `>>` to append data:
        ```
        cat test.txt
        Tue Mar 28 07:08:42 PDT 2017
        date >> test.txt
        cat test.txt
        Tue Mar 28 07:08:42 PDT 2017
        Tue Mar 28 07:09:18 PDT 2017
        ```

- Error Output Redirection <a id="RIAO-EOR">[≡](#≡)</a>

    The bash shell uses symbol `2>` for error output redirection, It' will send error output from a command to a file or device instead of terminal:
        ```
        $ cat non-exists-file 2> error.log
        $ cat error.log
        cat: non-exists-file: No such file or directory
        ```
    When the file "non-exists-file" is not exists, the `cat` command will output an error, the example above use error output redirection to redirect the error info to the file error.log. 

    If you use "output redirection", the error info will not be redirected as it's an error output, not an standard output.

    Like output redirection, you can also use `2>>` to append instead of overwriting the output to the redirect file or device.

    Example: 同时指定输出重定向和错误输出重定向
        ```
        $ su - setamv
        $ find /home -name somefile > ~/find_right.re 2> ~/find_error.re
        ```
    非root用户使用find命令查找文件时，可能遇到permission denied错误，上面的示例将错误信息重定向输出到 ~/find_error.re中，正确的查找结果输出到 ~/find_right.re中。

- Input Redirection <a id="RIAO-IR">[≡](#≡)</a>

    Input redirection is the opposite of output redirection. 

    + `<` File Input Redirection

        The file input redirection takes the content of a file and redirects it to a command, The input redirection symbol is the less-than symbol `<`:
            ```
            command < inputfile
            ```

        The easy way to remember this is that the command is always listed first in the command line, and the redirection symbol ‘‘points’’ to the way the data is flowing. The less-than symbol indicates that the data is flowing from the input file to the command.

        Example: Using input redirection with the wc command:
            ```
            wc < test.txt
            2 11 60
            ```
        The example above, redirecting a text file to the wc command, and get a quick count of the lines, words, and bytes in the file.

    + `<<` Inline Input Redirection
    
        The "inline input redirection" allows you to specify the data for input redirection on the command line instead of in a file. The inline input redirection symbol is the double less-than symbol `<<`.

        Besides this symbol, you must specify a text marker that delineates the beginning and end of the data used for input. You can use any string value for the text marker, but it must be the same at the beginning of the data and the end of the data.

        For example, count total word from standard input:
            ```
            $ wc -w << EOF
            > Hello
            > Script
            > EOF
            2
            ```
        When using inline input redirection on the command line, the shell will prompt for data using the secondary prompt, defined in the PS2 environment variable.

- Discard Output <a id="RIAO-DO">[≡](#≡)</a>

    When you want to supress the output of the command and don't direct the output to anywhere else, a file or device, you can use "/dev/null". 

    All output redirected to "/dev/null" will be discarded. For example:
        ```
        ls -al > /dev/null
        ```
    The script above will neither output anything to standard output nor output to a file. the output of `ls` command is vanished, so, the /dev/null like a trash to collect everything discarded.


- Redirection to Same Destination <a id="RIAO-RTSD">[≡](#≡)</a>

    当你想要将标准输出和标准错误输出重定向到相同的目标，该如何做呢？

    + 使输出和错误输出重定向相同的文件
            `$ find /home -name somefile > ~/find.re 2> ~/find.re`
        这种写法存在一定的问题，由于两股数据同时写入一个文件，又没有使用特殊的语法，此时两股数据可能会交叉写入该文件内，造成次序的错乱。并且这里标准输出和标准错误输出都是覆盖式的写入，有可能部分数据会丢失。

    + 使用 `2>&1`     
            `$ find /home -name somefile > ~/find.re 2>&1`
        上面的 `2>&1` 表示标准错误输出重定向到与标准输出一样的目标。
        当然，也可以使用 `2>>&1`，表示以“追加”的方式写入重定向的目标。

    + 使用 `&>1`
            `$ find /home -name somefile &> ~/find.re`
        这种写法也OK
        当然，也可以使用 `&>>`，表示以“追加”的方式写入重定向的目标。

- Redirection Summary <a id="RIAO-RS">[≡](#≡)</a>

    As specified above sections, there are three kinds of redirections:     
    
    + output redirection
    + error output redirection
    + input redirection
    
    Meanwhile, each redirection has a redirection code:

    + 0: the code of input redirection
    + 1: the code of output redirection
    + 2: the code of error output redirection
    
    Like error output redirection, You can also precede the symbol with redirection code in "input redirection" and "output redirection": `0<`, `0<<`, `1>`, `1>>`。

    Beacuse there is an unique interpretation with `>`, `>>`, `<`, `<<`, It's no matter to omit the preceding redirection code in "input redirection" and "output redirection", but not "error output redirection".
    
    The following is a table which summarize all kinds of redirections (Redir means Redirection):

    |      **Name**      | **Code** | **Symbol** |          **Description**           |
    |--------------------|----------|------------|------------------------------------|
    | Input Redir        |        0 | `<`        | redirect input from a file,        |
    |                    |          | `<<`       | redirect input from command line   |
    | Output Redir       |        1 | `>`        | redirect output by overwrite       |
    |                    |          | `>>`       | redirect output by append          |
    | Error Output Redir |        2 | '2>'       | redirect error output by overwrite |
    |                    |          | `2>>`      | redirect error output by append    |



### Pipes <a id="P">[≡](#≡)</a>

Instead of redirecting the output of a command to a file, you can redirect the output to another command. This process is called piping. The pipe symbol is the bar operator (|):
    `command1 | command2`

Piping provides a way to link commands to provide more detailed output. Don’t think of piping as running two commands back to back though. The Linux system actually runs both commands at the same time, linking them together internally in the system. As the first command produces output, it’s sent immediately to the second command. No intermediate files or buffer areas are used to transfer the data.

For example, you can first sort the `rpm` output and then read the sorted output with `more` command as follows:
    ```
    $ rpm -qa | sort | more
    ```

### Performing Math <a id="PM">[≡](#≡)</a>

There a two different ways to perform mathematical operations in your shell scripts。

- The `expr` command
    
    Originally, the Bourne shell provided a special command that was used for processing mathematical equations. The `expr` command allowed the processing of equations from the command line, but it is extremely clunky:
        ```
        $ expr 1 + 5
        6
        ```

    The expr command recognizes a few different mathematical and string operators, shown in table bellow:

    |     **Operator**    |                     **Description**                     |
    |---------------------|---------------------------------------------------------|
    | ARG1 / ARG2         | Return ARG1 if neither argument is null or zero;        |
    |                     | otherwise, return ARG2                                  |
    | ARG1 & ARG2         | Return ARG1 if neither argument is null or zero;        |
    |                     | otherwise, return 0                                     |
    | ARG1 < ARG2         | Return 1 if ARG1 is less than ARG2;                     |
    |                     | otherwise, return 0                                     |
    | ARG1 <= ARG2        | Return 1 if ARG1 is less than or equal to ARG2;         |
    |                     | otherwise, return 0.                                    |
    | ARG1 = ARG2         | Return 1 if ARG1 is equal to ARG2;                      |
    |                     | otherwise, return 0                                     |
    | ARG1 != ARG2        | Return 1 if ARG1 is not equal to ARG2;                  |
    |                     | otherwise, return 0                                     |
    | ARG1 >= ARG2        | Return 1 if ARG1 is greater than or equal to ARG2;      |
    |                     | otherwise, return 0                                     |
    | ARG1 > ARG2         | Return 1 if ARG1 is greater than ARG2;                  |
    |                     | otherwise, return 0                                     |
    | ARG1 + ARG2         | Return the arithmetic sum of ARG1 and ARG2              |
    | ARG1 - ARG2         | Return the arithmetic difference of ARG1 and ARG2       |
    | ARG1 * ARG2         | Return the arithmetic product of ARG1 and ARG2.         |
    | ARG1 / ARG2         | Return the arithmetic quotient of ARG1 divided by ARG2  |
    | ARG1 % ARG2         | Return the arithmetic remainder of ARG1 divided by ARG2 |
    | STRING : REGEXP     | Return the pattern match if REGEXP matches a            |
    |                     | pattern in STRING                                       |
    | match STRING REGEXP | Return the pattern match if REGEXP matches a            |
    |                     | pattern in STRING.                                      |
    | substr STRING       | Return the substring LENGTH characters in length,       |
    | POS LENGTH          | starting at position POS (starting at 1).               |
    | index STRING CHARS  | Return position in STRING where CHARS is found;         |
    |                     | otherwise, return 0                                     |
    | length STRING       | Return the numeric length of the string STRING          |
    | + TOKEN             | Interpret TOKEN as a string, even if it’s a keyword     |
    | (EXPRESSION)        | Return the value of EXPRESSION                          |

