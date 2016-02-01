package io.spring.initializr.statistics.elastic

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.spring.initializr.metadata.InitializrMetadataProvider

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

	public String toJson(ProjectRequestDocument stats) {
		this.objectMapper.writeValueAsString(stats)
	}

	private static ObjectMapper createObjectMapper() {
		def mapper = new ObjectMapper()
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper
	}

}
