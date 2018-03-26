/**
 * 
 */
package com.infogen.http;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.InfoGen;

/**
 * @author larry
 * @version 创建时间 2018年3月26日 下午5:37:16
 */
@WebListener
public class InfoGen_Listener implements ServletContextListener {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Listener.class.getName());

	public void contextInitialized(ServletContextEvent event) {
		InfoGen.aop();
		LOGGER.info("servlet context created");
	}

	public void contextDestroyed(ServletContextEvent event) {
		LOGGER.info("servlet context destroy");
	}
}
