/**
 * 
 */
package com.infogen.rpc.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.infogen.self_describing.InfoGen_Self_Describing;
import com.infogen.self_describing.component.Function;
import com.infogen.self_describing.component.InParameter;
import com.infogen.thrift.Message.Iface;
import com.infogen.thrift.Request;
import com.infogen.thrift.Response;
import com.infogen.tools.Tool_Jackson;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * thrift消息处理器
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年11月7日 上午10:54:29
 */
public class Thrift_Message_Handler implements Iface {
	public static final Logger logger = Logger.getLogger(Thrift_Message_Handler.class.getName());

	private Map<String, Function> functions = InfoGen_Self_Describing.getInstance().functions;

	private String class_string = "class java.lang.String";

	@Override
	public Response call(Request request) throws TException {
		Response response = new Response();
		response.setSessionID(request.getSessionID());
		response.setSequence(request.getSequence());
		response.setMethod(request.getMethod());

		Function function = functions.get(request.getMethod());
		if (function == null) {
			response.setSuccess(false);
			response.setCode(404);
			response.setNote("没有这个方法");
			return response;
		}

		List<InParameter> in_parameters = function.getIn_parameters();
		Map<String, String> request_parameters = request.getParameters();
		List<Object> value_parameters = new ArrayList<>(in_parameters.size());
		try {
			for (InParameter parameter : in_parameters) {
				String name = parameter.getName();
				String value = request_parameters.get(name);
				if (value == null && parameter.getRequired()) {
					response.setSuccess(false);
					response.setCode(CODE._401.code);
					response.setNote(name.concat("不能为空"));
					return response;
				}
				if (value.equals(class_string)) {
					Object object = Tool_Jackson.toObject(value, parameter.getType());
					value_parameters.add(object);
				} else {
					value_parameters.add(value);
				}
			}
			//
			Object invoke = function.getMethod().invoke(function.getInstance(), value_parameters.toArray());
			response.setSuccess(true);
			response.setCode(CODE._200.code);
			if (invoke == null) {
			} else if (invoke instanceof String) {
				response.setData((String) invoke);
			} else if (invoke instanceof Return) {
				response.setData(Tool_Jackson.toJson(invoke));
			}
		} catch (Exception e) {
			logger.error("映射失败", e);
			response.setSuccess(false);
			response.setCode(CODE._500.code);
			response.setNote(e.getMessage());
			return response;
		}
		return response;
	}

}
