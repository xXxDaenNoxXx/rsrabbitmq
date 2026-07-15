package cl.duoc.cdy2204.msrabbitmq.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;

import cl.duoc.cdy2204.msrabbitmq.config.RabbitMQConfig;
import cl.duoc.cdy2204.msrabbitmq.dto.GuiaDespachoDTO;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * MODIFICADO
 *
 * Consumidor real de cola1 (MAIN_QUEUE). El caso solo usa S3 como almacenamiento
 * (no hay base de datos Oracle), asi que en vez de guardar en una tabla, cada
 * guia consumida se sube como un archivo JSON a S3, bajo el prefijo
 * "guias-procesadas/", que es la "carpeta distinta a la usada en las sumativas
 * anteriores" (donde las guias originales se suben con su propio key).
 *
 * Si algo falla al procesar/guardar, se hace basicNack sin requeue, y gracias a
 * la configuracion de dead-letter-exchange definida en RabbitMQConfig, RabbitMQ
 * reenvia automaticamente ese mensaje a cola2 (errorQueue). No hace falta
 * reenviarlo a mano.
 */
@Component
public class GuiaDespachoListener {

	private final MessageConverter messageConverter;
	private final S3Client s3Client;
	private final ObjectMapper objectMapper;

	@Value("${s3.bucket.guias-procesadas}")
	private String bucketGuiasProcesadas;

	public GuiaDespachoListener(MessageConverter messageConverter, S3Client s3Client) {
		this.messageConverter = messageConverter;
		this.s3Client = s3Client;
		this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	@RabbitListener(id = "listener-guias-cola1", queues = RabbitMQConfig.MAIN_QUEUE, ackMode = "MANUAL")
	public void procesarGuia(Message mensaje, Channel canal) throws IOException {

		long deliveryTag = mensaje.getMessageProperties().getDeliveryTag();

		try {
			GuiaDespachoDTO guia = (GuiaDespachoDTO) messageConverter.fromMessage(mensaje);

			// Registro que se deja en S3, distinto al archivo original de la guia
			GuiaProcesadaRegistro registro = new GuiaProcesadaRegistro(guia, LocalDateTime.now());

			String key = "guias-procesadas/" + guia.getIdGuia() + ".json";
			String bucketDestino = (guia.getBucket() != null) ? guia.getBucket() : bucketGuiasProcesadas;

			byte[] json = objectMapper.writeValueAsBytes(registro);

			s3Client.putObject(
					PutObjectRequest.builder()
							.bucket(bucketDestino)
							.key(key)
							.contentType("application/json")
							.build(),
					RequestBody.fromBytes(json));

			canal.basicAck(deliveryTag, false);
			System.out.println("Guia " + guia.getIdGuia() + " guardada en s3://" + bucketDestino + "/" + key);

		} catch (Exception e) {
			System.out.println("Error al procesar guia de cola1, se envia a cola2 (DLQ): " + e.getMessage());
			canal.basicNack(deliveryTag, false, false); // false=false => va a la DLQ (cola2), no se reencola
		}
	}

	/**
	 * Pequeño record interno solo para darle forma al JSON que se guarda en S3.
	 */
	private record GuiaProcesadaRegistro(GuiaDespachoDTO guia, LocalDateTime fechaProcesado) {
	}
}
