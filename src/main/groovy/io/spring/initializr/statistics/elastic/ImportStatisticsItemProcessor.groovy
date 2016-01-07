package io.spring.initializr.statistics.elastic

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.util.logging.Slf4j
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.statistics.DefaultValueResolver

import org.springframework.batch.item.ItemProcessor
import org.springframework.web.util.UriComponentsBuilder

/**
 *
 * @author Stephane Nicoll
 */
@Slf4j
class ImportStatisticsItemProcessor implements ItemProcessor<LogEntry, ProjectRequestDocument> {

	static final String X_FORWARDED_FOR = 'x_forwarded_for:"'
	static final IP_PATTERN = Pattern.compile("[0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*")

	static final List<String> KNOWN_PREFIXES = ['GET /starter.zip', 'POST /starter.zip',
												'GET /starter.tgz', 'POST /starter.tgz',
												'GET /pom.xml', 'POST /pom.xml',
												'GET /build.gradle', 'POST /build.gradle']

	private final DefaultValueResolver valueResolver
	private final InitializrMetadataProvider metadataProvider

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
					throw new IllegalArgumentException("Failed to process entry $logEntry.entry", ex)
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
		document.requestIp = extractRequestIp(logEntry.entry)
		def builder = UriComponentsBuilder.fromUriString(url).build()
		def params = builder.getQueryParams()

		document.groupId = params.getFirst('groupId') ?: valueResolver.getDefaultGroupId(timestamp)
		document.artifactId = params.getFirst('artifactId') ?: valueResolver.getDefaultArtifactId(timestamp)
		document.packageName = params.getFirst('packageName') ?: valueResolver.getDefaultPackageName(timestamp)
		document.bootVersion = params.getFirst('bootVersion') ?: valueResolver.getDefaultBootVersion(timestamp)

		def javaVersion = params.getFirst('javaVersion')
		if (javaVersion && !metadata.javaVersions.get(javaVersion)) {
			document.invalid = true
			document.invalidJavaVersion = true
			log.warn("Invalid java version '$javaVersion' from $logEntry.entry")
		}
		document.javaVersion = javaVersion ?: valueResolver.getDefaultJavaVersion(timestamp)

		def language = params.getFirst('language')
		if (language && !metadata.languages.get(language)) {
			document.invalid = true
			document.invalidLanguage = true
			log.warn("Invalid language '$language' from $logEntry.entry")
		}
		document.language = language ?: valueResolver.getDefaultLanguage(timestamp)

		def packaging = params.getFirst('packaging')
		if (packaging && !metadata.packagings.get(packaging)) {
			document.invalid = true
			document.invalidPackaging = true
			log.warn("Invalid packaging '$packaging' from $logEntry.entry")
		}
		document.packaging = packaging ?: valueResolver.getDefaultPackaging(timestamp)

		def type = params.getFirst('type')
		if (type && !metadata.types.get(type)) {
			document.invalid = true
			document.invalidType = true
			log.warn("Invalid type '$type' from $logEntry.entry")
		}
		document.type = type ?: valueResolver.getDefaultType(timestamp, url)


		def dependencies = []
		dependencies.addAll(cleanDependenciesParam(params.get('style')))
		dependencies.addAll(cleanDependenciesParam(params.get('dependencies')))
		dependencies.each {
			if (metadata.dependencies.get(it)) {
				document.dependencies << it
			} else {
				document.invalid = true
				document.invalidDependencies << it
				log.warn("Unknown dependency '$it' from $logEntry.entry")
			}
		}
		document
	}

	private static Collection<String> cleanDependenciesParam(List<String> value) {
		List<String> result = []
		if (value) {
			value.forEach {
				if (it) {
					def dep = URLDecoder.decode(it, 'UTF-8')
					result.addAll(dep.split(','))
				}
			}
		}
		result.unique()
	}

	private static String extractRequestIp(String entry) {
		int start = entry.indexOf(X_FORWARDED_FOR) + X_FORWARDED_FOR.length()
		int end = entry.indexOf('\"', start)
		// only matches IPv4 addresses
		String ips = entry.substring(start, end)
		Matcher matcher = IP_PATTERN.matcher(ips)
		if (matcher.find()) {
			return matcher.group()
		} else {
			log.warn("Could not parse IP string '$ips' from $entry, falling back to 127.0.0.1")
			return '127.0.0.1'
		}
	}

	private static String extractUrl(String entry) {
		for (String prefix : KNOWN_PREFIXES) {
			if (entry.contains(prefix)) {
				return extractUrl(entry, prefix)
			}
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
		return url.trim()
	}

}
