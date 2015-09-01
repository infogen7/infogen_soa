package com.infogen.self_description;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;

import com.infogen.self_description.annotation.Describe;
import com.infogen.self_description.annotation.InParam;
import com.infogen.self_description.annotation.OutParam;
import com.infogen.self_description.component.Function;
import com.infogen.self_description.component.InParameter;
import com.infogen.self_description.component.OutParameter;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * 扫描本地方法自描述的接口
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午4:39:00
 * @since 1.0
 * @version 1.0
 */
public abstract class Self_Description {
	private static final Logger LOGGER = Logger.getLogger(Self_Description.class.getName());

	public abstract Map<String, Function> self_description(Class<?> clazz);

	// 使用javassist获取参数名
	private static final ClassPool class_pool = ClassPool.getDefault();

	private static final Map<String, CtClass> jdk_ctclass_maps = new HashMap<>();

	public CtClass get_jdk_ctclass(String name) throws NotFoundException {
		CtClass ctClass = jdk_ctclass_maps.get(name);
		if (ctClass == null) {
			ctClass = class_pool.get(name);
			jdk_ctclass_maps.put(name, ctClass);
		}
		return ctClass;
	}

	public CtClass get_ctclass(Class<?> clazz) throws NotFoundException {
		ClassPool class_pool = ClassPool.getDefault();
		class_pool.insertClassPath(new ClassClassPath(clazz));// war包下使用必须
		CtClass ct_class = class_pool.get(clazz.getName());
		ct_class.defrost();
		return ct_class;
	}

	public void getDescribe(Function function, Method method) {
		Describe describe = method.getAnnotation(Describe.class);
		if (describe != null) {
			function.setAuthor(describe.author());
			function.setDescribe(describe.value());
			function.setVersion(describe.version());
			function.setTags(describe.tags());
		}
	}

	public void getOutParam(Function function, Method method) {
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
	}

	public void getInParam(Function function, Method method, Class<?> clazz, Class<?>... excepts) {
		Map<String, InParam> inparam_map = new HashMap<>();// 参数名-注解映射
		for (InParam inParam : method.getAnnotationsByType(InParam.class)) {
			inparam_map.put(inParam.name(), inParam);
		}

		Map<String, Parameter> names_map = get_in_parameter_names_map(clazz, method.getName(), method.getParameters());
		D: for (Entry<String, Parameter> entry : names_map.entrySet()) {
			String parameter_name = entry.getKey();
			java.lang.reflect.Parameter reflect_parameter = entry.getValue();
			for (Class<?> except_clazz : excepts) {
				if (reflect_parameter.getType().equals(except_clazz)) {
					continue D;
				}
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
	}

	public Map<String, java.lang.reflect.Parameter> get_in_parameter_names_map(Class<?> clazz, String method_name, java.lang.reflect.Parameter[] reflect_parameters) {
		String[] get_in_parameter_names = reflect_parameters.length == 0 ? new String[] {} : null;// 参数名顺序数组
		try {
			get_in_parameter_names = get_in_parameter_names(clazz, method_name, reflect_parameters);
		} catch (NotFoundException e) {
			get_in_parameter_names = new String[reflect_parameters.length];
		}

		Map<String, java.lang.reflect.Parameter> map = new HashMap<>();
		for (int i = 0; i < reflect_parameters.length; i++) {
			String parameter_name = get_in_parameter_names[i];
			java.lang.reflect.Parameter reflect_parameter = reflect_parameters[i];
			map.put(parameter_name, reflect_parameter);
		}
		return map;
	}

	public String[] get_in_parameter_names(Class<?> clazz, String method_name, java.lang.reflect.Parameter[] reflect_parameters) throws NotFoundException {
		CtClass ct_class = get_ctclass(clazz);

		CtClass[] types = new CtClass[reflect_parameters.length];
		for (int i = 0; i < reflect_parameters.length; i++) {
			types[i] = get_jdk_ctclass(reflect_parameters[i].getType().getName());
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
		if (pos == 0) {
			LOGGER.error(clazz.getName() + "::" + method_name + "是静态方法,可能无法获取准确的入参");
		}
		for (int j = 0; j < attr.tableLength(); j++) {
			if (attr.variableName(j).equals("this")) {
				pos = j + 1;
			}
		}
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}

}
