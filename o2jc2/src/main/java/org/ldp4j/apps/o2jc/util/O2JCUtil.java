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

package org.ldp4j.apps.o2jc.util;

import com.google.common.base.Preconditions;
import org.ldp4j.apps.o2jc.listeners.ConfigManager;

public class O2JCUtil {

    private static final String CONTEXT_PATH = "contexts/";

    public static String convertToContextURL(String id){

        Preconditions.checkNotNull(id, "JSON-LD context URL generation error - ID can not be null");

        return ConfigManager.getBaseURL(true) + CONTEXT_PATH + id;

    }
}
