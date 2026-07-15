package cl.duoc.cdy2204.msrabbitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * AGREGADO / MODIFICADO
 *
 * Mismo patron de seguridad usado en ms-administracion-archivos: valida el JWT
 * emitido por Azure AD B2C para el resto de endpoints. Los endpoints bajo
 * /api/guias/** (GuiaDespachoController) quedan con permitAll(): son llamados
 * internamente por ms-administracion-archivos via RestTemplate SIN adjuntar
 * token (GuiaQueueClientService no reenvia Authorization).
 *
 * MODIFICADO: se deshabilita CSRF. Este microservicio no sirve un frontend con
 * sesion de navegador, solo expone una API REST consumida server-to-server
 * (RestTemplate). El CsrfFilter corre ANTES que la evaluacion de
 * authorizeHttpRequests, asi que aunque /api/guias/** este en permitAll(),
 * sin esto igual rechazaba todo POST/PUT/DELETE con 401 (Spring redirige los
 * fallos de CSRF de un usuario anonimo al AuthenticationEntryPoint en vez del
 * AccessDeniedHandler), impidiendo que cualquier guia llegara a cola1/cola2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/guias/**").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}
