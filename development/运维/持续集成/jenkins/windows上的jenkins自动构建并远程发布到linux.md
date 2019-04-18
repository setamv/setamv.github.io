# windows上的jenkins自动构建并远程发布到linux
windows上的jenkins自动构建并远程发布到linux分为以下几个步骤：
1. 构建发布的包；
2. 上传包到linux指定目录
3. 远程执行linux服务器的脚本，自动部署

## 构建发布的包
在jenkins中配置构建的路径等

## 上传包到linux指定目录
在windows上传文件到linux，可以使用pscp命令。
1. 下载pscp命令执行文件
    下载地址：[pscp命令执行文件下载地址](https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html)
2. 将pscp命令执行文件所在文件夹加入环境变量中
    有两种方式，其一是将pscp命令文件放到C:/windows/system32中，另一种方式是在系统环境变量的path中增加文件夹所在的路径。
3. 编写pscp文件上传的bat脚本
    脚本内容如下所示（假设构建的包所在路径为：
    ```
    echo 'start upload package'
    pscp -l username -pw xxx -p -v -r path_to_package_file username@ip:path_to_linux_fold < C:/deploy/confirm.bat
    echo 'finished upload package'
    ```
    其中：
    - -l 后面跟linux系统用户名称
    - -pw 后面跟linux系统用户密码
    - -p 拷贝文件的时候保留源文件建立的时间
    - -v 拷贝文件时，显示提示信息
    - r 拷贝整个目录
    - path_to_package_file 构建出来的包的绝对路径，例如：C:/saas_pre/supplier-web/target/*.war
    - username@ip 为linux系统用户名称@linux系统的IP
    - path_to_linux_fold 文件上传到linux服务器的目录
    - `C:/deploy/confirm.bat` 为一个文件，内容如下所示：
        ```
        y

        ```
        需要该文件，是因为pscp执行的过程中需要进行交互确认，即在命令执行过程中需要用户输入"y"进行确认，但是在脚本中用户无法输入，所以使用重定向符。

## 远程执行linux服务器的脚本，自动部署
jenkins远程执行linux服务器的脚本，需要安装插件`SSH plugin`，安装方式：
1. 依次进入jenkins的【系统管理】->【插件管理】->【可选插件】，搜索`SSH`，然后选中`SSH plugin`进行安装
2. 重启jenkins
3. 依次进入jenkins的【系统管理】->【系统设置】，在`SSH remote hosts`一栏，增加远程linux服务器的主机信息
    添加远程linux主机信息时，需要选择一个Credential，可以到jenkins首页、凭据中进行添加
4. 进入jenkins的构建项目的配置界面，在`构建`一栏的`增加构建步骤`中将多出一个选择项：`Execute shell script on remote host using ssh`，选择该项，然后选择第3步中配置的remote host，并在Command一栏中输入需要远程执行的脚本文件或命令。