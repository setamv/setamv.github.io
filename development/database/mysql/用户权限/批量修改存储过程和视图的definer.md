# 批量修改存储过程和视图的definer
有的时候存储过程或视图的definer用户被删除后，会导致存储过程不能执行，此时，需要修改他们的definer.

## 批量修改存储过程的definer 
存储过程和函数的信息全部保存在mysql.proc中，可以通过修改mysql.proc表的definer来批量修改存储过程和函数的definer
首先查出存储过程和函数
```
select db, name, type, `definer` from mysql.proc where db = 'db_name';
```
然后更新即可
```
update mysql.proc set definer = 'db_user@127.0.0.1' where db = 'db_name';
```

## 批量修改视图的definer 
视图的信息保存在information_schema.views表中，该表不能编辑，但是可以通过拼接视图的创建语句来批量重新创建的视图。
```
SELECT CONCAT('CREATE OR REPLACE VIEW ', TABLE_NAME, ' AS ', VIEW_DEFINITION, ';')
FROM information_schema.views WHERE TABLE_SCHEMA = 'db_name';
```
上面的查询语句结果为创建所有视图的SQL语句。