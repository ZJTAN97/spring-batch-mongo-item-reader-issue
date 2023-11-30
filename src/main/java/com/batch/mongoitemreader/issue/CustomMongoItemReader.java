package com.batch.mongoitemreader.issue;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.util.json.ParameterBindingDocumentCodec;
import org.springframework.data.mongodb.util.json.ParameterBindingJsonReader;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CustomMongoItemReader<T> extends MongoItemReader<T> {

    private MongoOperations template;
    private Query query;
    private String queryString;
    private Class<? extends T> type;
    private Sort sort;
    private String hint;
    private String fields;
    private String collection;
    private List<Object> parameterValues = new ArrayList();

    @Override
    protected Iterator<T> doPageRead() {
        PageRequest pageRequest;
        if (this.queryString != null) {
            pageRequest = PageRequest.of(this.page, this.pageSize, this.sort);
            String populatedQuery = this.replacePlaceholders(this.queryString, this.parameterValues);
            BasicQuery mongoQuery;
            if (StringUtils.hasText(this.fields)) {
                mongoQuery = new BasicQuery(populatedQuery, this.fields);
            } else {
                mongoQuery = new BasicQuery(populatedQuery);
            }

            mongoQuery.with(pageRequest);
            if (StringUtils.hasText(this.hint)) {
                mongoQuery.withHint(this.hint);
            }

            return StringUtils.hasText(this.collection) ?
                    // Changing from `find` to `stream`
                    (Iterator<T>) this.template.stream(mongoQuery, this.type, this.collection).iterator() :
                    (Iterator<T>) this.template.stream(mongoQuery, this.type).iterator();
        } else {
            pageRequest = PageRequest.of(this.page, this.pageSize);
            this.query.with(pageRequest);
            return StringUtils.hasText(this.collection) ?
                    // Changing from `find` to `stream`
                    (Iterator<T>) this.template.stream(this.query, this.type, this.collection).iterator() :
                    (Iterator<T>) this.template.stream(this.query, this.type).iterator();
        }
    }

    private String replacePlaceholders(String input, List<Object> values) {
        ParameterBindingJsonReader reader = new ParameterBindingJsonReader(input, values.toArray());
        DecoderContext decoderContext = DecoderContext.builder().build();
        Document document = (new ParameterBindingDocumentCodec()).decode(reader, decoderContext);
        return document.toJson();
    }

}
