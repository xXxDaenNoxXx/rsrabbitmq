package cl.duoc.cdy2204.msrabbitmq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AGREGADO
 *
 * Mismo patron que AwsS3Config en ms-administracion-archivos. Este bean lo usa
 * GuiaDespachoListener para dejar en S3 el registro de cada guia consumida
 * desde cola1 (ya no se usa Oracle/JPA, el caso solo trabaja con S3).
 */
@Configuration
public class AwsS3Config {

	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
				.region(Region.US_EAST_1)
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
