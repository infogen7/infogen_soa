package com.infogen.self_description;

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

import org.apache.log4j.Logger;

import com.infogen.self_description.component.Function;

/**
 * 扫描本地方法自描述的接口
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午4:39:00
 * @since 1.0
 * @version 1.0
 */
public abstract class Self_Description {
	private static final Logger LOGGER = Logger.getLogger(Self_Description.class.getName());

	public abstract Map<String, Function> self_description(Set<Class<?>> class_set);

	// 使用javassist获取参数名
	private ClassPool class_pool = ClassPool.getDefault();

	private Map<String, CtClass> ctclass_maps = new HashMap<>();

	public CtClass get_ctclass(String name) throws NotFoundException {
		CtClass ctClass = ctclass_maps.get(name);
		if (ctClass == null) {
			ctClass = class_pool.get(name);
			ctclass_maps.put(name, ctClass);
		}
		return ctClass;
	}

	public String[] get_in_parameter_names(Class<?> clazz, String method_name, java.lang.reflect.Parameter[] reflect_parameters) throws NotFoundException {
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
