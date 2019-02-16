[首页](/index.md) << ... << [索引](index.md)

# shell中的until循环
until循环直到满足条件才退出，否则执行

## 语法格式
    until 条件
    do
        action
    done

## 示例
+ 示例代码
    ```
    [root@centos-7-192 shell]# vi until.sh
    #!/bin/bash

    i=3
    until [[ $i -lt 0 ]];
    do
            echo $i
            (( i-- ))
    done
    ```
+ 执行结果
    ```
    [root@centos-7-192 shell]# ./until.sh 
    3
    2
    1
    0
    ```