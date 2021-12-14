package com.magg.files.repository;

import com.magg.files.domain.FileDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileDomain, String>
{
}
