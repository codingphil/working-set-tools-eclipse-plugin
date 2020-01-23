package dev.lonsing.eclipse.plugins.workingsettools.exceptions;

public class WorkingSetToolsException extends RuntimeException {

  private static final long serialVersionUID = 463546198344117069L;

  public WorkingSetToolsException(String message) {
    super(message);
  }

  public WorkingSetToolsException(String message, Throwable cause) {
    super(message, cause);
  }

  public WorkingSetToolsException(Throwable cause) {
    super(cause);
  }

}
