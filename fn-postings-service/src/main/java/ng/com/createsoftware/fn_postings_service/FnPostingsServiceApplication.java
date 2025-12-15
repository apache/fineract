package ng.com.createsoftware.fn_postings_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_postings_service.client")
public class FnPostingsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnPostingsServiceApplication.class, args);
	}

}
