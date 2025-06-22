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

import com.neuromotion.citas.exceptions.ServiceCommunicationException;
import com.neuromotion.citas.model.Especialidad;
import com.neuromotion.citas.model.Sede;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdministrationClient {
    
    private final RestTemplate restTemplate;
    private final String adminServiceUrl;
    
    public AdministrationClient(RestTemplate restTemplate,
                              @Value("${microservices.administration.url}") String adminServiceUrl) {
        this.restTemplate = restTemplate;
        this.adminServiceUrl = adminServiceUrl;
    }
    
    public List<Sede> getAllSedes(String authorizationHeader) {
        try {
            String url = adminServiceUrl + "/api/sedes";
            log.info("Calling administration service: {}", url);
              HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader); // Propagar el token
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Sede[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,  Sede[].class);
            
              if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling administration service for sede {}: {}",e.getMessage());
            throw new ServiceCommunicationException("Error retrieving sede information", e);
        }
    }

     public List<Especialidad> getAllEspecialidades(String authorizationHeader) {
        try {
            String url = adminServiceUrl + "/api/especialidades";
            log.info("Calling administration service: {}", url);
              HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader); // Propagar el token
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Especialidad[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,  Especialidad[].class);
            
              if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling administration service for sede {}: {}",e.getMessage());
            throw new ServiceCommunicationException("Error retrieving especialidad information", e);
        }
    }
}
