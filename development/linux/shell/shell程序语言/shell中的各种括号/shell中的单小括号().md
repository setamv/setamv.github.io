[首页](/index.md) << ... << [导航](./index.md)

# shell中的单小括号
单小括号的作用包括：1）命令组；2）命令替换；3）初始化数组

## 命令组
单小括号用于命令组时，括号中的命令将会新开一个子shell顺序执行，所以括号中的变量不能够被脚本余下的部分使用。括号中多个命令之间用分号隔开，最后一个命令可以没有分号，各命令和括号之间不必有空格。

### 示例
```
#!/bin/bash
a=1
(
        b=2
        echo "a=$a b=$b"
)
echo "a=$a b=$b again"
```
执行上述脚本得到的结果是：
```
a=1 b=2
a=1 b= again
```

## 命令替换
单小括号用于命令替换时，等同于`cmd`，shell扫描一遍命令行，发现了$(cmd)结构，便将$(cmd)中的cmd执行一次，得到其标准输出，再将此输出放到原来命令。有些shell不支持，如tcsh。
### 示例
```
#!/bin/bash
filename=some_file_$(date +%Y%m%d_%H%M%S)
echo "filename is $filename"

filecount=$(ls | wc -l)
if [ $filecount -gt 0 ]; then
        echo "$PWD has $filecount file or subdirectories"
else
        echo "$PWD no file and subdirectories"
fi
```
执行上述脚本得到的结果是：
```
filename is some_file_20190202_215715
/root/shell has 4 file or subdirectories
```


## 初始化数组
如：array=(a b c d)