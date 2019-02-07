[Back](index.md)

# Subject: Shell中的条件判断

# Introduction

# Content Catalogue

# Content

## `expr`命令

`expr`命令用于表达式求值。

### `expr`命令格式

`expr`命令格式如下所示：     
```
$ expr 1 + 5
6
$ echo $?
0
```
**_注意_**：在运算符两边必须有至少1个空格。

### `expr`命令的执行结果

`expr`命令将表达式的运算结果输出到STDOUT。并且会有一个返回值（通过'$?'环境变量查看），其返回值的规则为：
  
1. 当整个表达式的运算结果不为"null"且不为"0"时，返回值为0
2. 当整个表达式的运算结果为"null"或"0"时，返回值为1
3. 如果表达式有语法错误，返回值为2
4. 当发生错误时，返回值为3
  
例如，`$ expr 2 - 2`的运算结果为0，所以返回值是1；而`$ expr 1 + 2`的运算结果为3，所以返回值是0。

### `expr`命令支持的运算符

`expr`命令支持的运算符如下表所示：

|     **Operator**    |                   **Output To STDOUT**                  | ** Need Escape** |
|---------------------|---------------------------------------------------------|------------------|
| ARG1 竖线 ARG2      | ARG1 if ARG1 is neither null nor 0, otherwise ARG2      | true             |
| ARG1 & ARG2         | ARG1 if neither argument is null or 0, otherwise 0      | true             |
| ARG1 < ARG2         | 1 if ARG1 is less than ARG2; otherwise 0                | true             |
| ARG1 <= ARG2        | 1 if ARG1 is less than or equal to ARG2; otherwise 0    | true             |
| ARG1 = ARG2         | 1 if ARG1 is equal to ARG2; otherwise 0                 |                  |
| ARG1 != ARG2        | 1 if ARG1 is not equal to ARG2; otherwise 0             |                  |
| ARG1 >= ARG2        | 1 if ARG1 is greater than or equal to ARG2; otherwise 0 | true             |
| ARG1 > ARG2         | 1 if ARG1 is greater than ARG2; otherwise 0             | true             |
| ARG1 + ARG2         | **_arithmetic_** sum of ARG1 and ARG2                   |                  |
| ARG1 - ARG2         | **_arithmetic_** difference of ARG1 and ARG2            |                  |
| ARG1 * ARG2         | **_arithmetic_** product of ARG1 and ARG2               | true             |
| ARG1 / ARG2         | **_arithmetic_** quotient of ARG1 divided by ARG2       |                  |
| ARG1 % ARG2         | **_arithmetic_** remainder of ARG1 divided by ARG2      |                  |
| STRING : REGEXP     | anchored pattern match of REGEXP in STRING              |                  |
| match STRING REGEXP | same as STRING : REGEXP                                 |                  |
| substr STRING       | substring of STRING, POS counted from 1                 |                  |
| POS LENGTH          | `$ expr substr 'abcd' 2 2` outputs 'cd'                 |                  |
| index STRING CHARS  | index in STRING where any CHARS is found, or 0          |                  |
| length STRING       | length of STRING                                        |                  |
| + TOKEN             | Interpret TOKEN as a string, even if it’s a keyword     |                  |
| (EXPRESSION)        | the value of EXPRESSION                                 |                  |

**_说明_**：
1. "Output To STDOUT"一列中如果以"**_arithmetic_**"开头，表明该运算符为数学运算符，运算符两边的操作数只能是整数。否则，既可以是整数也可以是字符串。如，"+"运算符为"arithmetic"运算符，只能用于两个整数的相加；而"<"运算符不是"arithmetic"运算符，所以也可以用于字符串的比较。

2. 当运算符两边都为整数时，操作数将按数值进行比较；否则，将按字典顺序进行比较。如，`$ expr 10 \< 8`将按数值进行比较，运算结果为0；而`$ expr 10 \< 2a`将按字典顺序进行比较，运算结果为1。

3. 关于`STRING : REGEXP` 和 `match STRING REGEXP` 
    1. 这两个表达式为 "anchored pattern match"，即该表达式的输出为最后匹配的位置，如：`$ expr 'abcd' : 'ab'` 将输出2，即匹配的最后位置为第2个字符。
    2. 这两个表达式默认为从字符串STRING的开始位置进行匹配，不能进行部分匹配（而grep可以进行部分匹配），所以，`$ expr 'abcd' : 'ab'` 的结果为2（即匹配到'abcd'中的'ab'子串），而 `$ expr 'abcd' : 'cd'` 并不能匹配到'abcd'中的子串'cd'，因为'cd'子串不是从开始位置匹配的。
    3. 当REGEXP中的部分模式使用'\('和'\)'包裹时，表达式的输出将变为匹配的子串中与'\('和'\)'之间子模式匹配的内容。如：`$ expr 'abcd' : 'a\(b\)'` 将输出"b"。

4. 关于`+ TOKEN`
    
    `+ TOKEN`用于将`TOKEN`所表示的字符串解释为一个普通字符串，即便当它是一个关键字(如match)。   
    比如，当你要输出字符串'match'的长度时，如果直接使用表达式 `$ expr length 'match'`，将报语法错误，因为'match'是expr表达式里的一个关键字，此时，你必须使用`+ TOKEN`的方式，正确的写法为：`$ expr length + 'match'`。
    你也可以在`TOKEN`为普通字符串的时候这样使用，并不会有什么不好的用作，如：`$ expr length + 'abcd'` 和 `$ expr length 'abcd'`是一样的。

4. `expr`命令中也可以使用变量，如：    
    ```
    $ num1=2
    $ num2=3
    $ expr $num1 + $num2
    5
    ```


## `$[]`命令

`$[]`运算符用于执行算数运算。注意，**该命令并不能用于shell条件判断中。**

### `$[]`命令的基本格式

命令格式如下：   

```
$ $[ EXPRESSION ]
```

其中，EXPRESSION为算术运算表达式。

### `$[]`命令的执行结果

`$[]`命令并不直接输出任何信息到STDOUT，需要借助`echo`命令将其输出信息输出到STDOUT。     
`$[]`命令执行完毕后有一个返回值（通过'$?'环境变量查看），其返回值的规则为：     
当整个算术运算表达式的计算结果为一个正常值时，返回值为0；否则返回1。     
其中，对于非正常值得理解为：当表达式包含错误的格式或其他异常情况（如被除数为0），例如：`$ echo $[1/0]` 和 `$ echo $[4.1/2]`的返回值都为1，因为第一个被除数为0，而第二个的除数为浮点数（不支持浮点数运算）。

### `$[]`命令支持的算数运算符      

`$[]`命令支持的算数运算符如下表所示：

|     **Operator**    |                             **Output To STDOUT**                            |
|---------------------|-----------------------------------------------------------------------------|
| ARG1 竖线 ARG2      | ARG1和ARG2按位'或'的运算结果。如：`2 竖线 4`输出6                           |
| ARG1 & ARG2         | ARG1和ARG2按位'与'的运算结果。如：`2 & 4`输出0                              |
| ARG1 < ARG2         | 1 if ARG1 is less than ARG2; otherwise 0                                    |
| ARG1 <= ARG2        | 1 if ARG1 is less than or equal to ARG2; otherwise 0                        |
| ARG1 == ARG2        | 1 if ARG1 is equal to ARG2; otherwise 0（expr表达式中等于比较是一个等于号） |
| ARG1 != ARG2        | 1 if ARG1 is not equal to ARG2; otherwise 0                                 |
| ARG1 >= ARG2        | 1 if ARG1 is greater than or equal to ARG2; otherwise 0                     |
| ARG1 > ARG2         | 1 if ARG1 is greater than ARG2; otherwise 0                                 |
| ARG1 + ARG2         | sum of ARG1 and ARG2                                       |
| ARG1 - ARG2         | difference of ARG1 and ARG2                                |
| ARG1 * ARG2         | product of ARG1 and ARG2                                   |
| ARG1 / ARG2         | quotient of ARG1 divided by ARG2                           |
| ARG1 % ARG2         | remainder of ARG1 divided by ARG2                          |

**_注意_**:    
1. 表达式 _EXPRESSION_ 与`$[]`之间 以及 表达式 _EXPRESSION_ 中的操作数和运算符之间可以有空格，也可以没有。如：`$[1+2]`和`$[ 1 + 2 ]`的输出结果是一样的。这点和`expr`表达式不一样。
2. _EXPRESSION_ 中的所有运算符都不需要转义，这点和`expr`表达式不一样。
3. EXPRESSION中的参数只能是整数，不能是浮点数或字符串。


## `test`和中括号`[ ]`命令

`test`和中括号`[ ]`命令用于测试条件表达式。并当条件表达式的结果为true时，`test`和中括号`[ ]`命令返回0；否则，返回1.

注意：`test`命令和中括号`[ ]`命令是等价的，只是写法不一样。

### `test`和中括号`[ ]`命令的格式

`test`和中括号`[ ]`命令格式，如下所示：   
```
test EXPRESSIONS
```
或
```
[ EXPRESSIONS ]
```
其中，EXPRESSIONS为变量和值组成的单个条件表达式 或 由多个条件表达式组成的复合条件表达式。
注意，第二种写法必须在EXPRESSIONS和中括号之间放置至少1个空格。

复合条件表达式包括以下几种：  

1. !EXPRESSION
    对条件表达式EXPRESSION取反
2. EXPRESSION1 -a EXPRESSION2
    当EXPRESSIONS1和EXPRESSIONS2同时为true时，整个表达式的值才为true；否则为false.
3. EXPRESSION1 -o EXPRESSION2
    当EXPRESSIONS1和EXPRESSIONS2同时为false时，整个表达式的值才为false；否则为true.

### `test`命令支持的条件表达式

`test`命令支持三种类型的条件表达式，分别是：           
1. 数值比较
2. 字符串比较
3. 文件比较

#### 数值比较

`test`命令可以用于两个数值之间的比较。其支持的比较方式如下表所示：　           

| **Comparison** |                **Return value**                |
|----------------|------------------------------------------------|
| n1 -eq n2      | 0 if n1 is equal to n2, else 1                 |
| n1 -ge n2      | 0 if n1 is greater than or equal to n2, else 1 |
| n1 -gt n2      | 0 if n1 is greater than n2, else 1             |
| n1 -le n2      | 0 if n1 is less than or equal to n2, else 1    |
| n1 -lt n2      | 0 if n1 is less than n2, else 1                |
| n1 -ne n2      | 0 if n1 is not equal to n2, else 1             |

**_注意_**
1. `test`命令只能用于整数之间的比较，不能用于浮点数的比较。


#### 字符串比较

`test`命令也可用于字符串之间的比较，其支持的比较方式如下表所示：　　

| **Comparison** |                **Return Value**                |
|----------------|-----------------------------------------------|
| str1 = str2    | 0 if str1 is the same as string str2, else 1.     |
| str1 != str2   | 0 if str1 is not the same as str2, else 1.        |
| str1 < str2    | 0 if str1 is less than str2, else 1.              |
| str1 > str2    | 0 if str1 is greater than str2, else 1.            |
| -n str1        | 0 if str1 has a length greater than zero, else 1. |
| -z str1        | 0 if str1 has a length of zero, else 1.           |

**_注意_**：
1. 字符串比较中的"<"和">"符号必须使用转义字符（如 `test 'ab' \> 'cd'` 和 `[ 'ab' \> 'cd' ]`），否则，会被当做输入或输出重定向符号。
2. 字符串之间的比较是依据字符对应的ASCII码的数值进行比较的，比如'a'和'A'的ASCII码值分别为97和65，所以'a'比'A'大。这一点与`sort`命令的排序依据有一些不同。

#### 文件比较

`test`命令可用于文件比较，其支持的比较方式如下表所示：　　

| **Comparision** |                      **Return Value**                      |
|-----------------|------------------------------------------------------------|
| -d file         | 0 if file exists and is a directory, else 1.               |
| -e file         | 0 if file exists, else 1.                                  |
| -f file         | 0 if file exists and is a file, else 1.                    |
| -r file         | 0 if file exists and is readable, else 1.                  |
| -s file         | 0 if file exists and is not empty, else 1.                 |
| -w file         | 0 if file exists and is writable, else 1.                  |
| -x file         | 0 if file exists and is executable, else 1.                |
| -O file         | 0 if file exists and is owned by the current user, else 1. |
| -G file         | 0 if file exists and the default group is                  |
|                 | the same as the current user, else 1.                      |
| file1 -nt file2 | 0 if file1 is newer than file2, else 1.                    |
| file1 -ot file2 | 0 if file1 is older than file2, else 1.                    |


## 双括号命令 `(( ))`

双括号命令`(( ))`用于算术运算的条件比较。详细的内容请看：[双括号运算符](../../reading-notes/linux/Linux Command Line and Shell Scripting Bible/Using Structured Commands.md#AITF-UDP)

## 双中括号命令 `[[ ]]`

双中括号命令提供了更高级的字符串比较。详细的内容请看：[双括号运算符](../../reading-notes/linux/Linux Command Line and Shell Scripting Bible/Using Structured Commands.md#AITF-UDB)