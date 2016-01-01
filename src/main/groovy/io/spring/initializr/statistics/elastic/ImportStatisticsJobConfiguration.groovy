package io.spring.initializr.statistics.elastic

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.MultiResourceItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.BindException

/**
 *
 * @author Stephane Nicoll
 */
@Configuration
@EnableConfigurationProperties(ImportStatisticsJobProperties)
class ImportStatisticsJobConfiguration {

	@Autowired
	private ImportStatisticsJobProperties properties

	@Bean
	public Job importStatisticsJob(JobBuilderFactory jobs, Step step1) {
		return jobs.get("importStatisticsJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1)
				.end()
				.build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory,
					  ItemProcessor<LogEntry, ProjectRequestDocument> processor,
					  ItemWriter<ProjectRequestDocument> writer) {
		return stepBuilderFactory.get("step1")
				.<LogEntry, ProjectRequestDocument> chunk(10)
				.reader(reader())
				.processor(processor)
				.writer(writer)
				.build();
	}

	@Bean
	ItemReader<LogEntry> reader() {
		if (!this.properties.job.input) {
			throw new IllegalStateException("No log input provided, please check your configuration.")
		}
		def reader = new FlatFileItemReader<LogEntry>()


		def mapper = new DefaultLineMapper<LogEntry>()
		mapper.setLineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB))
		mapper.setFieldSetMapper(new FieldSetMapper<LogEntry>() {
			@Override
			LogEntry mapFieldSet(FieldSet fieldSet) throws BindException {
				LogEntry entry = new LogEntry()
				entry.timestamp = fieldSet.values[2]
				entry.applicationName = fieldSet.values[4]
				entry.entry = fieldSet.values[fieldSet.fieldCount - 1]
				entry
			}
		})
		reader.setLineMapper(mapper)

		def wrapper = new MultiResourceItemReader<LogEntry>()
		wrapper.setDelegate(reader)
		wrapper.setResources(this.properties.job.input)
		wrapper
	}

}
