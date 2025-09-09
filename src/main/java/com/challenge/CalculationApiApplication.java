package com.challenge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicación principal del Challenge Backend API REST
 *
 * Características implementadas:
 * - Cálculo con porcentaje dinámico
 * - Sistema de caché con expiración
 * - Historial de llamadas
 * - Documentación con Swagger
 * - Monitoreo con Actuator
 *
 * @author Angel C.
 * @since 2025
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class CalculationApiApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CalculationApiApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("First Init SpringBoot");
    }
}
