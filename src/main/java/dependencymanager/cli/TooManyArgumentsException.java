package dependencymanager.cli;

/**
 * a cli command had too many missing arguments
 */
public class TooManyArgumentsException extends RuntimeException {
  public final String command;

  public TooManyArgumentsException(String command) {
    super(String.format("%s too many arguments.", command));
    this.command = command;
  }

  private static final long serialVersionUID = 1L;
}