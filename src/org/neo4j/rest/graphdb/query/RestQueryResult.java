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

import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.converter.RestEntityExtractor;
import org.neo4j.rest.graphdb.converter.RestTableResultExtractor;
import org.neo4j.rest.graphdb.util.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mh
 * @since 03.05.12
 */
public class RestQueryResult implements QueryResult<Map<String, Object>> {
    private final QueryResultBuilder<Map<String, Object>> result;

    @Override
    public <R> ConvertedResult<R> to(Class<R> type) {
        return result.to(type);
    }

    @Override
    public <R> ConvertedResult<R> to(Class<R> type, ResultConverter<Map<String, Object>, R> converter) {
        return result.to(type, converter);
    }

    @Override
    public void handle(Handler<Map<String, Object>> handler) {
        result.handle(handler);
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        return result.iterator();
    }

    public RestQueryResult(Map<?, ?> result, RestAPI restApi, ResultConverter resultConverter) {
        final RestTableResultExtractor extractor = new RestTableResultExtractor(new RestEntityExtractor(restApi));
        final List<Map<String, Object>> data = extractor.extract(result);
        this.result = new QueryResultBuilder<Map<String, Object>>(data, resultConverter);
    }
}
