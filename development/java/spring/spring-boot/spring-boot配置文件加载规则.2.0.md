# spring boot配置文件搜索
spring boot配置文件的搜索包括两部分：
1. 搜索哪些配置文件？
2. 配置文件的优先级规则是怎样的？也就是说如果同一个配置属性在多个配置文件中出现，最终生效的将是哪个？    

## spring boot搜索哪些配置文件？

### spring boot搜索配置文件的顺序
spring boot将配置文件的绝对路径分成了4个可配置的部分，他们分别是：
1）profile，即环境参数。profile的值用于构成配置文件名称的后缀；
2）location，配置文件所在的目录；
3）name，配置文件的名称；
4）extension，配置文件的扩展名。
例如，当 profile = "default", location = "file:./config", name = "application", extension = "properties" 时，spring boot将从 "file:./config/application-default.properties" 加载配置文件。

spring boot搜索配置文件的过程，也就是确认这4个可配置部分的值的过程。

下面说一下这4个部分的值的确认顺序：profile -> location -> name -> extension。也就是说，
1）spring boot会先确认需要搜索哪些profile相关的配置文件，比如说有需要搜索["dev", "test"]两个profile相关的配置项，
2）然后，依次对每一个profile，确认该profile有哪些location需要搜索，...
3）接着，依次对每一个location，确认需要搜索哪些name
4）最后，依次对每一个name，确认需要搜索哪些extension。
换成伪代码就是：
```
LinkedHashSet<Profile> profiles = getProfiles();
for (Profile profile : profiles) {
	LinkedHashSet<String> locations = getLocations();
	for (String location : locations) {
		LinkeHashSet<String> names = getNames();
		for (String name : names) {
			LinkeHashSet<String> extensions = getExtensions();
			// 开始加载配置文件
			List<PropertySource> propertySources = load(profile, location, name, extension);

			// 每个profile按配置文件加载的顺序加入profile的propertySourceList中
			profile.getPropertySourceList().addAll(propertySources);
		}
	}
}
// 将所有profile加载的配置文件列表，按遍历profiles的倒序，加入应用的环境参数中
for (int i = profiles.size() - 1; i--; i >= 0) {
	ApplicationContext.getEnvironment().getPropertySources().addAll(profiles.getPropertySourceList());
}
```

spring boot应用获取配置信息时，是从ApplicationContext.getEnvironment().getPropertySources()中依序查找的，所以，ApplicationContext.getEnvironment().getPropertySources()中的顺序越靠前，其对应的配置文件的配置项优先级就更高。


### profile范围的确定
```
/**
* 该方法负责获取和搜索配置文件相关的profile集合
* 注意：this.profiles 中，排在越前面的profile，和该profile相关的配置项优先级越低，例如 this.profiles 中按顺序有 ["dev", "test"]，如果这两个profile都有相关配置项prop1，则"test"相关的配置项prop1优先级更高。
* 和搜索配置文件相关的profile集合构成：
*   1）null，即空的profile
*   2）getOtherActiveProfiles(activatedViaProperty)，即有效的active profile,但不包含在通过配置项"spring.profiles.active"和配置项"spring.profiles.include"设置的profile集合中的profile
*   3）通过配置项"spring.profiles.active"和配置项"spring.profiles.include"设置的profile集合
*   4）如果1）、2）、3）步骤中只存在null的profile，则加入默认的profile "default"
* profile在搜索配置文件过程中的作用是：在搜索的配置文件名称上追加profile后缀。
* 比如 当搜索的配置文件名称为 "application"，如果 profile = "dev"，则最终搜索的文件名将是 "application-dev"；如果 profile = null，则最终搜索的文件名是 "application"
*/
private void initializeProfiles() {
	// 默认加载空的profile
	this.profiles.add(null);
	
	// 获取通过配置项"spring.profiles.active"和"spring.profiles.include"设置的profile值集合（具体代码看下面）
	// 注意：这里返回的集合Set是一个LinkedHashSet对象，LinkedHashSet中对象是按加入的先后顺序排序的。
	Set<Profile> activatedViaProperty = getProfilesActivatedViaProperty();

	// 从其他方式设置的active profile。包括 ApplicationContext.getEnvironment().getActiveProfiles() 过滤掉 activatedViaProperty 中的profiles后的值集合
	// 注意：这里的profile集合比 activatedViaProperty 先加入有效的profiles集合中，所以优先级也低一些
	this.profiles.addAll(getOtherActiveProfiles(activatedViaProperty));

	// 这里将上面获取到的activatedViaProperty加入有效的profiles集合。   
	addActiveProfiles(activatedViaProperty);

	// 如果只有空的profile，则加入默认的profie，这里默认的profile是"default"
	if (this.profiles.size() == 1) { // only has null profile
		for (String defaultProfileName : this.environment.getDefaultProfiles()) {
			Profile defaultProfile = new Profile(defaultProfileName, true);
			this.profiles.add(defaultProfile);
		}
	}
}
```

### location范围的确定
```
/**
* 该方法用于获取需要搜索的配置文件所在的目录集合，逻辑如下：
* 1、如果指定了配置项"spring.config.location"的值，返回该配置项值的解析后的集合。例如`spring.config.location=./config/,./config/dev`时，将返回：["./config/dev", "./config/"]。注意是倒序排列
* 2、否则：
*   1）如果指定了配置项"spring.config.additional-location"的值，获取该配置项值解析后的搜索目录集合
*   2）如果设置了this.searchLocations，获取this.searchLocations指定的搜索目录集合；
*       否则，获取默认的搜索目录"classpath:/,classpath:/config/,file:./,file:./config/"，对应返回的集合为：["file:./config/", "file:./", "classpath:/config/", "classpath:/"]，注意切割后变成了倒序。
* 注意：返回对象为LinkedHashSet类型，该类型是有序集合。返回的搜索目录，排在前面的搜索目录中加载的配置项，优先级更高。
*/
private Set<String> getSearchLocations() { 
	// 1）从配置项"spring.config.location"的值取搜索目录           
	if (this.environment.containsProperty("spring.config.location")) {
		return getSearchLocations("spring.config.location");
	}
	// 2）从配置项"spring.config.additional-location"的值取搜索目录   
	Set<String> locations = getSearchLocations(
			"spring.config.additional-location");

	// 2）取this.searchLocations，如果没有，取默认的搜索目录，其中默认的搜索目录 DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/"
	locations.addAll(
			asResolvedSet(ConfigFileApplicationListener.this.searchLocations,
					DEFAULT_SEARCH_LOCATIONS));
	return locations;
}

/**
* 该方法用于从指定的配置项值中获取配置文件搜索目录
* 例如指定了配置项"spring.config.location"的值为"searchPath1,searchPath2,searchPath3"时，将返回：["searchPath3", "searchPath2", "searchPath1"]
* 注意：
*   1）该方法返回的是一个LinkedHashSet集合，该集合是有序集合，顺序是按元素加入的顺序
*   2）该方法返回的集合中元素的顺序是配置项值的倒序，例如，配置项值是"searchPath1,searchPath2,searchPath3"时，将返回：["searchPath3", "searchPath2", "searchPath1"]
*       这一点非常重要！！因为多个搜索目录之间的顺序将决定他们搜索到的配置文件中配置项的优先级。
*/
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
```

### name搜索范围的确定
```
/**
* 该方法用于获取要搜索的配置文件名，逻辑如下：
*   1）如果指定了配置项`spring.config.name`，返回该配置项值的解析后的集合。例如`spring.config.name=application,application-dev`时，将返回：["application-dev", "application"]，注意：是倒序排列
*   2）如果没有指定启动参数`spring.config.name`，但是设置了this.names，则返回this.names指定的名称集合，注意，这里也是倒序排列。
*   3）如果上面两个都没有设置，则返回默认的搜索名称 “application”
* 注意：返回对象为LinkedHashSet类型，该类型是有序集合。返回的配置文件名，排在前面的，优先级更高。
*/
private Set<String> getSearchNames() {
	// 取配置项 "spring.config.name" 指定的名称
	if (this.environment.containsProperty("spring.config.name")) {
		String property = this.environment.getProperty("spring.config.name");
		return asResolvedSet(property, null);
	}

	// 取this.names设置的名称，如果没有，取默认的名称 "application"
	return asResolvedSet(ConfigFileApplicationListener.this.names, "application");
}
```

### extension搜索范围的确定
```
/**
* 在搜索配置文件之前，必须确定4个信息：1）搜索目录；2）要搜索的文件名称；3）profile；4）要搜索的文件扩展名
* 该方法就是在确定了：搜索目录location、配置文件名称name、profile后，对所有有效的配置文件扩展名，一一执行搜索和配置信息加载
* 其中，有效的配置文件扩展名包括：["properties", "xml", "yml", "yaml"]，并且，排在前面的扩展名，优先越高，如以"properties"为扩展名的配置文件中的配置项比"yml"为扩展名的优先级高。
*/
private void load(String location, String name, Profile profile,
		DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
	...
	// 这里的 this.propertySourceLoaders = [org.springframework.boot.env.PropertiesPropertySourceLoader, org.springframework.boot.env.YamlPropertySourceLoader]
	// 他们的方法 "getFileExtensions()" 的代码在下面贴出来了。
	// 这两个propertySourceLoaders是在spring的配置文件"spring-boot-2.0.3.RELEASE.jar!/META-INF/spring.factories"中设置的，摘抄如下：
	// # PropertySource Loaders
	// org.springframework.boot.env.PropertySourceLoader=org.springframework.boot.env.PropertiesPropertySourceLoader,org.springframework.boot.env.YamlPropertySourceLoader
	for (PropertySourceLoader loader : this.propertySourceLoaders) {                
		for (String fileExtension : loader.getFileExtensions()) {
			...
		}
	}
}
```