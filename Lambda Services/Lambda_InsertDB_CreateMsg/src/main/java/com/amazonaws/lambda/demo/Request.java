package com.amazonaws.lambda.demo;


public class Request {
	public int emp_id;
	public int month;
	public int year;
	public int overtime;
	String content;

	



	public Request() {
	}



	public Request(int emp_id,int month,int year,int overtime,String content) {
		this.emp_id=emp_id;
		this.month = month;
		this.year=year;
		this.overtime=overtime;
		this.content=content;
	}



	public int getEmp_id() {
		return emp_id;
	}

	public void setEmp_id(int emp_id) {
		this.emp_id = emp_id;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getOvertime() {
		return overtime;
	}

	public void setOvertime(int overtime) {
		this.overtime = overtime;
	}



	



	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}






	@Override
	public String toString() {
		return "Employee { " + ", Month =" + month + ", Year =" + year + ", Overtime ="+ overtime + ", Employee ID ="+ emp_id +'}';
	}
}
