package com.batch.mongoitemreader.issue;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PetRepo {

    private final MongoTemplate mongoTemplate;

    public MongoItemReader<PetDomain> petReader() {

        Map<String, Sort.Direction> sorts = new HashMap<>();

        Query query = new Query();

        // Old Implementation
        var reader = new MongoItemReaderBuilder<PetDomain>()
                .name("petReader")
                .collection("pet")
                .pageSize(500)
                .template(mongoTemplate)
                .targetType(PetDomain.class)
                .sorts(sorts)
                .query(query)
                .build();

        // New implementation
        var customReader = new CustomMongoItemReader<PetDomain>();
        customReader.setName("petReader");
        customReader.setCollection("pet");
        customReader.setPageSize(500);
        customReader.setTemplate(mongoTemplate);
        customReader.setType(PetDomain.class);
        customReader.setSort(sorts);
        customReader.setQuery(query);

        return customReader;
    }

}
