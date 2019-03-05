package com.yh.dwdatalink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DwdataletApplication {
	public static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(DwdataletApplication.class, args);
	}

}
