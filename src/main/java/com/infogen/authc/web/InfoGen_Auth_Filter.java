package com.infogen.authc.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.authc.InfoGen_Auth;
import com.infogen.authc.exception.InfoGen_Auth_Exception;
import com.infogen.authc.subject.Redis_Subject_DAO;
import com.infogen.authc.subject.Subject;
import com.infogen.authc.subject.Subject_DAO;
import com.infogen.util.Return;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * API认证的过滤器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Auth_Filter implements Filter {
	public final static Logger logger = Logger.getLogger(InfoGen_Auth_Filter.class.getName());

	public static void main(String[] args) {
		System.out.println((null instanceof InfoGen_Auth_Exception));
	}

	protected FilterConfig filterConfig;

	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	private static Subject_DAO subject_dao = new Redis_Subject_DAO("192.168.0.145", 6379, "redis2014!!");
	public static String TOKEN_NAME = "x-access-token";

	/**
	 * @js 前端页面加载时判断是否有 x-access-token 没有跳转到登录页面
	 * @ajax 调用后判断如果为没有权限执行登录操作
	 * @只有存在 x-access-token 并通过有效期验证的才生成用于验证权限的subject
	 */
	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;

		String x_access_token = request.getParameter(TOKEN_NAME);
		if (x_access_token == null) {
			x_access_token = request.getHeader(TOKEN_NAME);
		}

		try {
			if (x_access_token != null && !x_access_token.trim().isEmpty()) {
				X_Access_Token token = Tool_Jackson.toObject(x_access_token, X_Access_Token.class);
				Subject subject = subject_dao.get(token.user);
				InfoGen_Auth.setSubject(subject);
			}
			filterChain.doFilter(srequset, sresponse);
		} catch (Exception e) {
			if (e instanceof InfoGen_Auth_Exception) {
				InfoGen_Auth_Exception cause = (InfoGen_Auth_Exception) e;
				response.getWriter().write(Return.FAIL(cause.name(), cause.note()).toJson());
			} else if (e.getCause() instanceof InfoGen_Auth_Exception) {
				InfoGen_Auth_Exception cause = (InfoGen_Auth_Exception) e.getCause();
				response.getWriter().write(Return.FAIL(cause.name(), cause.note()).toJson());
			} else {
				throw e;
			}
		}
	}

	@Override
	public void destroy() {
	}
}