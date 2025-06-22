package com.neuromotion.citas.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.neuromotion.citas.dto.DoctorResponse;
import com.neuromotion.citas.dto.UsuarioResponse;
import com.neuromotion.citas.exceptions.ServiceCommunicationException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthenticationClient {
    
    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    
    public AuthenticationClient(RestTemplate restTemplate, 
                              @Value("${microservices.administration.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }
    


    public List<UsuarioResponse> getAllUsers(String authorizationHeader) {
        try {
            String url = authServiceUrl + "/api/usuarios" ;
            log.info("Calling authentication service: {}", url);
            // Configurar headers con el token del cliente
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader); // Propagar el token
            HttpEntity<String> entity = new HttpEntity<>(headers);
            // Hacer la solicitud
            ResponseEntity<UsuarioResponse[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, UsuarioResponse[].class
            );            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling authentication service for doctors  {}: {}", e.getMessage());
            throw new ServiceCommunicationException("Error retrieving doctors ", e);
        }
    }
        public List<DoctorResponse> getAllDoctors(String authorizationHeader) {
        try {
            String url = authServiceUrl + "/api/doctores" ;
            log.info("Calling authentication service: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<DoctorResponse[]> response = restTemplate.
            exchange(url,HttpMethod.GET, entity, DoctorResponse[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling authentication service for doctors  {}: {}", e.getMessage());
            throw new ServiceCommunicationException("Error retrieving doctors ", e);
        }
    }
}
