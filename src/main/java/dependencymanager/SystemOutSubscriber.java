package dependencymanager;

import com.google.common.eventbus.Subscribe;

import dependencymanager.events.DependEvent;
import dependencymanager.events.InstallingEvent;
import dependencymanager.events.ListingEvent;
import dependencymanager.events.RemovingEvent;

/**
 * a dependency manager event bus subscriber that prints events to System.out
 */
public class SystemOutSubscriber {

  @Subscribe
  public void handle(DependEvent event) {
    System.out.println(String.format("depend %s %s", event.component, event.dependencies));
  }
  //###TODO install
  //###TODO remove
  //###TODO list

  @Subscribe
  public void handle(InstallingEvent event) {
    System.out.println(String.format("\tInstalling %s", event.component));
  }

  @Subscribe
  public void handle(RemovingEvent event) {
    System.out.println(String.format("\tRemoving %s", event.component));
  }

  @Subscribe
  public void handle(ListingEvent event) {
    for (String component : event.components)
      System.out.println(String.format("\t%s", component));
  }

}