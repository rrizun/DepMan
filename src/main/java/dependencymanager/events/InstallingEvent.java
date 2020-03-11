package dependencymanager.events;

/**
 * after-the-fact: a component was installed
 */
public class InstallingEvent {
  public final String component;
  public InstallingEvent(String component) {
    this.component = component;
  }
}