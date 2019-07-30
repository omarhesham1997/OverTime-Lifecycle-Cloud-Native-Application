package com.amazonaws.lambda.demo;



import java.io.ByteArrayInputStream;

import java.io.IOException;

import java.io.InputStream;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

import java.util.Base64;

import java.util.Calendar;



import org.hibernate.Session;

import org.hibernate.SessionFactory;

import org.json.simple.JSONObject;



import com.amazonaws.AmazonServiceException;

import com.amazonaws.SdkClientException;

import com.amazonaws.lambda.demo.Emp;

import com.amazonaws.lambda.demo.HibernateUtil;

import com.amazonaws.lambda.demo.Request;



import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.amazonaws.services.s3.model.ObjectMetadata;



import com.amazonaws.lambda.demo.*;



public class LambdaFunctionHandler implements RequestHandler<Request, String> {



	String dstBucket = 	System.getenv("bucketname");	
	String host_name=System.getenv("host_name");
	String user_name=System.getenv("user_name");
	String password=System.getenv("password");
	String dbname=System.getenv("dbname");
	@Override

	public String handleRequest(Request request, Context context) {

		String s=" ";

		



		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

		try (Session session = sessionFactory.openSession()) {

			int ctr=0;



			Connection connect;

			connect = DriverManager.getConnection(  

					"jdbc:mysql://" +host_name+":3306/"+dbname,user_name,password);

			

			

			int month= request.getMonth();

			int year=request.getYear();

			int overtime=request.getOvertime();

			int empid=request.getEmp_id();

			Calendar Year= Calendar.getInstance();

			int CurrentYear=Year.get(Year.YEAR);

			

			if((request.getMonth()<=12 && request.getMonth() >=1) && (request.getOvertime()>=1 && request.getOvertime()<=56)  && (request.getYear()>=CurrentYear && request.getYear()<=CurrentYear+2) )

			{

				/*

			JSONObject obj = new JSONObject();

			obj.put("month", request.getMonth());

			obj.put("year",request.getYear());

			obj.put("overtime",request.getOvertime());

			obj.put("emp_id",request.getEmp_id());

			


*/

		

					Statement statement = connect.createStatement();

					// Result set get the result of the SQL query

					String query="SELECT emp_name,emp_mail,manager_id FROM employee WHERE emp_id="+empid;

					ResultSet resultSet = statement.executeQuery(query);

					if (resultSet.next()==true)

					{

						Statement statementthree = connect.createStatement();

						// Result set get the result of the SQL query

						String querythree="SELECT * FROM overtime WHERE emp_id="+empid;

						ResultSet resultSetthree = statementthree.executeQuery(querythree);

						while (resultSetthree.next())

						{

							if(request.getMonth()==Integer.parseInt(resultSetthree.getString("month")) && request.getYear()==Integer.parseInt(resultSetthree.getString("year"))) {

								 ctr++;

							}

							/*else {

								ctr=0;

							}*/

						}

						if(ctr!=0)

						{

							 s="File isn't uploaded and the Data isn't added to the database because the data is duplicated!";



						}

						else {



							session.beginTransaction();

							Emp employee = new Emp();

							employee.setEmp_id(request.emp_id);

							employee.setMonth(request.month);

							employee.setYear(request.year);

							employee.setOvertime(request.overtime);

							

							session.save(employee);

							session.getTransaction().commit();

							AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();  

								String objectKey = request.getEmp_id()+"_"+request.getMonth()+"_"+request.getYear()+".msg";// create object key ie. file to be uploaded

								ObjectMetadata metax = null;

								InputStream inputstream = null;

								String msg = request.getContent();

								Base64.Decoder enc = Base64.getDecoder();

								byte[] encbytes = enc.decode(msg.getBytes());

								inputstream = new ByteArrayInputStream(encbytes);







								metax = new ObjectMetadata();	        

								metax.setContentLength(encbytes.length);     

								//finally upload the file by specifying destination bucket name, file name, input stream having content to be uploaded along with meta information

								s3Client.putObject(dstBucket, objectKey, inputstream, metax);

								//System.out.println("File uploaded.");

								 s="File uploaded, "+"Added "+ request.getEmp_id()+" "+request.getMonth()+" "+request.getYear()+" "+request.getOvertime()+"  to the database.";



						}

					

						}

					else {

						 s="File isn't uploaded and the Data isn't added to the database because the employee ID you've entered is not available!";

						 

					}



			}

			else {

				if((request.getMonth()>12 || request.getMonth()<1) )

				{

					s="File isn't uploaded and the Data isn't added to the database because the month value is invalid you should enter a month from 1 to 12!";

				}

				else if((request.getOvertime()<1 || request.getOvertime()>56) )

				{

					s="File isn't uploaded and the Data isn't added to the database because the overtime value is invalid you should enter a number from 1 to 56!";

				}

				else if((request.getYear()<CurrentYear || request.getYear()>CurrentYear+2) )

				{

					s="File isn't uploaded and the Data isn't added to the database because the year value is invalid you should enter a year from current year till 2 years after!";

				}

				 

			}

			

				} catch (SQLException e) {

					// TODO Auto-generated catch block

					e.printStackTrace();
				context.getLogger().log("error : "+e);

				}


if (s=="")
  {
	s="Sucess " + 
			 String.format("Added %s %s %s %s %s.", request.emp_id, request.month,request.year,request.overtime);
	}

				return s;

				}

				 

			

			

			

			

			



	}







