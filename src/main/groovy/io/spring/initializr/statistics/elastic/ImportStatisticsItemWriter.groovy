package io.spring.initializr.statistics.elastic

import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 *
 * @author Stephane Nicoll
 */
@Component
class ImportStatisticsItemWriter implements ItemWriter<ProjectRequestDocument> {

	private final ProjectRequestDocumentSerializer serializer
	private final RestTemplate restTemplate

	@Autowired
	ImportStatisticsItemWriter(ProjectRequestDocumentSerializer serializer) {
		this.serializer = serializer
		this.restTemplate = new RestTemplate()
	}

	@Override
	void write(List<? extends ProjectRequestDocument> list) throws Exception {
		list.each {
			String json = serializer.toJson(it)
			restTemplate.postForObject('http://localhost:9200/intializr/request', json, String)
		}

	}
}
