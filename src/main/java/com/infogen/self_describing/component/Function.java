/**
 * 
 */
package com.infogen.self_describing.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 保存方法的属性描述
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月9日 下午3:18:27
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class Function {
	// 方法名
	private String request_method;
	private String submit_mode;// GET POST
	private String author;
	private Double version;
	private String describe;

	private String tags; // 方法的分类 比如前端，数据库，中间件，测试
	private Boolean authc = false;
	private String[] roles = new String[0];

	// 参数
	private List<InParameter> in_parameters = new ArrayList<>();
	// 返回值
	private List<OutParameter> out_parameters = new ArrayList<>();

	@JsonIgnore
	private transient Method method;
	@JsonIgnore
	private transient Object instance;

	public List<InParameter> getIn_parameters() {
		return in_parameters;
	}

	public void setIn_parameters(List<InParameter> in_parameters) {
		this.in_parameters = in_parameters;
	}

	public List<OutParameter> getOut_parameters() {
		return out_parameters;
	}

	public void setOut_parameters(List<OutParameter> out_parameters) {
		this.out_parameters = out_parameters;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getSubmit_mode() {
		return submit_mode;
	}

	public String getRequest_method() {
		return request_method;
	}

	public void setRequest_method(String request_method) {
		this.request_method = request_method;
	}

	public void setSubmit_mode(String submit_mode) {
		this.submit_mode = submit_mode;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getTags() {
		return tags;
	}

	public Boolean getAuthc() {
		return authc;
	}

	public void setAuthc(Boolean authc) {
		this.authc = authc;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

}
