package es.upm.oeg.sandbox.jenkins.jenkinsrdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class ProjectAction extends GetRDFAction {

    private AbstractProject project;

    public ProjectAction (AbstractProject project) {
        this.project = project;
    }

    @Override
    public String getTurtle() {

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("ex", "http://example.org/vocab#");

        Property hasBuild = model.createProperty("http://example.org/vocab#", "hasBuild");

        Resource projectRes = model.createResource("");
        projectRes.addProperty(DCTerms.title, project.getFullDisplayName());

        for (Object object : project._getRuns().entrySet()) {
            Run run = (Run)((Map.Entry) object).getValue();
            projectRes.addProperty(hasBuild, run.getUrl());
        }

        Writer writer = new StringWriter();
        model.write(writer, "TURTLE");

        return writer.toString();

    }
}
