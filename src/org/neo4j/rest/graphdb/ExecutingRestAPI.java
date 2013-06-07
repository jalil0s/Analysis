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
package org.neo4j.rest.graphdb;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.ValueContext;
import org.neo4j.rest.graphdb.batch.BatchCallback;
import org.neo4j.rest.graphdb.batch.BatchRestAPI;
import org.neo4j.rest.graphdb.converter.RelationshipIterableConverter;
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.converter.RestIndexHitsConverter;
import org.neo4j.rest.graphdb.entity.RestEntity;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.IndexInfo;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.index.RestIndexManager;
import org.neo4j.rest.graphdb.index.RetrievedIndexInfo;
import org.neo4j.rest.graphdb.index.SimpleIndexHits;
import org.neo4j.rest.graphdb.query.RestGremlinQueryResult;
import org.neo4j.rest.graphdb.query.RestQueryResult;
import org.neo4j.rest.graphdb.services.PluginInvocation;
import org.neo4j.rest.graphdb.services.RequestType;
import org.neo4j.rest.graphdb.services.RestInvocationHandler;
import org.neo4j.rest.graphdb.services.ServiceInvocation;
import org.neo4j.rest.graphdb.traversal.RestTraversal;
import org.neo4j.rest.graphdb.traversal.RestTraverser;
import org.neo4j.rest.graphdb.util.JsonHelper;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.neo4j.rest.graphdb.util.ResultConverter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static javax.ws.rs.core.Response.Status.CREATED;


public class ExecutingRestAPI implements RestAPI {

    protected RestRequest restRequest;
    private long propertyRefetchTimeInMillis = 1000;
    protected final RestAPI facade;

    protected ExecutingRestAPI(String uri, RestAPI facade) {
        this.facade = facade;
        this.restRequest = createRestRequest(uri, null, null);
    }

    protected ExecutingRestAPI(String uri, String user, String password, RestAPI facade) {
        this.facade = facade;
        this.restRequest = createRestRequest(uri, user, password);
    }

    protected RestRequest createRestRequest(String uri, String user, String password) {
        return new ExecutingRestRequest(uri, user, password);
    }

    @Override
    public RestIndexManager index() {
        return new RestIndexManager(facade);
    }

    @Override
    public RestNode getNodeById(long id) {
        RequestResult response = restRequest.get("node/" + id);
        if (response.statusIs(Status.NOT_FOUND)) {
            throw new NotFoundException("" + id);
        }
        return new RestNode(response.toMap(), facade);
    }

    @Override
    public RestRelationship getRelationshipById(long id) {
        RequestResult requestResult = restRequest.get("relationship/" + id);
        if (requestResult.statusIs(Status.NOT_FOUND)) {
            throw new NotFoundException("" + id);
        }
        return new RestRelationship(requestResult.toMap(), facade);
    }


    @Override
    public RestNode createNode(Map<String, Object> props) {
        RequestResult requestResult = restRequest.post("node", props);
        return createRestNode(requestResult);
    }

    @Override
    public RestNode createRestNode(RequestResult requestResult) {
        if (requestResult.statusOtherThan(CREATED)) {
            final int status = requestResult.getStatus();
            throw new RuntimeException("" + status);
        }
        final String location = requestResult.getLocation();
        return new RestNode(location, facade);
    }

    @Override
    public RestRelationship createRelationship(Node startNode, Node endNode, RelationshipType type, Map<String, Object> props) {
        // final RestRequest restRequest = ((RestNode) startNode).getRestRequest();
        final RestNode end = (RestNode) endNode;
        Map<String, Object> data = MapUtil.map("to", end.getUri(), "type", type.name());
        if (props != null && props.size() > 0) {
            data.put("data", props);
        }
        final RestNode start = (RestNode) startNode;
        RequestResult requestResult = getRestRequest().with(start.getUri()).post("relationships", data);
        return createRestRelationship(requestResult, startNode);
    }

    @Override
    public RestRelationship createRestRelationship(RequestResult requestResult, PropertyContainer element) {
        if (requestResult.statusOtherThan(CREATED)) {
            final int status = requestResult.getStatus();
            throw new RuntimeException("Error creating relationship " + status+" "+requestResult.getText());
        }
        final String location = requestResult.getLocation();
        return new RestRelationship(location, facade);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PropertyContainer> RestIndex<T> getIndex(String indexName) {
        final RestIndexManager index = this.index();
        if (index.existsForNodes(indexName)) return (RestIndex<T>) index.forNodes(indexName);
        if (index.existsForRelationships(indexName)) return (RestIndex<T>) index.forRelationships(indexName);
        throw new IllegalArgumentException("Index " + indexName + " does not yet exist");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void createIndex(String type, String indexName, Map<String, String> config) {
        Map<String,Object> data=new HashMap<String, Object>();
        data.put("name",indexName);
        data.put("config",config);
        restRequest.post("index/" + type, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PropertyContainer> RestIndex<T> createIndex(Class<T> type, String indexName, Map<String, String> config) {
        if (Node.class.isAssignableFrom(type)) {
            return (RestIndex<T>) index().forNodes( indexName, config);
        }
        if (Relationship.class.isAssignableFrom(type)) {
            return (RestIndex<T>) index().forRelationships(indexName, config);
        }
        throw new IllegalArgumentException("Required Node or Relationship types to create index, got " + type);
    }

    @Override
    public RequestResult execute(RequestType requestType, String uri, Object params) {
        return requestType.makeRequest(uri, params, getRestRequest());
    }

    @Override
    public void close() {
        ExecutingRestRequest.shutdown();
    }

    @Override
    public boolean isAutoIndexingEnabled(Class<? extends PropertyContainer> clazz) {
        RequestResult response = getRestRequest().get(buildPathAutoIndexerStatus(clazz));
        if (response.statusIs(Response.Status.OK)) {
            return Boolean.parseBoolean(response.getText());
        } else {
            throw new IllegalStateException("received " + response);
        }
    }

    @Override
    public void setAutoIndexingEnabled(Class<? extends PropertyContainer> clazz, boolean enabled) {
        RequestResult response = getRestRequest().put(buildPathAutoIndexerStatus(clazz), enabled);
        if (response.statusOtherThan(Status.NO_CONTENT)) {
            throw new IllegalStateException("received " + response);
        }
    }

    @Override
    public Set<String> getAutoIndexedProperties(Class forClass) {
        RequestResult response = getRestRequest().get(buildPathAutoIndexerProperties(forClass).toString());
        Collection<String> autoIndexedProperties = (Collection<String>) JsonHelper.readJson(response.getText());
        return new HashSet<String>(autoIndexedProperties);
    }

    @Override
    public void startAutoIndexingProperty(Class forClass, String s) {
        try {
            // we need to use a inputstream instead of the string directly. Otherwise "post" implicitly uses
            // StreamJsonHelper.writeJsonTo which quotes a given string
            InputStream stream = new ByteArrayInputStream(s.getBytes("UTF-8"));
            RequestResult response = getRestRequest().post(buildPathAutoIndexerProperties(forClass).toString(), stream);
            if (response.statusOtherThan(Status.NO_CONTENT)) {
                throw new IllegalStateException("received " + response);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void stopAutoIndexingProperty(Class forClass, String s) {
        RequestResult response = getRestRequest().delete(buildPathAutoIndexerProperties(forClass).append("/").append(s).toString());
        if (response.statusOtherThan(Status.NO_CONTENT)) {
            throw new IllegalStateException("received " + response);
        }
    }

    private String buildPathAutoIndexerStatus(Class<? extends PropertyContainer> clazz) {
        return buildPathAutoIndexerBase(clazz).append("/status").toString();
    }

    private StringBuilder buildPathAutoIndexerProperties(Class<? extends PropertyContainer> clazz) {
        return buildPathAutoIndexerBase(clazz).append("/properties");
    }

    private StringBuilder buildPathAutoIndexerBase(Class<? extends PropertyContainer> clazz) {
        return new StringBuilder().append("index/auto/").append(clazz.getSimpleName().toLowerCase());
    }


    public RestRequest getRestRequest() {
        final BatchRestAPI restAPI = current();
        return restAPI == null ? restRequest : restAPI.getRestRequest();
    }

    private BatchRestAPI current() {
        return BatchTransaction.getRestApi();
    }


    @Override
    public TraversalDescription createTraversalDescription() {
        return new RestTraversal();
    }

    @Override
    public Node getReferenceNode() {
        Map<?, ?> map = restRequest.get("").toMap();
        String referenceNodeUri = (String) map.get("reference_node");
        if (referenceNodeUri==null) throw new NotFoundException("Reference node not available");

        RequestResult response = restRequest.get(referenceNodeUri);
        if (response.statusIs(Status.NOT_FOUND)) {
            throw new NotFoundException("Reference node not available");
        }
        return new RestNode(response.toMap(), facade);
    }

    public long getPropertyRefetchTimeInMillis() {
        return propertyRefetchTimeInMillis;
    }

    public String getBaseUri() {
        return restRequest.getUri();
    }


    public void setPropertyRefetchTimeInMillis(long propertyRefetchTimeInMillis) {
        this.propertyRefetchTimeInMillis = propertyRefetchTimeInMillis;
    }


    public <T> T executeBatch(BatchCallback<T> batchCallback) {
        throw new UnsupportedOperationException();
/*
        final BatchTransaction transaction = beginTx();
        try {
            T batchResult = batchCallback.recordBatch(transaction.getRestApi());
            transaction.success();
            return batchResult;
        } finally {
            transaction.finish();
        }
*/
    }

    @Override
    public BatchTransaction beginTx() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public Iterable<Relationship> wrapRelationships(RequestResult requestResult) {
        return (Iterable<Relationship>) new RelationshipIterableConverter(facade).convertFromRepresentation(requestResult);
    }

    public RestEntityExtractor createExtractor() {
        return new RestEntityExtractor(facade);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends PropertyContainer> IndexHits<S> queryIndex(String indexPath, Class<S> entityType) {
        RequestResult response = restRequest.get(indexPath);
        if (response.statusIs(Response.Status.OK)) {
            return new RestIndexHitsConverter(facade, entityType).convertFromRepresentation(response);
        } else {
            return new SimpleIndexHits<S>(Collections.emptyList(), 0, entityType, facade);
        }
    }
    
    @Override
    public void deleteEntity(RestEntity entity) {
        getRestRequest().with(entity.getUri()).delete( "" );
    }
    @Override
    public IndexInfo indexInfo(final String indexType) {
        RequestResult response = restRequest.get("index/" + indexType);
        return new RetrievedIndexInfo(response);
    }
    
    @Override
    public void setPropertyOnEntity(RestEntity entity, String key, Object value) {
        getRestRequest().with(entity.getUri()).put( "properties/" + key, value);
        entity.invalidatePropertyData();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPropertiesFromEntity(RestEntity entity){
        RequestResult response = getRestRequest().with(entity.getUri()).get("properties");
        Map<String, Object> properties;
        boolean ok = response.statusIs( Status.OK );
        if ( ok ) {
            properties = (Map<String, Object>) response.toMap(  );
        } else {
            properties = Collections.emptyMap();
        }
       
        return properties;
    }

    private void deleteIndex(String indexPath) {
        getRestRequest().delete(indexPath);
    }
    
    @Override
    public void delete(RestIndex index) {
        deleteIndex(index.indexPath(null,null));
    }
    
    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key, Object value) {
        deleteIndex(indexPath(index.indexPath(key, value), entity));
    }

    protected <T extends PropertyContainer> String indexPath(String indexPath, T restEntity) {
        return indexPath + "/" + ((RestEntity)restEntity).getId();
    }

    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key) {
        deleteIndex(indexPath(index.indexPath(key, null), entity));
    }

    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity) {
        deleteIndex(indexPath(index.indexPath( null, null), entity));
    }

    
    @Override
    public <T extends PropertyContainer> void addToIndex(T entity, RestIndex index, String key, Object value) {
        final RestEntity restEntity = (RestEntity) entity;
        String uri = restEntity.getUri();       
        if (value instanceof ValueContext) {
            value = ((ValueContext)value).getCorrectValue();
        }
        final Map<String, Object> data = MapUtil.map("key", key, "value", value, "uri", uri);       
        final RequestResult result = getRestRequest().post(index.indexPath(), data);
        if (result.statusOtherThan(Status.CREATED)) throw new RuntimeException(String.format("Error adding element %d %s %s to index %s", restEntity.getId(), key, value, index.getIndexName()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PropertyContainer> T putIfAbsent(T entity, RestIndex index, String key, Object value) {
        final RestEntity restEntity = (RestEntity) entity;
        String uri = restEntity.getUri();
        if (value instanceof ValueContext) {
            value = ((ValueContext)value).getCorrectValue();
        }
        final Map<String, Object> data = MapUtil.map("key", key, "value", value, "uri", uri);
        final RequestResult result = getRestRequest().post(index.uniqueIndexPath(), data);
        if (result.statusIs(Response.Status.CREATED)) {
            if (index.getEntityType().equals(Node.class)) return (T)createRestNode(result);
            if (index.getEntityType().equals(Relationship.class)) return (T)createRestRelationship(result,restEntity);
        }
        if (result.statusIs(Response.Status.OK)) {
            return (T)createExtractor().convertFromRepresentation(result);
        }
        throw new RuntimeException(String.format("Error adding element %d %s %s to index %s", restEntity.getId(), key, value, index.getIndexName()));
    }

    @Override
    public Map<?, ?> getData(RestEntity entity) {
        return getRestRequest().get(entity.getUri()).toMap();
    }

    @Override
    public boolean hasToUpdate(long lastUpdate) {
        return timeElapsed(lastUpdate, getPropertyRefetchTimeInMillis());
    }

    @Override
    public void removeProperty(RestEntity entity, String key) {
        restRequest.with(entity.getUri()).delete("properties/" + key);
        entity.invalidatePropertyData();
    }

    private boolean timeElapsed( long since, long isItGreaterThanThis ) {
        return System.currentTimeMillis() - since > isItGreaterThanThis;
    }

    @Override
    public RestNode getOrCreateNode(RestIndex<Node> index, String key, Object value, final Map<String, Object> properties) {
        if (index==null || key == null || value==null) throw new IllegalArgumentException("Unique index "+index+" key "+key+" value must not be null");
        final Map<String, Object> data = MapUtil.map("key", key, "value", value, "properties", properties);
        final RequestResult result = getRestRequest().post(index.uniqueIndexPath(), data);
        if (result.statusIs(Response.Status.CREATED) || result.statusIs(Response.Status.OK)) {
            return (RestNode)createExtractor().convertFromRepresentation(result);
        }
        throw new RuntimeException(String.format("Error retrieving or creating node for key %s and value %s with index %s", key, value, index.getIndexName()));
    }

    @Override
    public RestRelationship getOrCreateRelationship(RestIndex<Relationship> index, String key, Object value, final RestNode start, final RestNode end, final String type, final Map<String, Object> properties) {
        if (index==null || key == null || value==null) throw new IllegalArgumentException("Unique index "+index+" key "+key+" value must not be null");
        if (start == null || end == null || type == null) throw new IllegalArgumentException("Neither start, end nore type must be null");
        final Map<String, Object> data = MapUtil.map("key", key, "value", value, "properties", properties,"start",start.getUri(), "end",end.getUri(), "type",type);
        final RequestResult result = getRestRequest().post(index.uniqueIndexPath(), data);
        if (result.statusIs(Response.Status.CREATED) || result.statusIs(Response.Status.OK)) {
            return (RestRelationship)createExtractor().convertFromRepresentation(result);
        }
        throw new RuntimeException(String.format("Error retrieving or creating relationship for key %s and value %s with index %s", key, value, index.getIndexName()));
    }

    public <T> T getPlugin(Class<T> type){
        return RestInvocationHandler.getInvocationProxy(type, facade, new PluginInvocation(facade, type));
     }

     @Override
     public <T> T getService(Class<T> type, String baseUri){
        return RestInvocationHandler.getInvocationProxy(type, facade, new ServiceInvocation(facade, type, baseUri));
     }

    public Map<?, ?> query(String statement, Map<String, Object> params) {
        params =  (params==null) ? Collections.<String,Object>emptyMap() : params;
        final RequestResult requestResult = getRestRequest().post("cypher", MapUtil.map("query", statement, "params", params));
        return getRestRequest().toMap(requestResult);
    }

    @Override
    public Iterable<Relationship> getRelationships(RestNode restNode, String path) {
        return wrapRelationships(getRestRequest().with(restNode.getUri()).get(path));
    }

    private static final String FULLPATH = "fullpath";

    @Override
    public RestTraverser traverse(RestNode restNode, Map<String, Object> description) {
        final RequestResult result = getRestRequest().with(restNode.getUri()).post("traverse/" + FULLPATH, description);
        if (result.statusOtherThan(Response.Status.OK)) throw new RuntimeException(String.format("Error executing traversal: %d %s",result.getStatus(), description));
        final Object col = result.toEntity();
        if (!(col instanceof Collection)) throw new RuntimeException(String.format("Unexpected traversal result, %s instead of collection", col!=null ? col.getClass() : null));
        return new RestTraverser((Collection) col,restNode.getRestApi());
    }

    public QueryResult<Map<String, Object>> query(String statement, Map<String, Object> params, ResultConverter resultConverter) {
        final Map<?, ?> resultMap = query(statement, params);
        if (RestResultException.isExceptionResult(resultMap)) throw new RestResultException(resultMap);
        return new RestQueryResult(resultMap, facade, resultConverter);
    }

    public QueryResult<Object> run(String statement, Map<String, Object> params, ResultConverter resultConverter) {
        final  Map<String, Object> data = MapUtil.map("script", statement, "params", params);
        final RequestResult requestResult = getRestRequest().get("ext/GremlinPlugin/graphdb/execute_script", data);
        final Object result = requestResult.toEntity();
        if (requestResult.getStatus() == 500) {
            return handleError(result);
        } else {
            return new RestGremlinQueryResult(result, facade,resultConverter);
        }
    }

    private QueryResult<Object> handleError(Object result) {
        if (result instanceof Map) {
            Map<?, ?> mapResult = (Map<?, ?>) result;
            if (RestResultException.isExceptionResult(mapResult)) {
                throw new RestResultException(mapResult);
            }
        }
        throw new RestResultException(Collections.singletonMap("exception", result.toString()));
    }

    public RequestResult batch(Collection<Map<String, Object>> batchRequestData) {
        return restRequest.post("batch",batchRequestData);
    }
}
