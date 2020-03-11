package dependencymanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

import dependencymanager.events.DependEvent;
import dependencymanager.events.InstallingEvent;
import dependencymanager.events.ListingEvent;
import dependencymanager.events.RemovingEvent;
import dependencymanager.exceptions.AlreadyInstalledException;
import dependencymanager.exceptions.DependsOnException;
import dependencymanager.exceptions.NotInstalledException;
import dependencymanager.exceptions.StillNeededByException;

public class DependencyManager {

  public final EventBus eventBus;

  // set of explicitly installed components
  private final Set<String/*component*/> explicitComponents = new HashSet<>();

  // set of installed components
  private final Set<String/*component*/> installedComponents = new LinkedHashSet<>(); // insertion order

  // static dependencies
  private final MutableGraph<String/*component*/> graph = GraphBuilder.directed().build();

  /**
   * ctor
   * 
   * @param eventBus event bus for dependency manager to publish intesting events to (see the "events" java package)
   */
  public DependencyManager(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * depend
   * 
   * add static dependencies.. adds "dependencies" as dependencies to component
   * 
   * @param component
   * @param dependencies set of dependencies
   * 
   * @throws IllegalArgumentException if component is null
   * @throws IllegalArgumentException if dependencies is null
   * @throws DependsOnException       if input would result in circular dependencies
   */
  public void depend(String component, Set<String> dependencies) {

    // validate
    if (component==null)
      throw new IllegalArgumentException("component is null");
    if (dependencies==null)
      throw new IllegalArgumentException("dependencies is null");

    // validate (circular dependency)
    for (String dependency : dependencies) {
      if (graph.hasEdgeConnecting(dependency, component))
        throw new DependsOnException(component, dependency);
    }

    // notify listeners
    eventBus.post(new DependEvent(component, dependencies));

    // add static dependency
    graph.addNode(component);
    for (String dependency : dependencies)
      graph.putEdge(component, dependency);
  }

  /**
   * isdepend - query static dependencies
   * 
   * returns true if component depends on dependencies
   * 
   * @param component
   * @param dependencies
   * @return
   */
  public boolean isdepend(String component, Set<String> dependencies) {

    // validate
    if (component == null)
      throw new IllegalArgumentException("component is null");
    if (dependencies == null)
      throw new IllegalArgumentException("dependencies is null");
  
    for (String dependency : dependencies) {
      if (!graph.hasEdgeConnecting(component, dependency))
        return false;
    }

    return true;
  }

  /**
   * install
   * 
   * install component and dependencies
   * 
   * @param componentToBeInstalled
   * @return set of installed components in the order they were installed
   * @throws IllegalArgumentException
   * @throws AlreadyInstalledException
   */
  public Set<String> install(String componentToBeInstalled) {
    // log.log("install", componentToBeInstalled);

    // validate
    if (componentToBeInstalled==null)
      throw new IllegalArgumentException("componentToBeRemoved is null");

    // validate
    if (installedComponents.contains(componentToBeInstalled))
      throw new AlreadyInstalledException(componentToBeInstalled);

    // install
    //###TODO post install event here
    explicitComponents.add(componentToBeInstalled);
    graph.addNode(componentToBeInstalled);

    List<String> componentsToBeInstalled = new ArrayList<>(Graphs.reachableNodes(graph, componentToBeInstalled));

    Collections.reverse(componentsToBeInstalled);

    Set<String> installed = new LinkedHashSet<>();

    for (String component : componentsToBeInstalled) {
      if (!installedComponents.contains(component)) { // skip self
        // notify listeners
        eventBus.post(new InstallingEvent(component));

        // install component
        installedComponents.add(component);

        // add to result
        installed.add(component);
      }
    }

    return installed;
  }

  /**
   * remove
   * 
   * remove component and non-explicitly-installed dependencies from installed components
   * 
   * @param componentToBeRemoved
   * @return set of removed components in the order they were removed
   * @throws IllegalArgumentException
   * @throws StillNeededException
   */
  public Set<String> remove(String componentToBeRemoved) {
    // log.log("remove", componentToBeRemoved);

    // validate
    validateInternal(componentToBeRemoved);
    
    // remove
    return removeInternal(componentToBeRemoved);
  }

  /**
   * validateInternal
   * 
   * @param componentToBeRemoved
   */
  private void validateInternal(String componentToBeRemoved) {
    // validate 1
    if (componentToBeRemoved==null)
      throw new IllegalArgumentException("componentToBeRemoved is null");

    // validate 2
    if (!installedComponents.contains(componentToBeRemoved))
      throw new NotInstalledException(componentToBeRemoved);
  
    // validate 3
    Set<String/*installedComponent*/> stillNeededBy = new LinkedHashSet<>();

    for (String installedComponent : installedComponents) {
      if (!installedComponent.equals(componentToBeRemoved)) { // skip self
        if (Graphs.reachableNodes(graph, installedComponent).contains(componentToBeRemoved))
          stillNeededBy.add(installedComponent);
      }
    }

    if (!stillNeededBy.isEmpty())
      throw new StillNeededByException(componentToBeRemoved, stillNeededBy);
  }

  /**
   * removeInternal
   * 
   * @param componentToBeRemoved
   * @return
   */
  private Set<String> removeInternal(String componentToBeRemoved) {

    //###TODO post remove event here

    // return removed components in the order they were removed
    Set<String> removed = new LinkedHashSet<>();

    // STEP 1 remove component
    explicitComponents.remove(componentToBeRemoved);

    // STEP 2 auto remove implicit dependencies
    for (String component : Graphs.reachableNodes(graph, componentToBeRemoved)) {
      if (!explicitComponents.contains(component)) { // skip self
        boolean dependedOn = false;
        for (String dependency : Graphs.reachableNodes(Graphs.transpose(graph), component)) {
          if (!dependency.equals(component)) { // skip self
            if (installedComponents.contains(dependency))
              dependedOn = true;
          }
        }
        if (!dependedOn) {
          // notify listeners
          eventBus.post(new RemovingEvent(component));

          // remove component
          installedComponents.remove(component);

          // add to result
          removed.add(component);
        }
      }
    }

    return removed;
  }

  /**
   * list
   * 
   * returns sorted set of installed components
   * 
   * @return sorted set of installed components
   */
  public Set<String> list() {
    // notify listeners
    eventBus.post(new ListingEvent(installedComponents));

    return installedComponents;
  }

}