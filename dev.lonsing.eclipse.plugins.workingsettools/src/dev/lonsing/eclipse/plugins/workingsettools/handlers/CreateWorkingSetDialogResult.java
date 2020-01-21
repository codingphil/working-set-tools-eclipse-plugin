package dev.lonsing.eclipse.plugins.workingsettools.handlers;

import org.eclipse.core.resources.IProject;

public class CreateWorkingSetDialogResult {
  public CreateWorkingSetDialogResult(String workingSetName, IProject[] projects) {
    this.workingSetName = workingSetName;
    this.projects = projects;
  }

  public String getWorkingSetName() {
    return workingSetName;
  }

  public void setWorkingSetName(String workingSetName) {
    this.workingSetName = workingSetName;
  }

  public IProject[] getProjects() {
    return projects;
  }

  public void setProjects(IProject[] projects) {
    this.projects = projects;
  }

  private String workingSetName;
  private IProject[] projects;
}
