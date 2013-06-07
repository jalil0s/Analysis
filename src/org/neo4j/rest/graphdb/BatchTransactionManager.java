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

import javax.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * @author mh
 * @since 04.05.12
 */
public class BatchTransactionManager implements TransactionManager,Transaction {
    private final RestAPI restAPI;

    public BatchTransactionManager(RestAPI restAPI) {
        this.restAPI = restAPI;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        restAPI.beginTx();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        final BatchTransaction current = BatchTransaction.current();
        if (current==null) throw new IllegalStateException("Not in transaction");
        current.success();
        current.finish();
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws IllegalStateException, RollbackException, SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() throws SystemException {
        final BatchTransaction current = BatchTransaction.current();
        if (current==null) return 0;
        return Status.STATUS_ACTIVE;
    }

    @Override
    public void registerSynchronization(Synchronization synch) throws IllegalStateException, RollbackException, SystemException {

    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return this;
    }

    @Override
    public void resume(Transaction tobj) throws IllegalStateException, InvalidTransactionException, SystemException {
         throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        final BatchTransaction current = BatchTransaction.current();
        if (current==null) throw new IllegalStateException("Not in transaction");
        current.failure();
        current.finish();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        final BatchTransaction current = BatchTransaction.current();
        if (current==null) throw new IllegalStateException("Not in transaction");
        current.failure();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException();
    }
}
