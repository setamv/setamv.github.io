# gawk

## 知识导航

## 什么是gawk
    gawk options program file

## 命令格式


## 示例
以下部分示例中引用的文件“data”，其内容如下所示：
```
[root@centos-7-192 ~]# cat data
This is a cat
the cat like fish
can you give the cat a fish?
```

+ 打印第一个field的内容
    ```
    # echo "This is a test" | gawk '{print $1}'
    This
    ```
+ 将内容按冒号分隔后打印第一个field的内容
    ```
    # echo "This:is:a:test" | gawk -F: '{print $1}'
    This
    ```

+ 多个命令，field内容替换
    将This替换为That后输出
    ```
    # echo "This is a test" | gawk '{$1="That"; print $0}'
    That is a test
    ```

+ 引用文件中的命令
    ```
    [root@centos-7-192 ~]# cat awkcmd.sh 
    {print $5 "'s userid is " $1}
    [root@centos-7-192 ~]# gawk -F: -f awkcmd.sh /etc/passwd
    root's userid is root
    bin's userid is bin
    ...
    ```

+ 引用文件中的多行命令
    ```
    [root@centos-7-192 ~]# cat multiawkcmd.sh 
    {
    text="'s userid is "
    print $5 text $1
    }
    [root@centos-7-192 ~]# gawk -F: -f multiawkcmd.sh /etc/passwd
    root's userid is root
    bin's userid is bin
    ```
    注意：命令文件中的多个命令也可以写在一行并用分号分隔，如：
    ```
    {
        text="'s userid is "; print $5 text $1
    }
    ```

+ 处理数据之前和之后先执行命令
    ```
    [root@centos-7-192 ~]# gawk 'BEGIN {print "User List:"; FS=":"} {print $1} END {print "Bye!"}' /etc/passwd
    User List:
    root
    bin
    daemon
    ...
    Bye!
    ```
    注意：
    1. 关键字BEGIN后面的第一个{}中的命令是在开始数据处理之前执行了一次，并且在开始数据处理以后，没有执行第一个命令了。
    2. 关键字END后面的命令在数据处理完成后执行了一次。
    3. 在BEGIN后面的命令中，通过为变量FS赋值“:”，改变了gawk命令分隔域的分隔符，这种方式与-F选项的效果是一样的。