package dependencymanager.exceptions;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * remove: the component is still needed by the stillNeededBy components
 */
public class StillNeededByException extends ClientException {
  // the component that is needed by the stillNeededBy set
  public final String component;
  // the components that still need component
  public final Set<String/*component*/> stillNeededBy = new LinkedHashSet<>(); // insertion order

  public StillNeededByException(String component, Set<String> stillNeededBy) {
    super(String.format("%s is still needed by %s.", component, stillNeededBy));
    this.component = component;
    this.stillNeededBy.addAll(stillNeededBy); // deep copy
  }

  private static final long serialVersionUID = 1L;
}