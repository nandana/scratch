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

import com.google.common.html.HtmlEscapers;
import com.mongodb.DBObject;
import org.ldp4j.apps.o2jc.listeners.ConfigManager;
import org.ldp4j.apps.o2jc.persistence.MongoDBClient;
import org.ldp4j.apps.o2jc.util.O2JCUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ContextSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        String type = request.getParameter("type");
        String uri = request.getParameter("uri");
        String key = request.getParameter("key");

        if(type == null){
            throw new ServletException("The type of the search is not specified.");
        } else if ("uri".equals(type)) {

            ConfigManager.DBCredentials credentials = ConfigManager.getDBCredentials();

            MongoDBClient dbClient = new MongoDBClient(credentials.getUsername(), credentials.getPassword());
            List<DBObject> results = dbClient.findByURI(uri);

            if(results.size() == 0){
                PrintWriter writer = response.getWriter();
                writer.println("<ul class=\"list-group\">");
                writer.println("<li class=\"list-group-item\" >No Matches found</li>");
                writer.println("</ul>");
                writer.flush();
                return;
            } else {
                PrintWriter writer = response.getWriter();
                writer.println("<ul class=\"list-group\">");
                for (DBObject dbObject : results) {
                    String contextID = (String) dbObject.get("_id");
                    String contextURL = O2JCUtil.convertToContextURL(contextID);
                    String contextURLEncoded = HtmlEscapers.htmlEscaper().escape(contextURL);
                    writer.println(String.format("<li class=\"list-group-item\" >%s</li>", contextURLEncoded));
                }
                writer.println("</ul>");
                writer.flush();
            }

        } else if ("key".equals(type)) {

            ConfigManager.DBCredentials credentials = ConfigManager.getDBCredentials();

            MongoDBClient dbClient = new MongoDBClient(credentials.getUsername(), credentials.getPassword());
            List<DBObject> results = dbClient.findByKey(key);

            if(results.size() == 0){
                PrintWriter writer = response.getWriter();
                writer.println("<ul class=\"list-group\">");
                writer.println("<li class=\"list-group-item\" >No Matches found</li>");
                writer.println("</ul>");
                writer.flush();
                return;
            } else {
                PrintWriter writer = response.getWriter();
                writer.println("<ul class=\"list-group\">");
                for (DBObject dbObject : results) {
                    String contextID = (String) dbObject.get("_id");
                    String contextURL = O2JCUtil.convertToContextURL(contextID);
                    String contextURLEncoded = HtmlEscapers.htmlEscaper().escape(contextURL);
                    writer.println(String.format("<li class=\"list-group-item\" >%s</li>", contextURLEncoded));
                }
                writer.println("</ul>");
                writer.flush();
            }

        }

    }
}
