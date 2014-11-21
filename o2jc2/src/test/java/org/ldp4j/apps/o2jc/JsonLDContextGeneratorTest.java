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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mongodb.BasicDBObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class JsonLDContextGeneratorTest {

    public static final Logger logger = LoggerFactory.getLogger(JsonLDContextGeneratorTest.class);

    @Test
    public void testOntologyWithMetadata() throws Exception {

        URL url = this.getClass().getClassLoader().getResource("onto-metadata.ttl");
        File owlFile = new File(url.toURI());

        JsonLDContextGenerator contextGenerator = new JsonLDContextGenerator(owlFile, "TURTLE");
        BasicDBObject jsonLDContext = contextGenerator.process();

        String contextString = jsonLDContext.toString();

        logger.debug("JSON-LD Context {} \n", contextString);

        InputStream io = new ByteArrayInputStream(jsonLDContext.toString().getBytes());

        //Build the RDF model from the JSON-LD context
        Model model = ModelFactory.createDefaultModel();
        model.read(io, null, "JSON-LD");

        Resource ontology = model.getResource("http://example.org/vocab/terms#");
        assertThat("Ontology shouldn't be null", ontology, is(notNullValue()));

        Statement titleStatement = ontology.getProperty(DCTerms.title);
        assertThat("Title shouldn't be null", titleStatement, is(notNullValue()));

        String title = titleStatement.getObject().asLiteral().getLexicalForm();
        assertThat("Checking the title value", title, is("My Ontology"));

        Statement descriptionStatement = ontology.getProperty(DCTerms.description);
        assertThat("Description shouldn't be null", descriptionStatement, is(notNullValue()));

        String description = descriptionStatement.getObject().asLiteral().getLexicalForm();
        assertThat("Checking the description value", description, is("Simple vocabulary with some metadata"));

        Statement labelStatement = ontology.getProperty(RDFS.label);
        assertThat("Label shouldn't be null", labelStatement, is(notNullValue()));

        String label = labelStatement.getObject().asLiteral().getLexicalForm();
        assertThat("Checking the label value", label, is("My Ontology"));

        Statement seeAlsoStatement = ontology.getProperty(RDFS.seeAlso);
        assertThat("See also shouldn't be null", seeAlsoStatement, is(notNullValue()));

        String seeAlso = seeAlsoStatement.getObject().asResource().getURI();
        assertThat("Checking the see also value", seeAlso, is("http://example.org/vocab/ontology.html"));

    }

    @Test
    public void testOntologyWithoutMetadata() throws Exception {

        URL url = this.getClass().getClassLoader().getResource("onto-without-metadata.ttl");
        File owlFile = new File(url.toURI());

        JsonLDContextGenerator contextGenerator = new JsonLDContextGenerator(owlFile, "TURTLE");
        BasicDBObject jsonLDContext = contextGenerator.process();

        String contextString = jsonLDContext.toString();

        logger.debug("JSON-LD Context {} \n", contextString);

        InputStream io = new ByteArrayInputStream(jsonLDContext.toString().getBytes());

        //Build the RDF model from the JSON-LD context
        Model model = ModelFactory.createDefaultModel();
        model.read(io, null, "JSON-LD");

        List<Resource> ontologyList = model.listSubjectsWithProperty(RDF.type, OWL.Ontology).toList();

        assertThat("There should be one resource which is an ontology", ontologyList.size(), is(1));

    }

    @Test
    public void testValidateValidContext() {

    }

    @Test
    public void testValidateInvalidContext() {

    }


}
