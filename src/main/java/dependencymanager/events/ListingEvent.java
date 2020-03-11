package dependencymanager.events;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * after-the-fact: installed components were listed
 */
public class ListingEvent {
  public final Set<String> components = new LinkedHashSet<>();
  public ListingEvent(Set<String> components) {
    this.components.addAll(components);
  }
}