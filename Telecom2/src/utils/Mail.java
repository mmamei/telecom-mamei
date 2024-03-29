package utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

public class Mail {
	
	private static String email = "marco.mamei@gmail.com";
	private static final String PW_FILE = "C:/Users/marco/gmailpassword.ser";
	
	
	public static void send(String message) {
		send(email,message);
	}
	public static void send(String address, String message) {
		try {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	
	        // Get a Properties object
	        Properties props = System.getProperties();
	        props.setProperty("mail.smtps.host", "smtp.gmail.com");
	        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
	        props.setProperty("mail.smtp.socketFactory.fallback", "false");
	        props.setProperty("mail.smtp.port", "465");
	        props.setProperty("mail.smtp.socketFactory.port", "465");
	        props.setProperty("mail.smtps.auth", "true");
	
	        /*
	        If set to false, the QUIT command is sent and the connection is immediately closed. If set 
	        to true (the default), causes the transport to wait for the response to the QUIT command.
	
	        ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
	                http://forum.java.sun.com/thread.jspa?threadID=5205249
	                smtpsend.java - demo program from javamail
	        */
	        props.put("mail.smtps.quitwait", "false");
	
	        Session session = Session.getInstance(props, null);
	
	        // -- Create a new message --
	        final MimeMessage msg = new MimeMessage(session);
	
	        // -- Set the FROM and TO fields --
	        msg.setFrom(new InternetAddress(email));
	        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(address, false));
	
	        msg.setSubject(message);
	        msg.setText(message, "utf-8");
	        msg.setSentDate(new Date());
	
	        SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
	        
	        File f = new File(PW_FILE);
	        if(!f.exists()) createPasswordFile();
	        
	        t.connect("smtp.gmail.com", email, (String)CopyAndSerializationUtils.restore(new File(PW_FILE)));
	        t.sendMessage(msg, msg.getAllRecipients());      
	        t.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createPasswordFile() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enetr Password:");
		String pw = br.readLine();
		CopyAndSerializationUtils.save(new File(PW_FILE), pw);
	}
	
	
	
	public static void main(String[] args) {
		send("marco.mamei@gmail.com","miao!");
	}
	
}
