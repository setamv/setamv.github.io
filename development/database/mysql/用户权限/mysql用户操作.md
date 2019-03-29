[首页](/index.md) << ... << [上一级](../index.md)

# mysql用户操作

## 新增、修改、删除用户

### 新增用户
```
CREATE USER 'setamv'@'localhost' IDENTIFIED BY 'password';
```

### 删除用户
```
DROP USER 'root'@'localhost';
```

## 设置用户密码
```
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('newpassword');
```

## 为用户授权
```
GRANT privileges ON databasename.tablename TO 'username'@'host';
```
其中，privileges 为用户的操作权限，如SELECT , INSERT , UPDATE 等。

## 查看用户的授权信息
```
show grant for root@localhost;
```