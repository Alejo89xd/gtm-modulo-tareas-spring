package co.edu.sena.gtm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del módulo Tareas del Sistema de Gestión de Tareas (GTM).
 * {@code @SpringBootApplication} activa la configuración automática: Spring
 * arranca un Tomcat embebido, se conecta a MySQL y registra los controladores.
 */
@SpringBootApplication
public class GtmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GtmApplication.class, args);
    }
}
