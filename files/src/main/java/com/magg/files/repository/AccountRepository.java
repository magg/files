package com.magg.files.repository;

import com.magg.files.domain.AccountDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<AccountDomain, String>
{
}
