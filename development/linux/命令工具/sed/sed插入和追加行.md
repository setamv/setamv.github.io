[首页](/index.md) << ... << [索引](index.md)

# sed插入和追加行

## 命令格式
    sed '[address]command\
    new line’
其中：
+ command为“i”时，为插入一行（插入的位置为address的前面）；command为“a”时，为追加一行（追加的位置为address的后面）
+ new line为新行的内容
+ address 插入或追加的行地址，参见[sed指定地址](./sed指定地址.md)。只能为一个确定的行或模式，不能是一个范围

## 示例
+ 插入一行内容
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4
    [root@centos-7-192 ~]# sed '2i\
    > hello, sed' linedata
    This is line number 1
    hello, sed
    This is line number 2
    This is line number 3
    This is line number 4
    ```

+ 追加一行内容
    ```
    [root@centos-7-192 ~]# cat linedata 
    This is line number 1
    This is line number 2
    This is line number 3
    This is line number 4
    [root@centos-7-192 ~]# sed '2a\
    > hello, sed' linedata
    This is line number 1
    This is line number 2
    hello, sed
    This is line number 3
    This is line number 4
    ```

+ 追加多行内容
    ```
    [root@centos-7-192 ~]# sed '2a\
    > append row 1\
    > append row 2' linedata
    This is line number 1
    This is line number 2
    append row 1
    append row 2
    This is line number 3
    This is line number 4
    ```