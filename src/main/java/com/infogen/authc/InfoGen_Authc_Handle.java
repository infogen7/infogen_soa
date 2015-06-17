package com.infogen.authc;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.InfoGen;
import com.infogen.authc.exception.InfoGen_Auth_Exception;
import com.infogen.authc.exception.impl.Authentication_Fail_Exception;
import com.infogen.authc.subject.Default_Subject_DAO;
import com.infogen.authc.subject.Subject;
import com.infogen.authc.subject.Subject_DAO;
import com.infogen.self_describing.component.Function;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * API认证的过滤器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Authc_Handle {
	private final static Logger logger = Logger.getLogger(InfoGen_Authc_Handle.class.getName());

	private static Subject_DAO subject_dao = new Default_Subject_DAO();
	public static String TOKEN_NAME = "x-access-token";
	public Map<String, Function> functions = InfoGen.getInstance().getConfiguration().register_server.getFunctions();

	/**
	 * js 前端页面加载时判断是否有 x-access-token 没有跳转到登录页面
	 * 
	 * ajax 调用后判断如果为没有权限执行登录操作
	 * 
	 * 只有存在 x-access-token 并通过有效期验证的才生成用于验证权限的subject
	 */
	public Boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// request.getRealPath("/")); F:\Tomcat 6.0\webapps\news\test
		// System.out.println(request.getRequestURL()); // http://localhost:8080/news/main/list.jsp
		// System.out.println(request.getContextPath()); // /news
		// System.out.println(request.getServletPath()); // /main/list.jsp 配置spring mvc 的<mvc:default-servlet-handler />后始终为空
		// System.out.println(request.getRequestURI()); // /news/main/list.jsp

		try {
			// 当前请求页面 判断是否需要认证
			String requestURI = request.getRequestURI();
			String contextPath = request.getContextPath();
			if (requestURI.startsWith(contextPath)) {
				requestURI = requestURI.substring(contextPath.length());
			}
			Function function = functions.get(requestURI);
			if (function == null || !function.getAuthc()) {
				return true;
			}

			// 认证
			String x_access_token = request.getParameter(TOKEN_NAME);
			if (x_access_token == null) {
				x_access_token = request.getHeader(TOKEN_NAME);
			}
			if (x_access_token == null || x_access_token.trim().isEmpty()) {
				throw new Authentication_Fail_Exception();
			}
			Subject subject = subject_dao.get(x_access_token);
			subject.checkExpiration();
			String[] roles = function.getRoles();
			if (roles.length > 0) {
				subject.hasRole(roles);
			}
			subject.hasRole(function.getRoles());
			// 缓存
			ThreadLocal_Auth.setSubject(subject);
		} catch (InfoGen_Auth_Exception e) {
			response.getWriter().write(Return.FAIL(e.name(), e.note()).toJson());
			return false;
		} catch (Exception e) {
			logger.error("认证异常:", e);
			response.getWriter().write(Return.FAIL(CODE._500).toJson());
			return false;
		}
		return true;
	}
}