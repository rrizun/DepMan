package dependencymanager.cli;

/**
 * a cli command was missing arguments
 */
public class MissingArgumentException extends RuntimeException {
  public final String command;

  public MissingArgumentException(String command) {
    super(String.format("%s missing argument.", command));
    this.command = command;
  }

  private static final long serialVersionUID = 1L;
}