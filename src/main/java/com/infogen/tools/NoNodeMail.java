package com.infogen.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.mail.Address;
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



public class NoNodeMail { 

	private static NoNodeMail instance;
	
	private static final Logger logger = Logger.getLogger(NoNodeMail.class.getName());
	
	private MimeMessage mimeMsg; //MIME邮件对象 
	private Session session; //邮件会话对象 
	private Properties props; //系统属性 
	//smtp认证用户名和密码 
	private String username; 
	private String password; 
	private String from;
	private String localIP;
	
	private Multipart mp; //Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象 
	 
	/**
	 * Constructor
	 * @param smtp 邮件发送服务器
	 */
	public NoNodeMail(){ 
		
	}
	public NoNodeMail(String smtp){ 
		setSmtpHost(smtp); 
		createMimeMessage();
	}
	
	public NoNodeMail(String smtp,String from,String username,String password){ 
		setSmtpHost(smtp); 
		createMimeMessage(); 
		this.from = from ;
		this.password = password;
		this.username = username;
		this.localIP = getLocalIp().concat("上:");
		
	}
	
    public static NoNodeMail getInstance(String smtp,String from,String username,String password){
        if(instance ==null){
            synchronized (NoNodeMail.class){
                if(instance ==null){
                    instance = new NoNodeMail(smtp,from,username,password);
                }
            }
        }
        return instance;
    }
    
    public static NoNodeMail getInstance(){
        if(instance ==null){
           logger.error("邮件配置没有初始化");   
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
	 * @param hostName String 
	 */
	public void setSmtpHost(String hostName) { 
		System.out.println("设置系统属性：mail.smtp.host = "+hostName); 
		if(props == null)
			props = System.getProperties(); //获得系统属性对象 	
		props.put("mail.smtp.host",hostName); //设置SMTP主机 
	} 


	/**
	 * 创建MIME邮件对象  
	 * @return
	 */
	public boolean createMimeMessage() 
	{ 
		try { 
			System.out.println("准备获取邮件会话对象！"); 
			session = Session.getDefaultInstance(props,null); //获得邮件会话对象 
		} 
		catch(Exception e){ 
			System.err.println("获取邮件会话对象时发生错误！"+e); 
			return false; 
		} 
	
		System.out.println("准备创建MIME邮件对象！"); 
		try { 
			mimeMsg = new MimeMessage(session); //创建MIME邮件对象 
			mp = new MimeMultipart(); 
		
			return true; 
		} catch(Exception e){ 
			System.err.println("创建MIME邮件对象失败！"+e); 
			return false; 
		} 
	} 	
	
	/**
	 * 设置SMTP是否需要验证
	 * @param need
	 */
	public void setNeedAuth(boolean need) { 
		System.out.println("设置smtp身份认证：mail.smtp.auth = "+need); 
		if(props == null) props = System.getProperties(); 
		if(need){ 
			props.put("mail.smtp.auth","true"); 
		}else{ 
			props.put("mail.smtp.auth","false"); 
		} 
	} 

	/**
	 * 设置用户名和密码
	 * @param name
	 * @param pass
	 */
	public void setNamePass(String name,String pass) { 
		username = name; 
		password = pass; 
	} 

	/**
	 * 设置邮件主题
	 * @param mailSubject
	 * @return
	 */
	public boolean setSubject(String mailSubject) { 
		System.out.println("设置邮件主题！"); 
		try{ 
			mimeMsg.setSubject(mailSubject); 
			return true; 
		} 
		catch(Exception e) { 
			System.err.println("设置邮件主题发生错误！"); 
			return false; 
		} 
	}
	
	/** 
	 * 设置邮件正文
	 * @param mailBody String 
	 */ 
	public boolean setBody(String mailBody) { 
		try{ 
			BodyPart bp = new MimeBodyPart(); 
			bp.setContent(""+mailBody,"text/html;charset=GBK"); 
			mp.addBodyPart(bp); 
		
			return true; 
		} catch(Exception e){ 
		System.err.println("设置邮件正文时发生错误！"+e); 
		return false; 
		} 
	} 
//	/** 
//	 * 添加附件
//	 * @param filename String 
//	 */ 
//	public boolean addFileAffix(String filename) { 
//	
//		System.out.println("增加邮件附件："+filename); 
//		try{ 
//			BodyPart bp = new MimeBodyPart(); 
//			FileDataSource fileds = new FileDataSource(filename); 
//			bp.setDataHandler(new DataHandler(fileds)); 
//			bp.setFileName(fileds.getName()); 
//			
//			mp.addBodyPart(bp); 
//			
//			return true; 
//		} catch(Exception e){ 
//			System.err.println("增加邮件附件："+filename+"发生错误！"+e); 
//			return false; 
//		} 
//	} 
	
	/** 
	 * 设置发信人
	 * @param from String 
	 */ 
	public boolean setFrom(String from) { 
		System.out.println("设置发信人！"); 
		try{ 
			mimeMsg.setFrom(new InternetAddress(from)); //设置发信人 
			return true; 
		} catch(Exception e) { 
			return false; 
		} 
	} 
	/** 
	 * 设置收信人
	 * @param to String 
	 */ 
	public boolean setTo(String to){ 
		if(to == null)return false; 
		try{ 
			mimeMsg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to)); 
			return true; 
		} catch(Exception e) { 
			return false; 
		} 	
	} 
	
	/** 
	 * 设置抄送人
	 * @param copyto String  
	 */ 
	public boolean setCopyTo(String copyto) 
	{ 
		if(copyto == null)return false; 
		try{ 
		mimeMsg.setRecipients(Message.RecipientType.CC,(Address[])InternetAddress.parse(copyto)); 
		return true; 
		} 
		catch(Exception e) 
		{ return false; } 
	} 
	
	/** 
	 * 发送邮件
	 */ 
	public boolean sendOut() 
	{ 
		try{ 
			mimeMsg.setContent(mp); 
			mimeMsg.saveChanges(); 
			logger.info("正在发送邮件...."); 
			
			Session mailSession = Session.getInstance(props,null); 
			Transport transport = mailSession.getTransport("smtp"); 
			transport.connect((String)props.get("mail.smtp.host"),username,password); 
			transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.TO)); 
//			transport.sendMessage(mimeMsg,mimeMsg.getRecipients(Message.RecipientType.CC)); 
			//transport.send(mimeMsg); 
			
			logger.info("发送邮件成功！"); 
			transport.close(); 
			
			return true; 
		} catch(Exception e) { 
			logger.info("邮件发送失败！"+e); 
			return false; 
		} 
	} 

	/**
	 * 调用sendOut方法完成邮件发送
	 * @param smtp
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public static boolean send(String smtp,String from,String to,String subject,String content,String username,String password) {
		NoNodeMail theMail = new NoNodeMail(smtp);
		theMail.setNeedAuth(true); //需要验证
		
		if(!theMail.setSubject(subject)) return false;
		if(!theMail.setBody(content)) return false;
		if(!theMail.setTo(to)) return false;
		if(!theMail.setFrom(from)) return false;
		theMail.setNamePass(username,password);
		
		if(!theMail.sendOut()) return false;
		return true;
	}
	
	/**
	 * 调用sendOut方法完成邮件发送,带抄送
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
	public void send(String to,String subject,String content) {
		NoNodeMail theMail = NoNodeMail.getInstance();
		if(theMail!=null){
			theMail.setNeedAuth(true); //需要验证
			
			theMail.setSubject(subject);
			theMail.setBody(localIP.concat(content));
			theMail.setTo(to) ;
			theMail.setFrom(from); 
			theMail.setNamePass(username,password);
			
			Runnable r = new Runnable() {
				public void run() {
					try {
						theMail.sendOut();
					} catch (Exception ex) {
						logger.error("写日志失败", ex);
					}
				}
			};
			pool.execute(r);
			
		}else{
			logger.info("跳过发送邮件");
			
		}
		
	}
		
//	public static boolean sendAndCc(String Content) {
//		
//			Mail theMail = Mail.getInstance();
//			if(theMail.mailswitch.equals("true")){
//				theMail.setNeedAuth(true); //需要验证
//				
//				if(!theMail.setSubject("infogen节点错误")) return false;
//				if(!theMail.setBody(Content)) return false;
//				if(!theMail.setTo("online@juxinli.com")) return false;
//				if(!theMail.setCopyTo("chenjian@juxinli.com")) return false;
//				if(!theMail.setFrom("services@juxinli.com")) return false;
//				theMail.setNamePass("services@juxinli.com","JUXINLI2014");
//				
//				if(!theMail.sendOut()) return false;
//				return true;
//			}else{
//				logger.info("跳过发送邮件");
//				return true;
//			}
//
//	}
	/**
	 * 调用sendOut方法完成邮件发送,带附件
	 * @param smtp
	 * @param from
	 * @param to
	 * @param subject
	 * @param content
	 * @param username
	 * @param password
	 * @param filename 附件路径
	 * @return
	 */
//	public static boolean send(String smtp,String from,String to,String subject,String content,String username,String password,String filename) {
//		Mail theMail = new Mail(smtp);
//		theMail.setNeedAuth(true); //需要验证
//		
//		if(!theMail.setSubject(subject)) return false;
//		if(!theMail.setBody(content)) return false;
//		if(!theMail.addFileAffix(filename)) return false; 
//		if(!theMail.setTo(to)) return false;
//		if(!theMail.setFrom(from)) return false;
//		theMail.setNamePass(username,password);
//		
//		if(!theMail.sendOut()) return false;
//		return true;
//	}
	
	/**
	 * 调用sendOut方法完成邮件发送,带附件和抄送
	 * @param smtp
	 * @param from
	 * @param to
	 * @param copyto
	 * @param subject
	 * @param content
	 * @param username
	 * @param password
	 * @param filename
	 * @return
	 */
//	public static boolean sendAndCc(String smtp,String from,String to,String copyto,String subject,String content,String username,String password,String filename) {
//		Mail theMail = new Mail(smtp);
//		theMail.setNeedAuth(true); //需要验证
//		
//		if(!theMail.setSubject(subject)) return false;
//		if(!theMail.setBody(content)) return false;
//		if(!theMail.addFileAffix(filename)) return false; 
//		if(!theMail.setTo(to)) return false;
//		if(!theMail.setCopyTo(copyto)) return false;
//		if(!theMail.setFrom(from)) return false;
//		theMail.setNamePass(username,password);
//		
//		if(!theMail.sendOut()) return false;
//		return true;
//	}
	
	private String getLocalIp(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args){
		String smtp = "smtp.exmail.qq.com";
		String from = "zhashiwen@juxinli.com"; //发件人
		String to = "lixinhao@juxinli.com";   //收件人
		String subject = "ceshi";             //主题
		String content = "馨浩就是大逗比";       //内容
		String username="zhashiwen@juxinli.com";  //邮箱账户
		String password="a921005b930518";         //邮箱密码
		NoNodeMail.send(smtp, from, to, subject, content, username, password);
	}
} 

