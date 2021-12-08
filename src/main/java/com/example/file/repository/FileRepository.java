package com.example.file.repository;

import com.example.file.domain.FileDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileDomain, String>
{
}
