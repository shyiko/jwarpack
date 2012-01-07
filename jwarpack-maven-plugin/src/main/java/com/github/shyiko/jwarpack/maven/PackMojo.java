/*
 * Copyright 2012 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shyiko.jwarpack.maven;

import com.github.shyiko.jwarpack.packager.Metadata;
import com.github.shyiko.jwarpack.packager.Packager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @goal pack
 * @requiresDependancyResolution compile
 * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
 */
public class PackMojo extends AbstractMojo {

    private static final String ES_GROUP_PREFIX = "com.github.shyiko.jwarpack.es";
    private static final String ES_ARTIFACT_PREFIX = "jwarpack-es";

    /**
     * Location of ES JAR file. Required only if ES JAR is not declared inside
     * &lt;plugin&gt;&lt;dependencies&gt;...&lt;/dependencies&gt;&lt;/plugin&gt;.
     * @parameter expression="${jwarpack.serverLauncherJar}"
     */
    private File serverLauncherJar;

    /**
     * Location of application WAR file. Required only if application WAR is not declared inside
     * &lt;plugin&gt;&lt;dependencies&gt;...&lt;/dependencies&gt;&lt;/plugin&gt;.
     * @parameter expression="${jwarpack.applicationWar}"
     */
    private File applicationWar;

    /**
     * Output file or directory.
     * @parameter expression="${jwarpack.outputJar}" default-value="${basedir}/target/${project.build.finalName}.jar"
     * @required
     */
    private String outputJar;

    /**
     * True if compression should be used, false otherwise.
     * @parameter expression="${jwarpack.useCompression}" default-value="false"
     */
    private boolean useCompression;

    private MavenProject getProject() {
        return (MavenProject) getPluginContext().get("project");
    }

    private PluginDescriptor getPluginDescriptor() {
        return (PluginDescriptor) getPluginContext().get("pluginDescriptor");
    }

    private Plugin findPlugin(String groupId, String artifactId) {
        Plugin result = null;
        String key = groupId + ":" + artifactId;
        List buildPlugins = getProject().getBuildPlugins();
        for (Object buildPlugin : buildPlugins) {
            Plugin plugin = (Plugin) buildPlugin;
            if (plugin.getKey().equals(key)) {
                result = plugin;
                break;
            }
        }
        return result;
    }
    
    private File getESJar() throws MojoExecutionException {
        if (serverLauncherJar != null) {
            return serverLauncherJar;
        }
        String key = null;
        PluginDescriptor pluginDescriptor = getPluginDescriptor();
        Plugin plugin = findPlugin(pluginDescriptor.getGroupId(), pluginDescriptor.getArtifactId());
        for (Object o : plugin.getDependencies()) {
            Dependency dependency = (Dependency) o;
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            if (groupId.startsWith(ES_GROUP_PREFIX) &&
                artifactId.startsWith(ES_ARTIFACT_PREFIX)) {
                if (key != null) {
                    throw new MojoExecutionException("Multiple " + ES_ARTIFACT_PREFIX + " artifacts found. " +
                            "Please check <plugin><dependencies>...</dependencies></plugin>.");
                }
                key = groupId + ":" + artifactId;
            }
        }
        if (key == null) {
            throw new MojoExecutionException("No " + ES_ARTIFACT_PREFIX + " artifacts found. " +
                    "Please add one inside <plugin><dependencies>...</dependencies></plugin>.");
        }
        Artifact artifact = (Artifact) pluginDescriptor.getArtifactMap().get(key);
        return artifact.getFile();
    }
    
    private File getAppWar() throws MojoExecutionException {
        if (applicationWar != null) {
            return applicationWar;
        }
        String key = null;
        PluginDescriptor pluginDescriptor = getPluginDescriptor();
        Plugin plugin = findPlugin(pluginDescriptor.getGroupId(), pluginDescriptor.getArtifactId());
        for (Object o : plugin.getDependencies()) {
            Dependency dependency = (Dependency) o;
            if ("war".equals(dependency.getType())) {
                if (key != null) {
                    throw new MojoExecutionException("Multiple WAR artifacts found. " +
                            "Please check <plugin><dependencies>...</dependencies></plugin>.");
                }
                key = dependency.getGroupId() + ":" + dependency.getArtifactId();
            }
        }
        if (key == null) {
            throw new MojoExecutionException("No artifacts with <type>war</type> found. " +
                    "Please add one inside <plugin><dependencies>...</dependencies></plugin>.");
        }
        Artifact artifact = (Artifact) pluginDescriptor.getArtifactMap().get(key);
        return artifact.getFile();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log logger = getLog();
        File esJar = getESJar();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("ES JAR location: %s", esJar));
        }
        File appWar = getAppWar();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Application WAR location: %s", appWar));
        }
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Output JAR location: %s", outputJar));
        }
        Metadata metadata = new Metadata(esJar.getAbsolutePath(), appWar.getAbsolutePath(), outputJar, useCompression);
        try {
            new Packager().pack(metadata);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
            throw new MojoFailureException("Packaging failed. Error message: " + e.getMessage());
        }
    }
}
