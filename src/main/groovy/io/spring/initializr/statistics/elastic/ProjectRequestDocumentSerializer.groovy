package io.spring.initializr.statistics.elastic

import com.fasterxml.jackson.databind.ObjectMapper
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.statistics.ProjectGenerationStatistics

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 *
 * @author Stephane Nicoll
 */
@Service
class ProjectRequestDocumentSerializer {

	private final InitializrMetadataProvider metadataProvider;
	private final ObjectMapper objectMapper;

	@Autowired
	ProjectRequestDocumentSerializer(InitializrMetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider
		this.objectMapper = createObjectMapper()
	}

	public String toJson(ProjectGenerationStatistics stats) {
		this.objectMapper.writeValueAsString(toProjectRequestDocument(stats))
	}

	ProjectRequestDocument toProjectRequestDocument(ProjectGenerationStatistics stats) {
		ProjectRequestDocument document = new ProjectRequestDocument()
		document.generationTimestamp = stats.generationTimestamp
		def request = stats.request

		// Basic info
		document.groupId = request.groupId
		document.artifactId = request.artifactId
		document.packageName = request.packageName

		// TODO: remove junk
		document.bootVersion = request.bootVersion
		document.javaVersion = request.javaVersion
		document.language = request.language
		document.packaging = request.packaging
		document.type = request.type

		request.resolvedDependencies.each {
			document.dependencies << it.id
		}

		document
	}

	private static ObjectMapper createObjectMapper() {
		new ObjectMapper()
	}

}
