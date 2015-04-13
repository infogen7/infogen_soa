/**
 * 
 */
package com.infogen.aop;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.infogen.aop.annotation.Execution_Logger;
import com.infogen.aop.annotation.Invoke_Logger;
import com.infogen.aop.handle.Execution_Logger_Handle;
import com.infogen.aop.handle.Invoke_Logger_Handle;
import com.infogen.infogen_aop.InfoGen_Agent_Advice_Class;
import com.infogen.infogen_aop.InfoGen_Agent_Advice_Method;
import com.infogen.infogen_aop.InfoGen_Agent_Cache;
import com.infogen.infogen_aop.InfoGen_Agent_Path;
import com.infogen.tools.Tool_Jackson;
import com.infogen.util.InfoGen_ClassLoader;

/**
 * 通过附着agent的方式实现面向切面的功能
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月11日 下午5:32:23
 */
public class InfoGen_AOP {
	public final Logger logger = Logger.getLogger(InfoGen_AOP.class.getName());

	private static class InnerInstance {
		public static InfoGen_AOP instance = new InfoGen_AOP();
	}

	public static InfoGen_AOP getInstance() {
		return InnerInstance.instance;
	}

	private String agent_path = InfoGen_Agent_Path.path();
	private Method loadAgent = null;
	// private Method detach = null;
	private Object virtualmachine_instance = null;
	private InfoGen_ClassLoader classLoader = new InfoGen_ClassLoader(new URL[] {}, null);

	private InfoGen_AOP() {
		try {
			String port = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

			String java_home = System.getProperty("java.home");
			logger.info("java.home  => " + java_home);
			if (System.getProperty("os.name").indexOf("Windows") != -1) {
				if (java_home.contains("jdk")) {
					java_home = java_home.replace("jre", "").concat("lib/tools.jar");
				} else {
					java_home = java_home.replace("jre", "jdk").concat("/lib/tools.jar");
				}
			} else {
				java_home = java_home.replace("jre", "").concat("lib/tools.jar");
			}

			logger.info("jdk home dir => " + java_home);
			classLoader.addJar(Paths.get(java_home).toUri().toURL());
			Class<?> clazz = classLoader.loadClass("com.sun.tools.attach.VirtualMachine");
			Method attach = clazz.getMethod("attach", new Class[] { String.class });
			virtualmachine_instance = attach.invoke(null, new Object[] { port });
			loadAgent = clazz.getMethod("loadAgent", new Class[] { String.class, String.class });
			// TODO 如果只加载一次应该在使用完成后close
			// detach = clazz.getMethod("detach", new Class[] {});
			classLoader.close();
		} catch (Exception e) {
			logger.error("初始化AOP失败", e);
		}
	}

	public void attach(Class<?> clazz) {
		String class_name = clazz.getName();
		List<InfoGen_Agent_Advice_Method> methods = new ArrayList<>();

		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			// 添加日志
			Execution_Logger[] infogen_logger = method.getAnnotationsByType(Execution_Logger.class);
			if (infogen_logger.length != 0) {
				methods.add(Execution_Logger_Handle.attach(class_name, method.getName(), infogen_logger[0].value()));
			}
			Invoke_Logger[] service_logger = method.getAnnotationsByType(Invoke_Logger.class);
			if (service_logger.length != 0) {
				methods.add(Invoke_Logger_Handle.attach(class_name, method.getName(), service_logger[0].value()));
			}
			// TODO 继续添加...
		}
		if (methods.isEmpty()) {
			return;
		}
		try {

			InfoGen_Agent_Advice_Class infogen_advice = new InfoGen_Agent_Advice_Class();
			infogen_advice.setClass_name(class_name);
			infogen_advice.setMethods(methods);
			InfoGen_Agent_Cache.class_advice_map.put(class_name, Tool_Jackson.toJson(infogen_advice));
			loadAgent.invoke(virtualmachine_instance, new Object[] { agent_path, class_name });
		} catch (Exception e) {
			logger.error("注入代码失败", e);
		}
	}
}
