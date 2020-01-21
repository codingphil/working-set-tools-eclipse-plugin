package dev.lonsing.eclipse.plugins.workingsettools.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "dev.lonsing.eclipse.plugins.workingsettools.dialogs.messages"; //$NON-NLS-1$
  public static String CreateWorkingSetDialog_ProjectSelectionListMessage;
  public static String CreateWorkingSetDialog_Title;
  public static String CreateWorkingSetDialog_ValidationEmptyWorkingSetName;
  public static String CreateWorkingSetDialog_ValidationNoProjectsSelected;
  public static String CreateWorkingSetDialog_ValidationWorkingSetAlreadyExists;
  public static String CreateWorkingSetDialog_WorkingSetNameTextFieldLabel;
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
