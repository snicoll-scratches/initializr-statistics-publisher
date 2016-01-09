package io.spring.initializr.statistics.elastic

import org.springframework.batch.item.ItemWriter
import org.springframework.web.client.RestTemplate

/**
 * Post {@link ProjectRequestDocument} to an elastic search instance
 *
 * @author Stephane Nicoll
 */
class ImportStatisticsItemWriter implements ItemWriter<ProjectRequestDocument> {

	private final String entityUrl
	private final ProjectRequestDocumentSerializer serializer
	private final RestTemplate restTemplate

	ImportStatisticsItemWriter(ImportStatisticsJobProperties properties, ProjectRequestDocumentSerializer serializer) {
		this.entityUrl = properties.job.entityUrl
		this.serializer = serializer
		this.restTemplate = new RestTemplate()
	}

	@Override
	void write(List<? extends ProjectRequestDocument> list) throws Exception {
		list.each {
			String json = serializer.toJson(it)
			restTemplate.postForObject(this.entityUrl, json, String)
		}

	}
}
