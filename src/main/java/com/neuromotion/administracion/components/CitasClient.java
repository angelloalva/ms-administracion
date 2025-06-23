package com.neuromotion.administracion.components;

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

import com.neuromotion.administracion.dto.CitaResponse;
import com.neuromotion.administracion.exceptions.ServiceCommunicationException;
import com.neuromotion.administracion.model.Especialidad;
import com.neuromotion.administracion.model.Sede;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CitasClient {
    
    private final RestTemplate restTemplate;
    private final String adminServiceUrl;
    
    public CitasClient(RestTemplate restTemplate,
                              @Value("${microservices.citas.url}") String adminServiceUrl) {
        this.restTemplate = restTemplate;
        this.adminServiceUrl = adminServiceUrl;
    }
    
   

     public List<CitaResponse> getCitas(String pacienteId,String authorizationHeader) {
        try {
            String url = adminServiceUrl + "/api/citas/doctor/" + pacienteId;
            
              // Log the URL being called
            log.info("Calling administration service: {}", url);
              HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader); // Propagar el token
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<CitaResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,  CitaResponse[].class);
            
              if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling administration service for citas {}: {}",e.getMessage());
            throw new ServiceCommunicationException("Error retrieving citas information", e);
        }
    }
}
