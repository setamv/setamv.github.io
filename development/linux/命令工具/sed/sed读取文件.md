[首页](/index.md) << ... << [索引](index.md)

# sed读取文件

## 概述
sed的r命令读取文件的内容并将读取的内容插入指定行之后。

命令格式：
    sed [address]r filename
其中：
+ filename : 需要读取的文件，可以是绝对路径或相对路径
+ address : 指定替换的行地址，参见[sed指定地址](./sed指定地址.md)。只能是单行或一个模式，不能是一个行范围。
    如果是要将文件内容追加到末尾，地址中使用“$”符号即可。

## 示例
+ 替换指定的行
    下面的指令将data文件的内容插入linedata文件中的第三行后面
    ```
    [root@centos-7-192 ~]# cat data
    This is a cat
    the cat like fish
    can you give the cat a fish?

    [root@centos-7-192 ~]# cat linedata
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4

    [root@centos-7-192 ~]# sed '3r data' linedata
    This is line number 1
    This is line number 2
    This is line number 3
    This is a cat
    the cat like fish
    can you give the cat a fish?
    This is line number 4
    ```
