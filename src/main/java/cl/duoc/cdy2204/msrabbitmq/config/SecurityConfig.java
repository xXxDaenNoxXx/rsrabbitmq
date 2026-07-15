package cl.duoc.cdy2204.msrabbitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * AGREGADO / MODIFICADO
 *
 * Mismo patron de seguridad usado en ms-administracion-archivos: valida el JWT
 * emitido por Azure AD B2C para el resto de endpoints. Los endpoints bajo
 * /api/guias/** (GuiaDespachoController) quedan con permitAll(): son llamados
 * internamente por ms-administracion-archivos via RestTemplate SIN adjuntar
 * token (GuiaQueueClientService no reenvia Authorization), y al exigir
 * authenticated() ahi, msrabbitmq rechazaba esas llamadas con 401,
 * impidiendo que cualquier guia llegara a cola1/cola2. Como msrabbitmq no se
 * expone directamente al exterior (solo a ms-administracion-archivos dentro
 * de la red interna de Docker), esto es aceptable para el alcance del caso.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/guias/**").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}
