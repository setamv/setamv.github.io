[首页](/index.md) << ... << [索引](index.md)

# vi快捷键

## 导航


## 移动光标
+ h     向左移动光标
+ nh    向左移动光标n个字符
+ j     向下移动光标
+ nj    向下移动光标n行
+ k     向上移动光标
+ nk    向上移动光标n行
+ l     向右移动光标
+ nl    向右移动光标n个字符

+ w     将光标往前移动一个单词
+ nw    将光标往前移动n个单词，n为正整数
+ b     将光标往回移动一个单词的位置
+ nb    将光标往回移动n个单词，n为正整数
+ e     将光标移到当前单词最后一个字符
+ ge    将光标移到上一个单词的最后一个字符

+ $     将光标移动到行尾
+ n$    将光标移动到从当前行开始计数的第n行的行尾。例如，`2$`表示移动到下一行的行尾
+ 0     将光标移动到行首
+ ^     将光标移动到行首第一个非空白字符。空白字符包括空格、tab符号

+ fx    将光标移动到当前行中下一个字符“x”所在的位置。这里x为需要搜索的单个字符。f是find的首字母缩写
        注意：1）“x”只能为单个字符。相当于搜索单个字符的功能；2）只能在当前行光标所在位置往后搜索。不能跨行，不能往前搜
+ nfx   相当于执行n此快捷键`fx`，n为正整数
+ Fx    将光标向左移动到当前行中的下一个字符“x”所在的位置。
        注意：1）`Fx`与快捷键`fx`的区别是：`fx`为往前搜索；`Fx`为往回搜索。
+ nFx   相当于执行n此快捷键`Fx`，n为正整数
+ tx    与`fx`快捷键的唯一区别是：`tx`会将光标落在目标字符的前一个字符上。t是to的首字母缩写
+ Tx    与快捷键`tx`的区别是：`tx`为往前搜索；`Tx`为往回搜索。

+ %     将光标移动到匹配的括号上。如果光标当前不是在一个光标上，会将光标移动到最近的一个括号上。

+ gg    跳转到整个内容的首行第一个字符
+ G     跳转到整个内容的最后一行的第一个字符
+ n%    跳转到整个内容的百分之n处，n为正整数。例如，`50%`将跳转到内容的50%处
+ H     跳转到当前展示内容页的首行第一个字符。为Home的首字母缩写
        注意：`gg`是跳转到整个内容的首行；而`H`只会跳转到当前展示内容的第一行。
+ M     跳转到当前展示内容页的中间一行第一个字符。为Middle的首字母缩写
+ L     跳转到当前展示内容页的最后一行第一个字符。L为Last的首字母缩写
+ nG    跳转到n指定的行的第一个字符，n为正整数。例如，`5n`将跳转到第5行第一个字符
+ ctrl+d    往下翻半页内容
+ ctrl+u    往上翻半夜内容
+ ctrl+f    往下翻一页内容
+ ctrl+b    往上翻一页内容        


## 剪切
+ dd    剪切当前行
+ ndd   剪切n行（n为正整数）
+ dw    从光标处剪切至一个单字/单词的末尾，包括空格
+ de    从光标处剪切至一个单子/单词的末尾，不包括空格
+ d$    从当前光标剪切到行末
+ d0    从当前光标位置（不包括光标位置）剪切至行首

## 复制
+ yy    复制当前行

## 粘贴
+ p     将剪贴板的内容粘贴到光标所在处
    注意：如果是复制的一整行，将从当前光标的下一行开始粘贴
+ P     在光标所在位置的后面插入复制的文本，p是paste的首字母。

## 删除
+ :1,100d   删除第1行到第100行之间的所有内容