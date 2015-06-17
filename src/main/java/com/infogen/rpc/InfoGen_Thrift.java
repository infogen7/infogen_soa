/**
 * 
 */
package com.infogen.rpc;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportFactory;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.rpc.handler.Thrift_Message_Handler;
import com.infogen.thrift.Message;

/**
 * 启动thrift服务
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月30日 下午12:22:45
 */
@Deprecated
public class InfoGen_Thrift {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Thrift.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Thrift instance = new InfoGen_Thrift();
	}

	public static InfoGen_Thrift getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Thrift() {
	}

	// ///////////////////////////////////////////////////////////////////////////asyn//////////////////////////////////////////////////////
	/**
	 * 启动一个异步的thrift服务
	 * 
	 * @param infogen_configuration
	 * @return
	 */
	public InfoGen_Thrift start_asyn(InfoGen_Configuration infogen_configuration, Thrift_Message_Handler handler) {
		Thread t = new Thread(() -> {
			try {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				final Message.Processor processor = new Message.Processor(handler);
				//
				TNonblockingServerSocket socket = new TNonblockingServerSocket(infogen_configuration.rpc_port);

				TThreadedSelectorServer.Args arg = new TThreadedSelectorServer.Args(socket);
				arg.transportFactory(new TFramedTransport.Factory());// 以frame为单位进行传输，非阻塞式服务中使用
				arg.protocolFactory(new TCompactProtocol.Factory());// 压缩格式
				arg.processorFactory(new TProcessorFactory(processor));

				TServer server = new TThreadedSelectorServer(arg);

				System.out.println("#服务启动-使用:非阻塞&高效二进制编码");
				server.serve();
			} catch (Exception e) {
				LOGGER.error("启动失败", e);
			}
		});
		t.setDaemon(true);
		t.start();
		return this;
	}

	// ///////////////////////////////////////////////////////////////////////////blocking//////////////////////////////////////////////////////
	/**
	 * 启动一个同步的thrift服务
	 * 
	 * @param infogen_configuration
	 * @return
	 */
	public InfoGen_Thrift start_blocking(InfoGen_Configuration infogen_configuration, Thrift_Message_Handler handler) {
		Thread t = new Thread(() -> {
			try {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				final Message.Processor processor = new Message.Processor(handler);
				//
				TServerSocket socket = new TServerSocket(infogen_configuration.rpc_port);

				TThreadPoolServer.Args arg = new TThreadPoolServer.Args(socket);
				arg.transportFactory(new TTransportFactory());
				arg.protocolFactory(new TCompactProtocol.Factory());
				arg.processorFactory(new TProcessorFactory(processor));

				TServer server = new TThreadPoolServer(arg);

				System.out.println("#服务启动-使用:非阻塞&高效二进制编码");
				server.serve();
			} catch (Exception e) {
				LOGGER.error("启动失败", e);
			}
		});
		t.setDaemon(true);
		t.start();
		return this;
	}

}
