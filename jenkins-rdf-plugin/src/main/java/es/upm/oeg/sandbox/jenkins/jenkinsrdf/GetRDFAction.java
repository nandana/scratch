package es.upm.oeg.sandbox.jenkins.jenkinsrdf;

import hudson.model.Action;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class GetRDFAction implements Action {

    @Override
    @Exported(inline=true)
    public String getIconFileName() {
        return "/plugin/jenkins-rdf/images/rdf.jpg";
    }

    @Override
    @Exported(inline=true)
    public String getDisplayName() {
        return "Get RDF";
    }

    @Override
    @Exported(inline=true)
    public String getUrlName() {
        return "rdf";
    }

    @Exported(inline=true)
    public String[] getSupportedFormats() {
        return new String[]{"text/turtle"};
    }

    public abstract String getTurtle();

}
