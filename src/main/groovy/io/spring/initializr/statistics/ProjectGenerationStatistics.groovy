package io.spring.initializr.statistics

import io.spring.initializr.generator.ProjectRequest

/**
 *
 * @author Stephane Nicoll
 */
class ProjectGenerationStatistics {

	private final long generationTimestamp

	private final ProjectRequest request

	ProjectGenerationStatistics(Long generationTimestamp, ProjectRequest request) {
		this.generationTimestamp = generationTimestamp
		this.request = request
	}

	Long getGenerationTimestamp() {
		return generationTimestamp
	}

	ProjectRequest getRequest() {
		return request
	}

}
