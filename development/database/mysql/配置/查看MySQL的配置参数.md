# 查看MySQL的配置参数
1. 通过`show variables`查看
    下面查看系统配置项：validate_password
    ```
    mysql> show variables like '%validate_password%'
    ```