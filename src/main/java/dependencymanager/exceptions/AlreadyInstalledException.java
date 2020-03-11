package dependencymanager.exceptions;

/**
 * install: the component is already installed
 */
public class AlreadyInstalledException extends ClientException {
  public final String component;

  public AlreadyInstalledException(String component) {
    super(String.format("%s is already installed.", component));
    this.component = component;
  }

  private static final long serialVersionUID = 1L;
}