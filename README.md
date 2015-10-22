# 项目版本信息
项目名称：infogen_soa
提供了服务注册
服务客户端软负载调用(Http/RPC)
服务治理
加密框架
调用链分析

子项目:
infogen_core:项目基础包
包含一系列用于AOP的框架和组件
包含基础工具类:一致性hash,LRU map,通用exception,jackson工具类,NativePath(获取当前web或jar环境的根目录),jsonobject等
 包含infogen框架的返回值错误码

 infogen_rpc:基于Netty实现HTTP通讯协议,并使用protobuf提供接口自动生成和序列化的RPC协议
 提供java server
 提供java 同步/异步client
 支持通过http协议直接post protobuf字节数组调用
 *支持其它语言的GRPC客户端调用(支持c/c++,go,python等)
 
 infogen_authc:安全验证框架,功能类似shiro
 使用自定义实现的session机制
 彻底不支持cookie,可以通过header或参数传递sessionid
 
 infogen_hibernate:封装hibernate
 实现@AutoClose注解 用于自动关闭session
 封装初始化时通过指定包名自动扫描添加Model类

infogen_group_limit:按组限流
提供本地模式和基于redis的集群模式
提供自动更新服务接口

infogen_limit:限定方法并发次数
通过配置ini文件,配置方法的并发调用次数限制

infogen_demo:infogen项目示例程序


中间倒序增加
--------------------------------------------------------
发布  日期：20151022
发布版本号：V2.0.00R151022
更新  内容：
更新依赖包


发布  日期：20150901
发布版本号：V2.0.00R150901
更新  内容：
1:基于Netty 实现 使用 HTTP 协议接口描述和序列化采用 Protobuf的自定义RPC实现
2:rpc 自描述
3:RPC协议的调用链
4:添加默认的心跳接口
5:ddos并发过滤
6:提供http post json 接口
7:优化infogen rpc
8:拆分infogen_hibernate
9:拆分infogen_limit
10:拆分infogen_authc
11:拆分infogen_cluster_limit

发布  日期：20150803
发布版本号：V1.1.04R150803
更新  内容：
1:里程碑 - 之后版本将去除 thrift 依赖,并支持RPC

发布  日期：20150731
发布版本号：V1.1.04R150731
更新  内容：
1:服务端接口调用阀值配置
2:客户端接口调用阀值配置
3:调用链日志添加当前方法调用并发数
4:tools.jar重复加载引起的问题(一个jvm当中不允许加载一个dll两次 xxx NOT loaded java.lang.UnsatisfiedLinkError : Native Library XXX.dll already loaded in another classloader)
5:remotefunction对象,将远程方法可以单例出来,方便方法名修改
6:新增infogen_hibernate项目通过注解增加方法级别的session关闭
7:tomcat下spring已经加载过的类,注入属性后不可用的问题
8:NativeNode更名为RemoteNode/NativeServer更名为RemoteServer
9:infogen的rest调用方式调用端返回值改成Return继承自己实现的JSONObject和JSONArray
--------------------------------------------------------
发布  日期：20150702
发布版本号：V1.1.00R150702
更新  内容：
1:infogen开放内部调试接口(可以获取当前infogen所有native_server的情况  url:/infogen/native_server)
2:dopost 工具类添加发送application/json类型的工具方法 (Tool_HTTP中do_post_json方法)
3:调用链监控添加token字段(需客户端设置token到cookie或链接中)
4:调用超时和返回400+/500+的节点不会被踢除,但需要打印error级别的日志
5:Service添加根据ip或者hash seed获取调用的节点

--------------------------------------------------------
发布  日期：20150623
发布版本号：V1.1.00R150623
更新  内容：
1:监控服务池中疑似不稳定的节点移除过多从而导致的雪崩(添加min_nodes字段,实现在监控中实现)
2:添加RPC支持(thrift) 
eg:
Response blocking_rpc = demo.call((protocol) -> {
					Message.Client client = new Message.Client(protocol);
					return client.call(request);
				});
3:zookeeper监听字节点数据改变(每次只增量加载改动的节点)
4:修改获取节点的负载均衡算法为一致性hash
5;新增一致性hash数据结构实现  ConsistentHash
6:添加加解密和签名工具类 com.infogen.encryption包下

--------------------------------------------------------
发布  日期：20150614
发布版本号：V1.0.03R150614
更新  内容：
1:新增AUTHC认证框架使用: InfoGen_Authc.getInstance().authc("authc.ini"); 具体配置见infogen_demo示例项目
2:新增tracking框架(通过Execution注解记录调用链,type属性可以指定方法类型)
3:新增AOP框架(轻量级AOP和IOC以及获取对象占用内存空间大小等的API支持)
4:去除thrift调用封装
5:修改jetty服务支持http2调用
6:优化调用代码添加 Parameter.create().add("account", "a").add("password", "sadas") 返回一个map
7:新增本地LRU缓存map类  LRULinkedHashMap
8:新增infogen的ServletContainerInitializer(自定义的对象继承后会在web容器启动时被调用)
--------------------------------------------------------
发布  日期：20150521
发布版本号：V1.2.01R150521
更新  内容：
Y  频次为最长30秒持久化一次
Y  全局只打印一次的日志
Y  参数简化  list<KV>比较繁琐
Y  IP白名单
Y  修复先创建节点后写数据在慢网络下的问题 创建时写入数据
Y  ACL
Y  修改本地启动时路径问题
Y异步post失败后导致连接泄漏
Y user manager 服务
Y sesion扩展
Y 权限框架
Y 用户自描述修复类配置路径
Y infogen版本配置
--------------------------------------------------------
发布  日期：20150319
发布版本号：V1.2.01R150319
更新  内容：
1:RequestMapping中method没有配置时会报错的问题
2:kafka 异常捕获
*3:重复注册监听问题(重要)
4:去掉return中tojson的异常
--------------------------------------------------------