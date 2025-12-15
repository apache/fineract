package ng.com.createsoftware.fn_investment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_investment_service.client")
@EnableDiscoveryClient
public class FnInvestmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnInvestmentServiceApplication.class, args);
	}

}
