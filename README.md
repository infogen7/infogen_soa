# 项目版本信息
项目名称：infogen
项目代码：infogen
GIT库名称：infogen_zookeeper
建议项目使用时配置JVM java -jar -XX:+UseG1GC  -XX:+UseStringDeduplication TM.jar

中间倒序增加
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
