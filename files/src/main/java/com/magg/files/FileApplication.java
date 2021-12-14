package com.magg.files;

import com.magg.files.config.PlatformMongoDbConfiguration;
import com.magg.files.config.PlatformMongoDbProperties;
import com.magg.files.config.PlatformMongockConfiguration;
import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
	"com.magg.files",
	"com.magg.storage"
},
	exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Import({
	PlatformMongockConfiguration.class,
	PlatformMongoDbConfiguration.class,
	PlatformMongoDbProperties.class,

})
@EnableMongoRepositories(basePackages = {
	"com.magg.files.repository",
})
@EnableMongock
public class FileApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileApplication.class, args);
	}

}
