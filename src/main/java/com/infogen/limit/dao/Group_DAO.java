package com.infogen.limit.dao;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:10:20
 * @since 1.0
 * @version 1.0
 */
public abstract class Group_DAO {
	// 如DefaultSessionManager在创建完session后会调用该方法；如保存到关系数据库/文件系统/NoSQL数据库；即可以实现会话的持久化；返回会话ID；主要此处返回的ID.equals(session.getId())；
	public abstract Long increment_and_get(String group_by);
}
