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
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.entity.RestEntity;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.IndexInfo;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.index.RestIndexManager;
import org.neo4j.rest.graphdb.services.RequestType;
import org.neo4j.rest.graphdb.traversal.RestTraverser;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.neo4j.rest.graphdb.util.ResultConverter;

import java.util.Map;
import java.util.Set;

/**
 * @author mh
 * @since 02.05.12
 */
public interface RestAPI {
    RestIndexManager index();

    RestNode getNodeById(long id);

    RestRelationship getRelationshipById(long id);

    RestNode createNode(Map<String, Object> props);

    RestNode createRestNode(RequestResult requestResult);

    RestRelationship createRelationship(Node startNode, Node endNode, RelationshipType type, Map<String, Object> props);

    RestRelationship createRestRelationship(RequestResult requestResult, PropertyContainer element);

    @SuppressWarnings("unchecked")
    <T extends PropertyContainer> RestIndex<T> getIndex(String indexName);

    @SuppressWarnings("unchecked")
    void createIndex(String type, String indexName, Map<String, String> config);

    TraversalDescription createTraversalDescription();

    Node getReferenceNode();

    Transaction beginTx();

    @SuppressWarnings("unchecked")
    <S extends PropertyContainer> IndexHits<S> queryIndex(String indexPath, Class<S> entityType);

    void deleteEntity(RestEntity entity);

    IndexInfo indexInfo(String indexType);

    void setPropertyOnEntity(RestEntity entity, String key, Object value);

    @SuppressWarnings("unchecked")
    Map<String, Object> getPropertiesFromEntity(RestEntity entity);

    void delete(RestIndex index);

    <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key, Object value);

    <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity, String key);

    <T extends PropertyContainer> void removeFromIndex(RestIndex index, T entity);

    <T extends PropertyContainer> void addToIndex(T entity, RestIndex index, String key, Object value);

    @SuppressWarnings("unchecked")
    <T extends PropertyContainer> T putIfAbsent(T entity, RestIndex index, String key, Object value);

    Map<?,?> getData(RestEntity uri);

    boolean hasToUpdate(long lastUpdate);

    void removeProperty(RestEntity entity, String key);

    Map<?, ?> query(String statement, Map<String, Object> params);

    Iterable<Relationship> getRelationships(RestNode restNode, String path);


    RestTraverser traverse(RestNode restNode, Map<String, Object> description);

    String getBaseUri();

    <T> T getPlugin(Class<T> pluginType);

    <T> T getService(Class<T> type, String baseUri);

    <T> T executeBatch(BatchCallback<T> batchCallback);

    RestNode getOrCreateNode(RestIndex<Node> index, String key, Object value, Map<String, Object> properties);

    RestRelationship getOrCreateRelationship(RestIndex<Relationship> index, String key, Object value, RestNode start, RestNode end, String type, Map<String, Object> properties);

    QueryResult<Map<String, Object>> query(String statement, Map<String, Object> params, ResultConverter resultConverter);

    QueryResult<Object> run(String statement, Map<String, Object> params, ResultConverter resultConverter);

    RestEntityExtractor createExtractor();

    @SuppressWarnings("unchecked")
    <T extends PropertyContainer> RestIndex<T> createIndex(Class<T> type, String indexName, Map<String, String> config);

    RequestResult execute(RequestType requestType, String uri, Object params);

    void close();

    boolean isAutoIndexingEnabled(Class<? extends PropertyContainer> clazz);

    void setAutoIndexingEnabled(Class<? extends PropertyContainer> clazz, boolean enabled);

    Set<String> getAutoIndexedProperties(Class forClass);

    void startAutoIndexingProperty(Class forClass, String s);

    void stopAutoIndexingProperty(Class forClass, String s);
}
