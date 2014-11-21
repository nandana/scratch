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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.ldp4j.apps.o2jc.O2JCAppException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class OntologyUploaderServlet extends GenericUploaderServlet  {

    private static final long serialVersionUID = 1L;
 
    // upload settings
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
 
    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        // checks if the request actually contains upload file
        if (!ServletFileUpload.isMultipartContent(request)) {
            // if not, send an error message
            sendErrorMessage(request, response, "The request must be enctype=multipart/form-data.");
            return;
        }

        //Check whether we can generate the context URI
        String servletName = "generate";
        String servletURL = request.getRequestURL().toString();
        String baseURL;

        if (servletURL.endsWith(servletName)) {
            baseURL = servletURL.substring(0, (servletURL.length() - servletName.length()));
        } else {
            sendErrorMessage(request, response, "Error occurred while generating the context URL from the servlet " +
                    "URL " + servletURL);
            return;
        }
 
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        
        //String uploadPath = System.getenv("OPENSHIFT_DATA_DIR");
        String uploadPath = "/home/nandana/dev/temp";
        
        // sets temporary location to store files
        factory.setRepository(new File(uploadPath));
 
        ServletFileUpload upload = new ServletFileUpload(factory);
         
        // sets maximum size of upload file
        upload.setFileSizeMax(MAX_FILE_SIZE);
         
        // sets maximum size of request (include file + form data)
        upload.setSizeMax(MAX_REQUEST_SIZE);
 
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        
        try {
            // parses the request's content to extract file data
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);
 
            if (formItems != null && formItems.size() > 0) {
                // iterates over form's fields
            	String fileName;
            	File storeFile = null;
            	FileItem fileItem = null;
            	String format = null;
            	
                for (FileItem item : formItems) {
                    // processes only fields that are not form fields
                    if (!item.isFormField()) {
                    	fileItem = item;
                        fileName = new File(item.getName()).getName();
                        String filePath = uploadPath + File.separator + fileName;
                        storeFile = new File(filePath);
                        item.write(storeFile);

                    } else {
                    	if ("format".equals(item.getFieldName())) {
                    		format = item.getString();
                    	}
                    }
                }
                
                if(storeFile == null) {
                    sendErrorMessage(request, response, "Error occurred while uploading the file ...");
                    return;
                } else if (format == null) {
                    sendErrorMessage(request, response, "Format parameter is not specified ...");
                    return;
                }

                BasicDBObject basicDBObject = generateJsonLDContext(new FileInputStream(storeFile), format);

                if (fileItem != null) {
                    fileItem.delete();
                }

                String id = persist(basicDBObject);
                request.setAttribute("contextURL", baseURL +"contexts/" + id);

                redirect(request, response, basicDBObject);

            }
            
        } catch (Exception ex) {
            throw new O2JCAppException("Error processing the request ...", ex);
        }
    }

}
