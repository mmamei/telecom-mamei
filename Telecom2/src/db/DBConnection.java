package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import utils.CopyAndSerializationUtils;

public class DBConnection {
	
	private static final String PW_FILE = "C:/Users/marco/gmailpassword.ser";
	private static Connection c = null;
	
	
	public static void closeConnection() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Statement getStatement() {
		 try {
			if(c == null || c.isClosed() || !c.isValid(0)) {
				Class.forName("com.mysql.jdbc.Driver");
				c = DriverManager.getConnection("jdbc:mysql://localhost/telecom?user=root&password="+(String)CopyAndSerializationUtils.restore(new File(PW_FILE)));
			}
			return c.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		Statement s = getStatement();
		
		
		try {
			s.executeUpdate("drop table pls_ve_20130529");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			s.executeUpdate("drop table pls_ve_20130725");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			s.executeUpdate("drop table pls_ve_20130724");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}
	

}
