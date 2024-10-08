package com.springcloud.msvc.items;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class AppConfig {

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> customizerCircuitBreaker(){
        return (factory) -> factory.configureDefault( id -> {
            return new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(
                CircuitBreakerConfig.custom()
                .slidingWindowSize(10)  /*  Numero de muestras antes de tomar el total de llamadas al estado abierto */
                .failureRateThreshold(50)   /*  Porcentaje de llamadas antes de entrar a un estado Abierto de 0%-100% */
                .waitDurationInOpenState(Duration.ofSeconds(10L))   /*  Duracion en segundos del Estado Abierto para pasar despues a cerrado */
                .permittedNumberOfCallsInHalfOpenState(5)/* Numero de llamadas en un estado semi abierto */
                .slowCallDurationThreshold(Duration.ofSeconds(2L))  /*  Duracion de llamadas lentas debe ser menor al del timeout */
                .slowCallRateThreshold(50) /*   Porcentaje de llamadas lentas para entrar en circuito abierto 0%-100% */
                .build()
            ).timeLimiterConfig(
                TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5L))
                .build()
            ).build();
        });
    }
}
