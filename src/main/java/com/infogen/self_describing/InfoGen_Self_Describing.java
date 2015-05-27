/**
 * 
 */
package com.infogen.self_describing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;

import com.infogen.self_describing.annotation.Describe;
import com.infogen.self_describing.annotation.InParam;
import com.infogen.self_describing.annotation.OutParam;
import com.infogen.self_describing.component.Function;
import com.infogen.self_describing.component.InParameter;
import com.infogen.self_describing.component.OutParameter;

/**
 * 启动时扫描自描述配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月21日 下午5:20:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Self_Describing {
	public final Logger logger = Logger.getLogger(InfoGen_Self_Describing.class.getName());

	private static class InnerInstance {
		public static InfoGen_Self_Describing instance = new InfoGen_Self_Describing();
	}

	public static InfoGen_Self_Describing getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Self_Describing() {
	}

	public Map<String, Function> functions = new HashMap<>();

	public Map<String, Function> self_describing(Set<Class<?>> class_set) throws IOException {
		class_set.forEach((clazz) -> {
			try {
				RestController rest_controller = clazz.getAnnotation(RestController.class);
				if (rest_controller == null) {
					return;
				}

				String url_prefix = "";
				RequestMapping class_url_annotation = clazz.getAnnotation(RequestMapping.class);
				if (class_url_annotation != null) {
					url_prefix = class_url_annotation.value()[0];
					if (url_prefix.startsWith("/")) {
						url_prefix = url_prefix.substring(1);
					}
					if (url_prefix.endsWith("/")) {
						url_prefix = url_prefix.substring(0, url_prefix.length() - 2);
					}
				}

				Object instance = clazz.newInstance();
				for (Method method : clazz.getDeclaredMethods()) {// 遍历clazz对应类里面的所有方法
					RequestMapping request_mapping_annotation = method.getAnnotation(RequestMapping.class);// 方法映射路径和调用方式
					if (request_mapping_annotation == null) {
						continue;
					}
					// function
					Function function = new Function();

					function.setInstance(instance);// 实例对象
					function.setMethod(method);// 方法对象
					Describe describe = method.getAnnotation(Describe.class);// 方法描述注释
					if (describe != null) {
						function.setAuthor(describe.author());
						function.setDescribe(describe.value());
						function.setVersion(describe.version());
						function.setTags(describe.tags());
					}

					String url = "";// URL /a/b/c/ 转化为 a/b/c 第一个和最后一个/都会被过滤掉
					String[] values = request_mapping_annotation.value();
					if (values.length == 0) {
						url = url_prefix;
					} else {
						String url_suffix = values[0];
						if (url_suffix.isEmpty()) {
							url = url_prefix;
						} else {
							if (url_suffix.startsWith("/")) {
								url_suffix = url_suffix.substring(1);
							}
							if (url_suffix.endsWith("/")) {
								url_suffix = url_suffix.substring(0, url_suffix.length() - 2);
							}
							if (url_prefix.isEmpty()) {
								url = url_suffix;
							} else {
								url = new StringBuilder(url_prefix).append("/").append(url_suffix).toString();
							}
						}
					}
					function.setRequest_method(url);

					RequestMethod[] get_post_methods = request_mapping_annotation.method();
					if (get_post_methods.length == 0) {
						function.setSubmit_mode("GET");
					} else {
						function.setSubmit_mode(request_mapping_annotation.method()[0].name());// GET POST
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
						java.lang.reflect.Parameter reflect_parameter = reflect_parameters[i];
						String name = null;
						try {
							name = get_in_parameter_names[i];
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (reflect_parameter.getType().equals(HttpServletRequest.class)) {
							continue;
						}

						InParameter parameter = new InParameter();
						parameter.setName(name);// 参数名
						parameter.setType(reflect_parameter.getType());// 参数类型
						RequestParam param_annotation = reflect_parameter.getAnnotation(RequestParam.class);
						if (param_annotation != null) {
							String default_value = param_annotation.defaultValue();// 默认值
							default_value = default_value.equals(ValueConstants.DEFAULT_NONE) ? "" : default_value;
							parameter.setDefault_value(default_value);
							parameter.setRequired(param_annotation.required());// 是否必须
						}
						InParam inParam = inparam_map.get(name);
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
				logger.info("解析class失败:");
				e.printStackTrace();
			}
		});
		return functions;
	}

	// 使用javassist获取参数名
	private ClassPool class_pool = ClassPool.getDefault();

	private Map<String, CtClass> ctclass_maps = new HashMap<>();

	private CtClass get_ctclass(String name) throws NotFoundException {
		CtClass ctClass = ctclass_maps.get(name);
		if (ctClass == null) {
			ctClass = class_pool.get(name);
			ctclass_maps.put(name, ctClass);
		}
		return ctClass;
	}

	private String[] get_in_parameter_names(Class<?> clazz, String method_name, java.lang.reflect.Parameter[] reflect_parameters) throws NotFoundException {
		ClassPool class_pool = ClassPool.getDefault();
		class_pool.insertClassPath(new ClassClassPath(clazz));// war包下使用必须
		CtClass ct_class = class_pool.get(clazz.getName());
		ct_class.defrost();

		CtClass[] types = new CtClass[reflect_parameters.length];
		for (int i = 0; i < reflect_parameters.length; i++) {
			types[i] = get_ctclass(reflect_parameters[i].getType().getName());
		}
		CtMethod cm = ct_class.getDeclaredMethod(method_name, types);
		if (cm == null) {
			return new String[] {};
		}
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		String[] paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}

}