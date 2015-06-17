package com.infogen.authc.subject;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import com.infogen.authc.exception.impl.Roles_Fail_Exception;
import com.infogen.authc.exception.impl.Session_Expiration_Exception;
import com.infogen.configuration.InfoGen_Configuration;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午3:14:24
 * @since 1.0
 * @version 1.0
 */
public class Subject {
	protected String subject;
	/**
	 * 受众ID,客户端类型 eg:android 应用 A/ios 应用 B/web 应用 C
	 */
	protected String audience;
	/**
	 * 是否开启记住我
	 */
	protected Boolean remember;
	/**
	 * 创建时间
	 */
	protected Long issued_at;
	/**
	 * 最后一次通过认证时间
	 */
	protected Long last_access_time;
	/**
	 * 用户具有的角色 使用,分隔 eg:admin,employee
	 */
	protected String roles;
	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected long session_overtime = 12 * 60 * 60 * 1000;
	protected long remember_overtime = 5 * 24 * 60 * 60 * 1000;
	protected long max_overtime = 7 * 24 * 60 * 60 * 1000;

	/**
	 * 认证
	 */
	public void checkExpiration() throws Session_Expiration_Exception {
		long millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		long issued_at_overtime = millis - issued_at;
		long last_access_time_overtime = millis - last_access_time;
		if (issued_at_overtime > max_overtime) {
			throw new Session_Expiration_Exception();
		}
		if (remember && issued_at_overtime > remember_overtime && last_access_time_overtime > session_overtime) {
			throw new Session_Expiration_Exception();
		} else if (!remember && last_access_time_overtime > session_overtime) {
			throw new Session_Expiration_Exception();
		}
	}

	/**
	 * 授权
	 * 
	 * @param roles
	 * @throws Roles_Fail_Exception
	 */
	public void hasRole(String[] roles) throws Roles_Fail_Exception {
		if (roles == null || roles.length == 0) {
			return;
		}
		if (this.roles == null) {
			throw new Roles_Fail_Exception();
		}
		List<String> roles_list = Arrays.asList(this.roles.split(","));
		for (String string : roles) {
			if (roles_list.contains(string)) {
				return;
			}
		}
		throw new Roles_Fail_Exception();
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Boolean getRemember() {
		return remember;
	}

	public void setRemember(Boolean remember) {
		this.remember = remember;
	}

	public Long getIssued_at() {
		return issued_at;
	}

	public void setIssued_at(Long issued_at) {
		this.issued_at = issued_at;
	}

	public Long getLast_access_time() {
		return last_access_time;
	}

	public void setLast_access_time(Long last_access_time) {
		this.last_access_time = last_access_time;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

}
