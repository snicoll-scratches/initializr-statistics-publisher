package io.spring.initializr.statistics.elastic

import io.spring.initializr.metadata.InitializrMetadata
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.test.InitializrMetadataTestBuilder

/**
 *
 * @author Stephane Nicoll
 */
abstract class AbstractElasticTest {

	def metadata = InitializrMetadataTestBuilder
			.withDefaults()
			.addDependencyGroup('core', 'security')
			.addDependencyGroup('web', 'web', 'data-rest')
			.addDependencyGroup('data', 'data-jpa')
			.addDependencyGroup('database', 'h2')
			.build()

	def provider = new InitializrMetadataProvider() {
		@Override
		InitializrMetadata get() {
			return metadata
		}
	}


}
