package cl.duoc.cdy2204.msrabbitmq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cdy2204.msrabbitmq.dto.GuiaDespachoDTO;
import cl.duoc.cdy2204.msrabbitmq.service.ProducirMensajeService;

/**
 * AGREGADO
 *
 * Este es el punto de entrada que consume ms-administracion-archivos por HTTP
 * cada vez que se genera/sube una guia de despacho. Este microservicio es el
 * unico que habla directamente con RabbitMQ.
 */
@RestController
@RequestMapping("/api/guias")
public class GuiaDespachoController {

	private final ProducirMensajeService producirMensajeService;

	public GuiaDespachoController(ProducirMensajeService producirMensajeService) {
		this.producirMensajeService = producirMensajeService;
	}

	/**
	 * Camino feliz: la guia se generó correctamente -> se publica en cola1.
	 * Si la consumida (GuiaDespachoListener) falla al guardarla en Oracle,
	 * el propio RabbitMQ la reenvía automáticamente a cola2 (Dead Letter Exchange).
	 */
	@PostMapping
	public ResponseEntity<String> recibirGuia(@RequestBody GuiaDespachoDTO guia) {

		guia.setEstado("GENERADA");
		producirMensajeService.enviarObjeto(guia);
		return ResponseEntity.ok("Guia enviada a cola1: " + guia.getIdGuia());
	}

	/**
	 * Camino de error: ms-administracion-archivos no pudo ni siquiera completar
	 * la generacion/subida de la guia (ej. fallo S3, fallo de validacion) y la
	 * envia directo a cola2, tal como pide el caso.
	 */
	@PostMapping("/error")
	public ResponseEntity<String> recibirGuiaConError(@RequestBody GuiaDespachoDTO guia) {

		guia.setEstado("CON_ERROR");
		producirMensajeService.enviarACola2(guia);
		return ResponseEntity.ok("Guia enviada a cola2 (errores): " + guia.getIdGuia());
	}
}
