package cl.duoc.cdy2204.msrabbitmq.service;

public interface ProducirMensajeService {

	void enviarMensaje(String mensaje);

	public void enviarObjeto(Object objeto);

	// AGREGADO: publica directamente en cola2 (errorQueue) cuando el productor
	// (ms-administracion-archivos) no pudo siquiera generar/enviar la guia a cola1
	void enviarACola2(Object objeto);
}
