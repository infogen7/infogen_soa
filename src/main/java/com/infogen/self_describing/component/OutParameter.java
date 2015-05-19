/**
 * 
 */
package com.infogen.self_describing.component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 返回值的属性描述
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月9日 下午4:07:08
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class OutParameter {
	private String name;
	@JsonIgnore
	private Class<?> type;
	private Boolean required = true;
	private String default_value;
	private String describe;

	public OutParameter() {
	}

	public OutParameter(String name, Class<?> type, Boolean required, String default_value, String describe) {
		super();
		this.name = name;
		this.type = type;
		this.required = required;
		this.default_value = default_value;
		this.describe = describe;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getDefault_value() {
		return default_value;
	}

	public void setDefault_value(String default_value) {
		this.default_value = default_value;
	}

}
