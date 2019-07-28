package com.amazonaws.lambda.demo;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.session.Session;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.util.IOUtils;

public class Email_Test_With_S3_Event implements RequestHandler<S3Event, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	public Email_Test_With_S3_Event() {}

	// Test purpose only.
	Email_Test_With_S3_Event(AmazonS3 s3) {
		this.s3 = s3;
	}

	@Override
	public String handleRequest(S3Event event, Context context) {
		context.getLogger().log("Received event: " + event);

		// Get the object from the event and show its content type
		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();

		try {
			S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
			String contentType = response.getObjectMetadata().getContentType();
			context.getLogger().log("Response " + response);
			context.getLogger().log("FILE NAME: " + key);
			getNameFromJson(key,response);

			return contentType;
		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log(String.format(
					"Error getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.", key, bucket));
			throw e;
		}
	}
	public void getNameFromJson(String key,S3Object response)
	{

		final String SUBJECT = "Approval Request";

		String TO = null;
		String FROM = null;
		String BODY=null;
		String host_name=System.getenv("host_name");
		String user_name=System.getenv("user_name");
		String password=System.getenv("password");
		String dbname=System.getenv("dbname");

				try {

					String[] output = key.split("_");
					int empid=Integer.parseInt(output[0]);
					System.out.println(empid);
					try 
					{
						// This will load the MySQL driver, each DB has its own driver
						Class.forName("com.mysql.jdbc.Driver").newInstance();


						Connection connect=DriverManager.getConnection( "jdbc:mysql://" +host_name+":3306/"+dbname,user_name,password); 

						// Statements allow to issue SQL queries to the database
						Statement statement = connect.createStatement();
						// Result set get the result of the SQL query
						String query="SELECT emp_name,emp_mail,manager_id FROM employee WHERE emp_id="+empid;
						ResultSet resultSet = statement.executeQuery(query);
						while (resultSet.next()) {
							String user = resultSet.getString("emp_name");
							final String Mail = resultSet.getString("emp_mail");

							int Relation = resultSet.getInt("manager_id");
							System.out.println("Employee: " + user);
							System.out.println("Mail: " + Mail);
							System.out.println("Relation: " + Relation);
							Statement statementtwo = connect.createStatement();
							// Result set get the result of the SQL query
							String query2="SELECT emp_mail FROM employee WHERE emp_id="+Relation;
							ResultSet resultSet2 = statementtwo.executeQuery(query2);
							while (resultSet2.next()) {
								String ManagerMail = resultSet2.getString("emp_mail");
								System.out.println("ManagerMail: " + ManagerMail);
								FROM = Mail ;
								TO = ManagerMail;

							}
							Statement statementthree = connect.createStatement();
							String query3="SELECT month,year,overtime FROM overtime WHERE emp_id="+empid;
							ResultSet resultSet3 = statementthree.executeQuery(query3);
							while (resultSet3.next()) {
								int overtime = resultSet3.getInt("overtime");
								String month = resultSet3.getString("month");
								String year = resultSet3.getString("year");
								System.out.println("overtime: " + overtime);
								System.out.println("month: " + month);
								System.out.println("year: " + year);

								BODY = "Employee "+ user+" will have an overtime of "+ overtime+" hours, in month: "+month+
										" ,year: "+year+". Please Approve.";
							}

						}
						connect.close();
					} 
					catch (Exception e)
					{
						throw e;
					}


				}
		catch(Exception e) 
		{
			// if any error occurs
			e.printStackTrace();
		}

		javax.mail.Session session = javax.mail.Session.getDefaultInstance(new Properties());

		// Create a new MimeMessage object.
		MimeMessage message = new MimeMessage(session);
		byte[] byteStrem =null;
		// Add subject, from and to lines.

		try {
			message.setSubject(SUBJECT, "UTF-8");
			message.setFrom(new InternetAddress(FROM));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.        
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the text part.
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(BODY, "text/plain; charset=UTF-8");

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(BODY,"text/html; charset=UTF-8");

			// Add the text and HTML parts to the child container.
			msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msg = new MimeMultipart("mixed");

			// Add the parent container to the message.
			message.setContent(msg);

			// Add the multipart/alternative part to the message.
			msg.addBodyPart(wrap);

			// Define the attachment

			MimeBodyPart att = new MimeBodyPart();
			InputStream stream = response.getObjectContent();
			byteStrem = IOUtils.toByteArray(stream);
			DataSource fds = new ByteArrayDataSource(byteStrem,"application/vnd.ms-outlook");
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(key);
			msg.addBodyPart(att);
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		//DataSource fds = new ByteArrayDataSource(path);
		//att.setDataHandler(new DataHandler(fds));
		//att.setFileName(fds.getName());

		// Add the attachment to the message.


		// Try to send the email.
		try {
			System.out.println("Attempting to send an email through Amazon SES "
					+"using the AWS SDK for Java...");

			// Instantiate an Amazon SES client, which will make the service 
			// call with the supplied AWS credentials.
			AmazonSimpleEmailService client = 
					AmazonSimpleEmailServiceClientBuilder.standard()
					// Replace US_WEST_2 with the AWS Region you're using for
					// Amazon SES.
					.withRegion(Regions.US_EAST_1).build();

			// Print the raw email content on the console
			PrintStream out = System.out;
			message.writeTo(out);

			// Send the email.
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);
			RawMessage rawMessage = 
					new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = 
					new SendRawEmailRequest(rawMessage);


			client.sendRawEmail(rawEmailRequest);
			System.out.println("Email sent!");
			// Display an error if something goes wrong.
		} catch (Exception ex) {
			System.out.println("Email Failed");
			System.err.println("Error message: " + ex.getMessage());
			ex.printStackTrace();
		}
	}


}
