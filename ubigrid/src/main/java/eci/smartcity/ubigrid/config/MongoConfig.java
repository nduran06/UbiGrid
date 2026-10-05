package eci.smartcity.ubigrid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Configuration
@EnableMongoRepositories(basePackages = "eci.smartcity.ubigrid.repository")
@ComponentScan(basePackages = { "eci.smartcity.ubigrid.service", "eci.smartcity.ubigrid.service.impl", "eci.smartcity.ubigrid.repository" })
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Value("${spring.data.mongodb.database}")
	private String databaseName;

	@Bean
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(mongoUri);
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();

		return MongoClients.create(mongoClientSettings);
	}

	@Override
	protected String getDatabaseName() {
		return databaseName;
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongoClient(), getDatabaseName());
	}

	@Override
	public MongoCustomConversions customConversions() {
		return new MongoCustomConversions(
				Arrays.asList(new LocalDateTimeToDateConverter(), new DateToLocalDateTimeConverter()));
	}

	// Custom converters for handling LocalDateTime in MongoDB
	static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
		@Override
		public Date convert(LocalDateTime source) {
			return source == null ? null : Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
		}
	}

	static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
		@Override
		public LocalDateTime convert(Date source) {
			return source == null ? null : LocalDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
		}
	}

	@Override
	public Collection<String> getMappingBasePackages() {
		return Arrays.asList("eci.smartcity.ubigrid.model");
	}

	@Bean
	public MongoMappingContext mongoMappingContext() throws ClassNotFoundException {
		MongoMappingContext context = new MongoMappingContext();
		context.setAutoIndexCreation(true);
		return context;
	}

	@Bean
	public MappingMongoConverter customMappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context,
	        MongoCustomConversions conversions) {
	    DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
	    MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
	    converter.setCustomConversions(conversions);

	    // Optionally disable _class field insertion
	    converter.setTypeMapper(new DefaultMongoTypeMapper(null));

	    return converter;
	}
}