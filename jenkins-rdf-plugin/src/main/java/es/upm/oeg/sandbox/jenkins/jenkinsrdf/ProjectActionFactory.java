package es.upm.oeg.sandbox.jenkins.jenkinsrdf;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

@Extension
public class ProjectActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject project) {
        return Collections.singleton(new ProjectAction(project));
    }
}
