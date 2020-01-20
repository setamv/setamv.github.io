[首页](/index.md) << ... << [索引](index.md)

# sed删除行

## 概述
sed命令可以对输入流中的指定行执行删除，并输出余下的内容。需要删除的行可以通过地址来指定，参见[sed指定地址](./sed指定地址.md)

## 示例
+ 删除所有行
    ```
    # sed 'd' data
    ```

+ 通过行号地址删除指定的行
    ```
    [root@centos-7-192 ~]# cat data
    This is a cat
    the cat like fish
    can you give the cat a fish?
    [root@centos-7-192 ~]# sed '3d' data
    This is a cat
    the cat like fish
    [root@centos-7-192 ~]# sed '2,3d' data
    This is a cat
    ```

+ 通过文本模式过滤删除文本模式匹配的行
    下面删除了所有包含fish的行
    ```
    [root@centos-7-192 ~]# cat data
    This is a cat
    the cat like fish
    can you give the cat a fish?
    [root@centos-7-192 ~]# sed '/fish/d' data
    This is a cat
    ```

+ 删除两个模式之间的行
    下面删除了两个模式匹配的行之间的所有内容（包括模式匹配的行）
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4
    This is line number 2 again
    This is line number 5
    [root@centos-7-192 ~]# sed '/2/,/3/d' linedata 
    This is line number 1
    This is line number 4
    ```
    注意：两个模式中，第一个模式将开启删除特征；当遇到第二个模式匹配的行时，将关闭删除特征。如果第二个模式没有匹配的行时，删除特征将一直开启，直到输入内容的最后一行
    所以，上面的最后两行因为开启了删除特征，而没有关闭，所以都被删除了。



