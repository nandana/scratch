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

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.mongodb.BasicDBObject;

import java.io.*;
import java.util.*;

public class JsonLDContextGenerator {

	private File owlFile;

	private String format;

	// Counter for generating namespace prefixes
	private int nsCounter = 0;

	Map<String,String> nsMap;
	Map<String,String> nsInverseMap = new HashMap<String,String>();

	BiMap<String, String> terms = HashBiMap.create();

	public JsonLDContextGenerator(File owlFile, String format) {
		this.owlFile = owlFile;
		this.format = format;
	}

	public BasicDBObject process() throws IOException {

		OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		base.read(new FileInputStream(owlFile), null, format);

		BasicDBObject root = new BasicDBObject();

		BasicDBObject context = generateContext(base);

		root.put(Vocab.CONTEXT, context);

		generateOntologyMetadata(base, root, context);

		validate(root);

		return root;
	}

	protected BasicDBObject generateContext(OntModel base)  throws IOException {

        nsMap = base.getNsPrefixMap();

		// Provide a inverse map with namespace as the key and prefix as the value
		// Sometimes the same namespace is defined using multiple prefixes and in that case we will
		// only have one of the prefixes but it is not a problem as we just want to one prefix for the namespace.
		// One option is to use a com.google.common.collect.BiMap
        for (Map.Entry<String, String>  entry : nsMap.entrySet()) {
        	nsInverseMap.put(entry.getValue(), entry.getKey());
        }

		// Generate the namespace declarations in the context
        BasicDBObject context = new BasicDBObject();
        for (String prefix : nsMap.keySet()){
        	context.put(prefix, nsMap.get(prefix));
        }

		// Declare the namespaces used by the generator if they are not already generated
		generateNSPrefix(RDFS.getURI(), "rdfs");
		generateNSPrefix(OWL.getURI(), "owl");
		generateNSPrefix(DCTerms.getURI(), "dcterms");


		// Declare all the properties
        for (OntProperty property : base.listAllOntProperties().toSet()) {
        	
        	String ns = property.getNameSpace();
			String prefix = nsInverseMap.get(ns);

			// If the prefix is null, generate a new prefix and declare the namespace with a prefix
			if(prefix == null) {
				prefix = generateNSPrefix(ns, null);
				context.put(prefix, ns);
			}

        	String name = property.getLocalName();

			//We first check whether we already defined the local name i.e. same local name with different namespaces.
			//If so, we append the name with a integer. Not the best solution, but, well ...
			int counter = 0;
			while (terms.keySet().contains(name)) {
            	name =	name + counter++;
			}
			terms.put(name, property.getURI());

			// Declare the property in the JSON-LD context
			BasicDBObject dec = new BasicDBObject();
            dec.put(Vocab.ID, String.format("%s:%s", prefix, name));
            if (property.isObjectProperty()) {
            	dec.put(Vocab.TYPE, Vocab.ID);
            }
            
            context.put(name, dec);
        	
        }

		//Declare all the classes
		for (OntClass clazz : base.listClasses().toSet()) {

			String ns = clazz.getNameSpace();
			String prefix = nsInverseMap.get(ns);

			// If the prefix is null, generate a new prefix and declare the namespace with a prefix
			if(prefix == null) {
				prefix = generateNSPrefix(ns, null);
				context.put(prefix, ns);
			}

			String name = clazz.getLocalName();

			//We first check whether we already defined the local name i.e. same local name with different namespaces.
			//If so, we append the name with a integer. Not the best solution, but, well ...
			int counter = 0;
			while (terms.keySet().contains(name)) {
				name =	name + counter++;
			}
			terms.put(name, clazz.getURI());

			context.put(name, String.format("%s:%s", prefix, name));

		}

		return context;
		
	}

	protected  void generateOntologyMetadata(OntModel base, BasicDBObject root, BasicDBObject context) {

		//These can't be null because we declare them in the start if not present
		String owlPrefix = nsInverseMap.get(OWL.getURI());
		String rdfsPrefix = nsInverseMap.get(RDFS.getURI());

		Model model = base.getBaseModel();

		ResIterator resIterator = model.listSubjectsWithProperty(RDF.type, OWL.Ontology);

		List<Resource> ontologyList = resIterator.toList();
		if(ontologyList.size() != 1) {
			root.put(Vocab.ID, "_:ontology");
			root.put(Vocab.TYPE, owlPrefix + ":Ontology");
		} else {
			Resource ontology = ontologyList.get(0);
			if(!ontology.isAnon()) {
				root.put(Vocab.ID, ontology.getURI());
			} else {
				root.put(Vocab.ID, "_:ontology");
			}
			root.put(Vocab.TYPE, owlPrefix + ":Ontology");

			StmtIterator properties = ontology.listProperties();
			while (properties.hasNext()){
				Statement statement = properties.nextStatement();
				Property predicate = statement.getPredicate();
				String term = getTerm(predicate, context);

				RDFNode object = statement.getObject();
				if(object.isLiteral()) {
					root.put(term, object.asLiteral().getLexicalForm());
				} else if (object.isURIResource()) {
					BasicDBObject resource = new BasicDBObject();
					resource.put(Vocab.ID, object.asResource().getURI());
					resource.put(Vocab.TYPE, Vocab.ID);
					root.put(term, resource);
				}
			}

		}
	}

	protected String getTerm(Property predicate, BasicDBObject context) {

		String predicateURI = predicate.getURI();

		// If this property is already defined in the context, use the term that was used.
		String term = terms.inverse().get(predicateURI);
		if (term != null) {
			return term;
		}

		String nameSpace = predicate.getNameSpace();
		String name = predicate.getLocalName();

		String prefix = nsMap.get(nameSpace);

		if (prefix == null) {
			prefix = generateNSPrefix(nameSpace, null);
			context.put(prefix, nameSpace);
		}

		//We first check whether we already defined the local name i.e. same local name with different namespaces.
		//If so, we append the name with a integer. Not the best solution, but, well ...
		int counter = 0;
		while (terms.keySet().contains(name)) {
			name =	name + counter++;
		}

		terms.put(name, predicateURI);
		context.put(name, String.format("%s:%s", prefix, name));

		return name;
	}



	/**
	 * Assign a prefix for a namespace
	 * @param ns the namespace to be assigned a prefix
	 * @param preferredPrefix preferred prefix that is commonly used
	 * @return the prefix that was assigned
	 */
	protected String generateNSPrefix(String ns, String preferredPrefix){

		String prefix = nsInverseMap.get(ns);
		// If the namespace is already declared, we don't have to do anything.
		if(prefix != null){
			return prefix;
		}

		// If the namespace is not declared in the ontology file, we have to add it.
		// Check if the common prefix is used by some other namespace. It is rare but not impossible.
			if (preferredPrefix != null && !nsMap.keySet().contains(preferredPrefix)) {
				addNsPrefix(preferredPrefix, ns);
				return preferredPrefix;
			} else {
				String generatedPrefix = getNextNsPrefix();
				addNsPrefix(generatedPrefix, ns);
				return  generatedPrefix;
			}
	}

	/***
	 * Returns a namespace prefix that is not used by the current document and add it to the namespace maps
	 * @return a unique prefix
	 */
	protected String getNextNsPrefix(){

		Set<String> prefixes = nsMap.keySet();

		String prefix = String.format("ns%d", nsCounter);
		while(prefixes.contains(prefix)) {
			nsCounter++;
			prefix = String.format("ns%d", nsCounter);
		}

		return prefix;

	}

	/***
	 * Add the namespace and the prefix to a map
	 * @param prefix prefix
	 * @param ns namespace
	 */
	protected void addNsPrefix(String prefix, String ns) {
		nsMap.put(prefix, ns);
		nsInverseMap.put(ns, prefix);
	}


	protected void validate(BasicDBObject context)  {

		Preconditions.checkNotNull(context, "Context to be validated can not be null");

		InputStream io = new ByteArrayInputStream(context.toString().getBytes());

		//Build the model to check whether the JSON-LD context is valid
		Model model = ModelFactory.createDefaultModel();
		model.read(io, null, "JSON-LD");

	}

}
