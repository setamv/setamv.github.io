http://www.cnblogs.com/hustcat/archive/2009/10/28/1591648.html
http://www.jb51.net/article/50649.htm

CREATE TABLE People (
   last_name varchar(50)    not null,
   first_name varchar(50)    not null,
   dob        date           not null,
   gender     enum('m', 'f') not null,
   key(last_name, first_name, dob)
);

insert into People(last_name, first_name, dob, gender) values('Akroyd', 'Christian', '1958-12-07', 'm');
insert into People(last_name, first_name, dob, gender) values('Akroyd', 'Debbie', '1990-03-18', 'm');
insert into People(last_name, first_name, dob, gender) values('Akroyd', 'Kristen', '1978-11-02', 'm');
insert into People(last_name, first_name, dob, gender) values('Allen', 'Cuba', '1960-01-01', 'm');
insert into People(last_name, first_name, dob, gender) values('Allen', 'Kim', '1930-07-12', 'm');
insert into People(last_name, first_name, dob, gender) values('Allen', 'Meryl', '1980-12-12', 'm');
insert into People(last_name, first_name, dob, gender) values('Astaire', 'Angelina', '1960-01-01', 'm');
insert into People(last_name, first_name, dob, gender) values('Barrymore', 'Julia', '2000-05-16', 'm');
insert into People(last_name, first_name, dob, gender) values('Basinger', 'Viven', '1976-12-08', 'm');
insert into People(last_name, first_name, dob, gender) values('Basinger', 'Viven', '1979-01-24', 'm');


在MySQL查询语句中，总会发现明明已经建立了查询字段索引，可是却没有用到，这是因为在mysql中有些查询语句是用不到索引的，总结如下，以供大家分享。

对于单列索引，即非组合索引：
1.like语句中最

对于多列索引，即组合索引，创建索引时指定的列的顺序是非常重要的，MySQL仅能对索引最左边的前缀进行有效的查找。假设存在组合索引：idx_1(c1, c2, c3)，则：
1. where c1=v1 and c2=v2 和 where c1=v1都可以使用索引idx_1；而 where c2=v2就不能够使用该索引。因为没有组合索引的引导列，即，要想使用c2列进行查找，必需出现c1等于某值。

1.当一个索引为多列的联合索引时，组成索引的这些列是存在顺序的，即在创建索引时排在第一位的列，在索引中也处于第一位。
1.like语句中在最左边使用百分号，如：

2.列类型为字符串类型，查询时没有用单引号引起来 
3.在where查询语句中使用表达式 
  如：
  select t1.* 
from weibo_content t1
where t1.weibo_id = CONCAT('DDFCV', SUBSTR(t1.weibo_id FROM 6 FOR 4)) and t1.source='微博 weibo.com'

4.在where查询语句中对字段进行NULL值判断 
5.在where查询中使用了or关键字, myisam表能用到索引， innodb不行;(用UNION替换OR,可以使用索引) 
6.全表扫描快于索引扫描（数据量小时）

