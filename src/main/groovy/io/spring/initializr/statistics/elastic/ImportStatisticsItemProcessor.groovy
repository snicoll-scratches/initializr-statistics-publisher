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

	static final String GET_STARTER = "GET /starter.zip"
	static final String POST_STARTER_TGZ = 'POST /starter.tgz'

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
		document.bootVersion = params.getFirst('bootVersion') ?: metadata.bootVersions.getDefault().id
		document.javaVersion = params.getFirst('javaVersion') ?: metadata.javaVersions.getDefault().id
		document.language = params.getFirst('language') ?: metadata.languages.getDefault().id
		document.packaging = params.getFirst('packaging') ?: metadata.packagings.getDefault().id
		document.type = params.getFirst('type') ?: metadata.types.getDefault().id

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
			return cleanUrlSuffix(tmp)
		}
		if (entry.contains(POST_STARTER_TGZ)) {
			int index = entry.indexOf(POST_STARTER_TGZ)
			String tmp = entry.substring(index + 4, entry.size())
			return cleanUrlSuffix(tmp)
		}
		return null
	}

	private static String cleanUrlSuffix(String url) {
		int closingIndex = url.indexOf("\"")
		url = url.substring(0, closingIndex)
		// TODO
		if (url.endsWith(' HTTP/1.1')) {
			url = url.substring(0, url.length() - ' HTTP/1.1'.length())
		}
		return url
	}
}
