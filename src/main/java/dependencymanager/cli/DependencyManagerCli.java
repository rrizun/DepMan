package dependencymanager.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.eventbus.Subscribe;

import dependencymanager.DependencyManager;
import dependencymanager.events.InstallingEvent;
import dependencymanager.events.ListingEvent;
import dependencymanager.events.RemovingEvent;
import dependencymanager.exceptions.StillNeededByException;

class OutputLines {
  public final List<String> outputLines = new ArrayList<>();
  @Subscribe
  public void handle(InstallingEvent event) {
    outputLines.add(String.format("\tInstalling %s", event.component));
  }
  @Subscribe
  public void handle(RemovingEvent event) {
    outputLines.add(String.format("\tRemoving %s", event.component));
  }
  @Subscribe
  public void handle(ListingEvent event) {
    for (String component : event.components)
      outputLines.add(String.format("\t%s", component));
  }
}

/**
 * command line interface for dependency manager
 * 
 * wraps the "pure-java" DependencyManager with a command-line-like interface
 */
public class DependencyManagerCli {

  private final DependencyManager dependencyManager;
  private final Map<String, Function<Iterator<String>, List<String>>> commands = new HashMap<>();

  /**
   * ctor
   * 
   * @param dependencyManager
   */
  public DependencyManagerCli(DependencyManager dependencyManager) {
    this.dependencyManager = dependencyManager;
    
    // register commands

    commands.put("DEPEND", (iter)->{
      if (!iter.hasNext())
        throw new MissingArgumentException("INSTALL");
      String component = iter.next();
      Set<String> dependencies = new LinkedHashSet<>(); // insertion order
      Iterators.addAll(dependencies, iter);
      dependencyManager.depend(component, dependencies);
      return new ArrayList<>();
    });
    
    commands.put("INSTALL", (iter)->{
      if (!iter.hasNext())
        throw new MissingArgumentException("INSTALL");
      String component = iter.next();
      if (iter.hasNext())
        throw new TooManyArgumentsException("INSTALL");
      return new ArrayList<>(dependencyManager.install(component));
    });
    
    commands.put("REMOVE", (iter)->{
      if (!iter.hasNext())
        throw new MissingArgumentException("REMOVE");
      String component = iter.next();
      if (iter.hasNext())
        throw new TooManyArgumentsException("REMOVE");
      return new ArrayList<>(dependencyManager.remove(component));
    });
    
    commands.put("LIST", (iter)->{
      if (iter.hasNext())
        throw new TooManyArgumentsException("LIST");
      return new ArrayList<>(dependencyManager.list());
    });
    
    commands.put("END", (iter)->{
      return new ArrayList<>();
    });
  }

  /**
   * command
   * 
   * @param line e.g., "INSTALL foo"
   * @return outputLines
   * @throws IllegalArgumentException if line is null
   * @throws IllegalArgumentException if unknown command
   */
  public List<String> command(String line) {

    if (line == null)
      throw new IllegalArgumentException("line is null");

    OutputLines outputLines = new OutputLines();
    dependencyManager.eventBus.register(outputLines);
    try {

      // split line into tokens
      Iterator<String> iter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().split(line).iterator();
      if (iter.hasNext()) {
        String cmd = iter.next();

        Function<Iterator<String>, List<String>> commandFunction = commands.get(cmd);

        if (commandFunction==null)
          throw new BadCommandException(cmd);

        /*dontCare*/commandFunction.apply(iter);
      } else {
        // empty command line is ok
      }

    } finally {
      dependencyManager.eventBus.unregister(outputLines);
    }

    return outputLines.outputLines;
  }

  /**
   * batch
   * 
   * excutes a batch of commands.. all input commands will execute
   * 
   * input commands result in success or failure:
   * if success then the output (if any) is output to outputLines
   * if failure then the failure message is output to outputLines
   * 
   * ###TODO have a way to determine per-command success vs failure
   * ###TODO have a way to determine per-command success vs failure
   * ###TODO have a way to determine per-command success vs failure
   * 
   * @return outputLines the output as per the assignment
   * 
   * @throws IllegalArgumentException if inputLines is null
   */
  public List<String> batch(List<String> inputLines) throws IOException {

    if (inputLines==null)
      throw new IllegalArgumentException("inputLines is null");

    List<String> outputLines = new ArrayList<>();

    for (String inputLine : inputLines) {
      // echo
      outputLines.add(inputLine);
      try {
        outputLines.addAll(command(inputLine));
      } catch (StillNeededByException e) {
        // need to 'dump down' this message for unit test's sake.. omit stillNeededBy components
        outputLines.add(String.format("\t%s is still needed.", e.component));
      } catch (Exception e) {
        // all other exception messages can be rendered as-is
        outputLines.add(String.format("\t%s", e.getMessage()));
      }
    }

    return outputLines;
  }

}