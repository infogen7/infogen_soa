package com.infogen.self_description.parser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import com.infogen.self_description.Self_Description;
import com.infogen.self_description.annotation.Describe;
import com.infogen.self_description.annotation.InParam;
import com.infogen.self_description.annotation.OutParam;
import com.infogen.self_description.component.Function;
import com.infogen.self_description.component.InParameter;
import com.infogen.self_description.component.OutParameter;

import javassist.NotFoundException;

/**
* @author larry/larrylv@outlook.com/创建时间 2015年8月30日 上午11:03:50
* @since 1.0
* @version 1.0
*/
public class RPC_Parser  extends Self_Description {
	private static final Logger LOGGER = Logger.getLogger(HTTP_Parser.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.self_description.Self_Description#self_description(java.util.Set)
	 */
	@Override
	public Map<String, Function> self_description(Class<?> clazz) {
		Map<String, Function> functions = new HashMap<>();

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

			// function
			Function function = new Function();

			// url 后缀
			// 方法名会格式为: /get/message
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
			function.setRequest_method(url);

			// 调用方式 GET OR POST
			RequestMethod[] get_post_methods = request_mapping_annotation.method();
			if (get_post_methods.length == 0) {
				function.setSubmit_mode("GET");
			} else {
				function.setSubmit_mode(request_mapping_annotation.method()[0].name());// GET POST
			}

			// 方法描述注释
			Describe describe = method.getAnnotation(Describe.class);
			if (describe != null) {
				function.setAuthor(describe.author());
				function.setDescribe(describe.value());
				function.setVersion(describe.version());
				function.setTags(describe.tags());
			}

			// 输入参数注释(通过反射方法形参与注释的mapping)
			java.lang.reflect.Parameter[] reflect_parameters = method.getParameters();

			Map<String, InParam> inparam_map = new HashMap<>();// 参数名-注解映射
			for (InParam inParam : method.getAnnotationsByType(InParam.class)) {
				inparam_map.put(inParam.name(), inParam);
			}
			String[] get_in_parameter_names = reflect_parameters.length == 0 ? new String[] {} : null;// 参数名顺序数组
			try {
				get_in_parameter_names = get_in_parameter_names(clazz, method.getName(), reflect_parameters);
			} catch (NotFoundException e) {
				get_in_parameter_names = new String[reflect_parameters.length];
				LOGGER.warn("获取方法参数的名称失败", e);
			}

			for (int i = 0; i < reflect_parameters.length; i++) {
				String parameter_name = get_in_parameter_names[i];
				java.lang.reflect.Parameter reflect_parameter = reflect_parameters[i];

				if (reflect_parameter.getType().equals(HttpServletRequest.class) || reflect_parameter.getType().equals(HttpServletResponse.class)) {
					continue;
				}

				InParameter parameter = new InParameter();
				parameter.setName(parameter_name);// 参数名
				parameter.setType(reflect_parameter.getType());// 参数类型
				RequestParam param_annotation = reflect_parameter.getAnnotation(RequestParam.class);
				if (param_annotation != null) {
					String default_value = param_annotation.defaultValue();// 默认值
					default_value = default_value.equals(ValueConstants.DEFAULT_NONE) ? "" : default_value;
					parameter.setDefault_value(default_value);
					parameter.setRequired(param_annotation.required());// 是否必须
				}

				InParam inParam = inparam_map.get(parameter_name);
				if (inParam != null) {
					parameter.setDescribe(inParam.describe());// 参数描述
				}

				function.getIn_parameters().add(parameter);
			}

			// outParams
			OutParam[] outParams = method.getAnnotationsByType(OutParam.class);// 输出参数注释
			for (OutParam outParam : outParams) {
				OutParameter parameter = new OutParameter();
				parameter.setName(outParam.name());
				parameter.setDefault_value(outParam.default_value());
				parameter.setDescribe(outParam.describe());
				parameter.setRequired(outParam.required());
				parameter.setType(outParam.type());
				function.getOut_parameters().add(parameter);
			}

			//
			functions.put(function.getRequest_method(), function);
		}
		return functions;
	}

}
