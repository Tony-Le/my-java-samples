package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
	
	//private DataSource dataSource;
	
	private static final String DB_URL = "";
	
	public StudentDAO()
	{
		
	}
	
	public List<StudentBean> retrieve(String prefix, double minGpa, String sortBy) throws SQLException
	{
		List<StudentBean> list = new ArrayList<StudentBean>();
		list = this.retrieve(prefix, sortBy);
		if (minGpa > 0)
		{
			list.removeIf(i -> i.getGpa() < minGpa);
		}
		return list;
	}
	
	public List<StudentBean> retrieve(String prefix, String sortBy) throws SQLException 
	{
		Connection con = DriverManager.getConnection(DB_URL);
		List<StudentBean> list = new ArrayList<StudentBean>();
		Statement s = con.createStatement();
		s.executeUpdate("set schema roumani");
		PreparedStatement ps = null;
		String query = "SELECT SURNAME, GIVENNAME, MAJOR, COURSES, GPA FROM SIS";
		if (sortBy.toLowerCase().equals("last name"))
		{
			query += " ORDER BY SURNAME";
		}
		else if (sortBy.toLowerCase().equals("number of courses"))
		{
			query += " ORDER BY COURSES";
		}
		else if (sortBy.toLowerCase().equals("major"))
		{
			query += " ORDER BY MAJOR";
		}
		else if (sortBy.toLowerCase().equals("gpa"))
		{
			query += " ORDER BY GPA";
		}
		ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			String fullName = rs.getString("SURNAME") + ", " + rs.getString("GIVENNAME") ;
			String major = rs.getString("MAJOR");
			int courses = rs.getInt("COURSES");
			double gpa = rs.getDouble("GPA");
			list.add(new StudentBean(fullName, major, courses, gpa));
		}
		rs.close();
		con.close();
		if (!prefix.isEmpty())
		{
			list.removeIf(i -> !i.getName().toLowerCase().startsWith(prefix.toLowerCase()));
		}
		return list;
	}
}
