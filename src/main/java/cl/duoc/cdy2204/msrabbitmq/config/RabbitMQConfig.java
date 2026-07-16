package cl.duoc.cdy2204.msrabbitmq.config;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.cdy2204.msrabbitmq.dto.GuiaDespachoDTO;

/**
 * MODIFICADO
 *
 * msrabbitmq pasa a ser EXCLUSIVAMENTE el consumidor AMQP (requisito de la
 * rubrica: un micro productor, otro consumidor). Ya no publica mensajes de
 * (ver GuiaDespachoListener).
 */
@Configuration
public class RabbitMQConfig {

	public static final String MAIN_QUEUE = "myQueue";
	public static final String DLX_QUEUE = "errorQueue";

	public static final String MAIN_EXCHANGE = "myExchange";
	public static final String DLX_EXCHANGE = "errorExchange";

	public static final String DLX_ROUTING_KEY = "errorRouting-key";

	@Value("${spring.rabbitmq.host}")
	private String host;

	@Value("${spring.rabbitmq.port}")
	private int port;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	@Bean
	Jackson2JsonMessageConverter messageConverter() {

		// MODIFICADO: antes usaba el nombre completo de la clase Java (por
		// defecto) para saber que tipo deserializar via el header __TypeId__.
		// Como el productor (ms-administracion-archivos) vive en un paquete
		// Java distinto y no comparte esa clase, se usa un alias comun
		// ("guiaDespacho") que ambos proyectos mapean a su propio DTO local.
		Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

		DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
		typeMapper.setTrustedPackages("*");
		typeMapper.setIdClassMapping(Map.of("guiaDespacho", GuiaDespachoDTO.class));
		converter.setJavaTypeMapper(typeMapper);

		return converter;
	}

	@Bean
	CachingConnectionFactory connectionFactory() {

		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}

	@Bean
	Queue myQueue() {

		return new Queue(MAIN_QUEUE, true, false, false,
				Map.of("x-dead-letter-exchange", DLX_EXCHANGE, "x-dead-letter-routing-key", DLX_ROUTING_KEY));
	}

	@Bean
	Queue dlxQueue() {

		return new Queue(DLX_QUEUE);
	}

	@Bean
	DirectExchange myExchange() {

		return new DirectExchange(MAIN_EXCHANGE);
	}

	@Bean
	DirectExchange dlxExchange() {

		return new DirectExchange(DLX_EXCHANGE);
	}

	@Bean
	Binding binding(Queue myQueue, DirectExchange myExchange) {

		return BindingBuilder.bind(myQueue).to(myExchange).with("");
	}

	@Bean
	Binding dlxBinding() {

		return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
	}
}
