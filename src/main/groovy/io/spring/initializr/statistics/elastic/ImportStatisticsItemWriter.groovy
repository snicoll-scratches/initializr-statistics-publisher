package io.spring.initializr.statistics.elastic

import groovy.util.logging.Slf4j
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Bulk
import io.searchbox.core.Index

import org.springframework.batch.item.ItemWriter

/**
 * Post {@link ProjectRequestDocument} to an elastic search instance
 *
 * @author Stephane Nicoll
 */
@Slf4j
class ImportStatisticsItemWriter implements ItemWriter<ProjectRequestDocument> {

	private final ImportStatisticsJobProperties.Job properties
	private final JestClient jestClient

	ImportStatisticsItemWriter(ImportStatisticsJobProperties properties) {
		this.properties = properties.job
		this.jestClient = createJestClient(this.properties.url,
				this.properties.username, this.properties.password)
	}

	@Override
	void write(List<? extends ProjectRequestDocument> list) throws Exception {
		if (list.isEmpty()) {
			log.debug("No document to process")
			return
		}

		log.debug("About to inject " + list.size() + " document(s)")
		Bulk.Builder bulk = new Bulk.Builder()
				.defaultIndex(properties.indexName)
				.defaultType(properties.entityName)
		list.each {
			bulk.addAction(new Index.Builder(it).build())
		}
		def result = jestClient.execute(bulk.build())
		if (result.failedItems) {
			log.error("Some items could not be imported ----> $result.failedItems")
		} else {
			log.info("Successfully imported " + result.items.size() + " document(s)")
		}
	}

	private static JestClient createJestClient(String url, String username, String password) {
		JestClientFactory factory = new JestClientFactory();
		def builder = new HttpClientConfig.Builder(url).multiThreaded(true)
		if (username) {
			builder.defaultCredentials(username, password)
		}
		factory.setHttpClientConfig(builder.build());
		factory.getObject();
	}

}
