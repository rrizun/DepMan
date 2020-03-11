package dependencymanager.exceptions;

/**
 * remove: the component was not installed
 */
public class NotInstalledException extends ClientException {
  public final String component;

  public NotInstalledException(String component) {
    super(String.format("%s is not installed.", component));
    this.component = component;
  }

  private static final long serialVersionUID = 1L;
}