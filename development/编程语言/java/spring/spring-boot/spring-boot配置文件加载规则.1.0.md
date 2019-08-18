# spring boot配置文件搜索
spring boot配置文件的搜索包括三部分：
1. 从哪些目录搜索配置文件？
2. 根据什么命名规则搜索配置文件？
3. 配置文件的优先级规则是怎样的？也就是说如果同一个配置属性在多个配置文件中出现，最终生效的将是哪个？    

## spring boot从哪些目录搜索配置文件？
默认情况下，spring boot会从既定的默认位置搜索配置文件，同时，也可以通过启动参数指定搜索目录。

### 默认情况
spring boot加载配置文件的主要类`org.springframework.boot.context.config.ConfigFileApplicationListener.java`的JavaDoc有简要描述，如下：   
```
By default properties will be loaded from 'application.properties' and/or 'application.yml' files in the following locations:
	classpath:
	file:./
	classpath:config/
	file:./config/:
```
也就是说，spring boot在启动的过程中，会从以下几个目录搜索配置文件（注意：不会递归搜索，即不会到下面几个目录的子目录中搜索）： 
1. classpath:
	也就是类文件目录
2. file:./
	这个目录表示spring boot应用当前的工作目录。	这里要注意，如果是maven的多模块应用，比如有如下文件目录结构：
	parent-project
		-- spring-boot-app1
		-- spring-boot-app2
	即parent-project工程包含spring-boot-app1和spring-boot-app2两个子模块，此时，运行spring-boot-app1和spring-boot-app2时，它们的工作目录都是parent-project文件夹的根目录，而不是应用自己的根目录。
3. classpath:config/
	classpath根目录下的config子目录中寻找
4. file:./config/
	spring boot应用当前的工作目录下的config文件夹		

### 通过启动参数spring.config.location指定搜索目录
可以通过spring boot的启动参数`spring.config.location`设置配置文件的搜索目录。此时，spring boot只会在指定的目录进行搜索，上面4个默认的搜索目录（如 file:./等）将不会再进行搜索。
下面是设置启动参数`spring.config.location`的说明：

#### 搜索目录的格式
可以通过两种方式指定搜索目录：
1. 通过文件路径指定
	这种方式如果以“.”开头，表示相对路径，并且前缀“file:”可以加也可以不加。例如：
	- `file:./config/` 表示spring boot应用启动的工作目录下的config子文件夹
	- `E:/Workspace/setamv/jsetamv/` 表示绝对路径：“E:/Workspace/setamv/jsetamv/”
	- `file:/home/setamv/app/` 表示绝对路径：“/home/setamv/app/”
2. 通过classpath指定
	这种方式必须使用“classpath:”前缀，例如：
	- `classpath:/config/dev/` 表示classpath下的“config/dev”子目录

特别注意：
1. 上面指定的搜索目录一定要以"/"结尾，spring boot是以"/"结尾来判断路径是否为一个目录的。
	如果指定的搜索目录不是以“/”结尾，spring boot会将搜索目录当做一个配置文件的全路径，并根据当前可用的配置文件扩展名（包括properties、xml、yaml、yml）进行匹配，如果匹配上了，将加载该配置文件中的内容。
	例如，如果指定的搜索目录是`../application.properties`，则spring boot在判断出该目录不是以“/”结尾后，进一步比较该目录是“properties”后缀结尾，所以会将该目录直接当成一个配置文件执行加载，而不是当成一个搜索目录。
2. 对于指定的搜索目录，spring boot会对目录进行clean，如，会将`./`开始的目录中的`./`去掉，如`file:./config/`在clean之后变为`file:config/`，而`file:./`在clean之后会变为`file:`，所以，如果你指定了目录`file:./`，该目录会被spring boot忽略掉，因为clean之后的目录`file:`不是以反斜杠“/”结尾，从而spring boot不认为这是一个目录。

#### 指定参数`spring.config.location`的方式
1. 如果是在IDE（如Intellj IDEA）中运行spring boot程序，可以在执行参数“Program arguments”中，指定`--spring.config.location=file:./config/`
2. 如果是使用java命令直接运行spring boot的jar文件，可以通过指定如下命令格式指定：`java -jar --spring.config.location=file:./config/`

#### 指定多个搜索目录
指定多个搜索目录的方式有2种：
1. 在参数`spring.config.location`的值中使用逗号“,”将多个搜索目录进行分隔
	例如：`--spring.config.location=file:./config/,./config/dev/,classpath:./config/`同时指定了3个搜索目录：`file:./config/`, `./config/dev/`, `classpath:./config/`
2. 指定多个参数`spring.config.location`值
	例如，`java -jar --spring.config.location=file:./config/ --spring.config.location=file:./config/dev`同时指定了2个搜索目录：`file:./config/`，`file:./config/dev`。

#### spring boot目录搜索的源码（以下源码基于 spring boot 2.0.3.RELEASE）
类`org.springframework.boot.context.config.ConfigFileApplicationListener.Loader.java`第441行：
```
private void load(Profile profile, DocumentFilterFactory filterFactory,
			DocumentConsumer consumer) {
	getSearchLocations().forEach((location) -> {
		// 此处为判断搜索地址是否为一个文件目录，简单的以结尾是否为反斜杠“/”
		// 所以如果是指定的搜索目录，一定要以反斜杠“/”结尾
		boolean isFolder = location.endsWith("/");
		Set<String> names = (isFolder ? getSearchNames() : NO_SEARCH_NAMES);
		names.forEach(
				(name) -> load(location, name, profile, filterFactory, consumer));
	});
}

private Set<String> getSearchLocations() {
	if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
		return getSearchLocations(CONFIG_LOCATION_PROPERTY);
	}
	Set<String> locations = getSearchLocations(
			CONFIG_ADDITIONAL_LOCATION_PROPERTY);
	locations.addAll(
			asResolvedSet(ConfigFileApplicationListener.this.searchLocations,
					DEFAULT_SEARCH_LOCATIONS));
	return locations;
}
```

## 根据什么命名规则匹配配置文件
spring boot从文件名和文件扩展名两个维度在搜索目录下匹配配置文件：
### 文件名称   
默认情况下，spring boot只匹配名称为“application”以及“application-default”的文件；匹配名称“application-default”是因为spring boot在没有指定参数`spring.profiles.active`的时候，该参数默认的值是“default”
但是，可以通过参数`spring.config.name`以及`spring.profiles.active`来改变spring boot的默认行为。
1. 参数`spring.config.name`用于指定匹配的文件名称
	例如，使用`java -jar --spring.config.name=application-setamv xxx.jar`启动spring boot应用时，将只会匹配名称为“application-setamv”的配置文件。当如，如果没有指定参数`spring.profiles.active`，也会匹配名称为“application-setamv-default”的配置文件。
2. 参数`spring.profiles.active`
	如果启动spring boot的时候，指定了参数`spring.profiles.active`，spring boot将在之前的匹配规则外，再额外匹配之前的名称 + “-dev”后缀的名称。例如：
	+ `java -jar --spring.profiles.active=dev xxx.jar`
		该启动命令将匹配名称为“application”和“application-dev”的配置文件。
	+ `java -jar --spring.config.name=setamv --spring.profiles.active=dev xxx.jar` 
		该启动命令将匹配名称为“application-setamv”和“application-setamv-dev”的配置文件。 

### 文件扩展名   
	spring boot会匹配四种扩展名：“.properties”、“.xml”、“.yml”、“.yaml”。这四种扩展名是在两种配置文件资源加载器中定义：
	1. `org.springframework.boot.env.PropertiesPropertySourceLoader.java`中定义了"properties"和"xml"两种扩展名。
	2. `org.springframework.boot.env.YamlPropertySourceLoader.java`中定义了"yml"和"yaml"两种扩展名。 

### 总结
spring boot总是会匹配8个配置文件名，分别是：

1. 2种文件名称：由参数`spring.config.name`（如果未指定，默认为“application”）指定的名称和由`spring.profiles.active`（如果未指定，默认为“default”）指定的环境后缀
2. 4中文件扩展名     

2种文件名和4种扩展名结合后就能产生8种不同的文件名。

### 举例
+ `java -jar xxx.jar`
	匹配的文件名为8个：`application.properties`、`application.xml`、`application.yml`、`application.yaml`、`application-default.properties`、`application-default.xml`、`application-default.yml`、`application-default.yaml`

+ `java -jar --spring.config.name=setamv xxx.jar`
	匹配的文件名为8个：`application-setamv.properties`、`application-setamv.xml`、`application-setamv.yml`、`application-setamv.yaml`、`application-setamv-default.properties`、`application-setamv-default.xml`、`application-setamv-default.yml`、`application-setamv-default.yaml`

+ `java -jar --spring.profiles.active=dev xxx.jar`	
	匹配的文件名为8个：`application.properties`、`application.xml`、`application.yml`、`application.yaml`、`application-dev.properties`、`application-dev.xml`、`application-dev.yml`、`application-dev.yaml`

+ `java -jar --spring.config.name=setamv --spring.profiles.active=dev xxx.jar`	
	匹配的文件名为8个：`application-setamv.properties`、`application-setamv.xml`、`application-setamv.yml`、`application-setamv.yaml`、`application-setamv-dev.properties`、`application-setamv-dev.xml`、`application-setamv-dev.yml`、`application-setamv-dev.yaml`	

## 配置文件的优先级规则
如果同一个配置项在多个可以搜索到的配置文件中都有，spring boot是如何选择的呢？

### 在没有指定配置文件搜索目录时
假设没有指定参数`spring.config.name`和`spring.profiles.active`，即使用默认的参数值“application”和“default”（在指定了这两个参数的情况下，与默认情况一致，即将配置文件名替换掉“application”，环境参数替换掉“default”即可）
spring boot选择配置项的优先级按如下顺序（优先级从高到低）：
1. file:./config/application-default.properties
2. file:./application-default.properties
3. classpath:./config/application-default.properties
4. classpath:./application-default.properties
5. ./config/application.properties
6. ./application.properties
7. classpath:./config/application.properties
8. classpath:./application.properties

### 在指定多个搜索目录时
后指定的搜索目录，优先级更高，例如下面：  
`--spring.config.location=classpath:./config/location1/,classpath:./config/location2/`    
排在后面的目录`./config/location2/`优先级更高。

## 结合spring boot源码说明如何构建搜索目录、如何匹配文件名

### spring boot一些通用处理的说明
1. 首先说一下spring boot对启动参数的处理：
	spring boot中可以通过`ApplicationContext.getEnvrionment().getProperty("启动参数名称")`获取启动参数的值，如果对同一个启动参数指定了多次，此时`ApplicationContext.getEnvrionment().getProperty("启动参数名称")`返回的值是多个启动参数之间使用逗号“,”拼接的结果。
	例如：`java -jar --spring.config.name=application --spring.config.name=application-test`，则`ApplicationContext.getEnvrionment().getProperty("spring.config.name")`的值是`application,application-test`

### 源码说明
```
// 该方法在下面解析配置项值的地方将使用到，作用是：
// 1）参数value中的占位符替换 
// 2）将参数value按分隔符逗号“,”进行分割，并返回分割后的多个字符串组成的集合
// 例如：value="application,application-dev"时，将返回：["application", "application-dev"]
private Set<String> asResolvedSet(String value, String fallback) {
	List<String> list = Arrays.asList(StringUtils.trimArrayElements(
			StringUtils.commaDelimitedListToStringArray(value != null
					? this.environment.resolvePlaceholders(value) : fallback)));
	Collections.reverse(list);
	return new LinkedHashSet<>(list);
}

// 通过程序指定搜索目录，暂时不知道在哪里可以触发该设置
// 后面所有通过this.searchLocations引用的搜索目录都是通过这里设置的
public void setSearchLocations(String locations) {
	Assert.hasLength(locations, "Locations must not be empty");
	this.searchLocations = locations;
}

// 通过程序指定搜索名称，暂时不知道在哪里可以触发该设置
// 后面所有通过this.names引用的搜索名称都是通过这里设置的
public void setSearchNames(String names) {
	Assert.hasLength(names, "Names must not be empty");
	this.names = names;
}

// 该方法用于从指定的属性值中获取配置文件搜索目录
// 例如指定了配置项"spring.config.name"的值为"application,application-dev"时，将返回：["application", "application-dev"]
private Set<String> getSearchLocations(String propertyName) {
	Set<String> locations = new LinkedHashSet<>();
	if (this.environment.containsProperty(propertyName)) {
		for (String path : asResolvedSet(
				this.environment.getProperty(propertyName), null)) {
			if (!path.contains("$")) {
				path = StringUtils.cleanPath(path);
				if (!ResourceUtils.isUrl(path)) {
					path = ResourceUtils.FILE_URL_PREFIX + path;
				}
			}
			locations.add(path);
		}
	}
	return locations;
}

// 该方法用于获取搜索目录，逻辑如下：
// 1、如果指定了配置项"spring.config.location"的值，返回该配置项值的解析后的集合。例如`spring.config.location=./config/,./config/dev`时，将返回：["./config/", "./config/dev"]
// 2、否则，返回以下三个地方的搜索目录的合集：
//		1）如果指定了配置项"spring.config.additional-location"的值，获取该配置项值解析后的搜索目录集合
//		2）如果设置了this.searchLocations，获取this.searchLocations指定的搜索目录集合；否则，获取默认的搜索目录集合："classpath:/,classpath:/config/,file:./,file:./config/"
private Set<String> getSearchLocations() {
	// CONFIG_LOCATION_PROPERTY = "spring.config.location"
	if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
		return getSearchLocations(CONFIG_LOCATION_PROPERTY);
	}
	// CONFIG_ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location"
	Set<String> locations = getSearchLocations(
			CONFIG_ADDITIONAL_LOCATION_PROPERTY);

	// DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/"
	locations.addAll(
			asResolvedSet(ConfigFileApplicationListener.this.searchLocations,
					DEFAULT_SEARCH_LOCATIONS));
	return locations;
}

// 该方法用于获取要搜索的配置文件名，逻辑如下：
// 1）如果指定了配置项`spring.config.name`，返回该配置项值的解析后的集合。例如`spring.config.name=application,application-dev`时，将返回：["application", "application-dev"]
// 2）如果没有指定启动参数`spring.config.name`，但是设置了this.names，则返回this.names指定的名称集合。
// 3）如果上面两个都没有设置，则返回默认的搜索名称“application”
private Set<String> getSearchNames() {
	// CONFIG_NAME_PROPERTY = "spring.config.name"
	if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
		String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
		return asResolvedSet(property, null);
	}

	// DEFAULT_NAME = "application"
	return asResolvedSet(ConfigFileApplicationListener.this.names, DEFAULT_NAMES);
}

public void load() {
	...
	while (!this.profiles.isEmpty()) {
		Profile profile = this.profiles.poll();
		load(profile, this::getPositiveProfileFilter,
				addToLoaded(MutablePropertySources::addLast, false));
		...
	}
	load(null, this::getNegativeProfileFilter,
			addToLoaded(MutablePropertySources::addFirst, true));
	...
}

// 该方法主要实现：1）配置文件搜索目录的构建；2）需要匹配的配置文件名
private void load(Profile profile, DocumentFilterFactory filterFactory,
				DocumentConsumer consumer) {
	// getSearchLocations()方法构建搜索目录，然后针对每个搜索目录，构建需要匹配的文件名
	getSearchLocations().forEach((location) -> {
		boolean isFolder = location.endsWith("/");
		// NO_SEARCH_NAMES = [null]
		// 注意：如果location以“/”结尾，会通过getSearchNames()获取需要搜索的文件名集合，否则，需要搜索的文件名集合将为[null]
		Set<String> names = (isFolder ? getSearchNames() : NO_SEARCH_NAMES);
		// 对每个要匹配的文件名，加载对应的配置信息
		names.forEach(
				(name) -> load(location, name, profile, filterFactory, consumer));
	});
}

// 针对指定的搜索目录location、搜索文件名name，加载配置文件的配置项
private void load(String location, String name, Profile profile,
				DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
	// 如果要搜索的文件名是空或null，将直接将
	if (!StringUtils.hasText(name)) {
		for (PropertySourceLoader loader : this.propertySourceLoaders) {
			if (canLoadFileExtension(loader, location)) {
				load(loader, location, profile,
						filterFactory.getDocumentFilter(profile), consumer);
			}
		}
	}
	for (PropertySourceLoader loader : this.propertySourceLoaders) {
		for (String fileExtension : loader.getFileExtensions()) {
			String prefix = location + name;
			fileExtension = "." + fileExtension;
			loadForFileExtension(loader, prefix, fileExtension, profile,
					filterFactory, consumer);
		}
	}
}

/**
* @param location 配置文件所在的目录
* @param name 配置文件名称
* @param profile 环境参数，可以通过"spring.profiles.active"启动参数指定，默认为[null, "default"]
*/
private void load(String location, String name, Profile profile,
				DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
	if (!StringUtils.hasText(name)) {
		for (PropertySourceLoader loader : this.propertySourceLoaders) {
			if (canLoadFileExtension(loader, location)) {
				load(loader, location, profile,
						filterFactory.getDocumentFilter(profile), consumer);
			}
		}
	}
	for (PropertySourceLoader loader : this.propertySourceLoaders) {
		for (String fileExtension : loader.getFileExtensions()) {
			String prefix = location + name;
			fileExtension = "." + fileExtension;
			loadForFileExtension(loader, prefix, fileExtension, profile,
					filterFactory, consumer);
		}
	}
}
```
+ `java -jar --spring.config.location=./application.properties xx.jar`
	上述命令因为指定了`spring.config.location`，所以spring boot会在指定的目录搜索配置文件，但是因为指定的搜索目录`./application.properties`不是以“/”结尾，所以spring boot