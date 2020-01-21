package dev.lonsing.eclipse.plugins.workingsettools.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "dev.lonsing.eclipse.plugins.workingsettools.handlers.messages"; //$NON-NLS-1$
  public static String CreateWorkingSetWithDependenciesHandler_FailedToCheckJavaProject;
  public static String CreateWorkingSetWithDependenciesHandler_FailedToGetProjectDependencies;
  public static String CreateWorkingSetWithDependenciesHandler_MessageDialogTitle;
  public static String CreateWorkingSetWithDependenciesHandler_NoProjectSelected;
  public static String CreateWorkingSetWithDependenciesHandler_NotAJavaProject;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
