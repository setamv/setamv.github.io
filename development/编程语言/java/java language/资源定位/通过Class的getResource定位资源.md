# Java中的资源定位
Java中有很多种方式来定位资源的路径，如：
+ 通过System.getProperty("user.dir")先获取到当前工作目录，然后再根据资源相对工作目录的路径定位资源。
+ 通过当前类的Class.getResource(String)获取到当前类所在路径，然后再根据资源相对于类的相对路径进行定位


## System.getProperty("user.dir")获取工作目录
通过System.getProperty("user.dir")获取到的工作目录，在不同场景下有所不同。
+ 通过诸如Intellij IDEA这种集成环境执行时，工作目录一般是项目的根目录
+ 通过java命令直接执行class文件或jar包中的class文件时，工作目录为当前执行命令所在的目录


## 通过Class.getResource定位资源
Java中，java.lang.Class.getResource(String)将返回基于当前类所在路径结合参数计算得到的路径。
下面以类org.setamv.jsetamv.language.reslocation.ClassResourceLocation为例讲解。

### 当class文件在文件目录中时
为了便于描述，假设 
+ 类文件所在目录为：E:/Workspace/setamv/jsetamv/language/target/classes/org/setamv/jsetamv/language/reslocation/ 
+ 类文件的根目录为：E:/Workspace/setamv/jsetamv/language/target/classes，即不包含类的package部分。 

结果如下：
+ 当java.lang.Class.getResource(String)中的参数不是以"/"开始时（如"subdir"）
    将返回当前类文件所在的目录 + 参数部分：file:/E:/Workspace/setamv/jsetamv/language/target/classes/org/setamv/jsetamv/language/reslocation/subdir
+ 当参数为以"/"开始的字符串时（如"/org/setamv"）
    将返回当前类文件的根目录 + 参数部分：file:/E:/Workspace/setamv/jsetamv/language/target/classes/org/setamv

### 当class文件在jar包中时
为了便于描述，假设 
+ jar包的路径为：E:/Workspace/setamv/jsetamv/language/target/jsetamv-language-1.0-SNAPSHOT.jar 
+ 类的package路径为：org/setamv/jsetamv/language/reslocation 则：

结果如下：
+ 当参数不是以"/"开始时（如"subdir"）
    将返回jar包的路径 + ! + 类的package路径 + 参数部分：jar:file:/E:/Workspace/setamv/jsetamv/language/target/jsetamv-language-1.0-SNAPSHOT.jar!/org/setamv/jsetamv /language/reslocation/subdir
+ 当参数为以"/"开始的字符串时（如"/org/setamv"）
    将返回jar包的路径 + ! + 参数部分：jar:file:/E:/Workspace/setamv/jsetamv/language/target/jsetamv-language-1.0-SNAPSHOT.jar!/org/setam v

   