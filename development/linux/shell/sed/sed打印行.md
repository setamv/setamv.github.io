[首页](/index.md) << ... << [索引](index.md)

# sed打印行

## 概述
类似[sed文本替换](sed文本替换.md)中的p标志，sed中的p命令用于在输出中增加一行打印内容。
p命令最常用的场景是与-n选项结合使用，打印指定的行。
p命令的另一种场景是，在对文本进行替换之前，将原始文本内容打印出来，参见第二个示例。

## 命令格式
    sed '[address]p'
其中：
+ address 需要打印的行地址，参见[sed指定地址](./sed指定地址.md)

## 示例
+ 打印指定行的内容
    ```
    # [root@centos-7-192 ~]# sed -n '2,4p' linedata
    This is line number 2
    This is line number 3
    This is line number 4
    ```

+ 打印被替换之前和替换之后的行
    ```
    [root@centos-7-192 ~]# sed -n '3{
    p
    s/number/num/p}' linedata
    This is line number 3
    This is line num 3
    ```
    上面分别打印了文本替换之前的内容“This is line number 3”和文本替换之后的内容“This is line num 3”