/**
 * 
 */
package com.infogen.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 自定义类加载器
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2015年2月25日 下午2:05:56
 */
public class InfoGen_ClassLoader extends URLClassLoader {

	public InfoGen_ClassLoader(URL[] urls) {
		super(urls);
	}

	public InfoGen_ClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public void addJar(URL url) {
		this.addURL(url);
	}

}