# CentOS设置环境变量
CentOS可以通过好几种方式设置环境变量。以下描述中，`APPEND_PATH`为需要添加到环境变量的路径。

## 使用export命令
直接运行命令`export PATH=$PATH:APPEND_PATH`
使用这种方法，只会对当前会话生效，也就是说每当登出或注销系统以后，PATH 设置就会失效，只是临时生效。

## 修改~/.bash_profile文件
通过修改文件`~/.bash_profile`（~为当前登录用户home目录），在文件末尾追加如下内容：
```
PATH=$PATH:APPEND_PATH
export PATH
```
修改完成后，执行`source ~/.bash_profile`并可使修改立即生效。
使用这种方法，只对当前登陆用户生效，并且是永久性的。

## 修改/etc/profile文件
通过修改文件`etc/profile`，在文件末尾追加如下内容：
```
PATH=$PATH:APPEND_PATH
export PATH
```
修改完成后，执行`source /etc/profile`并可使修改立即生效。
使用这种方法，将对所有用户生效，并且是永久性的。