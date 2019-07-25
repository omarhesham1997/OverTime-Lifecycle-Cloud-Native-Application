package com.amazonaws.lambda.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LL implements RequestHandler<Input, String> {

	@Override
	public String handleRequest(Input i, Context context) {
		context.getLogger().log("Input: " + i);

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Connection connect;
		try {
			connect = DriverManager.getConnection(  
					"jdbc:mysql://trailxyz.cvobtfkjwwwi.us-east-1.rds.amazonaws.com:3306/Employee","test1","create123");


			String query="UPDATE overtime SET status = true where emp_id = " +i.getEmpid()+ " AND month = "+i.getMonth()+" AND year = "+i.getYear() ;
			PreparedStatement preparedStmt = connect.prepareStatement(query);
			preparedStmt.executeUpdate();
			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return "Success";
	}

}
