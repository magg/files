package com.example.file.service;

import com.example.file.config.ObjectMapperFactory;
import com.example.file.domain.PlatformBaseObject;
import com.example.file.exception.EntityNotFoundException;
import com.example.file.mapper.ObjectConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class ObjectService<DomainT extends PlatformBaseObject<DomainT>, ApiT,
    RepoU extends MongoRepository<DomainT, String>>
{
    protected static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getInstance();

    protected final Class<DomainT> domainClass;
    protected final RepoU repository;
    protected final Logger LOG;
    protected final ObjectConverter<DomainT, ApiT> converter;
    protected final String collectionName;


    protected ObjectService(
        String collectionName,
        RepoU repository,
        ObjectConverter<DomainT, ApiT> converter,
        Class<ApiT> clazz
    )
    {
        checkArgument(isNotBlank(collectionName), "collectionName can't be blank");
        requireNonNull(repository, "repository can't be null");
        requireNonNull(converter, "converter can't be null");
        requireNonNull(clazz, "clazz can't be null");

        this.collectionName = collectionName;
        this.converter = converter;
        this.repository = repository;
        this.domainClass = getPersistenceClass();
        this.LOG = LoggerFactory.getLogger(domainClass);
    }


    /**
     * Creates the object and returns the result with id and operational properties populated.
     * Enforces the custom schema associated with the collection of this service.
     *
     * @param apiObject The object from the API.
     * @return API object with id and audit properties.
     */
    public ApiT insertObject(ApiT apiObject) {
        return insert(apiObject);
    }

    private ApiT insert(ApiT apiObject) {
        return Optional
            .ofNullable(apiObject)
            .map(object -> apiToDomainModel(object))
            .map(this::doInsert)
            .map(this::domainToApiModel)
            .orElseThrow(NullPointerException::new);
    }


    /**
     * Inserts the given object into the underlying data store.
     *
     * @param dataObject The object to insert.
     * @return The inserted object.
     */
    protected DomainT doInsert(DomainT dataObject) {
        return repository.insert(dataObject);
    }

    /**
     * Retrieve an object by providing the identifier for it.
     * @param objectId The object's identifier
     * @return an object
     */
    public Optional<ApiT> getObject(String objectId) {
        requireNonNull(objectId);

        Optional<DomainT> domainObject = doGetById(objectId);

        Optional<ApiT> apiObject = domainObject.map(this::domainToApiModel);

        return apiObject;
    }


    @Transactional
    protected ApiT doUpdate(String objectId, ApiT apiT) throws EntityNotFoundException
    {
        // Validate the requested object exists to be patched
        DomainT currentObject = doGetById(objectId)
            .orElseThrow(() -> new EntityNotFoundException(objectId));

        DomainT domainPatch = apiToDomainModel(apiT);

        return domainToApiModel(domainPatch);
    }

    protected Optional<DomainT> doGetById(String domainId)
    {
        return repository.findById(domainId);
    }


    /**
     * Persist the patch on the object.
     * @param patch the patched object
     * @return the updated object
     */
    protected DomainT doPatch(DomainT patch) {
        return repository.save(patch);
    }

    private Class<DomainT> getPersistenceClass() {
        try {
            return (Class<DomainT>) Class.forName(((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0].getTypeName());
        } catch (ClassNotFoundException e) {
            // Tidy this up - prevent service start if we cannot determine persistence class
            throw new RuntimeException(e);
        }
    }


    /**
     * Maps an api object into a domain object.
     * @param apiObject The source object
     * @return A new domain object
     */
    public DomainT apiToDomainModel(ApiT apiObject) {

        return converter.apiModelToDataModel(apiObject);
    }

    /**
     * Maps a domain object into an api object. If the api object happens to be an ApiCustomisable object, it will
     * populate the custom fields from the unique and non unique fields.
     * @param object The domain object to convert
     * @return An api object
     */
    protected ApiT domainToApiModel(DomainT object) {
        return converter.dataModelToApiModel(object);
    }


    /**
     * Delete object with the given id.
=     * @param ids the id
     */
    public void delete(String id)
    {

        Optional<DomainT> domainT = doGetById(id);

        domainT.ifPresent(repository::delete);
    }
}
