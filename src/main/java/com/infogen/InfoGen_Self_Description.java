package com.infogen;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;

import com.larrylgq.aop.self_description.Self_Description;
import com.larrylgq.aop.self_description.annotation.Describe;
import com.larrylgq.aop.self_description.annotation.InParam;
import com.larrylgq.aop.self_description.annotation.OutParam;
import com.larrylgq.aop.self_description.component.Function;
import com.larrylgq.aop.self_description.component.InParameter;
import com.larrylgq.aop.self_description.component.OutParameter;

/**
 * 启动时扫描自描述配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月21日 下午5:20:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Self_Description extends Self_Description {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Self_Description.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Self_Description instance = new InfoGen_Self_Description();
	}

	public static InfoGen_Self_Description getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Self_Description() {
	}

	public Map<String, Function> self_description(Set<Class<?>> class_set) {
		Map<String, Function> functions = new HashMap<>();
		class_set.forEach((clazz) -> {
			try {
				RestController rest_controller = clazz.getAnnotation(RestController.class);
				if (rest_controller == null) {
					return;
				}

				// 方法名格式为: /get/message
				String pre_url = "";
				RequestMapping class_url_annotation = clazz.getAnnotation(RequestMapping.class);
				if (class_url_annotation != null) {
					pre_url = class_url_annotation.value()[0].trim();
				}
				if (pre_url.endsWith("/")) {
					pre_url = pre_url.substring(0, pre_url.length() - 1);
				}
				if (pre_url.length() > 0 && !pre_url.startsWith("/")) {
					pre_url = "/".concat(pre_url);
				}

				for (Method method : clazz.getDeclaredMethods()) {// 遍历clazz对应类里面的所有方法
					RequestMapping request_mapping_annotation = method.getAnnotation(RequestMapping.class);// 方法映射路径和调用方式
					if (request_mapping_annotation == null) {
						continue;
					}
					// function
					Function function = new Function();
					//
					String suf_url = "";// URL a/b/c/ 转化为 /a/b/c 地一个/会被补齐,最后一个/会被过滤掉
					String[] values = request_mapping_annotation.value();
					if (values.length != 0) {
						suf_url = values[0];
					}
					if (suf_url.endsWith("/")) {
						suf_url = suf_url.substring(0, suf_url.length() - 1);
					}
					if (suf_url.length() > 0 && !suf_url.startsWith("/")) {
						suf_url = "/".concat(suf_url);
					}
					function.setRequest_method(new StringBuilder(pre_url).append(suf_url).toString());
					//
					RequestMethod[] get_post_methods = request_mapping_annotation.method();
					if (get_post_methods.length == 0) {
						function.setSubmit_mode("GET");
					} else {
						function.setSubmit_mode(request_mapping_annotation.method()[0].name());// GET POST
					}
					//
					Describe describe = method.getAnnotation(Describe.class);// 方法描述注释
					if (describe != null) {
						function.setAuthor(describe.author());
						function.setDescribe(describe.value());
						function.setVersion(describe.version());
						function.setTags(describe.tags());
					}

					// inParams
					Map<String, InParam> inparam_map = new HashMap<>();// 输入参数注释(通过反射方法形参与注释的mapping)
					InParam[] inParams = method.getAnnotationsByType(InParam.class);
					for (InParam inParam : inParams) {
						inparam_map.put(inParam.name(), inParam);
					}
					java.lang.reflect.Parameter[] reflect_parameters = method.getParameters();// 方法参数的名称
					String[] get_in_parameter_names = reflect_parameters.length == 0 ? new String[] {} : get_in_parameter_names(clazz, method.getName(), reflect_parameters);

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
			} catch (Exception e) {
				LOGGER.error("解析class失败:", e);
			}
		});
		return functions;
	}

}
