package com.infogen.self_description;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.infogen.self_description.component.Function;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月18日 下午5:59:49
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Parser_HTTP extends InfoGen_Parser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.self_description.Self_Description#self_description(java.util.Set)
	 */
	@Override
	public List<Function> self_description(Class<?> clazz) {
		List<Function> functions = new ArrayList<>();

		// url 前缀
		String pre_url = "";
		RequestMapping class_url_annotation = clazz.getAnnotation(RequestMapping.class);
		if (class_url_annotation != null) {
			pre_url = class_url_annotation.value()[0].trim();
		}

		for (Method method : clazz.getDeclaredMethods()) {// 遍历clazz对应类里面的所有方法
			RequestMapping request_mapping_annotation = method.getAnnotation(RequestMapping.class);// 方法映射路径和调用方式
			if (request_mapping_annotation == null) {
				continue;
			}
			// url 方法名会格式为: /get/message
			String suf_url = "";// URL a/b/c/ 转化为 /a/b/c 第一个/会被补齐,最后一个/会被过滤掉
			String[] values = request_mapping_annotation.value();
			if (values.length != 0) {
				suf_url = values[0].trim();
			}
			String url = new StringBuilder("/").append(pre_url).append("/").append(suf_url).toString();
			if (url.endsWith("/")) {
				url.substring(0, url.length());
			}
			url = Pattern.compile("['/']+").matcher(url).replaceAll("/").trim();

			// function
			Function function = new Function();

			function.setRequest_method(url);

			// 调用方式 GET OR POST
			RequestMethod[] get_post_methods = request_mapping_annotation.method();
			if (get_post_methods.length == 0) {
				function.setSubmit_mode("GET");
			} else {
				function.setSubmit_mode(request_mapping_annotation.method()[0].name());// GET POST
			}

			// 方法描述注释
			getDescribe(function, method);

			// 输入参数注释(通过反射方法形参与注释的mapping)
			getInParam(function, method, clazz, HttpServletRequest.class, HttpServletResponse.class);

			// outParams
			getOutParam(function, method);

			//
			functions.add(function);
		}
		return functions;
	}

}
