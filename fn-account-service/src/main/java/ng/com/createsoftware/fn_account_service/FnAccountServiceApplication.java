package ng.com.createsoftware.fn_account_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_account_service.client")
public class FnAccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnAccountServiceApplication.class, args);
	}

}
