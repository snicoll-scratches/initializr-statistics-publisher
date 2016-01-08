package io.spring.initializr.statistics.elastic

import groovy.util.logging.Slf4j

import org.springframework.batch.core.ItemProcessListener

/**
 * Log URLs that were not processed.
 *
 * @author Stephane Nicoll
 */
@Slf4j
class RejectedEntryListener implements ItemProcessListener<LogEntry, ProjectRequestDocument> {

	final List<String> knownUrls = ['GET / HTTP', 'GET /dependencies', 'GET /ui/dependencies',
									'GET /spring.zip', 'GET /install.sh',
									'GET /metadata/config', 'GET /metadata/client',
									'GET /favicon.ico', 'GET /robots.txt', 'GET /apple', 'GET /?utm',
									'GET /js', 'GET /img', 'GET /fonts', 'GET /css',
									'GET /sts',
									'GET /info', 'GET /health', 'GET /metrics', 'GET /autoconfig',
									'GET /dump', 'GET /mappings', 'GET /trace', 'GET /beans', 'GET /env',
									'GET /configprops', 'GET /error']

	@Override
	void beforeProcess(LogEntry item) {
	}

	@Override
	void afterProcess(LogEntry item, ProjectRequestDocument result) {
		if (result != null) {
			return
		}
		if (!item.applicationName.equals('start.spring.io')) {
			return
		}
		String url = extractUrl(item.entry)
		if (url) {
			if (isKnown(url)) {
				log.trace("Ignoring $url")
			} else if (item.entry.contains('HTTP/1.1" 404')) {
				log.trace("Ignoring 404 on $url")
			} else {
				log.info("Rejected url $url from $item.entry")
			}
		}
	}

	private boolean isKnown(String url) {
		for (String it : knownUrls) {
			if (url.startsWith(it)) {
				return true
			}
		}
		return false
	}

	private static String extractUrl(String entry) {
		int i = entry.indexOf('GET ')
		if (i != -1) {
			String tmp = entry.substring(i, entry.size())
			int j = tmp.indexOf('"')
			return tmp.substring(0, (j != -1 ? j : tmp.size()))
		}
		i = entry.indexOf('POST ')
		if (i != -1) {
			String tmp = entry.substring(i, entry.size())
			int j = tmp.indexOf('"')
			return tmp.substring(0, (j != -1 ? j : tmp.size()))
		}
		return null
	}

	@Override
	void onProcessError(LogEntry item, Exception e) {

	}

}
