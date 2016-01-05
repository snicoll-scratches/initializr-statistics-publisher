package io.spring.initializr.statistics.elastic

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import io.spring.initializr.statistics.DefaultValueResolver
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 *
 * @author Stephane Nicoll
 */
class ImportStatisticsItemProcessorTest extends AbstractElasticTest {

	private final ImportStatisticsItemProcessor processor = new ImportStatisticsItemProcessor(
			new DefaultValueResolver(), provider)

	@Test
	void parSimpleLog() {
		def document = process('2015-12-29T00:00:21Z',
				'start.spring.io - [29/12/2015:00:00:21 +0000] "GET /starter.zip?name=demo&groupId=com.example&artifactId=demo&version=0.0.1-SNAPSHOT&description=Demo+project+for+Spring+Boot&packageName=com.example&type=maven-project&packaging=jar&javaVersion=1.8&language=java&bootVersion=1.3.1.RELEASE&dependencies=web HTTP/1.1" 200 0 51235 "-" "Java/1.8.0_65" 10.10.66.39:49859 x_forwarded_for:"2.177.163.44, 162.158.88.109" x_forwarded_proto:"http" vcap_request_id:5596f056-aad8-4e4e-622b-613acd701559 response_time:0.09861743 app_id:b017e0be-2460-49fe-b986-7377eb773926')

		assertEquals 'com.example', document.groupId
		assertEquals 'demo', document.artifactId
		assertEquals 'com.example', document.packageName
		assertEquals '1.3.1.RELEASE', document.bootVersion
		assertEquals '1.8', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'jar', document.packaging
		assertEquals 'maven-project', document.type
		assertEquals(['web'], document.dependencies)
	}

	@Test
	void parsePostTgz() {
		def document = process('2015-05-29T00:00:21Z',
				'start.spring.io - [29/12/2015:00:46:36 +0000] "POST /starter.tgz HTTP/1.1" 200 32 49931 "-" "curl/7.43.0" 10.10.66.39:45063 x_forwarded_for:"104.238.45.29, 108.162.215.204" x_forwarded_proto:"https" vcap_request_id:e68549aa-5117-498c-775e-a988a36ae1e0 response_time:0.080911139 app_id:b017e0be-2460-49fe-b986-7377eb773926')

		assertEquals 'org.test', document.groupId
		assertEquals 'demo', document.artifactId
		assertEquals 'demo', document.packageName
		assertEquals '1.2.3.RELEASE', document.bootVersion
		assertEquals '1.7', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'jar', document.packaging
		assertEquals 'maven-project', document.type
		assertDependencies([], document.dependencies)
	}

	@Test
	void parsePom() {
		def document = process('2015-12-29T15:21:57Z',
				'start.spring.io - [29/12/2015:21:57:18 +0000] "GET /pom.xml?&dependencies=data-jpa&type=maven-build&packaging=war&javaVersion=1.7&language=java&bootVersion=1.3.1.RELEASE&groupId=com.emc.esrs.dockerpcf&artifactId=cloudDbUtils&name=CloudDBUitls&description=&packageName=com.emc.esrs.dockerpcf.clouddbutils HTTP/1.1" 200 0 1635 "-" "IntelliJ IDEA" 10.10.66.39:32060 x_forwarded_for:"168.159.213.210, 108.162.219.112" x_forwarded_proto:"https" vcap_request_id:c6727a54-c3ee-4535-4463-39250f3edf3d response_time:0.040023006 app_id:b017e0be-2460-49fe-b986-7377eb773926')

		assertEquals 'com.emc.esrs.dockerpcf', document.groupId
		assertEquals 'cloudDbUtils', document.artifactId
		assertEquals 'com.emc.esrs.dockerpcf.clouddbutils', document.packageName
		assertEquals '1.3.1.RELEASE', document.bootVersion
		assertEquals '1.7', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'war', document.packaging
		assertEquals 'maven-build', document.type
		assertDependencies(['data-jpa'], document.dependencies)
	}

	@Test
	void parseGradleBuild() {
		def document = process('2015-12-29T19:40:16Z',
				'start.spring.io - [29/12/2015:19:40:16 +0000] "GET /build.gradle?&dependencies=h2&dependencies=data-rest&type=gradle-build&packaging=jar&javaVersion=1.8&language=java&bootVersion=1.3.1.RELEASE&groupId=com.vblazhnov.stats&artifactId=stats&name=stats&description=Statistics+collect&packageName=com.vblazhnov.stats HTTP/1.1" 200 0 921 "-" "IntelliJ IDEA (Minerva)" 10.10.2.247:2941 x_forwarded_for:"145.255.2.192, 141.101.80.79" x_forwarded_proto:"https" vcap_request_id:46205cf2-8d24-47e0-72f6-4657331beeed response_time:0.037459673 app_id:b017e0be-2460-49fe-b986-7377eb773926')

		assertEquals 'com.vblazhnov.stats', document.groupId
		assertEquals 'stats', document.artifactId
		assertEquals 'com.vblazhnov.stats', document.packageName
		assertEquals '1.3.1.RELEASE', document.bootVersion
		assertEquals '1.8', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'jar', document.packaging
		assertEquals 'gradle-build', document.type
		assertDependencies(['h2', 'data-rest'], document.dependencies)
	}

	@Test
	void decodeDependenciesParam() {
		def document = process('2015-12-29T01:12:33Z',
				'start.spring.io - [29/12/2015:01:12:33 +0000] "GET /starter.zip?dependencies=jersey%2Cjdbc%2Cmysql%2Ctest%2Clombok&artifactId=service-impl&type=maven-project&packaging=com.x.service HTTP/1.1" 200 0 51038 "-" "SpringBootCli/1.3.1.RELEASE" 10.10.66.39:1852 x_forwarded_for:"124.79.107.76, 108.162.215.117" x_forwarded_proto:"https" vcap_request_id:6ec1493e-cee5-491f-6fa1-328e9c118065 response_time:0.079351566 app_id:b017e0be-2460-49fe-b986-7377eb773926')
		assertEquals 'com.example', document.groupId
		assertEquals 'service-impl', document.artifactId
		assertEquals 'com.example', document.packageName
		assertEquals '1.3.1.RELEASE', document.bootVersion
		assertEquals '1.8', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'com.x.service', document.packaging
		assertEquals true, document.invalid
		assertEquals true, document.invalidPackaging
		assertEquals 'maven-project', document.type
		assertDependencies(['jersey', 'jdbc', 'mysql', 'test', 'lombok'], document.dependencies)
	}

	@Test
	void decodeExtraStyleParam() {
		def document = process('2015-12-23T11:42:12Z',
				'start.spring.io - [23/12/2015:11:42:12 +0000] "GET /starter.zip?type=maven-project&bootVersion=1.3.1.RELEASE&baseDir=ris&groupId=com.hsw&artifactId=ris&name=ris&description=ris&packageName=com.hsw&packaging=jar&javaVersion=1.8&language=java&autocomplete=&generate-project=&style=aop&style HTTP/1.1" 500 0 184 "http://start.spring.io/" "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)" 10.10.2.60:48104 x_forwarded_for:"220.181.126.92, 188.114.106.155" x_forwarded_proto:"http" vcap_request_id:75c644d6-4de5-483e-57ab-e4b38bfa533f response_time:0.101778597 app_id:b017e0be-2460-49fe-b986-7377eb773926')

		assertEquals 'com.hsw', document.groupId
		assertEquals 'ris', document.artifactId
		assertEquals 'com.hsw', document.packageName
		assertEquals '1.3.1.RELEASE', document.bootVersion
		assertEquals '1.8', document.javaVersion
		assertEquals 'java', document.language
		assertEquals 'jar', document.packaging
		assertEquals 'maven-project', document.type
		assertDependencies(['aop'], document.dependencies)
	}

	ProjectRequestDocument process(String timestamp, String log) {
		def entry = createSimpleLogEntry(timestamp, log)
		def document = processor.process(entry)
		assertEquals createTimestamp(timestamp).toInstant(ZoneOffset.UTC).toEpochMilli(),
				document.generationTimestamp
		document
	}

	private static void assertDependencies(def expected, def actual) {
		assertTrue "dependencies mismatch, expected $expected, got $actual",
				expected.containsAll(actual) && actual.containsAll(expected)
	}

	private static LocalDateTime createTimestamp(String date) {
		LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	}

	private static LogEntry createSimpleLogEntry(String timestamp, String log) {
		LogEntry entry = new LogEntry()
		entry.applicationName = 'start.spring.io'
		entry.timestamp = timestamp
		entry.entry = log
		entry
	}
}
