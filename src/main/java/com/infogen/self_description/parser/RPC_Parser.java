package com.infogen.self_description.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.protobuf.RpcController;
import com.infogen.self_description.Self_Description;
import com.infogen.self_description.annotation.RPCController;
import com.infogen.self_description.component.Function;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月30日 上午11:03:50
 * @since 1.0
 * @version 1.0
 */
public class RPC_Parser extends Self_Description {

	@Override
	public Map<String, Function> self_description(Class<?> clazz) {
		Map<String, Function> functions = new HashMap<>();

		// url 前缀
		String pre_url = "";
		RPCController class_url_annotation = clazz.getAnnotation(RPCController.class);
		if (class_url_annotation != null) {
			pre_url = class_url_annotation.value().trim();
		}

		for (Method method : clazz.getMethods()) {// 遍历clazz对应类里面的所有方法
			Annotation[] annotations = method.getDeclaredAnnotations();
			for (Annotation annotation : annotations) {
				System.out.println(annotation.annotationType());
			}

			Override request_mapping_annotation = method.getAnnotation(Override.class);// 方法映射路径和调用方式
			if (request_mapping_annotation == null) {
				continue;
			}
			// url 方法名会格式为: /get/message
			String suf_url = method.getName();// URL a/b/c/ 转化为 /a/b/c 第一个/会被补齐,最后一个/会被过滤掉
			String url = new StringBuilder("/").append(pre_url).append("/").append(suf_url).toString();
			if (url.endsWith("/")) {
				url.substring(0, url.length());
			}
			url = Pattern.compile("['/']+").matcher(url).replaceAll("/").trim();

			// function
			Function function = new Function();

			function.setRequest_method(url);

			// 方法描述注释
			getDescribe(function, method);

			// 输入参数注释(通过反射方法形参与注释的mapping)
			getInParam(function, method, clazz, RpcController.class);

			// outParams
			getOutParam(function, method);

			//
			functions.put(function.getRequest_method(), function);
		}
		return functions;
	}

}
