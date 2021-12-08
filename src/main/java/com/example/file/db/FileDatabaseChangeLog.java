package com.example.file.db;

import com.example.file.domain.FileDomain;
import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import static com.example.file.domain.FileDomain.FILE_COLLECTION_NAME;
import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Class Description goes here.
 * Created by miguelgonzalez on 21/09/20
 */
@SuppressWarnings("unused")
@ChangeLog
public class FileDatabaseChangeLog
{

    public static final String ADMIN_DB = "admin";

    /**
     * Create the initial file collection.
     *
     * @param mongoTemplate {@link MongoTemplate}
     */
    @ChangeSet(order = "file.001", id = "createInitialCollections", author = "miguel.gonzalez")
    public void createInitialCollections(MongockTemplate mongoTemplate) {
        mongoTemplate.createCollection(FILE_COLLECTION_NAME);


        Index accountIdIndex = new Index()
                .on("accountId", ASC);

        mongoTemplate.indexOps(FileDomain.class).ensureIndex(accountIdIndex);
    }
}
