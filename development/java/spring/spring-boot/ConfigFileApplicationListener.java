package org.springframework.boot.context.config;

/**
* spring boot配置文件加载的源码解析
* 在看源码之前需要了解的一些规则：
* 1. spring boot中可以通过`ApplicationContext.getEnvrionment().getProperty("启动参数名称")`获取启动参数的值，
*   如果对同一个启动参数指定了多次，此时`ApplicationContext.getEnvrionment().getProperty("启动参数名称")`返回的值是多个启动参数之间使用逗号“,”拼接的结果。
*	例如：`java -jar --spring.config.name=application,application-dev --spring.config.name=application-test`，
*   则`ApplicationContext.getEnvrionment().getProperty("spring.config.name")`的值是`application,application-dev,application-test`
* 
*/
public class ConfigFileApplicationListener {

    /**
    * Loader类负责spring boot配置文件的加载
    * 
    */
    private class Loader {

        ////////////////////////// 一些通用的方法 start

        /**
        * 该方法非常重要，在下面解析配置项值的很多地方被使用到了，它的工作是：
        * 1）如果参数value不为null，则替换value中的占位符；否则，使用fallback参数替代参数value
        * 2）将步骤1的结果按逗号“,”进行切割，获得切割后的字符串集合
        * 3）如对步骤2的结果进行倒序排列，并返回（倒序这一点非常重要，因为不同配置文件中的配置项优先级是不一样的，该优先级与配置文件的加载顺序有关，而配置文件的加载顺序就是这里的返回值顺序决定的）
        * 例如：value="classpath:/,classpath:/config/,file:./,file:./config/"时，将返回：["file:./config/", "file:./", "classpath:/config/", "classpath:/"]，注意返回值的顺序是倒序。
        */
        private Set<String> asResolvedSet(String value, String fallback) {
            List<String> list = Arrays.asList(StringUtils.trimArrayElements(
                    StringUtils.commaDelimitedListToStringArray(value != null
                            ? this.environment.resolvePlaceholders(value) : fallback)));
            Collections.reverse(list);
            return new LinkedHashSet<>(list);
        }

        ////////////////////////// 一些通用的方法 end


        ////////////////////////// 获取和搜索配置文件相关的profile集合 start

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

        /**
        * 从配置项值获取profile集合，包括通过配置项"spring.profiles.active"和"spring.profiles.include"设置的值
        * 例如，指定了启动参数："--spring.profiles.active=dev1,dev2 --spring.profiles.active=dev3 --spring.profiles.include=test1"，
        * 则 activeProfiles = ["dev1", "dev2", "dev3", "test1"]
        */
        private Set<Profile> getProfilesActivatedViaProperty() {
			...
            // 通过配置项"spring.profiles.active"和配置项"spring.profiles.include"解析有效的profile值集合
			activeProfiles.addAll(getProfiles(binder, "spring.profiles.active"));
			activeProfiles.addAll(getProfiles(binder, "spring.profiles.include"));
			return activeProfiles;
		}

        ////////////////////////// 获取和搜索配置文件相关的profile集合 end

        ////////////////////////// 获取需要搜索的配置文件所在目录集合 start

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

        /**
        * 通过程序指定搜索目录，暂时不知道在哪里可以触发该设置
        * 后面所有通过this.searchLocations引用的搜索目录都是通过这里设置的
        */
        public void setSearchLocations(String locations) {
            Assert.hasLength(locations, "Locations must not be empty");
            this.searchLocations = locations;
        }

        ////////////////////////// 获取需要搜索的配置文件所在目录集合 end



        ////////////////////////// 获取需要搜索的配置文件的名称集合（不包括文件扩展名部分） end

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
        
        // 通过程序指定搜索名称，暂时不知道在哪里可以触发该设置
        // 后面所有通过this.names引用的搜索名称都是通过这里设置的
        public void setSearchNames(String names) {
            Assert.hasLength(names, "Names must not be empty");
            this.names = names;
        }
        ////////////////////////// 获取需要搜索的配置文件的名称集合（不包括文件扩展名部分） end

        ////////////////////////// 获取需要搜索的配置文件的扩展名集合 end
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

        /**
        下面的代码位于  org.springframework.boot.env.YamlPropertySourceLoader 中
        */
        public String[] getFileExtensions() {
            return new String[] { "yml", "yaml" };
        }

        /**
        下面的代码位于  org.springframework.boot.env.PropertiesPropertySourceLoader 中
        */
        public String[] getFileExtensions() {
            return new String[] { "properties", "xml" };
        }
        
        ////////////////////////// 获取需要搜索的配置文件的扩展名集合 end

        ////////////////////////// 搜索配置文件 start
        /**
        * spring boot搜索配置文件时，将配置文件的全路径拆分成了4个部分，通过对每个部分进行设置，可以组合成非常灵活配置文件加载机制。这4个部分是：
        * 1）profile，这一部分构成了配置文件名后缀
        * 2）location，配置文件所在的目录
        * 3）name，配置文件的名称
        * 4）extension，配置文件的扩展名
        * spring boot对加载的多个配置文件，是有优先级顺序的。具体的规则是：先加载，优先级更高。
        * 这里的优先级是针对配置文件中的配置项来说的。
        * 例如，配置文件application-1.properties中有配置项"test.config=1"，配置文件application-2.properties中有配置项"test.config=2"，如果配置文件application-2.properties先加载，则获取配置项"test.config"的值将是"2"。
        * 所以，上面4个部分，每个部分的确定顺序是非常重要的，因为每个部分的确定顺序将决定他们对应的配置文件的优先级。
        * 上面4个部分确定的顺序为：：1）profile -> 2）location -> 3）name -> 4）extension。也就是先循环每个profile，确定了profile之后，再循环该profile下的每个location，....后面的name和extension依次类推
        * 
        * 配置文件加载后，会使用一个"org.springframework.core.env.PropertySource"对象存储所有的配置项信息。下面说一下其结构是怎样的。
        * 1）profile级：每一个profile会有一个对应的"org.springframework.core.env.MutablePropertySources"对象，该对象的属性"propertySourceList"按配置文件加载的顺序，依次存储了该profile下加载的所有配置文件的"PropertySource"对象。
        * 2）应用级：ApplicationContext.getEnvironment()"对象的"propertySources"属性，保存了所有profile的"propertySourceList"，并且按profile循环加载配置文件的倒序加入的。
        *
        * 当从应用中获取一个配置项的值的时候，spring boot会从ApplicationContext.getEnvironment().getPropertySources()中按次序查找。所以，排在前面的将被优先找到。这也就意味着：
        *   1）最后加载的profile对应的配置文件优先最高，因为profile对应的"propertySourceList"是按倒序加入的
        *   2）指定的多个搜索目录的优先级跟指定的顺序相反，因为搜索目录是按倒序搜索的
        *   3）指定的多个配置文件名称的优先级跟指定的顺序相反，因为搜索文件名称是按倒序搜索的
        *   4）指定的多个扩展名的优先级跟指定的顺序相同        
        */

        /**
        * 循环每个profile，最后处理 profile = null的
        */
        public void load() {
			...
			while (!this.profiles.isEmpty()) {
				Profile profile = this.profiles.poll();
                // 加载profile对应的配置文件，并保存到profile对应的"org.springframework.core.env.MutablePropertySources"中（参考 addToLoaded()方法）
				load(profile, this::getPositiveProfileFilter,
						addToLoaded(MutablePropertySources::addLast, false));
			}
			load(null, this::getNegativeProfileFilter,
					addToLoaded(MutablePropertySources::addFirst, true));

            // 将所有profile下已加载的配置文件按profile加载顺序的倒序，加入 ConfigFileApplicationListener.environment的propertySources
			addLoadedPropertySources();
		}

        /**
        * 该方法返回一个DocumentConsumer对象，该对象的接口参数有2个：1）加载的配置文件对应的profile；2）加载的配置文件信息（保存在document.getPropertySource()中
        * 这里先说明一下配置文件加载后的存储结构：
        *   每一个配置文件加载后，都会创建一个"org.springframework.core.env.PropertySource"对象里面存储了所有加载的配置项信息。
        *   每一个profile都有一个对应的"org.springframework.core.env.MutablePropertySources"对象，该对象的属性"propertySourceList"用于存储该profile下所有配置文件的PropertySource对象。
        * 该方法就是将当前加载的配置文件对象"PropertySource"加入对应profile的“propertySourceList"中。
        */
        private DocumentConsumer addToLoaded(
				BiConsumer<MutablePropertySources, PropertySource<?>> addMethod,
				boolean checkForExisting) {
			return (profile, document) -> {
				...
                // 
				MutablePropertySources merged = this.loaded.computeIfAbsent(profile,
						(k) -> new MutablePropertySources());
				addMethod.accept(merged, document.getPropertySource());
			};
		}

        /**
        * 该方法用于将所有profile加载的配置文件信息，加入到应用的环境参数environment中取，并且是按profile的倒序加入的。
        */
        private void addLoadedPropertySources() {
            // this.environment是应用级别的环境参数
			MutablePropertySources destination = this.environment.getPropertySources();
			List<MutablePropertySources> loaded = new ArrayList<>(this.loaded.values());
            // 注意这里的倒序
			Collections.reverse(loaded);
			String lastAdded = null;
			Set<String> added = new HashSet<>();
			for (MutablePropertySources sources : loaded) {
				for (PropertySource<?> source : sources) {
					if (added.add(source.getName())) {
						if (lastAdded == null) {
                            if (destination.contains(DEFAULT_PROPERTIES)) {
                                destination.addBefore(DEFAULT_PROPERTIES, source);
                            } else {
                                destination.addLast(source);
                            }
                        } else {
                            destination.addAfter(lastAdded, source);
                        }
						lastAdded = source.getName();
					}
				}
			}
		}

        /**
        * 确定profile后，对每个location依次加载。location是按倒序的（参见getSearchLocations()方法）
        * 确定location后，对每个name依次加载。name是按倒序的（参见getSearchNames()方法）
        */
        private void load(Profile profile, DocumentFilterFactory filterFactory,
				DocumentConsumer consumer) {
			getSearchLocations().forEach((location) -> {
				boolean isFolder = location.endsWith("/");
                // 注意，这里如果 location不是以"/"结尾，names会变成 [null]，所以最终会搜索文件名为null或null-profile的配置文件
				Set<String> names = (isFolder ? getSearchNames() : NO_SEARCH_NAMES);
				names.forEach(
						(name) -> load(location, name, profile, filterFactory, consumer));
			});
		}

        /**
        * 取定profile、location、name后，对每个extension依次加载
        * 下面有个特例，就是：如果name为空，会将location视为带文件名的
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

        /**
        * 这里将prefix(location + name)、profile、extension四个部分组合成一个配置文件的绝对路径，并加载该配置文件中的配置项。
        * 例如，当 location = "./config", name = "application", profile = "default", extension = ".properties"时，
        * 组合后的绝对路径是："./config/application-default.properties"
        */
        private void loadForFileExtension(PropertySourceLoader loader, String prefix,
				String fileExtension, Profile profile,
				DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
			...
			if (profile != null) {				
				String profileSpecificFile = prefix + "-" + profile + fileExtension;
				load(loader, profileSpecificFile, profile, defaultFilter, consumer);
				load(loader, profileSpecificFile, profile, profileFilter, consumer);
				// Try profile specific sections in files we've already processed
				for (Profile processedProfile : this.processedProfiles) {
					if (processedProfile != null) {
						String previouslyLoaded = prefix + "-" + processedProfile
								+ fileExtension;
						load(loader, previouslyLoaded, profile, profileFilter, consumer);
					}
				}
			}
			// Also try the profile-specific section (if any) of the normal file
			load(loader, prefix + fileExtension, profile, profileFilter, consumer);
		}

        /**
        * 执行配置文件加载
        */
        private void load(PropertySourceLoader loader, String location, Profile profile,
				DocumentFilter filter, DocumentConsumer consumer) {
			try {
				...
                // 加载配置文件
				List<Document> documents = loadDocuments(loader, name, resource);
                ...
			}
			catch (Exception ex) {
				
			}
		}

        
        ////////////////////////// 搜索配置文件 end
    }
}