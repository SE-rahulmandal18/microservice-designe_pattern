package com.example.jobms.job;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}

// we have created App Config class to make use of REST TEMPLATE
// and because we needed LoadBalanced version of REST TEMPLATE
// and that is because we were not able to work with SERVICE NAME

// LoadBalanced Annotation is not used with OPENFEIGN
// Clent side looadbalacing done automaticsally with OPENFeign