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
package org.neo4j.rest.graphdb.query;

import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestResultException;
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.converter.RestTableResultExtractor;
import org.neo4j.rest.graphdb.util.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author mh
 * @since 03.05.12
 */
public class RestGremlinQueryResult<T> implements QueryResult<T> {
    QueryResultBuilder<T> result;
    private final RestAPI restApi;


    @Override
    public <R> ConvertedResult<R> to(Class<R> type) {
        return result.to(type);
    }

    @Override
    public <R> ConvertedResult<R> to(Class<R> type, ResultConverter<T, R> converter) {
        return result.to(type, converter);
    }

    @Override
    public void handle(Handler<T> handler) {
        result.handle(handler);
    }

    @Override
    public Iterator<T> iterator() {
        return result.iterator();
    }

    public RestGremlinQueryResult(Object result, RestAPI restApi, ResultConverter resultConverter) {
        this.restApi = restApi;
        final Iterable<T> convertedResult = convertRestResult(result);
        this.result = new QueryResultBuilder<T>(convertedResult, resultConverter);
    }

    private Iterable<T> convertRestResult(Object result) {
        final RestEntityExtractor restEntityExtractor = new RestEntityExtractor(restApi);
        if (result instanceof Map) {
            Map<?, ?> mapResult = (Map<?, ?>) result;
            if (RestResultException.isExceptionResult(mapResult)) {
                throw new RestResultException(mapResult);
            }
            if (isTableResult(mapResult)) {
                return (Iterable<T>) new RestTableResultExtractor(restEntityExtractor).extract(mapResult);
            }
        }
        if (result instanceof Iterable) {
            return new IterableWrapper<T, Object>((Iterable<Object>) result) {
                @Override
                protected T underlyingObjectToObject(Object value) {
                    return (T) restEntityExtractor.convertFromRepresentation(value);
                }
            };
        }
        return Collections.singletonList((T) restEntityExtractor.convertFromRepresentation(result));
    }

    public static boolean isTableResult(Map<?, ?> mapResult) {
        return mapResult.containsKey("columns") && mapResult.containsKey("data");
    }
}
