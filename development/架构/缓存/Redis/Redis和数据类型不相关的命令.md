# 查询和变更键空间的命令
+ EXISTS 查询指定的Key是否存在
+ DEL 删除指定的Key和关联的Value

# 过期相关的命令
+ EXPIRE 为指定的Key设置过期时间，过期时间的单位可以为秒或毫秒。如：`> expire key 5`
+ TTL 查看指定的Key当前剩余的过期时间