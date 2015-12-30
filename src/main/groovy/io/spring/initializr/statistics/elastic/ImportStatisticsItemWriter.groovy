package io.spring.initializr.statistics.elastic

import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

/**
 *
 * @author Stephane Nicoll
 */
@Component
class ImportStatisticsItemWriter implements ItemWriter<String> {

	@Override
	void write(List<? extends String> list) throws Exception {

		list.each {
			println(it)
		}

	}
}
