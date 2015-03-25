package es.upm.oeg.sandbox.jenkins.jenkinsrdf;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link es.upm.oeg.sandbox.jenkins.jenkinsrdf.GetRDFBuilder.GetRDFBuilderDescriptor#newInstance(StaplerRequest)} is invoked
 * and a new {@link GetRDFBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream.
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 */
@ExportedBean
public class GetRDFBuilder extends Builder {

   // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public GetRDFBuilder() {

    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        BuildAction buildAction = new BuildAction(build);
        build.addAction(buildAction);

        return true;
    }

    /**
     * Descriptor for {@link GetRDFBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class GetRDFBuilderDescriptor extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Enable RDF plugin";
        }


        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            return super.configure(req,formData);
        }

    }
}

