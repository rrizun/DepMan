package dependencymanager.cli;

/**
 * the cli was issued a bad command
 */
public class BadCommandException extends RuntimeException {
  public final String command;

  public BadCommandException(String command) {
    super(String.format("%s bad command.", command));
    this.command = command;
  }

  private static final long serialVersionUID = 1L;
}