package model;

public class StudentBean {
	private String name;
	private String major;
	private int courses;
	private double gpa;
	
	public String getName()
	{
		return name;
	}


	public String getMajor()
	{
		return major;
	}


	public int getCourses()
	{
		return courses;
	}


	public double getGpa()
	{
		return gpa;
	}

	public StudentBean(String fullName, String major, int numberOfCourses, double gpa)
	{
		super();
		this.name = fullName;
		this.major = major;
		this.courses = numberOfCourses;
		this.gpa = gpa;
	}

	public StudentBean()
	{
		
	}

}
