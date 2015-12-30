package io.spring.initializr.statistics.elastic

import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 *
 * @author Stephane Nicoll
 */
@Component
class ImportStatisticsItemProcessor implements ItemProcessor<LogEntry, String> {

	public static final String GET_STARTER = "GET /starter.zip"
	private final ProjectRequestDocumentSerializer serializer

	@Autowired
	ImportStatisticsItemProcessor(ProjectRequestDocumentSerializer serializer) {
		this.serializer = serializer
	}

	@Override
	String process(LogEntry logEntry) throws Exception {
		if (logEntry.applicationName.equals('start.spring.io')) {
			// Check if that's a project request
			def entry = logEntry.entry
			if (entry.contains(GET_STARTER)) {
				int index = entry.indexOf(GET_STARTER)
				String tmp = entry.substring(index + 4, entry.size())
				int closingIndex = tmp.indexOf("\"")
				String url = tmp.substring(0, closingIndex)
				return url
			}
		}
		return null

		//return this.serializer.toJson(stats)
	}
}
