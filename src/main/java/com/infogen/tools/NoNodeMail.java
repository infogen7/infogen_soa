package com.infogen.tools;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.infogen.aop.tools.Tool_Core;
import com.infogen.logger.Logger_Once;

@Deprecated
public class NoNodeMail {
	private static NoNodeMail instance;

	private static final Logger logger = Logger.getLogger(NoNodeMail.class.getName());

	private MimeMessage mimeMsg; // MIME邮件对象
	private Session session; // 邮件会话对象
	private Properties props; // 系统属性
	// smtp认证用户名和密码
	private static String username;
	private static String password;
	private static String from;
	private static String localIP;

	private Multipart mp; // Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象

	public NoNodeMail(String smtp, String from, String username, String password) {
		setSmtpHost(smtp);
		createMimeMessage();
		NoNodeMail.from = from;
		NoNodeMail.password = password;
		NoNodeMail.username = username;
		NoNodeMail.localIP = Tool_Core.getLocalIP().concat("上:");

	}

	public static NoNodeMail getInstance(String smtp, String from, String username, String password) {
		if (instance == null) {
			synchronized (NoNodeMail.class) {
				if (instance == null) {
					instance = new NoNodeMail(smtp, from, username, password);
				}
			}
		}
		return instance;
	}

	public static final Executor pool = Executors.newFixedThreadPool(10, new ThreadFactory() {// 使用守护进程创建进程池
		public Thread newThread(Runnable r) {
			Thread s = Executors.defaultThreadFactory().newThread(r);
			s.setDaemon(true);
			return s;
		}
	});

	/**
	 * 设置邮件发送服务器
	 * 
	 * @param hostName
	 *            String
	 */
	public void setSmtpHost(String hostName) {
		System.out.println("设置系统属性：mail.smtp.host = " + hostName);
		if (props == null)
			props = System.getProperties(); // 获得系统属性对象
		props.put("mail.smtp.host", hostName); // 设置SMTP主机
	}

	/**
	 * 创建MIME邮件对象
	 * 
	 * @return
	 */
	public boolean createMimeMessage() {
		try {
			System.out.println("准备获取邮件会话对象！");
			session = Session.getDefaultInstance(props, null); // 获得邮件会话对象
		} catch (Exception e) {
			System.err.println("获取邮件会话对象时发生错误！" + e);
			return false;
		}

		System.out.println("准备创建MIME邮件对象！");
		try {
			mimeMsg = new MimeMessage(session); // 创建MIME邮件对象
			mp = new MimeMultipart();

			return true;
		} catch (Exception e) {
			System.err.println("创建MIME邮件对象失败！" + e);
			return false;
		}
	}

	/**
	 * 设置SMTP是否需要验证
	 * 
	 * @param need
	 */
	public void setNeedAuth(boolean need) {
		System.out.println("设置smtp身份认证：mail.smtp.auth = " + need);
		if (props == null)
			props = System.getProperties();
		if (need) {
			props.put("mail.smtp.auth", "true");
		} else {
			props.put("mail.smtp.auth", "false");
		}
	}

	/**
	 * 设置用户名和密码
	 * 
	 * @param name
	 * @param pass
	 */
	public void setNamePass(String name, String pass) {
		username = name;
		password = pass;
	}

	/**
	 * 设置邮件主题
	 * 
	 * @param mailSubject
	 * @return
	 */
	public boolean setSubject(String mailSubject) {
		System.out.println("设置邮件主题！");
		try {
			mimeMsg.setSubject(mailSubject);
			return true;
		} catch (Exception e) {
			System.err.println("设置邮件主题发生错误！");
			return false;
		}
	}

	/**
	 * 设置邮件正文
	 * 
	 * @param mailBody
	 *            String
	 */
	public boolean setBody(String mailBody) {
		try {
			BodyPart bp = new MimeBodyPart();
			bp.setContent("" + mailBody, "text/html;charset=GBK");
			mp.addBodyPart(bp);

			return true;
		} catch (Exception e) {
			System.err.println("设置邮件正文时发生错误！" + e);
			return false;
		}
	}

	/**
	 * 设置发信人
	 * 
	 * @param from
	 *            String
	 */
	public boolean setFrom(String from) {
		System.out.println("设置发信人！");
		try {
			mimeMsg.setFrom(new InternetAddress(from)); // 设置发信人
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 设置收信人
	 * 
	 * @param to
	 *            String
	 */
	public boolean setTo(String to) {
		if (to == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 发送邮件
	 */
	public boolean sendOut() {
		try {
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			logger.info("正在发送邮件....");

			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"), username, password);
			transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
			// transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.CC));
			// transport.send(mimeMsg);

			logger.info("发送邮件成功！");
			transport.close();

			return true;
		} catch (Exception e) {
			logger.info("邮件发送失败！" + e);
			return false;
		}
	}

	/**
	 * 调用sendOut方法完成邮件发送,带抄送
	 * 
	 * @param smtp
	 * @param from
	 * @param to
	 * @param copyto
	 * @param subject
	 * @param content
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public static void send(String to, String subject, String content) {
		if (instance == null) {
			Logger_Once.error("邮件配置没有初始化");
			return;
		}
		if (instance != null) {
			instance.setNeedAuth(true); // 需要验证

			instance.setSubject(subject);
			instance.setBody(localIP.concat(content));
			instance.setTo(to);
			instance.setFrom(from);
			instance.setNamePass(username, password);

			Runnable r = new Runnable() {
				public void run() {
					try {
						instance.sendOut();
					} catch (Exception ex) {
						logger.error("写日志失败", ex);
					}
				}
			};
			pool.execute(r);

		} else {
			logger.info("跳过发送邮件");

		}

	}

}
