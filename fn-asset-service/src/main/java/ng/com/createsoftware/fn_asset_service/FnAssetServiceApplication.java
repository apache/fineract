package ng.com.createsoftware.fn_asset_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ng.com.createsoftware.fn_asset_service.client")
public class FnAssetServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FnAssetServiceApplication.class, args);
	}

}
