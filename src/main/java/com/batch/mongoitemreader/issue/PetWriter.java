package com.batch.mongoitemreader.issue;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class PetWriter extends MongoItemWriter<PetDomain> implements ItemStream {

    public PetWriter(MongoOperations template) {
        this.setTemplate(template);
    }

    @Override
    public void write(Chunk<? extends PetDomain> chunk) throws Exception {
        System.out.println("-- nothing to write, main issue from reader --");
    }
}
