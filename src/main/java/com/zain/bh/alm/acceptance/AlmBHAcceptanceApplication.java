package com.zain.bh.alm.acceptance;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration.class
})
@ComponentScan(basePackages = "com.zain.bh.alm.acceptance")
@EnableScheduling
public class AlmBHAcceptanceApplication extends SpringBootServletInitializer {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Nairobi"));
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(AlmBHAcceptanceApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AlmBHAcceptanceApplication.class, args);
	}
}
