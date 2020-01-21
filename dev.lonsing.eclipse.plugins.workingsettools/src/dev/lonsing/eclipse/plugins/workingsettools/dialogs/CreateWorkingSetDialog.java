package dev.lonsing.eclipse.plugins.workingsettools.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class CreateWorkingSetDialog extends ListSelectionDialog {

  private String workingSetName;
  private List<String> existingWorkingSetNames;
  private ControlDecoration workingSetNameDecoration;
  private ControlDecoration projectListDecoration;

  public CreateWorkingSetDialog(Shell parentShell, String workingSetName, IProject rootProject,
      List<IProject> projectDependencies, List<String> existingWorkingSetNames) {
    super(parentShell, createProjectArray(rootProject, projectDependencies), ArrayContentProvider.getInstance(),
        new WorkbenchLabelProvider(), Messages.CreateWorkingSetDialog_ProjectSelectionListMessage);
    this.workingSetName = workingSetName;
    if (existingWorkingSetNames == null) {
      this.existingWorkingSetNames = List.of();
    } else {
      this.existingWorkingSetNames = List.copyOf(existingWorkingSetNames);
    }
    this.setTitle(Messages.CreateWorkingSetDialog_Title);
    this.setInitialElementSelections(createProjectList(rootProject, projectDependencies));
  }

  public String getWorkingSetName() {
    return workingSetName;
  }

  public void setWorkingSetName(String workingSetName) {
    this.workingSetName = workingSetName;
  }

  public IProject[] getSelectedProjects() {
    Object[] resultArray = this.getResult();
    IProject[] resultProjectArray = new IProject[resultArray.length];
    System.arraycopy(resultArray, 0, resultProjectArray, 0, resultArray.length);
    return resultProjectArray;
  }

  @Override
  public void create() {
    super.create();
    CheckboxTableViewer listViewer = super.getViewer();
    listViewer.addPostSelectionChangedListener(event -> validate());
    projectListDecoration = createWarningDecoration(listViewer.getControl());
    addButtonSelectionListener(IDialogConstants.SELECT_ALL_ID, event -> validate());
    addButtonSelectionListener(IDialogConstants.DESELECT_ALL_ID, event -> validate());
    validate();
  }

  @Override
  protected Label createMessageArea(Composite composite) {
    createWorkingSetNameTextField(composite);
    return super.createMessageArea(composite);
  }

  private void createWorkingSetNameTextField(Composite composite) {
    Composite container = new Composite(composite, SWT.NONE);
    container.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    container.setLayout(layout);

    Label labelWorkingSetName = new Label(container, SWT.NONE);
    labelWorkingSetName.setText(Messages.CreateWorkingSetDialog_WorkingSetNameTextFieldLabel);
    GridData labelWorkingSetNameGridData = new GridData();
    labelWorkingSetNameGridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
    labelWorkingSetName.setLayoutData(labelWorkingSetNameGridData);

    Text textFieldWorkingSetName = new Text(container, SWT.BORDER);
    GridData gridDataWorkingSetName = new GridData();
    gridDataWorkingSetName.grabExcessHorizontalSpace = true;
    gridDataWorkingSetName.horizontalAlignment = GridData.FILL;
    gridDataWorkingSetName.horizontalIndent = 10;
    textFieldWorkingSetName.setLayoutData(gridDataWorkingSetName);
    if (workingSetName == null) {
      workingSetName = ""; //$NON-NLS-1$
    }
    textFieldWorkingSetName.setText(workingSetName);
    textFieldWorkingSetName.setSelection(0, workingSetName.length());

    workingSetNameDecoration = createWarningDecoration(textFieldWorkingSetName);

    textFieldWorkingSetName.addModifyListener(e -> {
      Text textWidget = (Text) e.getSource();
      workingSetName = textWidget.getText();
      validate();
    });
  }

  private ControlDecoration createWarningDecoration(Control control) {
    ControlDecoration decoration = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
    Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING)
        .getImage();
    decoration.setDescriptionText(""); //$NON-NLS-1$
    decoration.setImage(image);
    decoration.setShowOnlyOnFocus(false);
    decoration.setMarginWidth(2);
    return decoration;
  }

  private void validate() {
    boolean valid = validateWorkingSetName();
    valid = validateSelectedProjects() && valid;
    getOkButton().setEnabled(valid);
  }

  private boolean validateSelectedProjects() {
    boolean valid = getViewer().getCheckedElements().length > 0;
    if (!valid) {
      projectListDecoration.setDescriptionText(Messages.CreateWorkingSetDialog_ValidationNoProjectsSelected);
      projectListDecoration.show();
    } else {
      projectListDecoration.hide();
    }
    return valid;
  }

  private boolean validateWorkingSetName() {
    boolean valid = validateWorkingSetNameIsNotEmpty() && validateWorkingSetNameDoesNotAlreadyExist();
    if (valid) {
      workingSetNameDecoration.hide();
    }
    return valid;
  }

  private boolean validateWorkingSetNameIsNotEmpty() {
    boolean valid = !workingSetName.isBlank();
    if (!valid) {
      workingSetNameDecoration.setDescriptionText(Messages.CreateWorkingSetDialog_ValidationEmptyWorkingSetName);
      workingSetNameDecoration.show();
    }
    return valid;
  }

  private boolean validateWorkingSetNameDoesNotAlreadyExist() {
    boolean valid = !this.existingWorkingSetNames.contains(this.workingSetName);
    if (!valid) {
      workingSetNameDecoration.setDescriptionText(Messages.CreateWorkingSetDialog_ValidationWorkingSetAlreadyExists);
      workingSetNameDecoration.show();
    }
    return valid;
  }

  private void addButtonSelectionListener(int buttonId, Consumer<SelectionEvent> listener) {
    this.getButton(buttonId).addSelectionListener(SelectionListener.widgetSelectedAdapter(listener));
  }

  private static IProject[] createProjectArray(IProject rootProject, List<IProject> projectDependencies) {
    return createProjectList(rootProject, projectDependencies).toArray(IProject[]::new);
  }

  private static List<IProject> createProjectList(IProject rootProject, List<IProject> projectDependencies) {
    List<IProject> allProjects = new ArrayList<>();
    allProjects.add(rootProject);
    allProjects.addAll(projectDependencies);
    allProjects.sort((a, b) -> {
      return a.getName().compareToIgnoreCase(b.getName());
    });
    return allProjects;
  }

  public List<String> getExistingWorkingSetNames() {
    return existingWorkingSetNames;
  }

  public void setExistingWorkingSetNames(List<String> existingWorkingSetNames) {
    this.existingWorkingSetNames = existingWorkingSetNames;
  }

}
