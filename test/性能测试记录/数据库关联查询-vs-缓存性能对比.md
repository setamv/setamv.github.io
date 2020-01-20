# 数据库关联查询VS缓存性能

## 测试用例1
使用两种查询方式对比，两种查询方式分别如下：
1. 所有的数据都通过SQL表关联查询，脚本如下所示：
    ```
    SELECT a.*, b.info_name, b.specification, c.name AS product_name, c.producer_id, d.name AS producer_name,
            e.name AS owner_customer_name, f.certificate_no, u.name AS unit_name
        FROM om_storeout_detail a
        LEFT JOIN pm_product b ON a.product_id = b.id
        LEFT JOIN pm_product_name c ON b.name_id = c.id
        LEFT JOIN pm_producer d ON c.producer_id = d.id
        LEFT JOIN bm_customer e ON a.owner_customer_id = e.id
        LEFT JOIN bm_supplier_product_name_reg f ON a.reg_id = f.id
        LEFT JOIN bm_supplier_packing_unit u ON a.unit_id = u.id
        <where>
            <include refid="queryConditions"/>
        </where>
    ```
2. 只有基本的数据通过SQL脚本查询，关联信息通过缓存获取，脚本如下所示：
    ```
    // 以下是SQL脚本
    SELECT a.*
        FROM om_storeout_detail a
        <where>
            <include refid="queryConditions"/>
        </where>

    // 以下是获取缓存的数据
    List<StoreoutDetailDTO> detailList = omStoreoutDetailDao.querySimpleInfo(queryParams);

		// 从缓存填充计量单位名称、生产企业名称
		Long supplierId = MapUtil.getLong(queryParams, "supplier_id");
		cacheFiller.filler(supplierId, detailList)
			.product(OmStoreoutDetail::getProduct_id, (detail, p) -> {
				detail.setInfo_name(p.getInfo_name());
				detail.setSpecification(p.getSpecification());
				detail.setName_id(p.getName_id());
			})
			.productName(OmStoreoutDetail::getName_id, (detail, n) -> {
				detail.setProduct_name(n.getName());
				detail.setProducer_id(n.getProducer_id());
			})
			.producer(StoreoutDetailDTO::getProducer_id, (detail, producer) -> detail.setProducer_name(producer.getName()))
			.customer(StoreoutDetailDTO::getOwner_customer_id, (detail, customer) -> detail.setOwner_customer_name(customer.getName()))
			.reg(StoreoutDetailDTO::getReg_id, (detail, reg) -> detail.setCertificate_no(reg.getCertificate_no()))
			.unit(StoreoutDetailDTO::getUnit_id, (detail, unit) -> detail.setUnit_name(unit.getName()));
    ```

上述表`om_storeout_detail`有17710条记录，其他的关联表数据记录都比较少，几百条。分别使用分页查询，每页查询10条记录。
测试代码如下，其中测试的次数根据实际测试情况进行调整：
```
public class OmStoreoutDetailServiceTest extends BaseTest {

    @Autowired
    private OmStoreoutDetailService omStoreoutDetailService;

    private static final int testTimes = 10000;     // 测试次数根据实际情况调整

    private static final Map<String, Object> queryParams;

    static {
        queryParams = new HashMap<>();
        queryParams.put("supplier_id", 145L);
        queryParams.put("limit", 10);
    }

    @Test
    public void testQueryTestPage() {
        Page<StoreoutDetailDTO> page1 = null, page2 = null;
        Long startTime = System.currentTimeMillis();
        for (int i = 0; i < testTimes; i++) {
            queryParams.put("pageno", (i + 1) % 1000);
            // 这里是通过SQL表关联查询
            page1 = omStoreoutDetailService.queryTestPage(queryParams);
        }
        Long endTime = System.currentTimeMillis();
        Long costTime1 = endTime - startTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < testTimes; i++) {
            queryParams.put("pageno", (i + 1) % 1000);
            // 只有基本的数据通过SQL脚本查询，关联信息通过缓存获取
            page2 = omStoreoutDetailService.queryTestWithCachePage(queryParams);
        }
        endTime = System.currentTimeMillis();
        Long costTime2 = endTime - startTime;

        System.out.println("~~~~~~~~~~~~~~ costTime1=" + costTime1 + "(ms), costTime2=" + costTime2 + "(ms)");
    }

}
```

测试硬件：联想笔记本电脑，i7-8550U，内存 8G，256G SSD，所有应用都跑在一台机器上

### 测试结果1
从分页的第1页，查询到最后1页，总共查询100次，测试代码以及耗时如下所示：
```
druid.sql.Statement 2019-11-25 16:39:29,625 -- DEBUG -- {conn-10005, pstmt-20003} executed. 77.7838 millis. SELECT count(0) FROM om_storeout_detail a LEFT JOIN pm_product b ON a.product_id = b.id LEFT JOIN pm_product_name c ON b.name_id = c.id LEFT JOIN pm_producer d ON c.producer_id = d.id LEFT JOIN bm_customer e ON a.owner_customer_id = e.id LEFT JOIN bm_supplier_product_name_reg f ON a.reg_id = f.id LEFT JOIN bm_supplier_packing_unit u ON a.unit_id = u.id WHERE a.supplier_id = ?
druid.sql.Statement 2019-11-25 16:39:29,638 -- DEBUG -- {conn-10005, pstmt-20005} executed. 12.2902 millis. SELECT a.*, b.info_name, b.specification, c.name AS product_name, c.producer_id, d.name AS producer_name, e.name AS owner_customer_name, f.certificate_no, u.name AS unit_name FROM om_storeout_detail a LEFT JOIN pm_product b ON a.product_id = b.id LEFT JOIN pm_product_name c ON b.name_id = c.id LEFT JOIN pm_producer d ON c.producer_id = d.id LEFT JOIN bm_customer e ON a.owner_customer_id = e.id LEFT JOIN bm_supplier_product_name_reg f ON a.reg_id = f.id LEFT JOIN bm_supplier_packing_unit u ON a.unit_id = u.id WHERE a.supplier_id = ? LIMIT ?, ?

...
druid.sql.Statement 2019-11-25 16:40:45,693 -- DEBUG -- {conn-10005, pstmt-20006} executed. 12.5347 millis. SELECT count(0) FROM om_storeout_detail a WHERE a.supplier_id = ?
druid.sql.Statement 2019-11-25 16:30:40,574 -- DEBUG -- {conn-10005, pstmt-20009} executed. 7.1318 millis. SELECT a.* FROM om_storeout_detail a WHERE a.supplier_id = ? LIMIT ?, ? 
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,701 -- DEBUG -- 开始从缓存获取对象. serviceName=pm_product, keyPrefix=pm_product:145, size=2
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,702 -- DEBUG -- 完成从缓存获取对象. serviceName=pm_product, key size=2, fetched size=2, cost time=1(ms)
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,702 -- DEBUG -- 完成从缓存获取对象. serviceName=pm_product_name, key size=1, fetched size=1, cost time=0(ms)
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,703 -- DEBUG -- 完成从缓存获取对象. serviceName=pm_producer, key size=1, fetched size=1, cost time=1(ms)
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,703 -- DEBUG -- 完成从缓存获取对象. serviceName=bm_customer, key size=1, fetched size=1, cost time=0(ms)
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,704 -- DEBUG -- 完成从缓存获取对象. serviceName=bm_supplier_product_name_reg, key size=1, fetched size=1, cost time=1(ms)
com.zhixin.supplier.service.cache.AbstractCacheService 2019-11-25 16:40:45,704 -- DEBUG -- 完成从缓存获取对象. serviceName=bm_supplier_packing_unit, key size=1, fetched size=1, cost time=0(ms)

...

~~~~~~~~~~~~~~ costTime1=10554(ms), costTime2=3271(ms)

```
测试结果分析：
1. 当通过SQL表关联查询时，总耗时10秒，而关联信息通过缓存获取时，耗时减少为3秒左右。
2. 当通过SQL表关联查询时，查询总记录数的耗时为77.7838毫秒，查询分页数据的耗时为12.2902毫秒；而关联信息通过缓存获取时，查询总记录数的耗时为12.5347毫秒，查询分页数据的耗时为7.1318毫秒，从缓存获取每一个分项数据的时间基本都在1毫秒以内。
3. CPU占用情况：
    当通过SQL表关联查询时，CPU占用情况稳定在：MySQL占用21%左右
    当关联信息通过缓存获取时，CPU占用情况稳定在：MySQL占用13%左右，redis占用1%左右
    
### 测试结果2
从分页的第1页，查询到最后1页，总共查询10000次，测试代码以及耗时如下所示：
```
~~~~~~~~~~~~~~ costTime1=1150430(ms), costTime2=296868(ms)
```
可以看到，当通过SQL表关联查询时，耗时1150秒，接近20分钟，而关联信息通过缓存获取时，耗时减少为296秒左右，只有5分钟。