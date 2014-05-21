package zzz_misc_code;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import utils.CopyAndSerializationUtils;


public class DBTest1 {
	private static final String PW_FILE = "C:/Users/marco/gmailpassword.ser";
	public static void main(String[] args) throws Exception {
		
		Class.forName("com.mysql.jdbc.Driver");
	    Connection c = DriverManager.getConnection("jdbc:mysql://localhost/telecom?user=root&password="+(String)CopyAndSerializationUtils.restore(new File(PW_FILE)));
	    Statement s = c.createStatement();
		s.close();
		c.close();
	}
	
}
