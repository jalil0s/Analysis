/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.rest.graphdb.batch;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.ValueContext;
import org.neo4j.rest.graphdb.*;
import org.neo4j.rest.graphdb.converter.RelationshipIterableConverter;
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.converter.RestEntityPropertyRefresher;
import org.neo4j.rest.graphdb.converter.RestIndexHitsConverter;
import org.neo4j.rest.graphdb.entity.RestEntity;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.IndexInfo;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.index.SimpleIndexHits;
import org.neo4j.rest.graphdb.services.RequestType;
import org.neo4j.rest.graphdb.util.JsonHelper;

public class BatchRestAPI extends ExecutingRestAPI {

    private final ExecutingRestAPI executingRestApi;

    public BatchRestAPI(String baseUri, RestAPIFacade facade) {
        super(baseUri,facade);
        executingRestApi = facade.getDirect();
        this.restRequest =  new RecordingRestRequest(new RestOperations(), baseUri);
    }

    @Override
    public RestRequest getRestRequest() {
        return restRequest;
    }

    @Override
    protected RestRequest createRestRequest( String uri, String user, String password){
        final ExecutingRestRequest executingRestRequest = new ExecutingRestRequest(uri, user, password);
        return new RecordingRestRequest(new RestOperations(), executingRestRequest.getUri());
    }
    
    
    @Override
    public RestNode createRestNode(RequestResult requestResult) {
        final long batchId = requestResult.getBatchId();
        RestNode node = new RestNode("{"+batchId+"}", facade);
        (getRecordingRequest()).getOperations().addToRestOperation(batchId, node, new RestEntityExtractor(facade));
        return node;
    }
          
    
    @Override
    public RestRelationship createRestRelationship(RequestResult requestResult, PropertyContainer element) {
        final long batchId = requestResult.getBatchId();
        RestRelationship relationship = new RestRelationship("{"+batchId+"}", facade);
        getRecordingRequest().getOperations().addToRestOperation(batchId, relationship, new RestEntityExtractor(facade));
        return relationship;
    }

    private RecordingRestRequest getRecordingRequest() {
        return (RecordingRestRequest)this.restRequest;
    }

    public RestOperations getRecordedOperations(){
       return (getRecordingRequest()).getOperations();
    }

    public void stop() {
        getRecordingRequest().stop();
    }

    @SuppressWarnings("unchecked")
    public Iterable<Relationship> wrapRelationships(  RequestResult requestResult ) {
        final long batchId = requestResult.getBatchId();
        final BatchIterable<Relationship> result = new BatchIterable<Relationship>(requestResult);
        getRecordingRequest().getOperations().addToRestOperation(batchId, result, new RelationshipIterableConverter(facade));
        return result;
    }

    public <S extends PropertyContainer> IndexHits<S> queryIndex(String indexPath, Class<S> entityType) {
        RequestResult response = restRequest.get(indexPath);
        final long batchId = response.getBatchId();
        final SimpleIndexHits<S> result = new SimpleIndexHits<S>(batchId, entityType, facade);
        getRecordingRequest().getOperations().addToRestOperation(batchId, result, new RestIndexHitsConverter(facade,entityType));
        return result;
    }    

    @Override
    public void setPropertyOnEntity( RestEntity entity, String key, Object value ) {       
        RequestResult response = getRestRequest().with(entity.getUri()).put("properties/" + key, value);
        final long batchId = response.getBatchId();     
        getRecordingRequest().getOperations().addToRestOperation(batchId, entity, new RestEntityPropertyRefresher(entity));       
    }
    
    @Override
    public <T extends PropertyContainer> void addToIndex( T entity, RestIndex index,  String key, Object value ) {
        final RestEntity restEntity = (RestEntity) entity;
        String uri = restEntity.getUri();
        if (value instanceof ValueContext) {
            value = ((ValueContext)value).getCorrectValue();
        }
        final Map<String, Object> data = MapUtil.map("key", key, "value", value, "uri", uri);
        restRequest.post(index.indexPath(), data);
    }

    public void executeBatchRequest() {
        stop();
        RestOperations operations = getRecordedOperations();
        RequestResult response = executingRestApi.batch(createBatchRequestData(operations));
        Map<Long, Object> mappedObjects = convertRequestResultToEntities(operations, response);
        updateRestOperations(operations, mappedObjects);
    }

    protected void updateRestOperations(RestOperations operations, Map<Long, Object> mappedObjects) {
        for (RestOperations.RestOperation operation : operations.getRecordedRequests().values()) {
            operation.updateEntity(mappedObjects.get(operation.getBatchId()), executingRestApi);
        }
    }

    @Override
    protected <T extends PropertyContainer> String indexPath(String indexPath, T entity) {
        RestEntity restEntity = (RestEntity) entity;
        if (isNewEntity(restEntity)) throw new UnsupportedOperationException("Cannot delete newly created entities from index " + entity);
        return super.indexPath(indexPath, restEntity);
    }

    private boolean isNewEntity(RestEntity restEntity) {
        return restEntity.getUri().startsWith("{");
    }

    @SuppressWarnings("unchecked")
    protected Map<Long, Object> convertRequestResultToEntities(RestOperations operations, RequestResult response) {
        Object result = response.toEntity();
        if (RestResultException.isExceptionResult(result)) {
            throw new RestResultException(result);
        }
        Collection<Map<String, Object>> responseCollection = (Collection<Map<String, Object>>) result;
        Map<Long, Object> mappedObjects = new HashMap<Long, Object>(responseCollection.size());
        for (Map<String, Object> entry : responseCollection) {
            if (RestResultException.isExceptionResult(entry)) {
                throw new RestResultException(entry);
            }
            final Long batchId = getBatchId(entry);
            final RequestResult subResult = RequestResult.extractFrom(entry);
            RestOperations.RestOperation restOperation = operations.getOperation(batchId);
            if (restOperation.getEntity() != null){
                Object entity = restOperation.getResultConverter().convertFromRepresentation(subResult);
                mappedObjects.put(batchId, entity);
            }

        }
        return mappedObjects;
    }


    private Long getBatchId(Map<String, Object> entry) {
        return ((Number) entry.get("id")).longValue();
    }

    protected Collection<Map<String, Object>> createBatchRequestData(RestOperations operations) {
        Collection<Map<String, Object>> batch = new ArrayList<Map<String, Object>>();
        final String baseUri = executingRestApi.getBaseUri();
        for (RestOperations.RestOperation operation : operations.getRecordedRequests().values()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("method", operation.getMethod());
            if (operation.isSameUri(baseUri)) {
                params.put("to", operation.getUri());
            } else {
                params.put("to",createOperationUri(operation));
            }
            if (operation.getData() != null) {
                params.put("body", operation.getData());
            }
            params.put("id", operation.getBatchId());
            batch.add(params);
        }
        return batch;
    }

    private String createOperationUri(RestOperations.RestOperation operation){
        String uri =  operation.getBaseUri();
        String suffix = operation.getUri();
        if (suffix.startsWith("/")){
            return uri + suffix;
        }
        return uri + "/" + suffix;
    }


    private static class BatchIndexInfo implements IndexInfo {

        @Override
        public boolean checkConfig(String indexName, Map<String, String> config) {
            return true;
        }

        @Override
        public String[] indexNames() {
            return new String[0];
        }

        @Override
        public boolean exists(String indexName) {
            return true;
        }

        @Override
        public Map<String, String> getConfig(String name) {
            return null;
        }
    }
}
