通过 infogen配置示例.properties 进行配置需要参数

一:通过infogen直接发布项目的示例(在程序入口处):

        Properties service_properties = new Properties();
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get("conf/infogen.properties"), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
			service_properties.load(inputstreamreader);
		}
		//加载配置并配置自描述中通用的返回值参数
		InfoGen_Configuration config = new InfoGen_Configuration(service_properties);
		config.add_basic_outparameter(new OutParameter("note", String.class, false, "", "错误描述"));
		config.add_basic_outparameter(new OutParameter("code", Integer.class, true, "200", "错误码<br>200 成功<br>400 参数不正确<br>401 特定参数不符合条件(eg:没有这个用户)<br>404 没有这个方法 (RPC调用)<br>500 错误"));
		//启动infogen并注册自身节点
		InfoGen.getInstance().start_and_watch(config).register();
		//如果配置了kafka可以打开kafka日志收集器
		Infogen_Kafka.getInstance().start(config);
		//通过jetty启动http服务(如果使用tomcat等web容器则不需要启动该步骤)
		InfoGen_Jetty.getInstance().start(config, "/", "src/main/webapp", "src/main/webapp/WEB-INF/web.xml");
		//通过thrift启动rpc服务
		InfoGen_Thrift.getInstance().start_asyn(config);
		
		// 自定义服务加载完成事件
		InfoGen.getInstance().init_server(Configuration.service_com_chengshu_auto_crawl, (server) -> {
			ServiceFactory.all_auto_nodes = ServiceFactory.exchange(server.getAvailable_nodes());
			ServiceFactory.auto_tasks_map.clear();
		});
		//配置加载事件
		InfoGen.getInstance().init_configuration("develop.com.chengshu.datasource", (data) -> {
			System.out.println(data);
		});
		//
		Thread.currentThread().join();
		
二:方法自描述:
	@Describe(author = "larry", version = 1.0, value = "新用户注册->登记基本信息 ")
	@InParam(name = "token", describe = "任务令牌")
	@InParam(name = "account", describe = "账户名称")
    @OutParam(name = "type", type = String.class, describe = "类型")
	@OutParam(name = "category", type = String.class, describe = "大类", required = false)
	@RequestMapping(value = "/demo", method = RequestMethod.GET)
	public Return demo(String token, String account) {}

三:接口调用:
	Service service = new Service("develop.com.xxx.xxx");
	List<BasicNameValuePair> name_value_pair = new ArrayList<>();
	name_value_pair.add(new BasicNameValuePair("token", "063b47fbe4fb47bd852323b51acd205b"));
	Return return0 = service.get("get_request", name_value_pair);