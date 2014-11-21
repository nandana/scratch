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

package org.ldp4j.apps.o2jc.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.mongodb.BasicDBObject;
import org.apache.jena.riot.RiotException;
import org.ldp4j.apps.o2jc.JsonLDContextGenerator;
import org.ldp4j.apps.o2jc.O2JCAppException;
import org.ldp4j.apps.o2jc.persistence.MongoDBClient;
import org.ldp4j.apps.o2jc.listeners.ConfigManager;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;


public class GenericUploaderServlet extends HttpServlet {

    protected  BasicDBObject generateJsonLDContext(InputStream io, String format)
            throws RiotException {

        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        base.read(io, null, format);

        JsonLDContextGenerator generator = new JsonLDContextGenerator();

        return  generator.process(base);

    }

    protected String persist(BasicDBObject dbObject) throws IOException {

        ConfigManager.DBCredentials credentials = ConfigManager.getDBCredentials();

        MongoDBClient dbClient = new MongoDBClient(credentials.getUsername(), credentials.getPassword());
        String id = dbClient.persist(dbObject);

        return id;
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, BasicDBObject dbObject) {

        String id = dbObject.get(MongoDBClient.ID).toString();

        //We can move this bit to the client side and do the pretty printing in the browser if needed
        dbObject.removeField(MongoDBClient.ID);
        ObjectMapper mapper = new ObjectMapper();

        String output = null;
        try {
            output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbObject);
        } catch (JsonProcessingException e) {
            throw new O2JCAppException("Error parsing JSON object from MongoDB ...", e);
        }

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/context.jsp");

        request.setAttribute("jsonData", output);

        try {
            dispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new O2JCAppException("Error forwarding the request ...", e);
        } catch (IOException e) {
            throw new O2JCAppException("Error forwarding the request ...", e);
        }

    }

    protected void sendErrorMessage (HttpServletRequest request, HttpServletResponse response, String msg) {

        RequestDispatcher errorDispatcher = getServletContext().getRequestDispatcher("/error.jsp");
        request.setAttribute("errorMessage", msg);
        try {
            errorDispatcher.forward(request, response);
        } catch (ServletException e) {
            throw new O2JCAppException("Error forwarding the request ...", e);
        } catch (IOException e) {
            throw new O2JCAppException("Error forwarding the request ...", e);
        }
    }

}
