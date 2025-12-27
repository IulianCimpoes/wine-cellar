package com.example.winecellar.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wineCellarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WineCellar API")
                        .version("v1")
                        .description("Wine and Winery management API"));
    }
}
