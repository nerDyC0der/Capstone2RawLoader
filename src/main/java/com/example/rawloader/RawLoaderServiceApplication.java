package com.example.rawloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.rawloader.client")
public class RawLoaderServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RawLoaderServiceApplication.class, args);
	}
}
