package io.spring.initializr.statistics.elastic

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import groovy.util.logging.Slf4j
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.statistics.DefaultValueResolver

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

	static final String GET_STARTER = 'GET /starter.zip'
	static final String GET_POM = 'GET /pom.xml'
	static final String GET_GRADLE_BUILD = 'GET /build.gradle'
	static final String POST_STARTER_TGZ = 'POST /starter.tgz'

	private final DefaultValueResolver valueResolver
	private final InitializrMetadataProvider metadataProvider

	@Autowired
	ImportStatisticsItemProcessor(DefaultValueResolver defaultValueResolver,
								  InitializrMetadataProvider metadataProvider) {
		this.valueResolver = defaultValueResolver
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
		def metadata = metadataProvider.get()
		ProjectRequestDocument document = new ProjectRequestDocument()


		LocalDateTime timestamp = LocalDateTime.parse(
				logEntry.timestamp, DateTimeFormatter.ISO_DATE_TIME)
		document.generationTimestamp = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli()
		def builder = UriComponentsBuilder.fromUriString(url).build()
		def params = builder.getQueryParams()

		document.groupId = params.getFirst('groupId') ?: valueResolver.getDefaultGroupId(timestamp)
		document.artifactId = params.getFirst('artifactId') ?: valueResolver.getDefaultArtifactId(timestamp)
		document.packageName = params.getFirst('packageName') ?: valueResolver.getDefaultPackageName(timestamp)
		document.bootVersion = params.getFirst('bootVersion') ?: valueResolver.getDefaultBootVersion(timestamp)

		def javaVersion = params.getFirst('javaVersion')
		if (javaVersion && !metadata.javaVersions.get(javaVersion)) {
			log.warn("Invalid java version '$javaVersion' from $logEntry.entry")
		}
		document.javaVersion = javaVersion ?: valueResolver.getDefaultJavaVersion(timestamp)

		def language = params.getFirst('language')
		if (language && !metadata.languages.get(language)) {
			log.warn("Invalid language '$language' from $logEntry.entry")
		}
		document.language = language ?: valueResolver.getDefaultLanguage(timestamp)

		def packaging = params.getFirst('packaging')
		if (packaging && !metadata.packagings.get(packaging)) {
			log.warn("Invalid packaging '$packaging' from $logEntry.entry")
		}
		document.packaging = packaging ?: valueResolver.getDefaultPackaging(timestamp)

		def type = params.getFirst('type')
		if (type && !metadata.types.get(type)) {
			log.warn("Invalid type '$type' from $logEntry.entry")
		}
		document.type = type ?: valueResolver.getDefaultType(timestamp)


		def dependencies = []
		dependencies.addAll(cleanDependenciesParam(params.get('style')))
		dependencies.addAll(cleanDependenciesParam(params.get('dependencies')))
		dependencies.each {
			if (metadata.dependencies.get(it)) {
				document.dependencies << it
			} else {
				log.warn("Unknown dependency '$it' from $logEntry.entry")
			}
		}
		document
	}

	private static Collection<String> cleanDependenciesParam(List<String> value) {
		List<String> result = []
		if (value) {
			value.forEach {
				def dep = URLDecoder.decode(it, 'UTF-8')
				result.addAll(dep.split(','))
			}
		}
		result.unique()
	}

	private static String extractUrl(String entry) {
		if (entry.contains(GET_STARTER)) {
			return extractUrl(entry, GET_STARTER)
		}
		if (entry.contains(GET_POM)) {
			return extractUrl(entry, GET_POM)
		}
		if (entry.contains(GET_GRADLE_BUILD)) {
			return extractUrl(entry, GET_GRADLE_BUILD)
		}
		if (entry.contains(POST_STARTER_TGZ)) {
			return extractUrl(entry, POST_STARTER_TGZ)
		}
		return null
	}

	private static String extractUrl(String entry, String prefix) {
		int index = entry.indexOf(prefix)
		String tmp = entry.substring(index + 4, entry.size())
		return cleanUrlSuffix(tmp)
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
