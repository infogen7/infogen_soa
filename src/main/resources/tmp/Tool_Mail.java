package com.infogen.tools;

import java.security.Security;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Tool_Mail {

	private static final Logger LOGGER = LogManager.getLogger(Tool_Mail.class.getName());

	private String smtp_host;
	private String smtp_port;
	private String from;
	private String password;

	public final Executor pool = Executors.newFixedThreadPool(10, new ThreadFactory() {// 使用守护进程创建进程池
		public Thread newThread(Runnable r) {
			Thread s = Executors.defaultThreadFactory().newThread(r);
			s.setDaemon(true);
			return s;
		}
	});

	private static class InnerInstance {
		public static final Tool_Mail instance = new Tool_Mail();
	}

	public static Tool_Mail getInstance() {
		return InnerInstance.instance;
	}

	private Tool_Mail() {
	}

	public void start(String smtp_host, String smtp_port, String from, String password) {
		this.smtp_host = smtp_host;
		this.smtp_port = smtp_port;
		this.from = from;
		this.password = password;
	}

	public void send(String to, String subject, String content, Map<String, String> attachment) {
		try {
			subject = (subject == null) ? "" : subject;
			content = (content == null) ? "" : content;

			Properties properties = new Properties();
			properties.put("mail.smtp.host", smtp_host);
			properties.put("mail.smtp.port", smtp_port);
			properties.put("mail.smtp.auth", "true");

			properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.setProperty("mail.smtp.socketFactory.fallback", "false");
			properties.setProperty("mail.smtp.socketFactory.port", smtp_port);
			@SuppressWarnings("restriction")
			com.sun.net.ssl.internal.ssl.Provider provider = new com.sun.net.ssl.internal.ssl.Provider();
			Security.addProvider(provider);

			javax.mail.Session sendMailSession = Session.getDefaultInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(from, password);
				}
			});
			MimeMessage mailMessage = new MimeMessage(sendMailSession);
			mailMessage.setFrom(new InternetAddress(from));
			// Message.RecipientType.TO属性表示接收者的类型为TO/CC
			mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mailMessage.setSubject(subject, "UTF-8");
			mailMessage.setSentDate(new Date());

			Multipart mainPart = new MimeMultipart();
			BodyPart html = new MimeBodyPart();
			html.setContent(content.trim(), "text/html; charset=utf-8");
			mainPart.addBodyPart(html);
			if (attachment != null) {
				for (Entry<String, String> eKey : attachment.entrySet()) {
					// 创建一个附件
					BodyPart _html = new MimeBodyPart();
					_html.setDataHandler(new DataHandler(eKey.getKey(), "text/plain; charset= gb2312"));
					_html.setFileName(eKey.getValue());
					mainPart.addBodyPart(_html);
				}
			}
			mailMessage.setContent(mainPart);

			Runnable r = new Runnable() {
				public void run() {
					try {
						LOGGER.info("正在发送邮件...");
						Transport.send(mailMessage);
						LOGGER.info("发送邮件成功");
					} catch (Exception e) {
						LOGGER.error("发送邮件失败", e);
					}
				}
			};
			pool.execute(r);
		} catch (Exception e) {
			LOGGER.error("发送邮件失败", e);
		}
	}

}
