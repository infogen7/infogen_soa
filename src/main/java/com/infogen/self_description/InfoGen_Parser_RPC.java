package com.infogen.self_description;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.protobuf.RpcController;
import com.infogen.rpc.annotation.RPCController;
import com.infogen.self_description.component.Function;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月30日 上午11:03:50
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Parser_RPC extends InfoGen_Parser {

	@Override
	public List<Function> self_description(Class<?> clazz) {
		List<Function> functions = new ArrayList<>();

		// url 前缀
		String pre_url = "";
		RPCController class_url_annotation = clazz.getAnnotation(RPCController.class);
		if (class_url_annotation != null) {
			pre_url = class_url_annotation.value().trim();
		}

		Class<?> super_class = null;
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> _interface : interfaces) {
			if (_interface.getName().endsWith("$BlockingInterface")) {
				super_class = _interface;
			}
		}
		if (super_class == null) {
			return functions;
		}

		Map<String, Method> map = new HashMap<>();
		for (Method method : clazz.getDeclaredMethods()) {
			StringBuilder stringbuilder = new StringBuilder(method.getName());
			for (Class<?> type_clazz : method.getParameterTypes()) {
				stringbuilder.append(",").append(type_clazz.getName());
			}
			map.put(stringbuilder.toString(), method);
		}

		for (Method super_method : super_class.getDeclaredMethods()) {// 遍历clazz对应类里面的所有方法
			StringBuilder stringbuilder = new StringBuilder(super_method.getName());
			for (Class<?> type_clazz : super_method.getParameterTypes()) {
				stringbuilder.append(",").append(type_clazz.getName());
			}
			Method method = map.get(stringbuilder.toString());

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
			function.setSubmit_mode("POST");

			// 方法描述注释
			getDescribe(function, method);

			// 输入参数注释(通过反射方法形参与注释的mapping)
			getInParam(function, method, clazz, RpcController.class);

			// outParams
			getOutParam(function, method);

			//
			functions.add(function);
		}
		return functions;
	}

}
