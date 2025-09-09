package com.challenge.config;

import com.challenge.config.properties.AsyncProperties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono
 *
 * Configura:
 * - Thread Pool personalizado para tareas asíncronas
 * - Manejo de rechazo de tareas
 * - Métricas y logging de tasks
 */
@Configuration
@Slf4j
@EnableAsync
@EnableConfigurationProperties(AsyncProperties.class)
public class AsyncConfig {

    private final AsyncProperties asyncProperties;

    public AsyncConfig(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    /**
     * Executor personalizado para procesamiento asíncrono
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.corePoolSize());
        executor.setMaxPoolSize(asyncProperties.maxPoolSize());
        executor.setQueueCapacity(asyncProperties.queueCapacity());
        executor.setThreadNamePrefix("AsyncTask-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            // Log del rechazo para monitoreo y alertas
            log.error("Task rejected - Queue full or executor shutdown: {}", r.toString());
            // Aquí se podría enviar métricas o alertas
        });
        executor.initialize();

        log.info("TaskExecutor configurado - Core: {}, Max: {}, Queue: {}",
                asyncProperties.corePoolSize(),
                asyncProperties.maxPoolSize(),
                asyncProperties.queueCapacity());

        return executor;
    }
}
