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
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月11日 下午5:32:23
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
				RestController[] rest_controllers = clazz.getAnnotationsByType(RestController.class);
				if (rest_controllers.length == 0) {
					return;
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
					String request_method = request_mapping_annotation.value()[0];
					if (request_method.startsWith("/")) {
						function.setRequest_method(request_method.substring(1));
					} else {
						function.setRequest_method(request_method);
					}
					RequestMethod[] get_post_methods = request_mapping_annotation.method();
					if (get_post_methods.length == 0) {
						function.setSubmit_mode("GET");
					} else {
						function.setSubmit_mode(request_mapping_annotation.method()[0].name());// GET POST
					}

					// inParams
					Map<String, InParam> inparam_map = new HashMap<>();
					InParam[] inParams = method.getAnnotationsByType(InParam.class);// 输入参数注释(通过反射方法形参与注释的mapping)
					for (InParam inParam : inParams) {
						inparam_map.put(inParam.name(), inParam);
					}
					java.lang.reflect.Parameter[] reflect_parameters = method.getParameters();
					String[] get_in_parameter_names = reflect_parameters.length == 0 ? new String[] {} : get_in_parameter_names(clazz, method.getName(), request_method);
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
						parameter.setName(name);
						parameter.setType(reflect_parameter.getType());
						RequestParam[] annotations = reflect_parameter.getAnnotationsByType(RequestParam.class);
						for (RequestParam annotation : annotations) {
							String default_value = annotation.defaultValue();
							default_value = default_value.equals(ValueConstants.DEFAULT_NONE) ? "" : default_value;
							parameter.setDefault_value(default_value);
							parameter.setRequired(annotation.required());
						}
						InParam inParam = inparam_map.get(name);
						if (inParam != null) {
							parameter.setDescribe(inParam.describe());
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

	private String[] get_in_parameter_names(Class<?> clazz, String method_name, String request_method) throws NotFoundException, ClassNotFoundException {
		class_pool.insertClassPath(new ClassClassPath(clazz));// war包下使用必须
		CtClass ct_class = class_pool.get(clazz.getName());
		ct_class.defrost();
		CtMethod cm = null;
		CtMethod[] cms = ct_class.getDeclaredMethods(method_name);
		for (CtMethod ctMethod : cms) {
			Object _annotation = ctMethod.getAnnotation(RequestMapping.class);
			if (_annotation == null) {
				continue;
			}
			RequestMapping annotation = (RequestMapping) _annotation;
			if (annotation.value()[0].equals(request_method)) {
				cm = ctMethod;
			}
		}
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
