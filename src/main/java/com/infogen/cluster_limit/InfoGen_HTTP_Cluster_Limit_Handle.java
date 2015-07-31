package com.infogen.cluster_limit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.cluster_limit.group_dao.Default_Group_DAO;
import com.infogen.cluster_limit.group_dao.Group_DAO;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_HTTP_Cluster_Limit_Handle {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_HTTP_Cluster_Limit_Handle.class.getName());

	// 初始化配置时赋值 <requestURI,<key-[value-limit]>>
	public static final Map<String, Limit_Model> limit_models = new HashMap<>();
	public static Group_DAO group_dao = new Default_Group_DAO();

	public Boolean doFilter(String requestURI, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Limit_Model limit_Model = limit_models.get(requestURI);
		if (limit_Model == null) {
			return true;
		}

		String group = limit_Model.getGroup();
		if (group == null) {
			return true;
		}

		String group_by = request.getParameter(group);
		Long limit = limit_Model.getLimits().getOrDefault(group_by, Long.MAX_VALUE);
		Long increment_and_get = group_dao.increment_and_get(group_by, 1);
		if (increment_and_get > limit) {
			LOGGER.info("用户调用次数超过限制:".concat(requestURI).concat("-").concat(limit.toString()));
			response.getWriter().write(Return.FAIL(CODE.limit_by_group).toJson());
			return false;
		}

		return true;
	}
}