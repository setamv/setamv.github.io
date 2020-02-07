# Java面试题

## Java语言

### 为什么 wait，notify 和 notifyAll 是在 Object 类中定义的而不是在 Thread 类中定义
1. wait 和 notify 不仅仅是普通方法或同步工具，更重要的是它们是 Java 中两个线程之间的通信机制。对语言设计者而言, 如果不能通过 Java 关键字(例如 synchronized)实现通信此机制，同时又要确保这个机制对每个对象可用, 那么 Object 类则是的正确声明位置。记住同步和等待通知是两个不同的领域，不要把它们看成是相同的或相关的。同步是提供互斥并确保 Java 类的线程安全，而 wait 和 notify 是两个线程之间的通信机制。
2. 每个对象都可上锁，这是在 Object 类而不是 Thread 类中声明 wait 和 notify 的另一个原因。
3. 在 Java 中为了进入代码的临界区，线程需要锁定并等待锁定，他们不知道哪些线程持有锁，而只是知道锁被某个线程持有， 并且他们应该等待取得锁, 而不是去了解哪个线程在同步块内，并请求它们释放锁定。
4. Java 是基于 Hoare 的监视器的思想(http://en.wikipedia.org/wiki/...。在Java中，所有对象都有一个监视器。
5. 线程在监视器上等待，为执行等待，我们需要2个参数：
    一个线程
    一个监视器(任何对象)
    在 Java 设计中，线程不能被指定，它总是运行当前代码的线程。但是，我们可以指定监视器(这是我们称之为等待的对象)。这是一个很好的设计，因为如果我们可以让任何其他线程在所需的监视器上等待，这将导致“入侵”，导致在设计并发程序时会遇到困难。请记住，在 Java 中，所有在另一个线程的执行中侵入的操作都被弃用了(例如 stop 方法)

### 为什么 String 在 Java 中是不可变的？
+ 字符串在 Java 中是不可变的，因为 String 对象缓存在 String 池中。由于缓存的字符串在多个客户之间共享，因此始终存在风险，其中一个客户的操作会影响所有其他客户。例如，如果一段代码将 String “Test” 的值更改为 “TEST”，则所有其他客户也将看到该值。由于 String 对象的缓存性能是很重要的一方面，因此通过使 String 类不可变来避免这种风险。
+ 同时，String 是 final 的，因此没有人可以通过扩展和覆盖行为来破坏 String 类的不变性、缓存、散列值的计算等。String 类不可变的另一个原因可能是由于 HashMap。
    Java 中的不可变 String 缓存其哈希码，并且不会在每次调用 String 的 hashcode 方法时重新计算，这使得它在 Java 中的 HashMap 中使用的 HashMap 键非常快。简而言之，因为 String 是不可变的，所以没有人可以在创建后更改其内容，这保证了 String 的 hashCode 在多次调用时是相同的。
+ 由于把字符串作为 HashMap 键很受欢迎。对于键值来说，重要的是它们是不可变的，以便用它们检索存储在 HashMap 中的值对象。由于 HashMap 的工作原理是散列，因此需要具有相同的值才能正常运行。如果在插入后修改了 String 的内容，可变的 String将在插入和检索时生成两个不同的哈希码，可能会丢失 Map 中的值对象。
+ 字符串已被广泛用作许多 Java 类的参数，例如，为了打开网络连接，你可以将主机名和端口号作为字符串传递，你可以将数据库 URL 作为字符串传递, 以打开数据库连接，你可以通过将文件名作为参数传递给 File I/O 类来打开 Java 中的任何文件。如果 String 不是不可变的，这将导致严重的安全威胁，我的意思是有人可以访问他有权授权的任何文件，然后可以故意或意外地更改文件名并获得对该文件的访问权限。由于不变性，你无需担心这种威胁。这个原因也说明了，为什么 String 在 Java 中是最终的，通过使 java.lang.String final，Java设计者确保没有人覆盖 String 类的任何行为。


### 什么是 serialVersionUID ？如果你不定义这个, 会发生什么？
serialVersionUID 是一个 private static final long 型 ID, 当它被印在对象上时, 它通常是对象的哈希码,你可以使用 serialver 这个 JDK 工具来查看序列化对象的 serialVersionUID。SerialVerionUID 用于对象的版本控制。 也可以在类文件中指定 serialVersionUID。不指定 serialVersionUID的后果是,当你添加或修改类中的任何字段时, 则已序列化类将无法恢复, 因为为新类和旧序列化对象生成的 serialVersionUID 将有所不同。Java 序列化过程依赖于正确的序列化对象恢复状态的, ,并在序列化对象序列版本不匹配的情况下引发 java.io.InvalidClassException 无效类异常,了解有关 serialVersionUID 详细信息

### 序列化时,你希望某些成员不要序列化？你如何实现它？
如什么是瞬态 trasient 变量, 瞬态和静态变量会不会得到序列化等,所以,如果你不希望任何字段是对象的状态的一部分, 然后声明它静态或瞬态根据你的需要, 这样就不会是在 Java 序列化过程中被包含在内。

### 如果类中的一个成员未实现可序列化接口, 会发生什么情况？
如果尝试序列化实现可序列化的类的对象,但该对象包含对不可序列化类的引用,则在运行时将引发不可序列化异常 NotSerializableException。非静态内部类因为默认持有外部类的引用，所以，内部类如果需要序列化，则外部类也必须实现序列化接口。

###  假设新类的超级类实现可序列化接口, 如何避免新类被序列化？
如果类的 Super 类已经在 Java 中实现了可序列化接口, 那么它在 Java 中已经可以序列化, 因为你不能取消接口, 它不可能真正使它无法序列化类, 但是有一种方法可以避免新类序列化。为了避免 Java 序列化,你需要在类中实现 writeObject() 和 readObject() 方法, 并且需要从该方法引发不序列化异常NotSerializableException。 这是自定义 Java 序列化过程的另一个好处, 如上述序列化面试问题中所述, 并且通常随着面试进度, 它作为后续问题提出。

### 假设你有一个类,它序列化并存储在持久性中, 然后修改了该类以添加新字段。如果对已序列化的对象进行反序列化, 会发生什么情况？
这取决于类是否具有其自己的 serialVersionUID。正如我们从上面的问题知道, 如果我们不提供 serialVersionUID, 则 Java 编译器将生成它, 通常它等于对象的哈希代码。通过添加任何新字段, 有可能为该类新版本生成的新 serialVersionUID 与已序列化的对象不同, 在这种情况下, Java 序列化 API 将引发 java.io.InvalidClassException, 因此建议在代码中拥有自己的 serialVersionUID, 并确保在单个类中始终保持不变。

### 在 Java 序列化期间,哪些变量未序列化？
由于静态变量属于类, 而不是对象, 因此它们不是对象状态的一部分, 因此在 Java 序列化过程中不会保存它们。由于 Java 序列化仅保留对象的状态,而不是对象本身。瞬态变量（声明了transient关键字）也不包含在 Java 序列化过程中, 并且不是对象的序列化状态的一部分。


## 同步
### 为什么Java中wait方法需要在synchronized的方法中调用？
+ Java 会抛出 IllegalMonitorStateException，如果我们不调用来自同步上下文的wait()，notify()或者notifyAll()方法。
+ 如果不在synchronized方法中调用 wait 和 notify，将存在潜在的竞争条件。
    例如，一个生产者线程P，一个消费者线程C，和队列Q，当P测试队列Q满后，如果C同时进行消费，并调用notify()进行通知，因为此刻P还未执行wait等待，所以P后面再执行wait等待后，将错过C的notify通知。

## 你能用Java覆盖静态方法吗？如果我在子类中创建相同的方法是编译时错误？
你不能在Java中覆盖静态方法，但在子类中声明一个完全相同的方法不是编译时错误，这称为隐藏在Java中的方法。
你不能覆盖Java中的静态方法，因为方法覆盖基于运行时的动态绑定，静态方法在编译时使用静态绑定进行绑定。虽然可以在子类中声明一个具有相同名称和方法签名的方法，看起来可以在Java中覆盖静态方法，但实际上这是方法隐藏。Java不会在运行时解析方法调用，并且根据用于调用静态方法的 Object 类型，将调用相应的方法。这意味着如果你使用父类的类型来调用静态方法，那么原始静态将从父类中调用，另一方面如果你使用子类的类型来调用静态方法，则会调用来自子类的方法。

## ThreadPoolExecutor
ThreadPoolExecutor类继承了AbstractExecutorService类，并提供了四个构造器。构造器中分别有一下参数：
+ corePoolSize
    核心池的大小，在创建线程池后，默认情况下，线程池中并没有任何线程，而是等待有任务到来才创建线程去执行任务，除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法来预创建线程。即在没有任务到来之前就创建corePoolSize个线程或者一个线程。默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中。
+ maximumPoolSize
    线程池最大的线程数，表示在线程池中最多能创建多少个线程；
+ keepAliveTime
    表示线程没有任务执行时最多保持多久时间会终止。默认情况下，只有当线程池中的线程数大于corePoolSize时，keepAliveTime才会起作用，直到线程池中的线程数不大于corePoolSize。即当线程池中的线程数大于corePoolSize时，如果一个线程空闲时间达到keepAliveTime，则会终止，直到线程池中的线程数不超过corePoolSize。但是如果调用了allowCoreThreadTimeOut(boolean)方法，在线程池中的线程数不大于corePoolSize时，keepAliveTime参数也会起作用，知道线程池中的线程数为0。
+ workQueue
    一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响，一版来说阻塞队列有以下几种：
    ArrayBlockingQueue
    LinkedBlockingQueue
    SynchronousQueue    这个队列比较特殊，他不会保存提交的任务，而是将直接新建的一个线程来执行新的任务。
    ArrayBlockingQueue使用较少，一使用LinkedBlokingQueue和SynchronousQueue。线程池排队策率与BlockingQueue有关

## HASHTABLE, HASHMAP，TreeMap区别
Hashmap和HashTable都实现了Map接口，但HashTable是线程安全的，HashMap是非线程安全的。HashMap中允许key-value值均为null，但HashTable则不允许。HashMap适合单线程，HashTable适合多线程。HashTAble中的hash数字默认大小是11，增加方式为old*2+1,HashMap中的hash默认大小为16，且均为2的指数。TreeMap则可以将保持的数据根据key值进行排列，可以按照指定的排序方式。默认为升序。

### ConcurrentHashMap和HashTable的区别
两者均应用于多线程中，但当HashTable增大到一定程度时，其性能会急剧下降。因为迭代时会被锁很长时间。但ConcurrentHashMap则通过引入分割来保证锁的个数不会很大。简而言之就是HashTable会锁住真个map，而ConcurrentHashMap则只需要锁住map的一个部分。

