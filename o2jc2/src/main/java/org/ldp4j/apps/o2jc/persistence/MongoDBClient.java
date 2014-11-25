/*
 * Copyright 2014 Ontology Engineering Group, Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.ldp4j.apps.o2jc.persistence;


import com.mongodb.*;
import org.ldp4j.apps.o2jc.Vocab;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MongoDBClient {

    private String username;
    private String password;

    public static final String DB_CONNECTION_URI = "mongodb://%s:%s@ds055680.mongolab.com:55680/test";

    public static final String DB_COLLECTION = "contexts";

    public static final String ID = "_id";

    public MongoDBClient(String username, String password) throws IOException {

        if (username == null || password == null) {
            throw new IllegalStateException("Username or/and password are null");
        }

        this.username = username;
        this.password = password;

    }

    public String persist(BasicDBObject dbObject) throws UnknownHostException {

        String uriString = String.format(DB_CONNECTION_URI, username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());

        DBCollection contexts = db.getCollection(DB_COLLECTION);

        String id = UUID.randomUUID().toString();
        dbObject.put(ID, id);

        contexts.insert(dbObject);

        client.close();

        return id;

    }

    public DBObject findByID(String id) throws UnknownHostException {

        String uriString = String.format(DB_CONNECTION_URI, username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());


        DBCollection coll = db.getCollection(DB_COLLECTION);

        BasicDBObject query = new BasicDBObject();
        query.put(ID, id);

        DBCursor cursor  = coll.find(query);

        try {
            if (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                dbObject.removeField(ID);
                return dbObject;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    public List<DBObject> findByURI(String ontoUri) throws UnknownHostException {

        String uriString = String.format(DB_CONNECTION_URI, username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());


        DBCollection coll = db.getCollection(DB_COLLECTION);

        BasicDBObject query = new BasicDBObject();
        query.put(Vocab.ID, ontoUri);

        DBCursor cursor  = coll.find(query);

        List<DBObject> matches = new ArrayList<DBObject>();

        try {
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                dbObject.removeField(ID);
                matches.add(dbObject);
            }
            return  matches;
        } finally {
            cursor.close();
        }

    }

    public List<DBObject> findByKey(String key) throws UnknownHostException {

        String uriString = String.format(DB_CONNECTION_URI, username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());

        DBCollection coll = db.getCollection(DB_COLLECTION);

        DBObject query = new BasicDBObject(key, new BasicDBObject("$exists", true));

        DBCursor cursor  = coll.find(query);

        List<DBObject> matches = new ArrayList<DBObject>();

        try {
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                dbObject.removeField(ID);
                matches.add(dbObject);
            }
            return  matches;
        } finally {
            cursor.close();
        }

    }


}
