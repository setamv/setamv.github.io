# windows远程上传文件到linux脚本

1. 在windows上安装pscp
    下载地址：https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html
    下载完成后，将pscp.exe放入c:/windows/system32下即可。
2. 编写上传脚本 
    下面是上传war包的脚本： 
    ```
    echo "start upload war package"
    pscp -l saaspre -pw ZxrcSaas.3ph9 -p -v -r C:/Windows/System32/config/systemprofile/.jenkins/workspace/saas_pre/supplier-web/target/*.war saaspre@47.99.166.220:/home/saaspre/deploy/saas < C:/Windows/System32/config/systemprofile/.jenkins/scripts/saas/confirm.bat
    echo "finished upload war package"
    exit
    ```

    confirm.bat文件的内容如下所示：
    ```
    y
    ```