# Zookeeper ACL
Zookeeper使用ACL对znodes的访问进行控制。每个znode都可以单独设置ACL，一个znode的ACL设置不会被继承到它的子节点上。

## ACL权限类型
Zookeeper中的ACL权限分为以下几种：
+ CREATE
    在指定的znode上拥有*CREATE*权限，将可以创建该znode的子节点
+ READ
    在指定的znode上拥有*READ*权限，将可以获取该znode的数据以及列出该znode的子节点清单
+ WRITE
    在指定的znode上拥有*WRITE*权限，将可以设置该znode的数据
+ DELETE
    在指定的znode上拥有*DELETE*权限，将可以删除该znode的子节点
+ ADMIN
    在指定的znode上拥有*ADMIN*权限，将可以对该znode的ACL权限进行设置

## ACL权限控制方案（鉴权方案）
Zookeeper使用ACL权限控制方案对znode的访问进行控制。ACL权限控制方案使用*scheme:expression:perms*的表达式形式， 其中：
+ *scheme*用于指定权限控制方案的类型
+ *expression*用于指定权限控制的表达式，不同的*scheme*下，权限控制的表达式格式也不一样
    在控制客户端访问指定znode时，客户端提供的值（该值被称为 *ACL ID entity*）通过了*expression*表达式的校验后，才能获得对应的访问权限。
+ *perms*用于指定允许的ACL权限类型，即：CREATE、READ、WRITE、DELETE、ADMIN

当一个znode设置了指定权限控制方案后，客户端在访问znode的时候必须提供有效的鉴权信息，该鉴权信息被称为*ACL ID*。它包括两部分：
+ *scheme*，用于指定客户端使用的鉴权方案的类型，必须与znode设置的权限控制方案的类型一致
+ *ACL ID entity*，Zookeeper将使用znode设置的权限控制表达式*expression*对*ACL ID entity*进行校验。

Zookeeper内建的权限控制方案的类型*scheme*包括以下几种：
+ world
    world模式中指定一个id，
+ auth
    auth模式，
+ digest    
    *digest*方式的权限控制方案下，客户端提供*username:password*（即用户名和密码）值，Zookeeper使用*username:passowrd*值生成一个MD5哈希值作为访问znode节点的*ACL ID entity**`。
+ ip
    *ip*方式的权限控制方案下，znode的权限控制表达式形式为*addr/bits*，其中，*addr*指定允许的ip地址，*bits*指定匹配*addr*中ip地址的位数。
    客户端访问znode时，Zookeeper使用客户端的IP作为*ACL ID entity*与*expression*部分进行校验，当客户端的IP地址的高*bits*位与znode权限控制表达式中*addr*的高*bits*位一致时，将通过校验。
    例如：znode的权限控制方案为：ip:19.22.0.0/16:READ；客户端的IP只要以19.22开始既满足znode的权限控制校验。