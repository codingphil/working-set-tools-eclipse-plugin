package dev.lonsing.eclipse.plugins.workingsettools.handlers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import dev.lonsing.eclipse.plugins.workingsettools.exceptions.WorkingSetToolsException;

public class ProjectUtil {

  public static List<IProject> getAllProjects() {
    return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
  }

  public static boolean isJavaProject(IProject project) {
    return hasProjectNature(project, JavaCore.NATURE_ID);
  }

  public static boolean isMavenProject(IProject project) {
    return hasProjectNature(project, "org.eclipse.m2e.core.maven2Nature");
  }

  public static IProject getActiveProject(ExecutionEvent event) {
    ISelection sel = HandlerUtil.getCurrentSelection(event);

    if (sel instanceof IStructuredSelection) {
      Object selected = ((IStructuredSelection) sel).getFirstElement();
      if (selected != null) {
        IResource resource = Platform.getAdapterManager().getAdapter(selected, IResource.class);
        if (resource != null) {
          return resource.getProject();
        }
      }
    }
    return null;
  }

  private static boolean hasProjectNature(IProject project, String natureId) {
    try {
      return project.isOpen() && Arrays.asList(project.getDescription().getNatureIds()).indexOf(natureId) != -1;
    } catch (CoreException e) {
      throw new WorkingSetToolsException(Messages.CreateWorkingSetWithDependenciesHandler_FailedToCheckProjectNature,
          e);
    }
  }

  private ProjectUtil() {
  }

}
