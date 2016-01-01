package io.spring.initializr.statistics.elastic

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 *
 * @author Stephane Nicoll
 */
class ImportStatisticsItemProcessorTest extends AbstractElasticTest {

	private final ImportStatisticsItemProcessor processor = new ImportStatisticsItemProcessor(provider)

	@Test
	void parSimpleLog() {
		def document = processor.process(createSimpleLogEntry())

		def expected = 1451343621000
		assertEquals document.generationTimestamp, expected
		assertEquals document.groupId, 'com.example'
		assertEquals document.artifactId, 'demo'
		assertEquals document.packageName, 'com.example'
		assertEquals document.bootVersion, '1.3.1.RELEASE'
		assertEquals document.javaVersion, '1.8'
		assertEquals document.language, 'java'
		assertEquals document.packaging, 'jar'
		assertEquals document.type, 'maven-project'
		assertEquals document.dependencies, ['web']
	}

	private static LogEntry createSimpleLogEntry() {
		LogEntry entry = new LogEntry()
		entry.applicationName = 'start.spring.io'
		entry.timestamp = '2015-12-29T00:00:21Z'
		entry.entry = 'start.spring.io - [29/12/2015:00:00:21 +0000] "GET /starter.zip?name=demo&groupId=com.example&artifactId=demo&version=0.0.1-SNAPSHOT&description=Demo+project+for+Spring+Boot&packageName=com.example&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=1.3.1.RELEASE&dependencies=web HTTP/1.1" 200 0 51235 "-" "Java/1.8.0_65" 10.10.66.39:49859 x_forwarded_for:"2.177.163.44, 162.158.88.109" x_forwarded_proto:"http" vcap_request_id:5596f056-aad8-4e4e-622b-613acd701559 response_time:0.09861743 app_id:b017e0be-2460-49fe-b986-7377eb773926'
		entry
	}
}
