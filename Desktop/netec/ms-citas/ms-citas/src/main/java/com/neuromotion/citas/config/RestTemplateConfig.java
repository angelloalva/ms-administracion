package com.neuromotion.citas.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configurar timeouts con SimpleClientHttpRequestFactory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos
        factory.setReadTimeout(10000);   // 10 segundos
        
        restTemplate.setRequestFactory(factory);
        
        // Agregar interceptores para logging
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        
        return restTemplate;
    }
    
    // Interceptor para logging
    public static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
        
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, 
                byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {
            
            log.info("Request: {} {}", request.getMethod(), request.getURI());
            
            long startTime = System.currentTimeMillis();
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Response: {} {} ({}ms)", 
                response.getStatusCode(), 
                request.getURI(), 
                duration);
            
            return response;
        }
    }
}