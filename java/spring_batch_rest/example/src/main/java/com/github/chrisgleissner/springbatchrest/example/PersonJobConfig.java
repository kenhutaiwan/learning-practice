package com.github.chrisgleissner.springbatchrest.example;

import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocScheduler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.LinkedList;
import java.util.List;

import static com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobPropertyResolvers.JobProperties;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.synchronizedList;

@Slf4j
@Configuration
public class PersonJobConfig {

    static final String JOB_NAME = "personJob";
    static final String LAST_NAME_PREFIX = "lastNamePrefix";

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    private AdHocScheduler adHocScheduler;

    @Bean
    Job personJob(@Qualifier("personStep") Step personStep) {
        Job job = jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(personStep)
                .end()
                .build();
        adHocScheduler.schedule(PersonJobConfig.JOB_NAME, job, "0 0 12 * * ?");
        return job;
    }

    @Bean
    Step personStep(@Qualifier("personProcessor") ItemProcessor personProcessor) {
        return stepBuilderFactory.get("personStep")
                .allowStartIfComplete(true)
                .<Person, Person>chunk(3)
                .reader(personReader())
                .processor(personProcessor)
                .writer(personWriter())
                .build();
    }

    @Bean
    FlatFileItemReader<Person> personReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("person.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    ItemProcessor personProcessor(@Qualifier("personNameCaseChange") ItemProcessor personNameCaseChange) {
        CompositeItemProcessor p = new CompositeItemProcessor();
        p.setDelegates(newArrayList(personNameFilter(), personNameCaseChange));
        return p;
    }

    @Bean
    ItemProcessor personNameFilter() {
        return new FunctionItemProcessor<Person, Person>(p -> {
            String lastNamePrefix = JobProperties.of(PersonJobConfig.JOB_NAME).getProperty(LAST_NAME_PREFIX);
            log.info("Last name prefix: {}", lastNamePrefix);
            return p.lastName != null && p.lastName.startsWith(lastNamePrefix) ? p : null;
        });
    }

    @StepScope
    @Bean
    ItemProcessor personNameCaseChange(@Value("#{jobParameters['upperCase']}") Boolean upperCaseParam) {
        boolean upperCase = upperCaseParam == null ? false : upperCaseParam;
        log.info("personNameCaseChange(upperCase={})", upperCase);
        return new FunctionItemProcessor<Person, Person>(p -> new Person(
                upperCase ? p.firstName.toUpperCase() : p.firstName.toLowerCase(),
                upperCase ? p.lastName.toUpperCase() : p.lastName.toLowerCase()));
    }

    @Bean
    CacheItemWriter<Person> personWriter() {
        return new CacheItemWriter<>();
    }

    public class CacheItemWriter<T> implements ItemWriter<T> {
        private List<T> items = synchronizedList(new LinkedList<>());

        @Override
        public void write(List<? extends T> items) {
            this.items.addAll(items);
        }

        public List<T> getItems() {
            return items;
        }

        public void clear() {
            items.clear();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        private String firstName;
        private String lastName;
    }
}
