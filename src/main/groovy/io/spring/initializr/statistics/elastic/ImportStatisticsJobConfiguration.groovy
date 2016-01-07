package io.spring.initializr.statistics.elastic

import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.statistics.DefaultValueResolver

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.partition.support.MultiResourcePartitioner
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.validation.BindException
import org.springframework.web.client.ResourceAccessException

/**
 *
 * @author Stephane Nicoll
 */
@Configuration
@EnableConfigurationProperties(ImportStatisticsJobProperties)
class ImportStatisticsJobConfiguration {

	@Autowired
	private ImportStatisticsJobProperties properties

	@Autowired
	private InitializrMetadataProvider metadataProvider

	@Autowired
	private DefaultValueResolver valueResolver

	@Autowired
	private ProjectRequestDocumentSerializer documentSerializer

	@Autowired
	private StepBuilderFactory stepBuilder

	@Bean
	public Job importStatisticsJob(JobBuilderFactory jobs, ItemReader<LogEntry> reader) {
		return jobs.get("importStatisticsJob")
				.incrementer(new RunIdIncrementer())
				.start(partitionStep(reader))
				.build();
	}

	@Bean
	public Step partitionStep(ItemReader<LogEntry> reader) {
		TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();

		partitionHandler.setTaskExecutor(new SimpleAsyncTaskExecutor());
		partitionHandler.setStep(step1(reader));

		partitionHandler.afterPropertiesSet();

		return stepBuilder.get("partitionStep")
				.partitioner("step1", partitioner())
				.partitionHandler(partitionHandler)
				.gridSize(12)
				.build();
	}

	@Bean
	public Step step1(ItemReader<LogEntry> reader) {
		return stepBuilder.get("step1")
				.<LogEntry, ProjectRequestDocument> chunk(10)
				.faultTolerant()
				.retryLimit(3)
				.retry(SocketException).retry(ResourceAccessException)
				.reader(reader)
				.processor(processor())
				.listener(new RejectedEntryListener())
				.writer(writer())
				.build();
	}

	@Bean
	public Partitioner partitioner() {
		if (!this.properties.job.input) {
			throw new IllegalStateException("No log input provided, please check your configuration.")
		}
		MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
		partitioner.setResources(this.properties.job.input);
		return partitioner;
	}

	@Bean
	@StepScope
	FlatFileItemReader<LogEntry> reader(@Value("#{stepExecutionContext['fileName']}") Resource resource) {
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
		reader.setResource(resource)
		reader
	}

	@Bean
	ItemProcessor<LogEntry, ProjectRequestDocument> processor() {
		new ImportStatisticsItemProcessor(valueResolver, metadataProvider)
	}

	@Bean
	ItemWriter<ProjectRequestDocument> writer() {
		new ImportStatisticsItemWriter(documentSerializer)
	}

}
