package com.proyectofs1.carrito.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

// Activa los clientes Feign del paquete "client".
// Va en una clase aparte (y no en CarritoApplication) para que las pruebas
// @WebMvcTest no intenten crear el cliente Feign real.
@Configuration
@EnableFeignClients(basePackages = "com.proyectofs1.carrito.client")
public class FeignConfig {
}