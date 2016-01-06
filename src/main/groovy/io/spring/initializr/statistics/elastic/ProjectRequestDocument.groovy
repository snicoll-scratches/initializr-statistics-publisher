package io.spring.initializr.statistics.elastic

import groovy.transform.ToString

/**
 *
 * @author Stephane Nicoll
 */
@ToString(ignoreNulls = true, includePackage = false, includeNames = true)
class ProjectRequestDocument {

	long generationTimestamp
	String requestIp
	String groupId
	String artifactId
	String packageName
	String bootVersion
	String javaVersion
	String language
	String packaging
	String type
	final List<String> dependencies = []

	boolean invalid
	boolean invalidJavaVersion
	boolean invalidLanguage
	boolean invalidPackaging
	boolean invalidType
	final List<String> invalidDependencies = []


}
