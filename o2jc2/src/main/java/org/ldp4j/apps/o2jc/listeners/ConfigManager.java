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

import org.ldp4j.apps.o2jc.O2JCAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static DBCredentials dbCredentials;

    public static final String CONFIG_FILE = "config.properties";

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new O2JCAppException("Database credentials - couldn't load the credentials ...", e);
        }

        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        if (username == null || password == null) {
            throw new O2JCAppException("Database credentials - username or/and password are null");
        }

        dbCredentials = new DBCredentials(username, password);

        logger.debug("Loaded the database credentials from {} ...", CONFIG_FILE);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    public static DBCredentials getDBCredentials(){
        return dbCredentials;
    }

    public class DBCredentials {

        private final String username;
        private final String password;

        DBCredentials(String username, String password){
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

    }
}
