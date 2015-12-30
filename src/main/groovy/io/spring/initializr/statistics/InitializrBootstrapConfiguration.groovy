package io.spring.initializr.statistics

import io.spring.initializr.metadata.InitializrMetadata
import io.spring.initializr.metadata.InitializrMetadataBuilder
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.support.DefaultInitializrMetadataProvider

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.UrlResource

/**
 *
 * @author Stephane Nicoll
 */
@Configuration
class InitializrBootstrapConfiguration {

	@Bean
	InitializrMetadataProvider initializrMetadataProvider() {
		InitializrMetadata metadata = InitializrMetadataBuilder.create()
				.withInitializrMetadata(new UrlResource('http://start.spring.io/metadata/config'))
				.build()
		new DefaultInitializrMetadataProvider(metadata)
	}

}
