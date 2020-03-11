package dependencymanager.exceptions;

// circular dependency
public class DependsOnException extends ClientException {
  public DependsOnException(String component, String dependency) {
    super(String.format("%s depends on %s. Ignoring command.", dependency, component));
  }

  private static final long serialVersionUID = 1L;
}