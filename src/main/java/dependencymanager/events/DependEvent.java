package dependencymanager.events;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * a depend command was invoked
 */
public class DependEvent {
  public final String component;
  public final Set<String> dependencies = new LinkedHashSet<>();
  public DependEvent(String component, Set<String> dependencies) {
    this.component = component;
    this.dependencies.addAll(dependencies);
  }
}