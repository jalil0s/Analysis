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
import org.neo4j.kernel.impl.core.LockReleaser;
import org.neo4j.rest.graphdb.index.RestIndexManager;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.transaction.NullTransactionManager;
import org.neo4j.rest.graphdb.util.ResultConverter;

import javax.transaction.TransactionManager;
import java.util.Map;


public class RestGraphDatabase extends AbstractRemoteDatabase {
    private RestAPI restAPI;
    private final RestCypherQueryEngine cypherQueryEngine;


    public RestGraphDatabase( RestAPI api){
    	this.restAPI = api;
        cypherQueryEngine = new RestCypherQueryEngine(restAPI);
    }
    
    public RestGraphDatabase( String uri ) {
        this( new RestAPIFacade( uri ));
    }

    public RestGraphDatabase( String uri, String user, String password ) {
        this(new RestAPIFacade( uri, user, password ));
    }

    public RestAPI getRestAPI(){
    	return this.restAPI;
    }

    public RestIndexManager index() {
       return this.restAPI.index();
    }

    public Node createNode() {
    	return this.restAPI.createNode(null);
    }
  
    public Node getNodeById( long id ) {
    	return this.restAPI.getNodeById(id);
    }

    public Node getReferenceNode() {
        return this.restAPI.getReferenceNode();
    }

    @Override
    public Iterable<Node> getAllNodes() {
        return cypherQueryEngine.query("start n=node(*) return n", null).to(Node.class);
    }

    @Override
    public Iterable<RelationshipType> getRelationshipTypes() {
        return cypherQueryEngine.query("start n=node(*) match n-[r]->() return distinct type(r) as rel_type", null).to(RelationshipType.class, new ResultConverter<Map<String, Object>, RelationshipType>() {
            @Override
            public RelationshipType convert(Map<String, Object> row, Class<RelationshipType> type) {
                return DynamicRelationshipType.withName((String)row.get("rel_type"));
            }
        });
    }

    public Relationship getRelationshipById( long id ) {
    	return this.restAPI.getRelationshipById(id);
    }    
    @Override
    public String getStoreDir() {
        return restAPI.getBaseUri();
    }

    @Override
    public TransactionManager getTxManager() {
        return new BatchTransactionManager(restAPI); //new NullTransactionManager();
    }

    @Override
    public Transaction beginTx() {
        return restAPI.beginTx();
    }

    @Override
    public void shutdown() {
        restAPI.close();
    }

	@Override
	@Deprecated
	public LockReleaser getLockReleaser() {
		// TODO Auto-generated method stub
		return null;
	}
}

