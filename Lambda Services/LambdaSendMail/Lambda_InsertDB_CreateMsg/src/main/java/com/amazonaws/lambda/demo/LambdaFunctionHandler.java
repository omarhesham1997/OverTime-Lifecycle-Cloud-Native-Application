package com.amazonaws.lambda.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

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

	String dstBucket = 	System.getenv("bucket_name");	
	@Override
	public String handleRequest(Request request, Context context) {
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		try (Session session = sessionFactory.openSession()) {
			session.beginTransaction();
			Emp employee = new Emp();
			employee.setEmp_id(request.emp_id);
			employee.setMonth(request.month);
			employee.setYear(request.year);
			employee.setOvertime(request.overtime);
			employee.setStatus(request.status);
			session.save(employee);
			session.getTransaction().commit();
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();   

			JSONObject obj = new JSONObject();
			obj.put("month", request.getMonth());
			obj.put("year",request.getYear());
			obj.put("overtime",request.getOvertime());
			obj.put("emp_id",request.getEmp_id());

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
			System.out.println("File uploaded.");
		}

		return String.format("Added %s %s %s %s %s.", request.emp_id, request.month,request.year,request.overtime,request.status);

	}
}


