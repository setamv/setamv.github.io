# Java中包装类型的大小比较
Java中包装类型因为引入了缓存，所以在对包装类型的大小进行比较时，可能会出现一些奇特的现象。

## Java中包装类型的大小比较的特殊案例
我们先来看下这个例子：
```
public class IntegerTest {
 
    public static void main(String[] args) {
        Integer a = 127;
        Integer b = 127;
        System.out.println(a == b);
 
        a = 128;
        b = 128;
        System.out.println(a == b);
 
        a = -128;
        b = -128;
        System.out.println(a == b);
 
        a = -129;
        b = -129;
        System.out.println(a == b);
    }
 
}
```
上述程序运行结果如下所示：
```
true
false
true
false
```
看到这个结果，是不是很疑惑，不应该都是true吗？

## 案例结果剖析
要弄懂这其中的缘由，我们要先明白上面的程序到底做了什么？
javap是JDK自带的反汇编器，可以查看java编译器为我们生成的字节码。通过它，我们可以对照源代码和字节码，从而了解很多编译器内部的工作。
于是，我们通过javap命令反编译IntegerTest.class字节码文件，得到结果如下：
```
$ javap -c IntegerTest
▒▒▒▒: ▒▒▒▒▒▒▒ļ▒IntegerTest▒▒▒▒com.lian.demo.IntegerTest
Compiled from "IntegerTest.java"
public class com.lian.demo.IntegerTest {
  public com.lian.demo.IntegerTest();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":                                                                                                              ()V
       4: return
 
  public static void main(java.lang.String[]);
    Code:
       0: bipush        127
       2: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
       5: astore_1
       6: bipush        127
       8: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      11: astore_2
      12: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                                                                              io/PrintStream;
      15: aload_1
      16: aload_2
      17: if_acmpne     24
      20: iconst_1
      21: goto          25
      24: iconst_0
      25: invokevirtual #4                  // Method java/io/PrintStream.printl                                                                                                              n:(Z)V
      28: sipush        128
      31: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      34: astore_1
      35: sipush        128
      38: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      41: astore_2
      42: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                                                                              io/PrintStream;
      45: aload_1
      46: aload_2
      47: if_acmpne     54
      50: iconst_1
      51: goto          55
      54: iconst_0
      55: invokevirtual #4                  // Method java/io/PrintStream.printl                                                                                                              n:(Z)V
      58: bipush        -128
      60: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      63: astore_1
      64: bipush        -128
      66: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      69: astore_2
      70: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                                                                              io/PrintStream;
      73: aload_1
      74: aload_2
      75: if_acmpne     82
      78: iconst_1
      79: goto          83
      82: iconst_0
      83: invokevirtual #4                  // Method java/io/PrintStream.printl                                                                                                              n:(Z)V
      86: sipush        -129
      89: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      92: astore_1
      93: sipush        -129
      96: invokestatic  #2                  // Method java/lang/Integer.valueOf:                                                                                                              (I)Ljava/lang/Integer;
      99: astore_2
     100: getstatic     #3                  // Field java/lang/System.out:Ljava/                                                                                                              io/PrintStream;
     103: aload_1
     104: aload_2
     105: if_acmpne     112
     108: iconst_1
     109: goto          113
     112: iconst_0
     113: invokevirtual #4                  // Method java/io/PrintStream.printl                                                                                                              n:(Z)V
     116: return
}
```
标号2的code调用了静态的valueOf方法解析Integer实例，也就是说Integer a = 127; 在编辑期进行了自动装箱，即把基本数据类型转换为包装类型。
从JDK1.5就开始引入了自动拆装箱的语法功能，也就是系统将自动进行基本数据类型和与之相对应的包装类型之间的转换，这使得程序员书写代码更加方便。

装箱过程是通过调用包装器的valueOf方法实现的。
拆箱过程是通过调用包装器的xxxValue方法实现的（xxx表示对应的基本数据类型）。
当给a赋值时，实际上是调用了Integer.valueOf(int i)方法。其JDK 8源码如下：
```
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```
继续看IntegerCache源码：
```
private static class IntegerCache {
    static final int low = -128;
    static final int high;
    static final Integer cache[];

    static {
        // high value may be configured by property
        int h = 127;
        String integerCacheHighPropValue =
            sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
        if (integerCacheHighPropValue != null) {
            try {
                int i = parseInt(integerCacheHighPropValue);
                i = Math.max(i, 127);
                // Maximum array size is Integer.MAX_VALUE
                h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
            } catch( NumberFormatException nfe) {
                // If the property cannot be parsed into an int, ignore it.
            }
        }
        high = h;

        cache = new Integer[(high - low) + 1];
        int j = low;
        for(int k = 0; k < cache.length; k++)
            cache[k] = new Integer(j++);

        // range [-128, 127] must be interned (JLS7 5.1.7)
        assert IntegerCache.high >= 127;
    }

    private IntegerCache() {}
}
```    
看到了没，当赋的基本数据类型值不在[-128, 127]之间，会去Java堆内存中new一个对象出来，显然它们是两个不同的对象，所以结果false；
而值在[-128, 127]之间，会直接从IntegerCache中获取，也就是从缓存中取值，不用再创建新的对象，即同一个对象，所以结果true。

关系操作符“==”生成的是一个boolean结果，它们计算的是操作数的值之间的关系。如果是基本类型则直接判断其值是否相等，如果是对象则判断是否是同一个对象的引用，即其引用变量所指向的对象的地址是否相同。

## 结论
在阿里巴巴Java开发手册中，它是这么描述的：

【强制】所有的相同类型的包装类对象之间值的比较，全部使用equals方法比较。

说明：对于Integer var = ? 在-128 至 127范围内的赋值，Integer对象是在IntegerCache.cache产生，会复用己有对象，这个区间内的Integer值可以直接使用==进行判断，但是这个区间之外的所有数据，都会在堆上产生，并不会复用己有对象，这是一个大坑，推荐使用equals方法进行判断。

可能你还会问，问啥是equals方法？这就要看equals方法到底做了什么？可以参考《Java中关系操作符“==”和equals()方法的区别》

我们来看下Integer类中equals源码：

public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }
直接通过intValue()方法拆箱，即将包类型转换为基本数据类型。所以equals方法比较的是它们的值了。

## 包装类型的缓存
包装类型	缓存赋值范围	基本数据类型	二进制数
Boolean    全部缓存          boolean	     1
Byte       [-128, 127]      byte	        8
Character  <=127            char	        16
Short      [-128, 127]      short	        16      
Integer    [-128, 127]      int	            32
Long       [-128, 127]      long	        64
Float       没有缓存        float	        32
Double      没有缓存        double	        64
所以两个同类型的Float或Double类型的==比较永远都是返回false。