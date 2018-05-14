# 模块信息
---------------------------------------------------
rpc服务发布/调用封装

// 启动一个 rpc 服务(不提供 rpc 服务不用启动)
		InfoGen_RPC.getInstance().start(infogen_configuration).registerService(MessageService.newReflectiveBlockingServic(new MessageServiceImpl()));

RemoteRPCChannel channel = Service.create("develop.com.infogen.demo").get_rpc_channel();
try {
	HttpRequest.Builder newBuilder = HttpRequest.newBuilder();
	HttpRequest request = newBuilder.setNote("Hi !").build();

	for (int i = 0; i < 1000; i++) {
		try {
			BlockingInterface service = MessageService.newBlockingStub(channel);
			HttpResponse httpResponse = service.get(new InfoGen_Controller(), request);
			LOGGER.info(httpResponse.getNote());
			
			InfoGen_Callback<HttpResponse> callback = new InfoGen_Callback<>();
			MessageService.newStub(channel).get(null, request, callback);
			LOGGER.info(callback.get(3000));
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} finally {
	channel.shutdown();
}