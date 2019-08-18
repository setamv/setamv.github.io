# spring事务的理解

## Spring绑定 @Transactional 注解声明的事务上下文对象
事务上下文对象为一个持有数据库连接Connection对象的ConnectionHolder对象。并且已经将Connection对象的autoCommit属性显示设置为false。

如果一个方法上面声明了 @Transactional 注解，spring的拦截器将在该方法被调用时，在代理方法中根据指定的事务传播属性，创建事务上下文对象。
@Transactional 注解代理方法的入口是：`org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(Method, Class<?>, InvocationCallback)`。 该方法用于处理 @Transactinal 注解

+ `org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(Method, Class<?>, InvocationCallback)`
    该方法主要调用 `org.springframework.transaction.interceptor.TransactionAspectSupport.createTransactionIfNecessary(PlatformTransactionManager, TransactionAttribute, String)` 方法，获取或创建事务上下文对象。

+ `org.springframework.transaction.interceptor.TransactionAspectSupport.createTransactionIfNecessary(PlatformTransactionManager, TransactionAttribute, String)`
    该方法会调用`org.springframework.transaction.support.AbstractPlatformTransactionManager.getTransaction(TransactionDefinition)`获取事务上下文对象，并将事务上下文对象包装进TransactionInfo对象，然后通过`org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo.bindToThread()`方法将TransactionInfo对象绑定到当前线程。

+ `org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo.bindToThread()`   
    将事务上下文对象绑定到当前线程中

+ `org.springframework.transaction.support.AbstractPlatformTransactionManager.getTransaction(TransactionDefinition)`
    该方法用于获取事务上下文对象。
    如果不存在事务上下文对象，将调用`org.springframework.jdbc.datasource.DataSourceTransactionManager.doBegin(Object, TransactionDefinition)`方法。参见该方法说明

+ `org.springframework.jdbc.datasource.DataSourceTransactionManager.doBegin(Object, TransactionDefinition)`   
    - 该方法用于创建一个事务上下文对象，即：从dataSource中获取一个数据库链接，并将autoCommit设置为false.
    - 同时，创建一个数据库连接持有对象`org.springframework.jdbc.datasource.ConnectionHolder.ConnectionHolder(Connection)`，ConnectionHolder对象保存有上面从dataSource获取到的数据库连接。事务上下文对象就是指的该ConnectionHolder对象。
    - 最后，该方法将ConnectionHolder对象保存到上下文中，即`org.springframework.transaction.support.TransactionSynchronizationManager.resources`对象中，key为dataSource对象；value为ConnectionHolder对象。

+ `org.springframework.transaction.support.TransactionSynchronizationManager.`  

注意：如果不是通过代理方式执行的方法，即便方法标注了注解 @Transactional ，也不会产生事务上下文对象。例如：有如下代码：
```
class A {
    @Transactional
    public void insert(obj) {...}  

    public void insertNoTrans(obj) { insert(obj); }
}
class B {
    @Autowired
    A a;

    public void insertA(obj) { a.insert(obj); }
}
```  
B中的方法B.insertA(obj)调用了A的insert(obj)方法，因为B中的对象a是通过注入的方式绑定，所以spring将通过代理来执行a.insert(obj)方法，这种情况下，A.insert的注解 @Transactional 将会产生一个事务上下文对象。
然而，A中的方法insertNoTrans(obj)，因为是调用的同一个类的方法，所以，根本就不会通过代理的方式触发，而是直接调用了，此时， @Transactional 注解将没有任何意义。所以也不会产生事务上下文对象。

## Mybatis在执行具体的数据库操作时，寻找事务上下文对象的方式
mybatis是通过代理方法`org.mybatis.spring.SqlSessionTemplate.SqlSessionInterceptor.invoke(Object, Method, Object[])`执行具体的数据库操作（mapper），

+ `org.mybatis.spring.SqlSessionTemplate.SqlSessionInterceptor.invoke(Object, Method, Object[])`
    该方法是mybatis执行mapper方法的代理方法，在执行数据库操作之前，代理方法将通过方法`org.mybatis.spring.SqlSessionUtils.getSqlSession(SqlSessionFactory, ExecutorType, PersistenceExceptionTranslator)`获取一个非常重要的对象：`org.apache.ibatis.session.SqlSession`，该对象将绑定到当前线程上下文中。
    然后，通过调用method.invoke(sqlSession, args)方法，执行具体的数据库操作。

+ `org.apache.ibatis.executor.SimpleExecutor.prepareStatement(StatementHandler, Log)`
    mybatis通过该方法构建数据库操作的Statement对象，在该方法中，依次通过下面三个方法，最终获取了数据库连接对象Connection。
    `org.apache.ibatis.executor.BaseExecutor.getConnection(Log)`
    `org.mybatis.spring.transaction.SpringManagedTransaction.getConnection()`
    `org.mybatis.spring.transaction.SpringManagedTransaction.openConnection()`

+ `org.mybatis.spring.transaction.SpringManagedTransaction.openConnection()`
    该方法通过`org.springframework.transaction.support.TransactionSynchronizationManager.getResource(Object)`获取事务上下文对象ConnectionHolder（key为当前的DataSource对象），该对象就是上面“Spring绑定 @Transactional 注解声明的事务上下文对象” 一节中保存的事务上下文对象。
    如果未能获取到事务上下文对象（即前面还没有过@Transactional注解），则直接通过dataSource获取一个Connection链接并返回，此时获取的Connection对象并不会创建事务上下文对象ConnectionHolder.

## 总结
通过上面两个小结 “Spring绑定 @Transactional 注解声明的事务上下文对象” 和 “Mybatis在执行具体的数据库操作时，寻找事务上下文对象的方式” 可以知道，如果在一个方法上标注了注解 @Transactional ，spring在从注入的代理对象调用该方法时，会在当前线程绑定一个事务上下文对象ConnectionHolder，后续该方法开始和结束之间（包括调用的其他非异步执行的方法）所有的数据库操作（通过myBatis的sqlSession），都将获取到该事务上下文对象。所以，如果该方法调用了其他类的方法（假设为 C.insert(obj)），且C.insert(obj)方法未标注 @Transactional 注解，C.insert(obj) 方法都将在当前的事务上下文中执行，即在同一个事务中。

### 示例
### 示例代码
```
public class Service
{
  // 该方法将正确执行并返回。
  public void addService1()
  {
    Entity o = getEntity();
    o.setId(1);
    mapper.insert(o);   // 这里插入数据库成功
  }

  // 该方法将正确执行并返回。
  @Transactional
  public void addService2()
  {
    Entity o = getEntity();
    o.setId(2);
    mapper.insert(o);   // 这里插入数据库成功
  }

  // 该方法将正确执行并返回。
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void addService3()
  {
    Entity o = getEntity();
    o.setId(3);
    mapper.insert(o);   // 这里插入数据库成功
  }

  // 该方法将在执行的过程中抛出错误
  @Transactional
  public void addServiceException()
  {
    Entity o = getEntity();
    o.setId(999);
    o.setName(null);    
    mapper.insert(o);   // 这里插入数据库失败！因为name为非空字段
  }

  // 未声明事务的服务调用
  public void noTransactionalService()
  {
    addService1();
    addService2();
    addService3();
    addServiceException();
  }

  // 声明了事务的服务调用
  @Transactional
  public void transactionalService()
  {
    addService1();
    addService2();
    addService3();
    addServiceException();
  }
}


class ServiceInvoker
{
  @Autowired
  Service service;

  // 未声明事务的服务调用
  public void noTransactionalService()
  {
    service.addService1();
    service.addService2();
    service.addService3();
    service.addServiceException();
  }

  // 声明了事务的服务调用
  @Transactional
  public void transactionalService()
  {
    service.addService1();
    service.addService2();
    service.addService3();
    service.addServiceException();
  }
}
```

### 执行结果分析：
#### ServiceInvoker.noTransactionalService
+ 执行结果
    ServiceInvoker.noTransactionalService() 执行结束后，数据库中将新增ID为 1, 2, 3的3条记录。
+ 分析
    因为该方法未声明事务，所以 addService1、addService2、addService3、addServiceException 四个方法调用都是在各自单独的事务管理中进行，其中，addService1没有事务，自动提交；addService2、addService3会开启一个事务并在方法离开的时候提交；addServiceException也会开启一个事务，并在遇到异常的时候回滚。

#### ServiceInvoker.transactionalService
+ 执行结果
    ServiceInvoker.transactionalService() 执行结束后，数据库中将新增ID为 3 的1条记录。
+ 分析
    因为该方法声明了事务注解，并且spring的事务传播属性为默认的 Propagation.REQUIRED，所以该方法会开启一个事务，并且因为最后一个方法addServiceException因为执行失败了，会导致该方法执行回滚。：
    - addService1
        未声明事务注解，但是因为外层调用transactionalService创建了事务上下文对象，该方法在执行mybatis的mapper方法时，将获取到该事务上下文对象，从而与transactionalService方法处于同一个事务当中，所以最后被回滚了。
    - addService2
        声明了事务注解，且事务传播属性为 Propagation.REQUIRED，该方法将与transactionalService方法处于同一个事务中，所以该方法的执行结果在transactionalService方法回滚的时候一起回滚：即ID为2的记录被回滚，从而保存失败。
    - addService3
        声明了事务注解，并且指定事务传播属性为 Propagation.REQUIRES_NEW，该方法将与transactionalService方法处于不同的事务中，所以transactionalService回滚不会影响该方法的最终执行结果：即ID为3的记录保存成功。
    - addServiceException
        声明了事务注解，且事务传播属性为 Propagation.REQUIRED，该方法将与transactionalService方法处于同一个事务中，所以该方法的回滚后，也会导致transactionalService方法回滚。

#### Service.transactionalService
+ 执行结果
    Service.transactionalService() 执行结束后，数据库中不会新增任何记录。
+ 分析
    因为 addService1、addService2、addService3、addServiceException 四个方法是被同一个对象的方法Service.transactionalService()所调用，所以这四个方法的 @Transactional 注解将被忽略（因为不是通过spring的代理对象触发的调用），即：这四个方法都处于 Service.transactionalService 开启的事务中，因为最后的addServiceException方法发生异常，发生全部都回滚了。

#### Service.noTransactionalService
+ 执行结果
    Service.transactionalService() 执行结束后，数据库中将会新增ID为 1, 2, 3 的3条记录。
+ 分析
    因为 addService1、addService2、addService3、addServiceException 四个方法是被同一个对象的方法Service.transactionalService()所调用，所以这四个方法的 @Transactional 注解将被忽略（因为不是通过spring的代理对象触发的调用），并且，Service.transactionalService()方法本身没有声明 @Transactional 注解，所以，这四个方法都将处于各自独立的事务中执行。
