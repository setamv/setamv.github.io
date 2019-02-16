[首页](/index.md) << ... << [索引](index.md)

# shell中的case语句

## 语法格式
    case $arg in
        pattern1)
        语句1
        ;;
        pattern2)
        语句2
        ;;
        *)
        语句3
        ;;
    esac

## 示例
+ 示例代码
    ```
    #!/bin/bash

    case $1 in      
            a)
            echo 'the first letter is a'
            ;;

            b)
            echo 'b is the second letter'
            ;;

            *)
            echo 'you can enter a or b'
            ;;
    esac
    ```
+ 执行结果
    ```
    # ./case.sh a
    the first letter is a
    ```