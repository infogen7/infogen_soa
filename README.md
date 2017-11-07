# 项目版本信息
项目名称：infogen_soa
提供了Rest+Json和RPC+Protobuf的服务发布
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

infogen_streaming:实现yarn模型并可独立模式启动的kafka ETL 到HDFS的工具可以直接使用，也可以继承mapper自定义实现ETL逻辑

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

