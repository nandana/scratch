package org.ldp4j.apps.test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.util.JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class MongoDBClient {

    public static void main(String[] args) throws IOException {

        MongoClientURI uri  = new MongoClientURI("mongodb://nandana:mongolab@ds055680.mongolab.com:55680/test");
        MongoClient client = new MongoClient(uri);
        DB db = client.getDB(uri.getDatabase());

        DBCollection contexts = db.getCollection("contexts");

        BasicDBObject foaf = new BasicDBObject();
        foaf.put("_id","1");
        foaf.put("fname","Nandana");
        foaf.put("lname","Mihindukulasooriya");

        contexts.insert(foaf);

        JsonNodeFactory factory = new JsonNodeFactory(false);
        JsonFactory jsonFactory = new JsonFactory();

        // the root node
        ObjectNode root = factory.objectNode();
        ObjectNode context = factory.objectNode();

        context.put("test", "testValue");
        context.put("test2", "testValue2");
        root.put("@context", context);

        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mapper.writer().writeValue(output, root);
       // Object o = ((DBObject)JSON.parse("Your JSON structure or JSONObj.toString()");



        client.close();

    }

}
