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
package org.neo4j.rest.graphdb.entity;


import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.rest.graphdb.*;
import org.neo4j.rest.graphdb.util.ArrayConverter;

public class RestEntity implements PropertyContainer, UpdatableRestResult<RestEntity> {
    private Map<?, ?> structuralData;
    private Map<String, Object> propertyData;
    private long lastTimeFetchedPropertyData;
    protected RestAPI restApi;

    private final ArrayConverter arrayConverter=new ArrayConverter();
    private String uri;

    public RestEntity( URI uri, RestAPI restApi ) {
        this( uri.toString(), restApi );
    }    

    public RestEntity( String uri, RestAPI restApi ) {
        this.uri = uri;
        this.restApi = restApi;
    }      

    public RestEntity( Map<?, ?> data, RestAPI restApi ) {
        this.structuralData = data;
        this.restApi = restApi;
        this.propertyData = (Map<String, Object>) data.get( "data" );
        this.lastTimeFetchedPropertyData = System.currentTimeMillis();
        this.uri = (String) data.get( "self" );
    }

    public String getUri() {       
        return uri;
    }
    
    public void updateFrom(RestEntity updateEntity, RestAPI restApi){
        if (this == updateEntity){            
            this.lastTimeFetchedPropertyData = 0;
        }
        this.uri = updateEntity.getUri();
        this.structuralData = updateEntity.getStructuralData();
        this.propertyData = updateEntity.getPropertyData();    
        this.lastTimeFetchedPropertyData = System.currentTimeMillis();
    }    

    Map<?, ?> getStructuralData() {
        if ( this.structuralData == null ) {
            this.structuralData = restApi.getData(this);
        }
        return this.structuralData;
    }    
   
    Map<String, Object> getPropertyData() {       
        if (hasToUpdateProperties()) {            
        	this.propertyData = restApi.getPropertiesFromEntity(this);
            this.lastTimeFetchedPropertyData = System.currentTimeMillis();
        }
        return this.propertyData;
    }

    private boolean hasToUpdateProperties() {
        if (this.propertyData == null) return true;
        return restApi.hasToUpdate(this.lastTimeFetchedPropertyData);
    }


    public Object getProperty( String key ) {
        Object value = getPropertyValue(key);
        if ( value == null ) {
            throw new NotFoundException( "'" + key + "' on " + this );
        }
        return value;
    }

    private Object getPropertyValue( String key ) {
        Map<String, Object> properties = getPropertyData();
        Object value = properties.get( key );
        if ( value == null) return null;
        if ( value instanceof Collection ) {
            Collection col= (Collection) value;
            if (col.isEmpty()) return new String[0]; // todo concrete value type ?
            Object result = arrayConverter.toArray( col );
            if (result == null) throw new IllegalStateException( "Could not determine type of property "+key );
            properties.put(key,result);
            return result;

        }
        return PropertiesMap.assertSupportedPropertyValue( value );
    }

    public Object getProperty( String key, Object defaultValue ) {
        Object value = getPropertyValue( key );
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public Iterable<String> getPropertyKeys() {
        return new IterableWrapper( getPropertyData().keySet() ) {
            @Override
            protected String underlyingObjectToObject( Object key ) {
                return key.toString();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public Iterable<Object> getPropertyValues() {
        return (Iterable<Object>) getPropertyData().values();
    }

    public boolean hasProperty( String key ) {
        return getPropertyData().containsKey( key );
    }

    public Object removeProperty( String key ) {
        Object value = getProperty( key, null );
        restApi.removeProperty(this, key);
        return value;
    }

    public void setProperty( String key, Object value ) {
       this.restApi.setPropertyOnEntity(this, key, value);
    }

    public void invalidatePropertyData() {
        this.propertyData = null;
    }

    static long getEntityId( String uri ) {
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }

    public long getId() {        
        return getEntityId( getUri() );
    }

    public void delete() {
        this.restApi.deleteEntity(this);
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @Override
    public boolean equals( Object o ) {
        if (o == null) return false;
        return getClass().equals( o.getClass() ) && getId() == ( (RestEntity) o ).getId();
    }

       
    public RestGraphDatabase getGraphDatabase() {
    	 return new RestGraphDatabase(restApi);
    }

    @Override
    public String toString() {
        return getUri();
    }
    
    public RestAPI getRestApi() {
		return restApi;
	}

    public void setLastTimeFetchedPropertyData(long lastTimeFetchedPropertyData) {
        this.lastTimeFetchedPropertyData = lastTimeFetchedPropertyData;
    }
}
