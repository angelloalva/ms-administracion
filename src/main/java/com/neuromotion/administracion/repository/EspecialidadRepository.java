package com.neuromotion.administracion.repository;


import com.neuromotion.administracion.model.Especialidad;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface EspecialidadRepository extends MongoRepository<Especialidad, String> {

    boolean existsByNombreIgnoreCase(String nombre);

}