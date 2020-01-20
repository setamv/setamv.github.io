# MySQL查询优化

## TEXT、BLOB大字段查询
当一个表中包含TEXT、BLOB大字段时，一定要避免在查询结果集很多的时候，查询字段中包含TEXT、BOLB类型的字段。
因为MySQL在查询过程中，查询结果集中包含大量的数据将在sendData阶段消耗很大的时间