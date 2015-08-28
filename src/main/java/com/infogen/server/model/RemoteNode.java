package com.infogen.server.model;

import java.time.Clock;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;

import net.jcip.annotations.ThreadSafe;

/**
 * 为本地调用处理扩展的节点属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月17日 下午5:30:01
 * @since 1.0
 * @version 1.0
 */
@ThreadSafe
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RemoteNode extends AbstractNode {
	@JsonIgnore
	public transient Long disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();
}
