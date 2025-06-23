package com.neuromotion.administracion.repository;


import com.neuromotion.administracion.model.Sede;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SedeRepository extends MongoRepository<Sede, String> {
}