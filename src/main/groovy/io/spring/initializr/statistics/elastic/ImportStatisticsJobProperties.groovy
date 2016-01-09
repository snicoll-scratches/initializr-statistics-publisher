package io.spring.initializr.statistics.elastic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource

/**
 *
 * @author Stephane Nicoll
 */
@ConfigurationProperties("initializr.statistics.elastic")
class ImportStatisticsJobProperties {

	final Job job = new Job()

	static class Job {

		String entityUrl = 'http://localhost:9200/initializr/request'

		Resource[] input = []

	}


}
