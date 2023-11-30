**Bug description**

While using `MongoItemReader`, I have configured my `Step` bean to utilize `faultTolerant` method and a `skipLimit` of 5
on the skip condition for `IllegalArgumentException`

```
@Configuration
@RequiredArgsConstructor
public class PetJobConfig {

    private final PetRepo petRepo;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final MongoTemplate mongoTemplate;

    @Bean
    public Step readPetFromMongo() {
        return new StepBuilder("petReaderMongo", jobRepository)
                .allowStartIfComplete(true)
                .<PetDomain, PetDomain>chunk(1000, platformTransactionManager)
                .reader(petRepo.petReader())
                .writer(new PetWriter(mongoTemplate))
                .faultTolerant()
                .skipLimit(5)
                .skip(IllegalArgumentException.class)
                .build();
    }

    @Bean
    public Job readPetFromMongoJob() {
        return new JobBuilder("petReaderMongoJob", jobRepository)
                .start(readPetFromMongo())
                .build();
    }

}
```

In my current context, there could be data in my database that does not fully conform to the target type provided to the
reader. And I would like to make use of the `faultTolerant` method to skip these dirty data.

type provided to MongoItemReader

```
public record Animal (
  String name,
  Animal animal
) {}


public enum Animal {
   CAT,
   DOG;
}

```

Repo class

```

@Repository
@RequiredArgsConstructor
public class PetRepo {

    private final MongoTemplate mongoTemplate;

    public MongoItemReader<PetDomain> petReader() {

        Map<String, Sort.Direction> sorts = new HashMap<>();

        Query query = new Query();

        var reader = new MongoItemReaderBuilder<PetDomain>()
                .name("petReader")
                .collection("pet")
                .pageSize(500)
                .template(mongoTemplate)
                .targetType(PetDomain.class)
                .sorts(sorts)
                .query(query)
                .build();

        return reader;
    }

}


```

E.g. dirty data from mongodb

```
{
  name: "Bingo",
  animal: "CAT2" // Does not conform to enum provided
}
```

However, due to the way `doPageRead` utilizes `MongoOperations` to retrieve data as a `list` instead of a `stream`, it
is unable to serialize to the type as long as there is dirty data.

So to be able to iterate through the iterator, I have to override the entire `doPageRead` method just to change
the `MongoOperation` method from `find`  to `stream`

```
protected Iterator<T> doPageRead() {
	if (queryString != null) {
		Pageable pageRequest = PageRequest.of(page, pageSize, sort);

		String populatedQuery = replacePlaceholders(queryString, parameterValues);

		Query mongoQuery;

		if (StringUtils.hasText(fields)) {
			mongoQuery = new BasicQuery(populatedQuery, fields);
		}
		else {
			mongoQuery = new BasicQuery(populatedQuery);
		}

		mongoQuery.with(pageRequest);

		if (StringUtils.hasText(hint)) {
			mongoQuery.withHint(hint);
		}

		if (StringUtils.hasText(collection)) {
			// return (Iterator<T>) template.find(mongoQuery, type, collection).iterator();
			return (Iterator<T>) template.stream(mongoQuery, type, collection).iterator();

		}
		else {
			// return (Iterator<T>) template.find(mongoQuery, type).iterator();
			return (Iterator<T>) template.stream(mongoQuery, type).iterator();

		}

	}
	else {
		Pageable pageRequest = PageRequest.of(page, pageSize);
		query.with(pageRequest);

		if (StringUtils.hasText(collection)) {
			// return (Iterator<T>) template.find(query, type, collection).iterator();
			return (Iterator<T>) template.stream(query, type, collection).iterator();
		}
		else {
			// return (Iterator<T>) template.find(query, type).iterator();
			return (Iterator<T>) template.stream(query, type).iterator();
		}
	}
}

```

I was wondering if its actually better to utilize `stream` instead as `find` prevents the iterator from iterating if a
document from the database does not conform to the class type provided.

**Environment**

- JDK 21
- spring batch 5.0.3

**Expected behavior**

If theres non conforming data from the database to the type specified in `MongoItemReader`, it should be able to move on
to the next item in the iterator.
