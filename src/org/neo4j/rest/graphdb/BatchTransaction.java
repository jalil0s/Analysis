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

import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.batch.BatchRestAPI;
import org.neo4j.rest.graphdb.batch.RestOperations;

/**
* @author mh
* @since 03.05.12
*/
public class BatchTransaction implements Transaction {
    private static ThreadLocal<BatchTransaction> current = new ThreadLocal<BatchTransaction>();
    private final BatchRestAPI batchRestAPI;
    private Boolean success=null;
    private int depth = 1;

    public static BatchRestAPI getRestApi() {
        final BatchTransaction currentTx = current.get();
        if (currentTx !=null) return currentTx.batchRestAPI;
        return null;
    }

    public static BatchTransaction current() {
        return current.get();
    }
    BatchTransaction(RestAPIFacade restAPI) {
        if (current.get()!=null) throw new IllegalStateException("Transaction already in progress!");
        this.batchRestAPI=createBatchRestAPI(restAPI);
        current.set(this);
    }


    @Override
    public void failure() {
        success = false;
    }

    @Override
    public void success() {
        if (success==null) success = true;
    }

    @Override
    public void finish() {
        depth--;
        if (depth > 0) return; // unroll stack
        if (depth < 0) throw new IllegalStateException("transaction already finished");
        try {
        if (success!=null && success) {
            final BatchTransaction currentTx = current.get();
            if (currentTx!=null) {
                current.remove();
                currentTx.batchRestAPI.executeBatchRequest();
            } else {
                throw new IllegalStateException("Not in Transaction/BatchOperation");
            }
        }
        } finally {
            current.remove();
        }
    }

    @Override
    public Lock acquireWriteLock(PropertyContainer propertyContainer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lock acquireReadLock(PropertyContainer propertyContainer) {
        throw new UnsupportedOperationException();
    }

    public static synchronized BatchTransaction begin(RestAPIFacade facade) {
        final BatchTransaction batchTransaction = current.get();
        if (batchTransaction !=null) {
            batchTransaction.depth++;
            return batchTransaction;
        }
        final BatchTransaction transaction = new BatchTransaction(facade);
        current.set(transaction);
        return transaction;
    }

    private static BatchRestAPI createBatchRestAPI(RestAPIFacade facade) {

        return new BatchRestAPI(facade.getBaseUri(),facade);
    }

    public static void shutdown() {
        current.remove();
        current = new ThreadLocal<BatchTransaction>();
    }
}
