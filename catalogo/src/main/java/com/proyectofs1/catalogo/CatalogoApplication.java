package com.proyectofs1.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Excluimos el usuario en memoria que Spring Security crea por defecto
// ("Using generated security password"): aquí se usa el JWT que firma el MS Auth.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CatalogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoApplication.class, args);
	}

}
