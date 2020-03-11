package dependencymanager.events;

/**
 * after-the-fact: a component was removed
 */
public class RemovingEvent {
  public final String component;
  public RemovingEvent(String component) {
    this.component = component;
  }
}