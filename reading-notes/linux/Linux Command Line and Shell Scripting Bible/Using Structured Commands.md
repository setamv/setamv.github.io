[Back](index.md)

# Reading Notes ~ Using Structured Commands

## Profile
- Book: Linux Command Line and Shell Scripting Bible
- Chapter: Chapter 9: Using Structured Commands
- Pages: {259, }
- Reading Time: 29/03/2017 21:07 ~ 30/03/2017 23:22

## Content Navigation <a id="≡"></a>
- [if-then Statement](#ITS)
- [if-then-else Statement](#IITS)
- [Nesting ifs](#NI)
- [The `test` Command](#TC)
    + [Numeric comparisons](#TC-NC)
    + [String comparisons](#TC-SC)
    + [File comparisons](#TC-FC)
- [Compound Condition Testing](#CCT)
- [Advanced if-then Features](#AITF)
    + [Using double parentheses](#AITF-UDP)
    + [Using double brackets](#AITF-UDB)
- [The case Command](#CaseC)

## Reading Notes

The structured commands allow you to alter the flow of operation of the program, executing some commands under some conditions, while skipping others under other conditions. There are quite a few structured commands available in the bash shell.

### if-then Statement <a id="ITS">[≡](#≡)

The if-then statement has the following format:     
```
if command
then
    commands
fi
```
or you can put keyword _then_ on then same line with _if_ as follows:       
```
if command; then
    commands
fi
```
Be aware of semicolon after _command_ in if statement.

In other programming languages, the object after the if statement is an equation that is evaluated for a TRUE or FALSE value. That’s not how the bash shell if statement works.  

The bash shell if statement runs the command defined on the _if_ line. If the exit status of the command is zero (the command completed successfully), the commands listed under the _then_ section are executed. If the exit status of the command is anything else, the _then commands_ aren’t executed, and the bash shell moves on to the next command in the script.       
Example:             
```
$ cat test1
#!/bin/bash
# testing the if statement
if date
then
echo "it worked"
fi
$ chmod u+x test1
$ ./test1
Wed Mar 29 06:20:28 PDT 2017
it worked
```
The example above executed the date command listed on the if line. Since the exit status was zero, it also executed the echo statement listed in the then section.

### if-then-else Statement <a id="ITES">[≡](#≡)</a>

The if-then-else statement provides another group of commands in the statement:     
```
if command
then
    commands
else
    commands
fi
```
or you can put keyword _then_ on then same line with _if_ as follows:       
```
if command; then
    commands
else
    commands
fi
```

If the command in the _if_ statement line returns with an exit status code of zero, the commands listed in the _then_ section are executed, just as in a normal _if-then_ statement. If the command in the _if_ statement line returns a non-zero exit status code, the bash shell executes the commands in the _else_ section.         

Example:        
```
$ cat test1
#!/bin/bash
testuser=baduser
if grep $testuser /etc/passwd
then
   echo The bash file for user $testuser are:
   ls -al /home/$testuser/.b*
else
   echo The user name $testuser does not exist on this system. 
fi
$ ./test1
The user name baduser does not exist on this system.
```

### Nesting ifs <a id="NI">[≡](#≡)</a>

Sometimes you must check for several situations in your script code. Instead of having to write separate if-then statements, you can use an alternative version of the else section, called elif, it has the following format:  

```
if command1
then
    commands
elif command2
then
    more commands
fi
```

The elif statement line provides another command to evaluate, similarly to the original if statement line. If the exit status code from the elif command is zero, bash executes the commands in the second then statement section.

### The `test` Command <a id="TC">[≡](#≡)</a>

the bash if-then statement has the ability to evaluate any condition other than the exit
status code of a command?       
The answer is **no**, it can’t.     

However, there’s a neat utility available in the bash shell that helps us evaluate other things, using the if-then statement.       

The `test` command provides a way to test different conditions in an if-then statement. If the condition listed in the `test` command evaluates to _true_, the test command exits with a _zero_ exit status code, making the if-then statement behave in much the same way that if-then statements work in other programming languages. If the condition is false, the test command exits with a one, which causes the if-then statement to fail.

The format of the test command is pretty simple:    
    `test condition`

The condition is a series of parameters and values that the test command evaluates. When
used in an if-then statement, the test command looks like this: 

```
if test condition
then
    commands
fi
```

The bash shell provides an alternative way of declaring the test command in an if-then
statement:

```
if [ condition ]
then
    commands
fi
```

**_Note:_** Be careful; you must have a space after the first bracket, and a space before the last bracket or you’ll get an error message.          

There are three classes of conditions the test command can evaluate:        
1. Numeric comparisons
2. String comparisons
3. File comparisons

#### Numeric comparisons <a id="TC-NC">[≡](#≡)</a>

The most common method for using the test command is to perform a comparison of two numeric values. The table bellow shows the list of condition parameters used for testing two numeric values:        

| **Comparison** |               **Description**               |
|----------------|---------------------------------------------|
| n1 -eq n2      | Check if n1 is equal to n2.                 |
| n1 -ge n2      | Check if n1 is greater than or equal to n2. |
| n1 -gt n2      | Check if n1 is greater than n2.             |
| n1 -le n2      | Check if n1 is less than or equal to n2.    |
| n1 -lt n2      | Check if n1 is less than n2.                |
| n1 -ne n2      | Check if n1 is not equal to n2.             |

Example:    
```
$ cat test1
#!/bin/bash
num1=10
num2=20
if [ $num1 -lt $num2 ]; then
   echo $num1 is less than $num2
else
   echo $num1 is greater than or equal to $num2
fi
$ ./test1
10 is less than 20
```

But there is a limitation to the `test` command, it wasn’t able to handle the floating-point value, for example:   
```
$ cat test1
#!/bin/bash
num1=`echo "scale=4; 10/3" | bc`
echo num1 is $num1
num2=3
if [ $num1 -le $num2 ]
then
   echo $num1 is less than or equal to $num2
else
   echo $num1 is greater than $num2
fi
$ ./test1
num1 is 3.3333
./test1: line 5: [: 3.3333: integer expression expected
3.3333 is less than 3
```
This example uses the bash calculator to produce a floating-point value, it uses the `test` command to compare the floating-point value to an integer value. Something obviously went wrong here.       

Remember, the only numbers the bash shell can handle are integers. When we utilize the bash calculator, we just fool the shell into storing a floating-point value in a variable as a string value. This works perfectly fine if all you need to do is display the result, using an echo statement, but this doesn’t work in numeric-oriented functions, such as our numeric test condition.        

#### String comparisons <a id="TC-SC">[≡](#≡)</a>

The table bellow shows the comparison functions you can use to evaluate two string values:

| **Comparison** |                **Description**                |
|----------------|-----------------------------------------------|
| str1 = str2    | Check if str1 is the same as string str2      |
| str1 != str2   | Check if str1 is not the same as str2.        |
| str1 < str2    | Check if str1 is less than str2.              |
| str1 > str2    | Check if str1 is greater than str2            |
| -n str1        | Check if str1 has a length greater than zero. |
| -z str1        | Check if str1 has a length of zero.           |

? When a variable's value is '', both `-n str` and `-z str` are true:       
```
$ cat test
#!/bin/bash
var1=''
if test -n $var1; then
   echo "'$var1' is not empty."
fi
if test -z $var1; then
   echo "'$var1' is empty."
fi
$ ./test
'' is not empty.
'' is empty
```
**_Why_** ?
这里对变量的引用有误，应该这样写：`if test -n "$var1"; then`，因为`-n`是对字符串进行比较的，所以变量`$var1`需要使用双引号引起来。

##### String equality

The equal and not equal conditions are fairly self-explanatory with strings. It’s pretty easy to know when two string values are the same or not:       
```
$ cat test.sh
#!/bin/bash
testuser=susie
if [ $testuser = $USER ]
then
   echo $testuser is the same with current user $USER
else
   echo $testuser is not the same with current user $USER
fi
$ ./test.sh
susie is not the same with current user setamv
```


##### String order

Trying to determine if one string is less than or greater than another is where things start getting tricky. There are two problems that often plague shell programmers when trying to use the greater-than or less-than features of the test command:      

1. The greater-than and less-than symbols must be escaped, or the shell will use them as redirection symbols, with the string values as filenames

    Let us take a look at following example:       

    ```
    $ cat badtest.sh 
    #!/bin/bash
    val1=baseball
    val2=hockey

    if [ $val1 > $val2 ]
    then
       echo $val1 is greater than $val2
    else
       echo $val1 is less than or equal to $val2
    fi
    $ ./badtest.sh
    baseball is greater than hockey
    $ ls -l hockey
    -rw-rw-r--. 1 setamv setamv 0 Mar 29 08:04 hockey
    ```

    By just using the greater-than symbol itself in the script, no errors are generated, but the results are wrong. The script interpreted the greater-than symbol as an output redirection, so it created a file called _hockey_.      

    Since the redirection completed successfully, the test command returns a zero exit status code, which the if statement evaluates as though things completed successfully!

    To fix this problem, you need to properly escape the greater-than symbol as follows:      
    ```
    if [ $val1 \> $val2 ]
    then
       echo $val1 is greater than $val2
    else
       echo $val1 is less than or equal to $val2
    fi
    ```


2. The greater-than and less-than order is not the same as that used with the sort command.

    The way the sort command handles upper-case letters is opposite of the way the test command considers them. Let’s test this feature in a script:

    ```
    $ cat test.sh 
    #!/bin/bash
    val1=testing
    val2=Testing
    if [ val1 \> $val2 ]
    then
       echo $val1 is greater than $val2
    else
       echo $val1 is less than or equal to $val2
    fi
    $ ./test.sh
    testing is greater than Testing

    $ cat sortdata
    testing
    Testing
    $ sort sortdata 
    testing
    Testing
    ```
    Capitalized letters appear less than lower-case letters in the test command. However, when you put the same strings in a file and use the sort command, the lower-case letters appear first.

    This is due to the ordering technique each command uses. The `test` command uses standard ASCII ordering, using each character’s ASCII numeric value to determine the sort order. The `sort` command uses the sorting order defined for the system locale language settings. For the English language, the locale settings specify that lower-case letters appear before upper-case letters in sort order.

- **_Note_**

    Notice that the `test` command uses the standard mathematical comparison symbols for string comparisons, and text codes for numerical comparisons. This is a subtle feature that many programmers manage to get reversed. If you use the mathematical comparison symbols for numeric values, the shell interprets them as string values and may not produce the correct results.

#### File comparisons <a id="TC-FC">[≡](#≡)</a>

The test command allows you to test the status of files and directories
on the Linux filesystem, the table bellow list these comparisions:

| **Comparision** |                    **Description**                     |
|-----------------|--------------------------------------------------------|
| -d file         | Check if file exists and is a directory                |
| -e file         | Checks if file exists                                  |
| -f file         | Checks if file exists and is a file                    |
| -r file         | Checks if file exists and is readable                  |
| -s file         | Checks if file exists and is not empty                 |
| -w file         | Checks if file exists and is writable                  |
| -x file         | Checks if file exists and is executable                |
| -O file         | Checks if file exists and is owned by the current user |
| -G file         | Checks if file exists and the default group is         |
|                 | the same as the current user.                          |
| file1 -nt file2 | Checks if file1 is newer than file2                    |
| file1 -ot file2 | Checks if file1 is older than file2.                   |

##### Checking directories

The -d test checks if a specified filename exists as a directory on the system. The following example is check the _HOME_ directory first and then list the content:    
```
$ cat test
#!/bin/bash
dir=$HOME/shellex
if [ -d $dir ]; then
   echo "directory '$dir' exists. list content:"
   ls -al $dir
else
   echo "directory '$dir' not exists."
fi
```


### Compound Condition Testing <a id="CCT">[≡](#≡)

The `if-then` statement allows you to use Boolean logic to combine tests. There are two Boolean operators you can use:

1. [ condition1 ] && [ condition2 ]
2. [ condition1 ] 双竖线 [ condition2 ]

The first Boolean operation uses the AND Boolean operator to combine two conditions. Both
conditions must be met for the then section to execute.

The second Boolean operation uses the OR Boolean operator to combine two conditions. If either condition evaluates to a true condition, the then section is executed.


### Advanced if-then Features <a id="AITF">[≡](#≡)

There are two relatively recent additions to the bash shell that provide advanced features that you can use in if-then statements:

1. Double parentheses for mathematical expressions
2. Double square brackets for advanced string handling functions


#### Using double parentheses <a id="AITF-UDP">[≡](#≡)

Since the `test` command only allows for simple arithmetic operations in the comparison. The double parentheses command provides more mathematical symbols that programmers from other languages are used to using. The format of the double parentheses command is:      
    
`(( expression1, expression2, ... ))`

The expression term can be any mathematical assignment or comparison expression.        
Table bellow shows the list of operators available for use in the double parentheses command:

|  **Symbol**  |                **Description**                |
|--------------|-----------------------------------------------|
| num1 == num2 | test if num1 is equal to num2, if true,       |
|              | the expression's exit code will be 0, else 1  |
| num1 != num2 | test if num1 is not equal to num2, if true,   |
|              | the expression's exit code will be 0, else 1  |
| num1 > num2  | test if num1 is greater than num2             |
| num1 >= num2 | test if num1 is greater than or equal to num2 |
| num1 < num2  | test if num1 is less than num2                |
| num1 <= num2 | test if num1 is less than or equal to num2    |
| val++        | post-increment                                |
| val--        | post-decrement                                |
| ++val        | pre-increment                                 |
| --val        | pre-decrement                                 |
| !            | logical negation                              |
| ~            | bitwise negation                              |
| **           | exponentiation                                |
| <<           | left bitwise shift                            |
| >>           | right bitwise shift                           |
| &            | bitwise Boolean AND                           |
| 竖线         | bitwise Boolean OR                            |
| &&           | logical AND                                   |
| 双竖线       | logical OR                                    |


**_Notice_**:

1. You don’t need to escape the greater-than symbol in the expression within the double parentheses. 
2. You don't need to place whitespace between operator and operand and parenthes.
3. You can reference a variable' value without a dollar sign '$'. For example:
    ```
    $ a=1
    $ ((a=a+1))
    $ echo $a
    2
    $ (( a = $a+1 ))
    $ echo $a
    3
    ```
    If you want to set a value to a variable, you must not add the dollar sign, like:    
    `$ (($a=$a+1))`
    will output an error as the left operand `$a` is not a variable, it's a number.
4. You can place multiple expression in double parentheses, expressions are separated by comma ','.
5. Prepend a dollar sign($) to double parentheses means return the last expression's result in double parenthese.

Example:

+ Use double parentheses to caculate numbers.       

    ```
    $ cat test.sh
    a=1
    b=2
    c=3

    ((a=a+1))
    echo $a

    a=$((a+1, b++, c--))
    echo $a, $b, $c

    $ ./test.sh
    2
    3, 3, 2
    ```
    
    When execute the script `a=$((a+1, b++, c--))`, the result of last expression `c--` will be returned and set to variable a, and be careful, the `c--` will return the value of c before decrement.

+ Use double parentheses in `if-then`

    ```
    #!/bin/bash
    $ cat test.sh
    a=1
    b=2
    if ((++a == b)); then
       echo "++a is equal to b"
    else
       echo "++a is not equal to b"
    fi

    $ ./test.sh
    ++a is equal to b
    ```


#### Using double brackets <a id="AITF-UDB">[≡](#≡)

The double bracket command provides advanced features for string comparisons. The double
bracket command format is:

`[[ expression ]]`

**_Note_** 

1. You must place whitespace between expression and cracket, but no whitespace is needed between operator and operands in expression, so , you can write as `[[ $USER==setamv ]]`  or `[[ $USER == setamv ]]`,  but you can't write as `[[$USER == setamv]]`. 
2. You must reference a variable with the dollar sign in expression.
2. You need not escape greater than symbol in expression
3.  [[ ]] 中字符串或者${}变量尽量使用”” 双引号扩住，如未使用”“会进行模式和元字符匹配 

The double bracketed expression uses the standard string comparison used in the test command. However, it provides an additional feature that the test command doesn’t,  pattern matching.

In pattern matching, you can define a regular expression that’s matched against the string value:   
```
$ cat test.sh
#!/bin/bash
if [[ $USER == se* ]]; then
   echo "The current user $USER is start with se"
else
   echo "The current user $USER is not start with se"
fi

$ ./test.sh
The current user setamv is start with se
```


### The case Command <a id="CaseC">[≡](#≡)

The case command checks multiple values of a single variable in a list-oriented format:  
```
case variable in
pattern1 | pattern2) commands1;;
pattern3) commands2;;
*) default commands;;
esac
```

Example:    
```
$ cat test.sh
#!/bin/bash
case $USER in
setamv) 
   echo "current user is setamv";;
susie)
   echo "current user is susie";;
hong | angel)
   echo "current user is hong";;
*)
   echo "current user is unknown-$USER";;
esac

$ ./test.sh
current user is setamv
```
