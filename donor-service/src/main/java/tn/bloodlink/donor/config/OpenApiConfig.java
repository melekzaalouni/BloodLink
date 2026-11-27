package tn.bloodlink.donor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI donorOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BloodLink donor API")
                .version("v1")
                .description("Donors, appointments, urgent needs and blood requests"));
    }
}