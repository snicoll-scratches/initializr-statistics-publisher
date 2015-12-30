package io.spring.initializr.statistics.elastic

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.metadata.InitializrMetadata
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.statistics.ProjectGenerationStatistics
import io.spring.initializr.test.InitializrMetadataTestBuilder
import org.junit.Test

/**
 *
 * @author Stephane Nicoll
 */
class ProjectRequestDocumentSerializerTest {

	@Test
	void simpleTest() {
		def metadata = InitializrMetadataTestBuilder
				.withDefaults()
				.addDependencyGroup('core', 'web', 'data-jpa', 'security')
				.addDependencyGroup('database', 'h2')
				.build()

		def provider = new InitializrMetadataProvider() {
			@Override
			InitializrMetadata get() {
				return metadata
			}
		}

		ProjectRequestDocumentSerializer publisher = new ProjectRequestDocumentSerializer(provider)

		ProjectRequest r = new ProjectRequest()
		r.initialize(metadata)
		r.groupId = 'org.foo'
		r.artifactId = 'my-project'
		r.packageName = 'org.foo.project'
		r.bootVersion = '1.3.1.RELEASE'
		r.type = 'gradle-project'
		r.packaging = 'jar'
		r.javaVersion = '1.8'
		r.language = 'java'
		r.style << 'web' << 'data-jpa' << 'h2' << 'security'

		r.resolve(metadata)

		println publisher.toJson(new ProjectGenerationStatistics(1L, r))
	}
}
