package io.spring.initializr.statistics.elastic

import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 *
 * @author Stephane Nicoll
 */
@Component
class ImportStatisticsItemWriter implements ItemWriter<ProjectRequestDocument> {

	private final ProjectRequestDocumentSerializer serializer

	@Autowired
	ImportStatisticsItemWriter(ProjectRequestDocumentSerializer serializer) {
		this.serializer = serializer
	}

	@Override
	void write(List<? extends ProjectRequestDocument> list) throws Exception {

		list.each {
			println(it)
		}

	}
}
