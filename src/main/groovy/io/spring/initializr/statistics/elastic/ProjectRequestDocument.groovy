package io.spring.initializr.statistics.elastic

/**
 *
 * @author Stephane Nicoll
 */
class ProjectRequestDocument {

	long generationTimestamp
	String groupId
	String artifactId
	String packageName
	String bootVersion
	String javaVersion
	String language
	String packaging
	String type
	final List<String> dependencies = []


}
