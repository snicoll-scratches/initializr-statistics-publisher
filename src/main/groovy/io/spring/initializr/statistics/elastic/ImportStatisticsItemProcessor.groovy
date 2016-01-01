package io.spring.initializr.statistics.elastic

import java.text.SimpleDateFormat

import groovy.util.logging.Slf4j
import io.spring.initializr.metadata.InitializrMetadataProvider

import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 *
 * @author Stephane Nicoll
 */
@Component
@Slf4j
class ImportStatisticsItemProcessor implements ItemProcessor<LogEntry, ProjectRequestDocument> {

	public static final String GET_STARTER = "GET /starter.zip"
	private final InitializrMetadataProvider metadataProvider

	@Autowired
	ImportStatisticsItemProcessor(InitializrMetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider
	}

	@Override
	ProjectRequestDocument process(LogEntry logEntry) throws Exception {
		if (logEntry.applicationName.equals('start.spring.io')) {
			// Check if that's a project request
			def entry = logEntry.entry
			String url = extractUrl(entry)
			if (url) {
				try {
					return processEntry(logEntry, url)
				} catch (Exception ex) {
					throw new IllegalArgumentException("Failed to process entry with url $url", ex)
				}
			} else {
				if (log.isTraceEnabled()) {
					log.trace("No project request url found in $entry")
				}
			}
		}
		return null
	}

	private ProjectRequestDocument processEntry(LogEntry logEntry, String url) {
		ProjectRequestDocument document = new ProjectRequestDocument()
		applyTimestamp(document, logEntry.timestamp)
		applyProjectSettings(document, url)
		return document
	}

	private void applyTimestamp(ProjectRequestDocument document, String timestamp) {
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(timestamp);
		document.generationTimestamp = date.getTime()
	}

	private void applyProjectSettings(ProjectRequestDocument document, String url) {
		def metadata = metadataProvider.get()

		def builder = UriComponentsBuilder.fromUriString(url).build()
		def params = builder.getQueryParams()
		document.groupId = params.getFirst('groupId') ?: metadata.groupId.content
		document.artifactId = params.getFirst('artifactId') ?: metadata.artifactId.content
		document.packageName = params.getFirst('packageName') ?: metadata.packageName.content
		document.bootVersion = params.getFirst('bootVersion') ?: metadata.bootVersions.getDefault()
		document.javaVersion = params.getFirst('javaVersion') ?: metadata.javaVersions.getDefault()
		document.language = params.getFirst('language') ?: metadata.languages.getDefault()
		document.packaging = params.getFirst('packaging') ?: metadata.packagings.getDefault()
		document.type = params.getFirst('type') ?: metadata.types.getDefault()

		def dependencies = []
		dependencies.addAll(params.get('style') ?: [])
		dependencies.addAll(params.get('dependencies') ?: [])
		dependencies.each {
			if (metadata.dependencies.get(it)) {
				document.dependencies << it
			}
		}

	}

	private static String extractUrl(String entry) {
		if (entry.contains(GET_STARTER)) {
			int index = entry.indexOf(GET_STARTER)
			String tmp = entry.substring(index + 4, entry.size())
			int closingIndex = tmp.indexOf("\"")
			tmp = tmp.substring(0, closingIndex)
			// TODO
			if (tmp.endsWith(' HTTP/1.1')) {
				tmp = tmp.substring(0, tmp.length() - ' HTTP/1.1'.length())
			}
			return tmp
		}
		return null
	}
}
