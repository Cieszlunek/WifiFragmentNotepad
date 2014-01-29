package com.example.wififragmentnotepad;


public class LastOpenedFile {
	private String name;
	private String patch;
	//private String date;
	public LastOpenedFile(String n, String p)
	{
		name = n;
		patch = p;
		//DateFormat df = DateFormat.getTimeInstance();
		//df.setTimeZone(TimeZone.getTimeZone("gmt"));
		//date = df.format(new Date());
	}
	public void setName(String n)
	{
		name = n;
	}
	public void setPatch(String p)
	{
		patch = p;
	}
	public String getName()
	{
		return name;
	}
	public String getPatch()
	{
		return patch;
	}
	//public String getDate()
	//{
	//	return date;
	//}
	//public String setDate(date)
}
