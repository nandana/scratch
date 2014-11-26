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

import com.google.common.io.Files;
import com.mongodb.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class MongoDBClientTest {

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    public static final String HOST = "localhost";

    public static final int PORT = 12345;

    public static final String DB_NAME = "test";

    public static final String MONGO_DB_URI = String.format("mongodb://%s:%d/%s",HOST, PORT, DB_NAME);

    private MongodExecutable executable;
    private MongodProcess mongodProcess;

    private MongoClient mongoClient;

    @Before
    public void setUp() throws Exception {

        executable = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .build());
        mongodProcess = executable.start();

        mongoClient = new MongoClient(HOST, PORT);
    }

    @Test
    public void testFindByID() throws IOException, URISyntaxException {

        Mongo mongo = getMongo();
        System.out.println(mongo.getAddress().toString());
        DB db = mongo.getDB("test");
        DBCollection contexts = db.getCollection("contexts");

        DBObject foafJson = getJSONObject("foaf.json");
        foafJson.put("_id", "101");
        contexts.insert(foafJson);

        MongoDBClient dbClient = new MongoDBClient(MONGO_DB_URI);
        DBObject result = dbClient.findByID("101");

        assertThat("The result shouldn't be null", result, is(notNullValue()));

    }

    @Test
    public void testFindByKey() throws IOException, URISyntaxException {

        Mongo mongo = getMongo();
        System.out.println(mongo.getAddress().toString());
        DB db = mongo.getDB("test");
        DBCollection contexts = db.getCollection("contexts");

        DBObject foafJson = getJSONObject("foaf.json");
        foafJson.put("_id", "101");
        contexts.insert(foafJson);

        MongoDBClient dbClient = new MongoDBClient(MONGO_DB_URI);
        List<DBObject> result = dbClient.findByKey("Person");

        assertThat("There should be exactly one result", result.size(), is(1));

        result = dbClient.findByKey("NotInFOAFVocab");

        assertThat("There should be zero results", result.size(), is(0));


    }

    @After
    public void tearDown() throws Exception {

        mongodProcess.stop();
        executable.stop();
    }

    public Mongo getMongo() {
        return mongoClient;
    }

    public DBObject getJSONObject(String file) throws URISyntaxException, IOException {

        URL url = this.getClass().getClassLoader().getResource(file);
        File jsonFile = new File(url.toURI());

        String jsonString = Files.toString(jsonFile, StandardCharsets.UTF_8);

        Object o = com.mongodb.util.JSON.parse(jsonString);
        DBObject dbObj = (DBObject) o;

        return  dbObj;

    }

}
