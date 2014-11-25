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

import com.mongodb.BasicDBObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.riot.RiotException;
import org.ldp4j.apps.o2jc.util.O2JCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class URLContentUploaderServlet extends GenericUploaderServlet  {

    private static final Logger logger = LoggerFactory.getLogger(URLContentUploaderServlet.class);

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        logger.debug("Started processing the request ...");

        String url = request.getParameter("url");
        String content = request.getParameter("content");
        String format = request.getParameter("format");

        logger.debug("URL request parameter - '{}'", url);
        logger.debug("Content request parameter - \n'{}'", content);
        logger.debug("Formant request parameter - '{}'", format);

        BasicDBObject basicDBObject = null;

        if (content != null){

            if (format == null) {
                sendErrorMessage(request,response, "Content format is not specified in the request ...");
            }

            basicDBObject = generateJsonLDContext(new ByteArrayInputStream(content.getBytes()), format);


        } else {

            if(url == null) {
                sendErrorMessage(request,response, "URL is not specified in the request ...");
            }

            String mediaType = null;
            if (format == null || "TURTLE".equals(format)) {
                mediaType = "text/turtle";
            } else if ("RDF/XML".equals(format)) {
                mediaType = "application/rdf+xml";
            } else {
                sendErrorMessage(request, response, String.format("Unsupported media type - '%s'", format));
            }

            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            get.setHeader("Accept", mediaType);

            HttpResponse res = client.execute(get);

            if(res.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
                sendErrorMessage(request, response, String.format("Error retrieving the ontology '%s' : '%s' '%s'",
                        url, res.getStatusLine().getStatusCode(), res.getStatusLine().getReasonPhrase()));
                return;
            } else if (res.getEntity() == null) {
                sendErrorMessage(request, response, String.format("The ontology '%s' sent an empty response.", url));
                return;
            }

            try {
                basicDBObject = generateJsonLDContext(res.getEntity().getContent(), format);
            } catch (RiotException ex) {
                sendErrorMessage(request, response, String.format("Error unmarshalling the ontology '%s' as '%s' ",
                        url, ex.getMessage()));
                return;
            }

        }

        String id = persist(basicDBObject);
        request.setAttribute("contextURL", O2JCUtil.convertToContextURL(id));

        redirect(request, response, basicDBObject);


    }
}
