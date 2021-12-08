package com.example.file;

import com.example.file.config.PlatformMongoDbConfiguration;
import com.example.file.config.PlatformMongoDbProperties;
import com.example.file.config.PlatformMongockConfiguration;
import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
	"com.example.file"
},
	exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Import({
	PlatformMongockConfiguration.class,
	PlatformMongoDbConfiguration.class,
	PlatformMongoDbProperties.class,

})
@EnableMongoRepositories(basePackages = {
	"com.example.file.repository",
})
@EnableMongock
public class FileApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileApplication.class, args);
	}

}
