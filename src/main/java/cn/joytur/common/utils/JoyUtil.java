package cn.joytur.common.utils;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.jfinal.kit.StrKit;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.joytur.common.mvc.constant.Enums;

/**
 * 项目辅助工具类
 * @author xuhang
 *
 */
public class JoyUtil {
	
	/**
	 * 发送简单邮件
	 * @param recever
	 * @param title
	 * @param content
	 * @return
	 */
	public static synchronized boolean sendEmail(String recever, String title, String content) {
        try {
        	String sendMail = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_SEND_MAIL.name());
        	String sendMailPwd = JoyConfigUtil.getConfigValue(Enums.SysConfigType.MONITOR_SEND_MAIL_PWD.name());
        	
        	if(StrKit.isBlank(sendMail) || StrKit.isBlank(sendMailPwd)){
        		return false;
        	}
        	
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.host", "smtp.163.com");
            props.setProperty("mail.transport.protocol", "smtp");

            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.port", "465");
            props.setProperty("mail.smtp.port", "465");

            Session session = Session.getInstance(props);
            Message msg = new MimeMessage(session);
            msg.setSubject(title);
            msg.setText(content);
            msg.setContent(content, "text/html;charset=utf-8");
            msg.setFrom(new InternetAddress(sendMail));
            Transport transport = session.getTransport();
            transport.connect(sendMail, sendMailPwd);
            transport.sendMessage(msg, new Address[]{new InternetAddress(recever)});
            transport.close();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
	
	/**
	 * 营业时间处理
	 * @param swTime
	 * @return
	 */
	public static String getBusinessTime(String swTime){
		//处理时间
		String[] sTimes = swTime.replaceAll(" ", "").split("-");
		
		sTimes[0] = sTimes[0].substring(0, 5);
		sTimes[1] = sTimes[1].substring(0, 5);
		
		return sTimes[0] + "-" + sTimes[1];
	}
	
	
	/**
	 * 营业时间比较
	 * @param swTime
	 * @return
	 */
	public static boolean getNowBetweenBusinessTime(String swTime){
		//处理时间
		String[] sTimes = swTime.replaceAll(" ", "").split("-");
		String nowDateStr = DateUtil.format(new java.util.Date(), DatePattern.NORM_DATE_PATTERN);
		
		java.util.Date beginDate = DateUtil.parse((nowDateStr + " " + sTimes[0]), DatePattern.NORM_DATETIME_PATTERN);
		java.util.Date endDate = DateUtil.parse((nowDateStr + " " + sTimes[1]), DatePattern.NORM_DATETIME_PATTERN);
		java.util.Date nowDate = new java.util.Date();
		return DateUtil.isIn(nowDate, beginDate, endDate);
	}
	
}