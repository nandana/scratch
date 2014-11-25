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

package org.ldp4j.apps.o2jc.listeners;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class RequestListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent event) {


    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {

        if ( ConfigManager.getBaseURL(false) == null &&  event.getServletRequest() instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest)event.getServletRequest();

            StringBuffer url = request.getRequestURL();
            String uri = request.getRequestURI();
            String ctx = request.getContextPath();
            String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";

            ConfigManager.setBaseURL(base);

        }

    }
}
