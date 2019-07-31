package com.amazonaws.lambda.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Input, String> {

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

		String host_name=System.getenv("host_name");
		String user_name=System.getenv("user_name");
		String password=System.getenv("password");
		String dbname=System.getenv("dbname");
		Connection connect;
		try {
			connect = DriverManager.getConnection(  
					"jdbc:mysql://" +host_name+":3306/"+dbname,user_name,password);

			String query="UPDATE overtime SET status ='"+i.getChoice()+ "' where emp_id = '" +i.getEmpid()+ "' AND month = '"+i.getMonth()+"' AND year = '"+i.getYear()+"'" ;
			PreparedStatement preparedStmt = connect.prepareStatement(query);
			preparedStmt.executeUpdate();
			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		
		
		return i.getChoice();
	}

}
