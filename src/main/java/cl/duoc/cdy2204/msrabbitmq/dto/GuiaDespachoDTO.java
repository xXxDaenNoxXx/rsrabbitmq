package cl.duoc.cdy2204.msrabbitmq.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AGREGADO
 *
 * Representa la guia de despacho que viaja como mensaje entre
 * ms-administracion-archivos (productor) y msrabbitmq (consumidor).
 *
 * Implementa Serializable porque Jackson2JsonMessageConverter serializa/deserializa
 * este objeto como JSON al pasar por RabbitMQ.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuiaDespachoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String idGuia;
	private String transportista;
	private LocalDate fechaDespacho;
	private String bucket;
	private String key;
	private Long tamanioBytes;
	private String estado; // ej: GENERADA, CON_ERROR
}
