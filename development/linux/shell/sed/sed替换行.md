[首页](/index.md) << ... << [索引](index.md)

# sed替换行

## 概述
sed命令可以对输入流中的行进行全部整行替换。需要替换的行可以通过地址来指定，参见[sed指定地址](./sed指定地址.md)

命令格式：
    sed '[address]c\
    new line’
其中：
+ new line为新行的内容，如果要替换成多行内容，每行结尾需要加上斜杠“\”
+ address : 指定替换的行地址，参见[sed指定地址](./sed指定地址.md)。
    1）如果是一个范围，该范围内的所有行会被整体替换为新的行（新的行只会出现一次）；
    2）如果是一个模式，且模式匹配到多行，多行的内容都会被单独替换成新行（例如模式匹配到3行，则新行也会出现3次）

## 示例
+ 替换指定的行
    下面的指令将linedata文件中的第三行内容替换成了“This is a changed line of text.”并输出
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4
    [root@centos-7-192 ~]# sed '3c\
    > This is a changed line of text.' linedata
    This is line number 1
    This is line number 2
    This is a changed line of text.
    This is line number 4
    ```

+ 替换指定范围的行
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4

    [root@centos-7-192 ~]# sed '2,3c\
    This is a changed line of text.\
    This is a changed line of text again' linedata
    This is line number 1
    This is a changed line of text.
    This is a changed line of text again
    This is line number 4
    ```
    注意：上面第2、3行内容被整体替换成
    This is a changed line of text.
    This is a changed line of text again
    

+ 通过模式匹配替换行
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4

    [root@centos-7-192 ~]# sed '/[23]/c\
    > This is a changed line of text' linedata
    This is line number 1
    This is a changed line of text
    This is a changed line of text
    This is line number 4
    ```
    注意：上面内容包含2或3的行，都被替换成了“This is a changed line of text”，这点跟范围匹配不一样。范围匹配时，是整个范围内的行被整体替换成了新行。