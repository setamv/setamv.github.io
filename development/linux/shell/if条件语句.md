[首页](/index.md) << ... << [索引](index.md)

# if条件语句

## if条件语句的格式

```
if (表达式); then 
    语句1
elif (表达式); then
else
    语句2
fi
```

## 例子

### 比较数字大小
```
num=10
if (( $num > 4 )); then         
    echo "The number $num is greater than 4"
else 
    echo "The number $num is less or equal than 4"
fi
```
注意：
1. if后面的表达式使用双括号括起来`(( $num > 4 ))`，括号里面开始可以有空格，也可以没有空格

### 判断目录是否存在
```
if [ ! -d /root/shell/aa ]; then
  mkdir -p /root/shell/aa
else
  echo "the directory /root/shell/aa exists."
fi
```