package com.saqib.placementportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PlacementportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlacementportalApplication.class, args);
	}

}
