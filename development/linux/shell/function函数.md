[首页](/index.md) << ... << [索引](index.md)

# shell中的function函数

## 语法格式
    function name() {
        command1
        ....
    }
## 函数调用方法
直接调用函数名即可，如 `name`
注意：
1. 函数必须先定义，后使用

## 示例
+ 示例代码
    ```
    [root@centos-7-192 shell]# vi function.sh
    #!/bin/bash

    # 定义一个根据传入参数安装指定的程序
    # 入参：1-安装apache；2-安装mysql；3-安装redis
    function setup( ) {
            case $1 in
                    1)
                    echo "install apache"
                    ;;

                    2)
                    echo "install mysql"
                    ;;

                    3)
                    echo "install redis"
                    ;;
            esac
    }

    setup 1
    ```

+ 执行结果
    ```
    [root@centos-7-192 shell]# ./function.sh 
    install apache
    ```