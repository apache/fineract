package ng.com.createsoftware.fn_agency_banking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_agency_banking_service.client")
public class FnAgencyBankingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnAgencyBankingServiceApplication.class, args);
	}

}
