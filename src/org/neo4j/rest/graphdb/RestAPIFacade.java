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
import org.neo4j.rest.graphdb.batch.BatchCallback;
import org.neo4j.rest.graphdb.batch.BatchRestAPI;
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.entity.RestEntity;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.IndexInfo;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.index.RestIndexManager;
import org.neo4j.rest.graphdb.services.RequestType;
import org.neo4j.rest.graphdb.transaction.NullTransaction;
import org.neo4j.rest.graphdb.traversal.RestTraverser;
import org.neo4j.rest.graphdb.util.Config;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.neo4j.rest.graphdb.util.ResultConverter;

import java.util.Map;
import java.util.Set;

/**
 * @author mh
 * @since 03.05.12
 */
public class RestAPIFacade implements RestAPI {

    @Override
    public RestIndexManager index() {
        return new RestIndexManager(this);
    }

    @Override
    public RestNode getNodeById(long id) {
        return current().getNodeById(id);
    }

    @Override
    public RestRelationship getRelationshipById(long id) {
        return current().getRelationshipById(id);
    }

    @Override
    public RestNode createNode(Map<String, Object> props) {
        return current().createNode(props);
    }

    @Override
    public RestNode createRestNode(RequestResult requestResult) {
        return current().createRestNode(requestResult);
    }

    @Override
    public RestRelationship createRelationship(Node startNode, Node endNode, RelationshipType type, Map<String, Object> props) {
        return current().createRelationship(startNode, endNode, type, props);
    }

    private RestAPI current() {
        final BatchRestAPI batchRestAPI = BatchTransaction.getRestApi();
        return batchRestAPI == null ? direct : batchRestAPI;
    }

    @Override
    public RestRelationship createRestRelationship(RequestResult requestResult, PropertyContainer element) {
        return current().createRestRelationship(requestResult, element);
    }

    @Override
    public <T extends PropertyContainer> RestIndex<T> getIndex(String indexName) {
        return current().getIndex(indexName);
    }

    @Override
    public void createIndex(String type, String indexName, Map<String, String> config) {
        direct.createIndex(type, indexName, config);
    }

    @Override
    public TraversalDescription createTraversalDescription() {
        return current().createTraversalDescription();
    }

    @Override
    public Node getReferenceNode() {
        return current().getReferenceNode();
    }

    @Override
    public Transaction beginTx() {
        if (Config.useBatchTransactions())
            return BatchTransaction.begin(this);
        return new NullTransaction();
    }

    @Override

    public <S extends PropertyContainer> IndexHits<S> queryIndex(String indexPath, Class<S> entityType) {
        return current().queryIndex(indexPath, entityType);
    }

    @Override
    public void deleteEntity(RestEntity entity) {
        current().deleteEntity(entity);
    }

    @Override
    public IndexInfo indexInfo(String indexType) {
        return direct.indexInfo(indexType);
    }

    @Override
    public void setPropertyOnEntity(RestEntity entity, String key, Object value) {
        current().setPropertyOnEntity(entity, key, value);
    }

    @Override

    public Map<String, Object> getPropertiesFromEntity(RestEntity entity) {
        return current().getPropertiesFromEntity(entity);
    }

    @Override
    public void delete(RestIndex index) {
        current().delete(index);
    }

    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key, Object value) {
        current().removeFromIndex(index, entity, key, value);
    }

    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key) {
        current().removeFromIndex(index, entity, key);
    }

    @Override
    public <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity) {
        current().removeFromIndex(index, entity);
    }

    @Override
    public <T extends PropertyContainer> void addToIndex(T entity, RestIndex index, String key, Object value) {
        current().addToIndex(entity, index, key, value);
    }

    @Override

    public <T extends PropertyContainer> T putIfAbsent(T entity, RestIndex index, String key, Object value) {
        return current().putIfAbsent(entity, index, key, value);
    }

    @Override
    public Map<?, ?> getData(RestEntity uri) {
        return current().getData(uri);
    }

    @Override
    public boolean hasToUpdate(long lastUpdate) {
        return current().hasToUpdate(lastUpdate);
    }

    @Override
    public void removeProperty(RestEntity entity, String key) {
        current().removeProperty(entity, key);
    }

    @Override
    public Map<?, ?> query(String statement, Map<String, Object> params) {
        return current().query(statement, params);
    }

    @Override
    public Iterable<Relationship> getRelationships(RestNode restNode, String path) {
        return current().getRelationships(restNode, path);
    }

    @Override
    public RestTraverser traverse(RestNode restNode, Map<String, Object> description) {
        return current().traverse(restNode, description);
    }

    @Override
    public String getBaseUri() {
        return current().getBaseUri();
    }

    @Override
    public <T> T getPlugin(Class<T> pluginType) {
        return current().getPlugin(pluginType);
    }

    @Override
    public <T> T getService(Class<T> type, String baseUri) {
        return current().getService(type, baseUri);
    }


    @Override
    public RestNode getOrCreateNode(RestIndex<Node> index, String key, Object value, Map<String, Object> properties) {
        return current().getOrCreateNode(index, key, value, properties);
    }

    @Override
    public RestRelationship getOrCreateRelationship(RestIndex<Relationship> index, String key, Object value, RestNode start, RestNode end, String type, Map<String, Object> properties) {
        return current().getOrCreateRelationship(index, key, value, start, end, type, properties);
    }

    @Override
    public QueryResult<Map<String, Object>> query(String statement, Map<String, Object> params, ResultConverter resultConverter) {
        return current().query(statement, params, resultConverter);
    }

    @Override
    public QueryResult<Object> run(String statement, Map<String, Object> params, ResultConverter resultConverter) {
        return current().run(statement, params, resultConverter);
    }

    @Override
    public RestEntityExtractor createExtractor() {
        return current().createExtractor();
    }

    @Override

    public <T extends PropertyContainer> RestIndex<T> createIndex(Class<T> type, String indexName, Map<String, String> config) {
        return current().createIndex(type, indexName, config);
    }

    @Override
    public RequestResult execute(RequestType requestType, String uri, Object params) {
        return current().execute(requestType, uri, params);
    }

    public void close() {
        BatchTransaction.shutdown();
    }

    @Override
    public boolean isAutoIndexingEnabled(Class<? extends PropertyContainer> clazz) {
        return current().isAutoIndexingEnabled(clazz);
    }

    @Override
    public void setAutoIndexingEnabled(Class<? extends PropertyContainer> clazz, boolean enabled) {
        current().setAutoIndexingEnabled(clazz, enabled);
    }

    @Override
    public Set<String> getAutoIndexedProperties(Class forClass) {
        return current().getAutoIndexedProperties(forClass);
    }

    @Override
    public void startAutoIndexingProperty(Class forClass, String s) {
        current().startAutoIndexingProperty(forClass, s);
    }

    @Override
    public void stopAutoIndexingProperty(Class forClass, String s) {
        current().stopAutoIndexingProperty(forClass, s);
    }

    private final ExecutingRestAPI direct;

    private RestAPIFacade(ExecutingRestAPI direct) {
        this.direct = direct;
    }

    public RestAPIFacade(String uri) {
        this.direct = new ExecutingRestAPI(uri, this);
    }

    public RestAPIFacade(String uri, String user, String password) {
        this.direct = new ExecutingRestAPI(uri, user, password, this);
    }

    public ExecutingRestAPI getDirect() {
        return direct;
    }

    public <T> T executeBatch(BatchCallback<T> batchCallback) {
        final BatchTransaction transaction = BatchTransaction.begin(this);
        try {
            T batchResult = batchCallback.recordBatch(this);
            transaction.success();
            return batchResult;
        } finally {
            transaction.finish();
        }
    }
}
