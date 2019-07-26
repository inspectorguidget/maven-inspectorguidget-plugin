package fr.inria.inspectorguidget.plugin;

import com.beust.klaxon.Klaxon;
import fr.inria.inspectorguidget.api.analyser.UIDataAnalyser;

import fr.inria.inspectorguidget.data.UIData;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

@Mojo( name = "extractdata",requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class InspectorMojo extends AbstractMojo {

    //maven project to analyse
    @Parameter( defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    // file containing data
    @Parameter(defaultValue = "data.json", required =true, readonly = true)
    private String dataFileName;

    // type of the analyser
    @Parameter (required = true)
    private String analyserType;


    public void execute() throws MojoExecutionException {

        UIDataAnalyser analyser = new UIDataAnalyser();
        PrintWriter pw = null;

        getLog().info("adding Input Ressources...");
        analyser.addInputResource(mavenProject.getBasedir().getAbsolutePath());

        getLog().info("adding dependencies path...");
        Set<Artifact> setDependency = mavenProject.getArtifacts();

        int i=0;
        String[] dependencies = new String[setDependency.size()];
        for(Artifact artifact : setDependency){
            dependencies[i++] = artifact.getFile().getAbsolutePath();
        }

        analyser.setSourceClasspath(dependencies);

        getLog().info("Extracting data...");
        UIData data = analyser.extractUIData();

        getLog().info("building data file...");
        try {
            pw = new PrintWriter(dataFileName);
            pw.print(new Klaxon().toJsonString(data,null));
        } catch (FileNotFoundException e) {
            getLog().error(e.fillInStackTrace());
        } finally {
            if(pw != null) {
                pw.close();
            }
        }
    }
}