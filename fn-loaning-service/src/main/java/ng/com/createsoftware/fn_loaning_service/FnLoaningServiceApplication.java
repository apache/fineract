package ng.com.createsoftware.fn_loaning_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_loaning_service.client")
public class FnLoaningServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnLoaningServiceApplication.class, args);
	}

}
