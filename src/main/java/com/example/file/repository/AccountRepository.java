package com.example.file.repository;

import com.example.file.domain.AccountDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<AccountDomain, String>
{
}
