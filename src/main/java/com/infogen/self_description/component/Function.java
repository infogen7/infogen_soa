/**
 * 
 */
package com.infogen.self_description.component;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * 方法的描述
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:21:40
 * @since 1.0
 * @version 1.0
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

	// 参数
	private List<InParameter> in_parameters = new ArrayList<>();
	// 返回值
	private List<OutParameter> out_parameters = new ArrayList<>();

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

}
