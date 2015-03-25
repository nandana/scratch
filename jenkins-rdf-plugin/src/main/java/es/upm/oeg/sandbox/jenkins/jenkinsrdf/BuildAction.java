package es.upm.oeg.sandbox.jenkins.jenkinsrdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Run;

import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

public class BuildAction extends GetRDFAction {

    private AbstractBuild build;

    public BuildAction(AbstractBuild build) {
        this.build = build;
    }

    public String getTurtle() {

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("ex", "http://example.org/vocab#");

        Property generated = model.createProperty("http://example.org/vocab#", "generated");
        Property previous = model.createProperty("http://example.org/vocab#", "previous");
        Property duration = model.createProperty("http://example.org/vocab#", "duration");
        Property runOf = model.createProperty("http://example.org/vocab#", "runOf");
        Property status = model.createProperty("http://example.org/vocab#", "status");
        Property causeProp = model.createProperty("http://example.org/vocab#", "cause");

        Resource buildRes = model.createResource("");
        buildRes.addProperty(DCTerms.title, build.getFullDisplayName());

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(build.getStartTimeInMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

        buildRes.addProperty(DCTerms.created, fmt.format(cal.getTime()));
        buildRes.addProperty(duration, build.getDurationString());
        buildRes.addProperty(runOf, build.getProject().getUrl());
        buildRes.addProperty(status, build.getBuildStatusSummary().message);

        if(build.getPreviousBuild() != null){
            buildRes.addProperty(previous, "../" + build.getPreviousBuild().getUrl() +"rdf");
        }
        for (Object element : build.getArtifacts()) {
            Run.Artifact artifact = (Run.Artifact) element;
            buildRes.addProperty(generated, "./../" + artifact.getHref());
        }

        List<Cause> causes = build.getCauses();
        for (Cause cause : causes) {
            buildRes.addProperty(causeProp, cause.getShortDescription());
        }

        Writer writer = new StringWriter();
        model.write(writer, "TURTLE");

        return writer.toString();

    }


}
