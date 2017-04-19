[Back](index.md)

# Subject: Shell中的条件判断

# Introduction

# Content Catalogue

# Content

## 数学运算

  Shell里面有2种方式执行数学运算，其一是使用`expr`命令；其二是使用中括号的方式 `[运算表达式]`

### `expr`命令

  `expr`命令用于表达式求值。

  1. `expr`命令格式
    `expr`命令格式如下所示：     
    ```
    $ expr 1 + 5
    6
    $ echo $?
    0
    ```
    **_注意_**：在运算符两边必须有至少1个空格。

  2. `expr`命令的执行结果

    `expr`命令将表达式的运算结果输出到STDOUT。并且会有一个返回值（通过'$?'环境变量查看），其返回值的规则为：
    
    1. 当整个表达式的运算结果不为"null"且不为"0"时，返回值为0
    2. 当整个表达式的运算结果为"null"或"0"时，返回值为1
    3. 如果表达式有语法错误，返回值为2
    4. 当发生错误时，返回值为3
  
    例如，`$ expr 2 - 2`的运算结果为0，所以返回值是1；而`$ expr 1 + 2`的运算结果为3，所以返回值是0。

  3. `expr`命令支持的运算符

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
    | ARG1 + ARG2         | arithmetic sum of ARG1 and ARG2                         |                  |
    | ARG1 - ARG2         | arithmetic difference of ARG1 and ARG2                  |                  |
    | ARG1 * ARG2         | arithmetic product of ARG1 and ARG2                     | true             |
    | ARG1 / ARG2         | arithmetic quotient of ARG1 divided by ARG2             |                  |
    | ARG1 % ARG2         | arithmetic remainder of ARG1 divided by ARG2            |                  |
    | STRING : REGEXP     | Return the pattern match if REGEXP matches a            |                  |
    |                     | pattern in STRING                                       |                  |
    | match STRING REGEXP | Return the pattern match if REGEXP matches a            |                  |
    |                     | pattern in STRING.                                      |                  |
    | substr STRING       | Return the substring LENGTH characters in length,       |                  |
    | POS LENGTH          | starting at position POS (starting at 1).               |                  |
    |                     |                                                         |                  |
    | index STRING CHARS  | Return position in STRING where CHARS is found;         |                  |
    |                     | otherwise, return 0                                     |                  |
    | length STRING       | Return the numeric length of the string STRING.         |                  |
    |                     | the STRING can not be empty, if it's empty,             |                  |
    |                     | `expr` will output an error.                            |                  |
    | + TOKEN             | Interpret TOKEN as a string, even if it’s a keyword     |                  |
    | (EXPRESSION)        | Return the value of EXPRESSION                          |                  |

    **_说明_**：   
    1. "Output To STDOUT"一列中如果以"arithmetic"开头，表明该运算符为数学运算符，运算符两边的操作数只能是整数。否则，既可以是整数也可以是字符串。如，"+"运算符为"arithmetic"运算符，只能用于两个整数的相加；而"<"运算符不是"arithmetic"运算符，所以也可以用于字符串的比较。
    2. 当运算符两边都为整数时，操作数将按数值进行比较；否则，将按字典顺序进行比较。如，`$ expr 10 \< 8`将按数值进行比较，运算结果为0；而`$ expr 10 \< 2a`将按字典顺序进行比较，运算结果为1。
    3. 
    Pattern matches return the string matched between \(  and  \)  or
       null;  if  \( and \) are not used, they return the number of characters
       matched or 0.






  **_注意事项_**：   
  1. 当运算符在Shell中有特殊意义时，需要对运算符进行转义，包括：`|`,`*`, `&`, `<`, `>`，如：两数相乘   
    ```
    $ expr 2 * 3
    expr: 语法错误
    $ expr 2 \* 3
    6
    ```
  2. 运算符和数字之间必须至少存在一个空格。
  3. 只能进行整数运算，不能进行浮点数运算。
  4. `expr`命令还可以进行一些字符串的操作，见表格。
  5. `expr`命令中也可以使用变量，如：
    ```
    $ num1=2
    $ num2=3
    $ expr $num1 + $num2
    5
    ```

### 使用中括号`$[]`执行整数运算

  '$'符号后跟中括号用于整数运算，如下所示：  
  ```
  $ echo $[ 1 + 2 ]
  3
  ```
