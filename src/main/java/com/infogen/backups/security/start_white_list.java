/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年5月8日 下午3:03:30
 * @version 1.0
 */
package com.infogen.backups.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.infogen.aop.tools.Tool_Jackson;
import com.infogen.backups.security.component.Security;
import com.infogen.cache.InfoGen_Cache_Configuration;
import com.infogen.cache.event_handle.InfoGen_Loaded_Handle_Configuration;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月8日 下午3:03:30
 * @since 1.0
 * @version 1.0
 */
public class start_white_list {
	public static final Logger logger = Logger.getLogger(start_white_list.class.getName());
	private InfoGen_Cache_Configuration CACHE_CONFIGURATION = InfoGen_Cache_Configuration.getInstance();
	public String infogen_security_name;

	@SuppressWarnings("unused")
	public start_white_list start_white_list0() {
		// 获取白名单配置
		if (infogen_security_name != null) {
			InfoGen_Loaded_Handle_Configuration configuration_loaded_handle = (security) -> {
				try {
					Security securitys = Tool_Jackson.toObject(security, Security.class);
					InfoGen_Security.getInstance().refresh_security(securitys);
					// 缓存
					CACHE_CONFIGURATION.depend_configuration.put(infogen_security_name, security);
					// 持久化
					CACHE_CONFIGURATION.persistence();
				} catch (Exception e) {
					logger.error("更新安全配置失败", e);
				}
			};
			// get_configuration(configuration.infogen_security_name, configuration_loaded_handle);
		} else {
			List<String> white_lists = new ArrayList<>();
			white_lists.add("/*");
			InfoGen_Security.getInstance().add_ignore(white_lists);
		}
		return this;
	}
}
