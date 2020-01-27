package dev.lonsing.eclipse.plugins.workingsettools.handlers;

import static dev.lonsing.eclipse.plugins.workingsettools.handlers.ProjectUtil.getActiveProject;
import static dev.lonsing.eclipse.plugins.workingsettools.handlers.ProjectUtil.isJavaProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import dev.lonsing.eclipse.plugins.workingsettools.dialogs.CreateWorkingSetDialog;

public class CreateWorkingSetWithDependenciesHandler extends AbstractHandler {
  private static final String JAVA_WORKING_SET_ID = "org.eclipse.jdt.ui.JavaWorkingSetPage"; //$NON-NLS-1$

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IProject activeProject = getActiveProject(event);
    if (activeProject == null) {
      showInformationMessage(event, Messages.CreateWorkingSetWithDependenciesHandler_NoProjectSelected);
      return null;
    }
    if (!isJavaProject(activeProject)) {
      showInformationMessage(event, Messages.CreateWorkingSetWithDependenciesHandler_NotAJavaProject);
      return null;
    }
    List<IProject> projectDependencies = getProjectDependencies(activeProject);
    CreateWorkingSetDialogResult dialogResult = showCreateWorkingSetDialog(event, activeProject, projectDependencies);
    if (dialogResult == null) {
      return null;
    }
    createWorkingSet(dialogResult.getWorkingSetName(), dialogResult.getProjects());
    return null;
  }

  private IWorkingSet createWorkingSet(String name, IProject[] selectedProjects) {
    IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    IWorkingSet workingSet = workingSetManager.createWorkingSet(name, selectedProjects);
    workingSet.setId(JAVA_WORKING_SET_ID);
    workingSetManager.addWorkingSet(workingSet);
    return workingSetManager.getWorkingSet(workingSet.getName());
  }

  private CreateWorkingSetDialogResult showCreateWorkingSetDialog(ExecutionEvent event, IProject activeProject,
      List<IProject> projectDependencies) throws ExecutionException {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    List<String> existingWorkingSetNames = getExistingWorkingSetNames();
    CreateWorkingSetDialog dialog = new CreateWorkingSetDialog(window.getShell(), activeProject.getName(),
        activeProject, projectDependencies, existingWorkingSetNames);
    if (dialog.open() == IDialogConstants.OK_ID) {
      return new CreateWorkingSetDialogResult(dialog.getWorkingSetName().trim(), dialog.getSelectedProjects());
    } else {
      return null;
    }
  }

  private List<IProject> getProjectDependencies(IProject rootProject) throws ExecutionException {
    try {
      DependencyCalculator dependencyCalculator = new DependencyCalculator();
      List<IProject> projects = new ArrayList<>();
      projects.addAll(dependencyCalculator.getReferencedProjects(rootProject));
      // Use loop instead of a recursion to avoid endless loop in case of a circular
      // dependency.
      int i = 0;
      while (i < projects.size()) {
        IProject currentProject = projects.get(i);
        Set<IProject> currentReferencedProjects = dependencyCalculator.getReferencedProjects(currentProject);
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

  private void showInformationMessage(ExecutionEvent event, String message) throws ExecutionException {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    MessageDialog.openInformation(window.getShell(),
        Messages.CreateWorkingSetWithDependenciesHandler_MessageDialogTitle, message);
  }

  private static List<String> getExistingWorkingSetNames() {
    IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    return Arrays.stream(workingSetManager.getAllWorkingSets()).map(IWorkingSet::getName).collect(Collectors.toList());
  }
}
