package com.amazonaws.lambda.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "overtime")
public class Emp implements java.io.Serializable {
    private int emp_id;
    private int month;
    private int year;
    private int overtime;
    boolean status;
    
    public Emp() {
    }

    public Emp(int emp_id,int month,int year,int overtime,boolean status) {
        this.emp_id=emp_id;
        this.month = month;
        this.year=year;
        this.overtime=overtime;
        this.status=status;
    }

  @Id
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

	

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}




    

   

    @Override
    public String toString() {
        return "Employee { " + ", Month =" + month + ", Year =" + year + ", Overtime ="+ overtime + ", Employee ID ="+ emp_id +", Status="+ status +'}';
    }
}
