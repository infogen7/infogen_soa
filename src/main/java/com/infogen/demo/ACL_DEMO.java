package com.infogen.demo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.infogen.security.component.Security;
import com.infogen.security.component.WhiteList;
import com.infogen.tools.Tool_Jackson;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月8日 下午3:35:46
 * @since 1.0
 * @version 1.0
 */
public class ACL_DEMO {
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, KeeperException, InterruptedException {
		List<ACL> acls = new ArrayList<ACL>();
		// 添加第一个id，采用用户名密码形式
		Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest("admin:admin"));
		ACL acl1 = new ACL(ZooDefs.Perms.ALL, id1);
		acls.add(acl1);
		// 添加第二个id，所有用户可读权限
		Id id2 = new Id("world", "anyone");
		ACL acl2 = new ACL(ZooDefs.Perms.READ, id2);
		acls.add(acl2);

		Security security = new Security();
		List<WhiteList> white_lists = new ArrayList<>();
		white_lists.add(new WhiteList("/*", "172*"));
		white_lists.add(new WhiteList("/*", "192*"));
		white_lists.add(new WhiteList("/*", "127*"));
		security.setWhite_lists(white_lists);
		ZooKeeper zk = new ZooKeeper("172.16.8.97:2181,172.16.8.98:2181,172.16.8.99:2181", 2000, null);
//		zk.addAuthInfo("digest", "admin:admin".getBytes());// /infogen_configuration/develop.com.chengshu.TM.whitelist
		zk.addAuthInfo("digest", "admin:admin".getBytes());
		// zk.create("/infogen_configuration/security/develop.com.chengshu.tm", Tool_Jackson.toJson(security).getBytes(), acls, CreateMode.PERSISTENT);

		zk.setData("/infogen_configuration/security/develop.com.chengshu.tm", Tool_Jackson.toJson(security).getBytes(), -1);
	}
}
