package dependencymanager.exceptions;

/**
 * base class for dependency manager client exceptions
 * 
 * these are exceptions that are "the client's fault".. not "the dependency manager's fault"
 */
public abstract class ClientException extends RuntimeException {

  public ClientException(String message) {
    super(message);
  }

  private static final long serialVersionUID = 1L;

}