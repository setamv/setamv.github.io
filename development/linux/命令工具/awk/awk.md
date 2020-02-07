# awk
*awk*是一种便于使用且表达能力强的程序设计语言，可应用于各种计算和数据处理任务。
以下所有的讲解将基于下面的示例数据（假设数据文件为`emp.data`），其中第1、2、3列分别为员工的姓名、薪资（美元/小时）、工作的小时数：
```
Beth	4.00	0
Dan	    3.75	0
kathy	4.00	10
Mark	5.00	20
Mary	5.50	22
Susie	4.25	18
will	5.50    18
will	5.50    18
will	5.50    18
```

## 初识awk命令
如果想要打印出工作时间超过18小时的员工姓名和工资（薪资乘以时间），输入以下命令即可：
```
# awk '$3 > 18 {print $1, $2 * $3}' emp.data
Mark 100
Mary 121
```
上面的命令`awk '$3 > 18 {print $1, $2 * $3}' emp.data`中包含三部分：
1. `awk`命令
2. *awk*程序
    上面的*awk*程序部分包括：`$3 > 18 {print $1, $2 * $3}`，*awk*程序跟在`awk`命令后面，并使用单引号'包裹。*awk*程序的详细介绍参见后面的章节*awk程序*
3. 输入
    输入可以是一个文件，也可以是管道数据，或者不指定输入也可以，`awk`命令将视后续换行后的输入内容作为输入。如：
    ```
    # awk '$3 > 18 {print $1, $2 * $3}'
    will 10 25
    will 250
    ```
    其中，"will 10 25"这一行内容是在终端通过键盘输入的，而"will 250"这一行内容是awk程序输出的。

## *awk*程序
每个*awk*程序都是一个或多个模式-动作语句的序列，如下所示：
```
[BEGIN {begin action}] 
pattern {action}
pattern {action}
[END {end action}]
```
其中：
+ `[BEGIN { begin action }]` 为处理输入之前的动作，可用于执行一些初始化
+ `pattern` 为*awk*程序中的模式，下面将详细讲解
+ `{action}` 为*awk*程序的动作语句，动作语句必须放在大括号"{}"之间。如果未指定动作语句，将打印整行内容
+ `[END {end action}]` 为处理完所有输入行后的动作，该动作将会在所有输入行都被处理完之后执行一次。

*awk*命令的基本操作是：
如果存在`[BEGIN {begin action}]`块，在开始扫描输入之前，将执行一次`[BEGIN {begin action}]`块中的动作。
然后一行一行地扫描输入，对输入中的每一行数据，*awk*命令将搜索`[BEGIN {begin action}]`块和`[END {end action}]`块之间的所有模式，如果有任意模式可以匹配当前行，将执行匹配模式后的动作，然后读取下一行并继续匹配，直到所有的输入读取完毕。
如果存在`[END {end action}]`块，在所有行处理完后，将执行`[END {end action}]`块中的动作。

### *awk*程序的模式：pattern 
pattern为*awk*程序的模式。
例如，*awk*程序`$3 > 18 {print $1, $2 * $3}`中，`$3 > 18`为模式部分，`{print $1, $2 * $3}`为动作部分，当输入的行中第3列的值>18时，将执行动作（即输出第1列，第2列*第3列）。

pattern是用于对输入的行进行匹配，如果一个动作前面没有任何模式，则该动作将在每一行输入上执行。
*awk*程序支持的模式包括：

#### 比较运算
| 运算符 | 示例                  | 说明                                                                             |
|--------|-----------------------|----------------------------------------------------------------------------------|
| >      | `$1 > 10 {print $0}`  | 当第1列的值大于10时将整行内容打印出来                                            |
| >=     | `$1 >= 10 {print $0}` | 当第1列的值大于或等于10时将整行内容打印出来                                      |
| <      | `$1 < 10 {print $0}`  | 当第1列的值小于10时将整行内容打印出来                                            |
| <=     | `$1 <= 10 {print $0}` | 当第1列的值小于或等于10时将整行内容打印出来                                      |
| ==     | `$1 == 10 {print $0}` | 当第1列的值等于10时将整行内容打印出来，也可以用于字符串比较，如：`$1 == "Susie"` |
| !=     | `$1 != 10 {print $0}` | 当第1列的值不等于10时将整行内容打印出来                                          |

#### 逻辑运算
| 运算符 | 示例                  | 说明                                        |
|--------|-----------------------|---------------------------------------------|
| &&     | `$1 > 10 && $1 < 15 {print $0}`  | 当第1列的值大于10且小于15时将整行内容打印出来       |
| \|\|     | `$1 < 10 || $1 > 20 {print $0}` | 当第1列的值小于或大于20时将整行内容打印出来 |
| !      | `!($1 < 10) {print $0}`  | 当第1列的值不小于10时将整行内容打印出来       |

#### 正则表达式
*awk*程序中，正则表达式必须放在pattern中，用于执行行匹配。正则表达式的模式部分放在双反斜杠之间"//"，如果输入行中的某个字符串与正则表达式相匹配，将执行pattern后的动作，如果没有指定动作，将打印整行。
如下的示例程序，将打印所有包含will的行：
```
# awk '/will/' data
will    5.50    18
will    5.50    18
will    5.50    18
```
注意：上面的正则表达式模式（"/will/"）后面没有指定动作，默认的动作是打印整行内容

**匹配操作符（~）**
使用正则表达式进行匹配时，可以指定只对指定字段进行匹配，此时需要使用匹配操作符*~*，如果不指定字段进行匹配，默认会对整行内容进行匹配。
如下面所示，只匹配员工的姓名：
```
# awk '$1 ~ /Su/' data
Susie   4.25    18
```

**awk支持的正则表达式元字符**
| 元字符 | 说明     | 示例       | 示例说明                |
|--------|----------|------------|-------------------------|
| ^      | 匹配串首 | '$1 ~ /^Su/' | 匹配第1列以"Su"开头的行 |
| $      | 匹配串尾 | '$1 ~ /ie$/' | 匹配第1列以"ie"结尾的行 |
| .      | 匹配任意单个字符 | '$1 /Su.s/' | |
| *      | 匹配零个或多个前导字符 | '$1 ~ /Su.*s/' | 可以匹配第1列为"Susie"的行|
| +      | 匹配一个或多个前导字符 | '$1 ~ /Su.+s/' | |
| ?      | 匹配零个或一个前导字符 | '$1 ~ /Su.?s/' | |
| [ABC]      | 匹配指定字符组（即A、B和C）中的任一字符，这里ABC可以换成其他任一字符 | '$1 ~ /Su[abs]/' | |
| [^ABC]      | 匹配任意一个不再指定字符组（即A、B和C）中的字符 |  | |
| [A-Z]      | 匹配A至Z之间的任一字符 |   | |
| [A|B]      | 匹配A或B，其中A或B可以是一个子表达式（使用括号包裹） | '$1 ~ /(^Su)|(^wi)/' |匹配第1列以"Su"或"wi"开头的行 |
| [A|B]      | 匹配A或B，其中A或B可以是一个子表达式（使用括号包裹） | '$1 ~ /(^Su)|(^wi)/' |匹配第1列以"Su"或"wi"开头的行 |
| \*      | 匹配*本身，同样，\.用于匹配.本身 |  | |
| &      | 用在替换串中，代表查找串中匹配到的内容 |  | |

## *awk*程序的常量和值引用
### 值引用
在*awk*程序处理输入的每一行数据时，将按指定分隔符（默认为空格“ ”）对该行数据进行切割，切割后的每一部分值可以使用`$n`进行引用（其中n为被切割后的顺序，从1开始），使用`$0`可以对整行内容进行引用。
如："1 2 3 4 5"被切割后，将分为5部分值，其中第1部分为"1"，第5部分为"5"，可以使用`$1`、`$5`对他们进行引用。如：`{print $1}`将打印"1"。

### 常量引用
*awk*程序有一些常量可以在*awk*程序中直接访问，包括：
| 常量 | 常量名称                              | 示例               | 示例说明                     |
|------|---------------------------------------|--------------------|------------------------------|
| NF   | 当前行切割后的字段数量                | `{print NF}`       | 打印每一行被切割后的字段数量 |
| NR   | 当前已经读取的行数，相当于行号        | `{print NR}`       | 打印每一行的行号             |
| FS   | *awk*程序当前的分隔符，默认为空白符号 | `BEGIN {FS = ","}` | 将分隔符号设置为逗号","      |

也可以在模式中引用常量，或让常量参与计算或模式匹配，如：
```
# awk 'NF > 2 && $(NF-1) > 10 {print $(NF - 1), $0}
```
上面将打印出字段数大于2，且倒数第2个字段的值大于10的行，打印的值包括：倒数第2个字段的值，整行值。。

### 计算
*awk*程序的模式和动作中都可以对字段的值进行计算，例如：`'$1 * $3 > 100 {print $1 * $3}'`，当第一列和第3列的值的乘积大于100时，打印他们的乘积。

### print打印
*awk*程序中可以使用`print`打印，使用`print`打印时，多个值可以直接拼在print后面，也可以将多个值使用逗号分隔，如：
```
# awk '{print $1, "'\''s amount is", $2 * $3}' data
Beth 's amount is 0
Dan 's amount is 0
kathy 's amount is 40
... 省略部分输出

awk '{print $1"'\''s amount is " $2 * $3}' data
Beth's amount is 0
Dan's amount is 0
kathy's amount is 40
... 省略部分输出
```
其中，第一部分为使用逗号分隔，这种方式下，print会为每一部分的后面自动输出一个空格。
第二部分直接将各部分值追加到了print后面，这种方式print就不会多输出空格了。
要打印单引号，必须使用`'\''`。

### printf格式化打印
另外还可以使用`printf`语句打印格式化的内容，其语法格式为：
```
printf(format, value1, value2, ..., valuen)
```
其中 format 是字符串，包含要逐字打印的文本，穿插着 format 之后的每个值该如何打印的规格(specification)。一个规格是一个 % 符，后面跟着一些字符，用来控制一个 value 的格式。第一个规格说明如何打印 value1 ，第二个说明如何打印 value2 ，... 。因此，有多少 value 要打印，在 format 中就要有多少个 % 规格。
format的格式包括以下几种：
+ `printf("%.2f", $0)` 表示打印的值保留两位小数
+ `printf("%6s", $0)` 表示打印的值最少占位6个字符宽度，如果不够宽，在左边补空格
    ```
    # awk '{printf("%6s\n", $0)}'
    12345
     12345
    abc
       abc
    ```
+ `printf("%-6s", $1)` 表示打印的值最少占位6个字符宽度，如果不够宽，在右边补空格   
    ```
    # awk '{printf("%-6s.\n", $0)}'
    12345
    12345 .
    abc
    abc   .
    ```
+ `printf("%6.2f", $0)` 表示打印的值保留两位小数，并且最少占位6个字符宽度，如果不够宽，在左边补空格
+ `printf("%-6.2f", $0)` 表示打印的值保留两位小数，并且最少占位6个字符宽度，如果不够宽，在右边补空格

下面的*awk*程序使用 printf 打印每位员工的总薪酬：
```
# awk '{printf("total pay for %s is $%.2f\n"), $1, $2 * $3} emp.data
... 这里省略部分输出
total pay for kathy is $40.00
total pay for Mark is $100.00
... 这里省略部分输出
```
printf 不会自动产生空格或者新的行，必须是你自己来创建，所以不要忘了 \n 。

## BEGIN和END
`BEGIN`用于匹配第一个输入文件的第一行之前的位置，在开始扫描输入之前，将执行一次`[BEGIN {begin action}]`块中的动作。
`END`则用于匹配处理过的最后一个文件的最后一行之后的位置，在所有输入行处理完后，将执行`[END {end action}]`块中的动作。
例如，下面的程序在开始打印一个标题行，并在结尾打印工资合计：
```
# awk 'BEGIN {printf("%-9s%-4s\n", "姓名", "总金额")} 
$2 * $3 >= 100 {amount = amount + $2 * $3; printf("%-9s%.2f\n", $1, $2 * $3)} 
END {printf("%-9s%.2f\n", "合计", amount)}' data
姓名       总金额 
Mark     100.00
Mary     121.00
Mary     154.00
合计       375.00
```

## 使用自定义的字段分隔符
*awk*程序默认的字段分隔符号为空字符串（这里的空字符串包括空格" "、Tab符号"    "或他们的连续组合，如一个空格加一个Tab符号）。
如果要设置自己的分隔符号，有两种方式：
1. 通过*awk*命令的`-F`选项指定，如下所示，*awk*程序按逗号进行分割输入行：
    ```
    # awk -F , '{print $1, NF}'
    a,b,c
    a 3
    ```

2. 可以通过在`BEGIN`动作中设置`FS`变量。如下所示，*awk*程序按逗号进行分割输入行：
    ```
    # awk 'BEGIN {FS = ","} {print $1}'
    susie,8,100
    susie
    ```

## 使用自定义变量求和和平均值
*awk*程序中可以使用自定义变量（只能在动作中使用），并且不需要对变量进行声明就可以直接使用。如下面的*awk*程序统计所有员工的总金额，并通过总金额和员工数计算平均工资：
```
awk '{amount = amount + $2 * $3} END {printf("总金额=%.2f, 平均工资=%.2f\n", amount, amount / NR)}' data
总金额=581.50, 平均工资=72.69
```

## 字符串拼接
可以合并老字符串来创建新字符串。这种操作称为连接（concatenation），如下面所示，将所有员工的姓名拼接后输出：
``` 
# awk '{names = names $1 " "} END {print names}' data
Beth Dan kathy Mark Mary Susie kathy Mary 
```

## 执行文件中的*awk*程序
*awk*命令也可以执行文件中的*awk*程序，使用`-f progfile`指定*awk*程序文件的路径。
如：`# awk -f ./prog.awk input files`，其中，"./prog.awk"为*awk*程序文件。

## 一次处理多个文件
*awk*程序可以一次处理多个文件，多个文件之间使用空格分开，如：`# awk {print $0} file1 file2`，其中，file1和file2为两个需要处理的数据文件。

## 数组
*awk*程序可以使用数组，并且数组的下标可以是数字，也可以是字符串（当下标为字符串时，数组有些像Java中的Map），通过数组，可以对输入的行执行分组统计。
例如，如下的程序，统计员工中同名员工的数量：
```
# awk '{emp[$1]++} END {for (name in emp) {print name, emp[name]}}' data
Dan 1
kathy 1
Mark 1
Mary 1
Susie 1
Beth 1
will 3
```

## 控制语句
*awk*程序提供了if-else和几个循环的控制语句。他们仅可以在动作中使用。

### if-else
如下程序将计算时薪达到5美元的员工的总薪酬与平均薪酬。它使用一个 if 来防范计算平均薪酬时的零除问题。
```
# awk '$2 >= 5 {n = n + 1; amount = amount + $2 * $3}
> END { if (n > 0) print n, "employee, total amount is", amount, "average amount is", amount / n
> else print "no employees are paid more than $6/hour"
> }' data
5 employee, total amount is 518 average amount is 103.6
```

## for循环
for循环和c语言的for循环相似，如下所示，先将员工的工资存放到一个数组中，最后通过循环打印出数组中的工资信息：
```
# awk '{salary[NR]=$2*$3} END {for (i=1; i<=NR; i++) {print salary[i]}}' data
... 省略部分数据
100
121
... 省略部分数据
```

## forin循环
forin循环是for的另一种循环方式，主要用于遍历数组中的元素。如下所示，先将员工的姓名和工资存放到一个数组中（姓名存放到数组的下标中），通过forin打印员工以及员工的工资信息：
```
# awk '{salary[$1]=$2*$3} END {for (name in salary) {print name "'\''s salary is", salary[name]}}' data
... 省略部分数据
Mark's salary is 100
Mary's salary is 121
Susie's salary is 76.5
... 省略部分数据
```

## while循环语句
一个 while 语句有一个条件和一个执行体。条件为真时执行体中的语句会被重复执行。
如下程序用于分组统计同名次数大于1的员工，并打印出来。
```
{   i = 1
    while (i <= $3) {
        printf("\t%.2f\n", $1 * (1 + $2) ^ i)
        i = i + 1
    }
}
```

## awk程序中的函数
### length函数
length函数用于计算字符串或数组的长度。如下所示，通过length函数打印字符串"Beth"和emp数组的长度。
```
# awk '{emp[NR]=$1} END {print length("Beth"), length(emp)}' data
4 9
```

### split函数
`split(s, a, p)`函数用于对字符串`s`进行切割（切割的分隔符由参数`p`指定），切割后的各个子串将保存到数组`a`中，split函数会返回切割后的子串数量。
如下面所示，Nginx日志中的url按字符串"?"进行切割，并打印?前面的部分：
```
# awk '{len = split($1, parts, "?"); print "split to", len, "parts, the first part is", parts[1]}'
/supplierModuleOperationItem/list.action?uid=393
split to 2 parts, the first part is /supplierModuleOperationItem/list.action
```
上面split($1, parts, "?")中的参数分别是："$1"为需要切割的字符串；"parts"为切割后子串保存的数组；"?"为切割的特征符号。

### substr函数    
`substr(s,p,n)`函数用于截取字符串`s`从位置`p`（从0开始）开始长度为`n`的子串。如果未指定参数`n`，将截取从位置`p`开始一直到字符串结尾的所有子串。
如下面的程序将截取第1列的前2个字符：
```
# awk '{print substr($1, 0, 2)}'
abc
ab
```

### index函数
`index(s,r)`函数返回字符串`r`在字符串`s`中第一次出现的位置（从1开始），如下面的程序将打印第一列中子串"dog"的位置：
```
# awk '{print index($1, "dog")}'
dog
1
dogdogdog
1
mydog
3
```

### sub函数
`sub(r1, r2[, s])`函数将字符串`s`中第一次出现的`r1`子串替换为字符串`r2`，这将改变字符串`s`本身的值。例如，`sub("cat", "dog", $1)`会影响`$1`的最终内容。
例如，下面的程序将替换第1列中第1次出现的"cat"：
```
# awk '{sub("cat", "dog", $1); print $1}'
catcat
dogcat
```
注意：
+ 如果不指定参数`s`，将对整个输入行中第1个满足条件的子串执行替换，这种情况下，还会影响到被替换的子串所在的字段值，如下面的例子所示：
    ```
    # awk '{sub("cat", "dog"); print $1, $0}'
    mycat mycat-yourcag-hiscafe
    mydog mydog mycat-yourcag-hiscafe
    ```
    上面的程序将整个输入行第1个"cat"替换为"dog"后，第1列（`$1`）的值也发生了改变。

### gsub函数
`gsub(r1, r2[, s])`函数将字符串`s`中所有匹配模式`r1`的子串替换为字符串`r2`，这将改变字符串`s`本身的值。例如，`gsub("cat", "dog", $1)`会影响`$1`的最终内容。
注意，1）上面的参数`r1`可以是一个正则表达式；2）可以不指定参数`s`，此时，被改变的将是当前输入行的`$0`参数。
如下面的程序，将替换第1列中所有出现的以"ca"开头并且长度为3的子串为"dog"（替换后`$1`本身的值发生了变化，变成了替换后的内容）：
```
# awk '{gsub("ca[a-z]", "dog", $1); print $1}'
mycat-yourcag-hiscafe
mydog-yourdog-hisdoge
```
注意：
+ 参数`r1`是一个正则表达式
+ 如果不指定参数`s`，将对整个输入行中的所有满足条件的子串执行替换，这种情况下，还会影响到被替换的子串所在的字段值，如下面的例子所示：
    ```
    # awk '{gsub("cat", "dog"); print $1, " | ", $0}'
    cat mycat yourcag his hercafe!
    dog  |  dog mydog yourcag his hercafe!
    ```
    上面的程序将整个输入行的所有"cat"替换为"dog"后，第1列（`$1`）的值也发生了改变。

### gensub函数
`gensub(r1, r2, n[, s)`函数将字符串`s`中第`n`次与模式`r1`匹配的子串替换为字符串`r2`后返回，字符串`s`的值不受影响（这点与sub、gsub函数不一样）。
例如，下面的程序，将第1列中第2个以"ca"开头并且长度为3的子串替换为"dog"后返回：
```
# awk '{r1=gensub("ca[a-z]", "dog", 2, $1); print r1, "|", $1}'
mycat-yourcafe-hercat
mycat-yourdoge-hercat | mycat-yourcafe-hercat
```
注意：字符串`$1`本身是没有发生任何变化的

### match函数
`match(s, r)`函数用于测试字符串`s`中是否包含匹配参数`r`的子串，如果包含，将返回true；否则，返回false。
如下面的程序将打印字段中包含"dog"的列：
```
# awk '{if (match($1, "dog")) print $1;}'
dogs cats       
dogs            // 这一行是awk程序输出的
mydogs cats
mydogs          // 这一行是awk程序输出的
```


## 综合实例

### 统计Nginx的访问日志中相同请求路径的访问次数，并按访问次数倒序排列
Nginx的访问日志格式如下所示：
```
42.236.10.84 - - [20/Jan/2020:14:02:17 +0800] "GET /jquery-easyui-1.4.2/jquery.easyui.min.js HTTP/1.1" 200 474269 "http://domain.com/statement/outcheckstatementindex.action" "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; EML-AL00 Build/HUAWEIEML-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 baidu.sogo.uc.UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN" "-" 1.096 0.010
182.109.187.60 - - [20/Jan/2020:15:16:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11902&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211902%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 2.606 0.051
182.109.187.60 - - [20/Jan/2020:15:20:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11903&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211903%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 3.266 0.055
```
统计相同请求路径时，必须将请求参数去除后再统计，可以使用split对字段的值进行切割。
```
# awk '{len=split($7, parts, "?"); url[parts[1]]++;} END {for (u in url) {print url[u], u}}' access.log | sort -nr
2 /comm/insertFileList.action
1 /jquery-easyui-1.4.2/jquery.easyui.min.js
```
上面使用`sort`命令进行排序，`-n`表示按数字比较，`-r`表示倒序排序

### 统计Nginx的访问日志中请求时间超过指定阈值
Nginx的访问日志格式如下所示：
```
42.236.10.84 - - [20/Jan/2020:14:02:17 +0800] "GET /jquery-easyui-1.4.2/jquery.easyui.min.js HTTP/1.1" 200 474269 "http://domain.com/statement/outcheckstatementindex.action" "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-CN; EML-AL00 Build/HUAWEIEML-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 baidu.sogo.uc.UCBrowser/11.9.4.974 UWS/2.13.1.48 Mobile Safari/537.36 AliApp(DingTalk/4.5.11) com.alibaba.android.rimet/10487439 Channel/227200 language/zh-CN" "-" 1.096 0.010
182.109.187.60 - - [20/Jan/2020:15:16:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11902&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211902%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 2.606 0.051
182.109.187.60 - - [20/Jan/2020:15:20:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11903&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211903%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 3.266 0.055
```
其中，倒数第2列为请求时间，下面使用*awk*程序统计请求时间超过2秒的请求：
```
awk '($(NF-1) ~ /^[0-9\.]+$/) && $(NF-1) > 2 {print $(NF-1), $0}' access.log
2.606 182.109.187.60 - - [20/Jan/2020:15:16:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11902&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211902%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 2.606 0.051
3.266 182.109.187.60 - - [20/Jan/2020:15:20:47 +0800] "POST /comm/insertFileList.action?supplier_id=159&fileItemId=11903&origin=customer&origin_id=2383 HTTP/1.1" 200 71 "http://domain.com/pages/comm/uploader.jsp?json=%7B%22supplier_id%22:%22159%22,%22fileItemId%22:%2211903%22,%22origin%22:%22customer%22,%22origin_id%22:%222383%22%7D" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36" "-" 3.266 0.055
```
判断倒数第2列是一个浮点数，并且浮点数的值大于1

## 参考资料
+ https://awk.readthedocs.io/en/latest/chapter-one.html
+ *awk*的*man*帮助
    在linux下执行`# man awk`可以查看*awk*命令的完整帮助信息