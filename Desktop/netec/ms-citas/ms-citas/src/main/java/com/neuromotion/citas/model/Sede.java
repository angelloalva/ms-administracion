package com.neuromotion.citas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "sedes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    private String id;

    private String nombre;
    private String direccion;
}
