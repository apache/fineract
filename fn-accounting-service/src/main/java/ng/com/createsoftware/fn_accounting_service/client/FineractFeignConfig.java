package ng.com.createsoftware.fn_accounting_service.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class FineractFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return template -> {
            template.header("Fineract-Platform-TenantId", "default");
            template.header("Authorization",
                    "Basic " + Base64.getEncoder()
                            .encodeToString("mifos:password".getBytes()));
            template.header("Content-Type", "application/json");
        };
    }
}

