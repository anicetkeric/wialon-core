package com.sdk.wialon;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sdk.integration.Demo;



@SpringBootApplication
public class WialonCoreApplication implements CommandLineRunner   {

	
	public static void main(String[] args) {
		SpringApplication.run(WialonCoreApplication.class, args);
	}
	@Override
	public void run(String... arg0) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("test");
		Demo c=new Demo();
		
		c.login();
				
			
	}
}
