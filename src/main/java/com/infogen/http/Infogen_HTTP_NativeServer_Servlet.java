package com.infogen.http;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.model.NativeServer;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年7月1日 下午2:37:15
 * @since 1.0
 * @version 1.0
 */
@WebServlet(name = "Infogen_HTTP_NativeServer_Servlet", urlPatterns = "/infogen/native_server")
public class Infogen_HTTP_NativeServer_Servlet extends HttpServlet {
	private static final long serialVersionUID = 4421704113732067430L;

	ConcurrentMap<String, NativeServer> depend_server = InfoGen_Cache_Server.getInstance().depend_server;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write(Tool_Jackson.toJson(depend_server));
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}
