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

package org.ldp4j.apps.o2jc;


import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.UUID;

public class MongoDBClient {

    private String username;
    private String password;

    public MongoDBClient() throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        properties.load(is);

        username = properties.getProperty("username");
        password = properties.getProperty("password");

        if (username == null || password == null) {
            throw new IllegalStateException("Username or/and password are null");
        }

    }

    public String persist(BasicDBObject dbObject) throws UnknownHostException {

        String uriString = String.format("mongodb://%s:%s@ds055680.mongolab.com:55680/test", username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());

        DBCollection contexts = db.getCollection("contexts");

        String id = UUID.randomUUID().toString();
        dbObject.put("_id", id);

        contexts.insert(dbObject);

        client.close();

        return id;

    }

    public DBObject find (String id) throws UnknownHostException {

        String uriString = String.format("mongodb://%s:%s@ds055680.mongolab.com:55680/test", username, password);

        MongoClientURI uri  = new MongoClientURI(uriString);
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());


        DBCollection coll = db.getCollection("contexts");

        BasicDBObject query = new BasicDBObject();
        query.put("_id", id);

        DBCursor cursor  = coll.find(query);

        try {
            if (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                dbObject.removeField("_id");
                return dbObject;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

}
