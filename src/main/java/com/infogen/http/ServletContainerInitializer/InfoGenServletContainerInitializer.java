package com.infogen.http.ServletContainerInitializer;

import java.lang.reflect.Modifier;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

/**
 * InfoGen的ServletContainerInitializer实现
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月30日 下午4:36:44
 * @since 1.0
 * @version 1.0
 */
@HandlesTypes(WebApplicationInitializer.class)
public class InfoGenServletContainerInitializer implements ServletContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> webApplicationInitializers, ServletContext servletContext) throws ServletException {
		if (webApplicationInitializers != null) {
			for (Class<?> webApplicationInitializerClass : webApplicationInitializers) {
				if (!webApplicationInitializerClass.isInterface() && !Modifier.isAbstract(webApplicationInitializerClass.getModifiers()) && WebApplicationInitializer.class.isAssignableFrom(webApplicationInitializerClass)) {
					try {
						((WebApplicationInitializer) webApplicationInitializerClass.newInstance()).onStartup(servletContext);
					} catch (Exception ex) {
						throw new ServletException("Failed to instantiate webApplicationInitializer class", ex);
					}
				}
			}
		}
	}
}