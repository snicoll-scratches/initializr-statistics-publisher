package io.spring.initializr.statistics

import io.spring.initializr.config.InitializrAutoConfiguration
import io.spring.initializr.config.InitializrMetricsExporterAutoConfiguration

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication(exclude = [InitializrAutoConfiguration.class, InitializrMetricsExporterAutoConfiguration.class])
@EnableBatchProcessing
@EnableCaching
class InitializrStatisticsPublisherApplication {

	static void main(String[] args) {
		SpringApplication.run InitializrStatisticsPublisherApplication, args
	}

}
