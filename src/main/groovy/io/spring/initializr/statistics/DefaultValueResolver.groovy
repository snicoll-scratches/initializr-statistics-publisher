package io.spring.initializr.statistics

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.springframework.stereotype.Service

/**
 * Resolve default values according to the time at which the project
 * was generated.
 *
 * @author Stephane Nicoll
 */
@Service
class DefaultValueResolver {

	private static final LocalDateTime JAVA_8_DEFAULT =
			LocalDateTime.parse('2015-06-09T11:47:00+02:00', DateTimeFormatter.ISO_OFFSET_DATE_TIME);

	private static final LocalDateTime GROUP_ID_PACKAGE_HARMONIZATION =
			LocalDateTime.parse('2015-08-06T11:16:00+02:00', DateTimeFormatter.ISO_OFFSET_DATE_TIME);

	private static final LocalDateTime BOOT_1_2_4 =
			LocalDateTime.parse('2015-06-04T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	private static final LocalDateTime BOOT_1_2_5 =
			LocalDateTime.parse('2015-07-02T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	private static final LocalDateTime BOOT_1_2_6 =
			LocalDateTime.parse('2015-09-16T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	private static final LocalDateTime BOOT_1_2_7 =
			LocalDateTime.parse('2015-10-16T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	private static final LocalDateTime BOOT_1_3_0 =
			LocalDateTime.parse('2015-11-16T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	private static final LocalDateTime BOOT_1_3_1 =
			LocalDateTime.parse('2015-12-16T16:00:00Z', DateTimeFormatter.ISO_OFFSET_DATE_TIME)

	String getDefaultGroupId(LocalDateTime timestamp) {
		(timestamp.isBefore(GROUP_ID_PACKAGE_HARMONIZATION) ? 'org.test' : 'com.example')
	}

	String getDefaultArtifactId(LocalDateTime timestamp) {
		'demo'
	}

	String getDefaultPackageName(LocalDateTime timestamp) {
		(timestamp.isBefore(GROUP_ID_PACKAGE_HARMONIZATION) ? 'demo' : 'com.example')
	}

	String getDefaultBootVersion(LocalDateTime timestamp) {
		if (timestamp.isBefore(BOOT_1_2_4)) {
			return '1.2.3.RELEASE'
		}
		if (timestamp.isBefore(BOOT_1_2_5)) {
			return '1.2.4.RELEASE'
		}
		if (timestamp.isBefore(BOOT_1_2_6)) {
			return '1.2.5.RELEASE'
		}
		if (timestamp.isBefore(BOOT_1_2_7)) {
			return '1.2.6.RELEASE'
		}
		if (timestamp.isBefore(BOOT_1_3_0)) {
			return '1.2.7.RELEASE'
		}
		if (timestamp.isBefore(BOOT_1_3_1)) {
			return '1.3.0.RELEASE'
		}
		'1.3.1.RELEASE'
	}

	String getDefaultJavaVersion(LocalDateTime timestamp) {
		(timestamp.isBefore(JAVA_8_DEFAULT) ? '1.7' : '1.8')
	}

	String getDefaultLanguage(LocalDateTime timestamp) {
		'java'
	}

	String getDefaultPackaging(LocalDateTime timestamp) {
		'jar'
	}

	String getDefaultType(LocalDateTime timestamp, String url) {
		if (!url) {
			return 'maven-project'
		}
		if (url.startsWith('/build.gradle')) {
			return 'gradle-build'
		}
		if (url.startsWith('/pom.xml')) {
			return 'maven-build'
		}
		return 'maven-project'
	}

}
