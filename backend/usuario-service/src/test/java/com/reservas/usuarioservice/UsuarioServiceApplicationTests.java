package com.reservas.usuarioservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requiere PostgreSQL; se reemplaza por tests de integración con Testcontainers")
class UsuarioServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}