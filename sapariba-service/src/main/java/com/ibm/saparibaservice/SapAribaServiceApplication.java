package com.ibm.saparibaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan(basePackages = { "com.ibm.csaservice","com.ibm.controller"} )
public class SapAribaServiceApplication {

	public static void main(String[] args) {

		SpringApplication springApplication = new SpringApplication(SapAribaServiceApplication.class);
		springApplication.addListeners(new ApplicationPidFileWriter("saparibaservice.pid"));
		springApplication.run(args);
	}
}
