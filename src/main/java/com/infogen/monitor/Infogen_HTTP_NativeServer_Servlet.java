package com.infogen.monitor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.infogen.core.json.Jackson;
import com.infogen.server.management.InfoGen_Server_Management;

/**
 * 项目监控和管理接口-获取当前本地依赖服务的状态
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月1日 下午2:37:15
 * @since 1.0
 * @version 1.0
 */
@WebServlet(name = "Infogen_HTTP_NativeServer_Servlet", urlPatterns = "/infogen/native_server")
public class Infogen_HTTP_NativeServer_Servlet extends HttpServlet {
	private static final long serialVersionUID = 4421704113732067430L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write(Jackson.toJson(InfoGen_Server_Management.getInstance().depend_server.values()));
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}
