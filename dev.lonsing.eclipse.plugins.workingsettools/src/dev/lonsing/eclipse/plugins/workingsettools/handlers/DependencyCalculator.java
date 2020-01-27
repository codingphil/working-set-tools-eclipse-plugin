package dev.lonsing.eclipse.plugins.workingsettools.handlers;

import static dev.lonsing.eclipse.plugins.workingsettools.handlers.ProjectUtil.isJavaProject;
import static dev.lonsing.eclipse.plugins.workingsettools.handlers.ProjectUtil.isMavenProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactRef;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

public class DependencyCalculator {
  private final Map<String, IProject> artifactIdToMavenProject = new HashMap<>();
  private final IMavenProjectRegistry projectRegistry = MavenPlugin.getMavenProjectRegistry();

  public DependencyCalculator() {
    initMavenProjects();
  }

  public List<IProject> getProjectDependencies(IProject rootProject) throws ExecutionException {
    try {
      List<IProject> projects = new ArrayList<>();
      projects.addAll(getReferencedProjects(rootProject));
      // Use loop instead of a recursion to avoid endless loop in case of a circular
      // dependency.
      int i = 0;
      while (i < projects.size()) {
        IProject currentProject = projects.get(i);
        Set<IProject> currentReferencedProjects = getReferencedProjects(currentProject);
        for (IProject project : currentReferencedProjects) {
          if (!projects.contains(project) && isJavaProject(project)) {
            projects.add(project);
          }
        }
        i++;
      }
      return projects;
    } catch (CoreException e) {
      throw new ExecutionException(Messages.CreateWorkingSetWithDependenciesHandler_FailedToGetProjectDependencies, e);
    }
  }

  public Set<IProject> getReferencedProjects(IProject project) throws CoreException {
    Set<IProject> referencedProjects = new HashSet<>();
    if (project.isOpen()) {
      referencedProjects.addAll(Arrays.asList(project.getReferencedProjects()));
      referencedProjects.addAll(getReferencedMavenProjects(project));
    }
    return referencedProjects;
  }

  private List<IProject> getReferencedMavenProjects(IProject project) {
    if (!isMavenProject(project)) {
      return Collections.emptyList();
    }
    IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
    Set<ArtifactRef> artifacts = mavenProjectFacade.getMavenProjectArtifacts();
    return artifacts.stream().map(this::getArtifactIdWithoutVersion).map(this.artifactIdToMavenProject::get)
        .filter(Objects::nonNull).collect(Collectors.toList());
  }

  private void initMavenProjects() {
    List<IProject> allMavenProjects = ProjectUtil.getAllProjects().stream().filter(ProjectUtil::isMavenProject)
        .collect(Collectors.toList());
    for (IProject project : allMavenProjects) {
      String id = getArtifactIdWithoutVersion(project);
      if (id != null) {
        artifactIdToMavenProject.put(id, project);
      }
    }
  }

  private String getArtifactIdWithoutVersion(ArtifactRef artifactRef) {
    return artifactRef.getGroupId() + ":" + artifactRef.getArtifactId();
  }

  private String getArtifactIdWithoutVersion(IProject project) {
    IMavenProjectFacade mavenProjectFacade = projectRegistry.getProject(project);
    if ((mavenProjectFacade == null) || (mavenProjectFacade.getArtifactKey() == null)) {
      return null;
    }
    String artifactId = mavenProjectFacade.getArtifactKey().getArtifactId();
    String groupId = mavenProjectFacade.getArtifactKey().getGroupId();
    if ((artifactId != null) && (groupId != null)) {
      return groupId + ":" + artifactId;
    }
    return null;
  }

}
