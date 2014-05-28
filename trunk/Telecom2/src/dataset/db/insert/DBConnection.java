package dataset.db.insert;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import utils.CopyAndSerializationUtils;

public class DBConnection {
	
	private static final String DBNAME = "telecom";
	private static final String DBUSER = "root";
	private static final String PW_FILE = "C:/Users/marco/gmailpassword.ser";
	private static Connection c = null;
	
	
	public static void closeConnection() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection openConnection() {
		 try {
			if(c == null || c.isClosed() || !c.isValid(0)) {
				Class.forName("com.mysql.jdbc.Driver");
				c = DriverManager.getConnection("jdbc:mysql://localhost/"+DBNAME+"?user="+DBUSER+"&password="+(String)CopyAndSerializationUtils.restore(new File(PW_FILE)));
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Statement getStatement() {
		try {
			return openConnection().createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		
		Statement s = getStatement();
		
		String[] tables2Drop = new String[]{
				"pls_lomb_20120404",
		};
		
		
		for(String t: tables2Drop) {
			try {
				s.executeUpdate("drop table "+t);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}
	

}
