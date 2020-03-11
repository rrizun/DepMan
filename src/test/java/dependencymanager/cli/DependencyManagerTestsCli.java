package dependencymanager.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dependencymanager.DependencyManager;

/**
 * unit tests for individual cli commands
 */
public class DependencyManagerTestsCli {

  private EventBus eventBus;
  private DependencyManager dependencyManager;
  private DependencyManagerCli dependencyManagerCli;

  @BeforeEach
  public void setUp() {
    eventBus = new EventBus();
    dependencyManager = new DependencyManager(eventBus);
    dependencyManagerCli = new DependencyManagerCli(dependencyManager);
  }

  @Test
  public void testNullCommand() {
    assertThrows(IllegalArgumentException.class, () -> {
      dependencyManagerCli.command(null);
    });
  }

  @Test
  public void testEmptyCommand() {
    assertTrue(dependencyManagerCli.command("").isEmpty());
  }

  @Test
  public void testBadCommand() {
    assertThrows(BadCommandException.class, () -> {
      dependencyManagerCli.command("FOO");
    });
  }

  @Test
  public void testMissingArgument() {
    assertThrows(MissingArgumentException.class, ()->{
      dependencyManagerCli.command("DEPEND");
    });
    assertThrows(MissingArgumentException.class, ()->{
      dependencyManagerCli.command("INSTALL");
    });
    assertThrows(MissingArgumentException.class, ()->{
      dependencyManagerCli.command("REMOVE");
    });
  }

  @Test
  public void testTooManyArguments() {
    assertThrows(TooManyArgumentsException.class, ()->{
      dependencyManagerCli.command("INSTALL FOO BAR");
    });
    assertThrows(TooManyArgumentsException.class, ()->{
      dependencyManagerCli.command("REMOVE FOO BAR");
    });
    assertThrows(TooManyArgumentsException.class, ()->{
      dependencyManagerCli.command("LIST FOO");
    });
  }

  @Test
  public void testDepend() {
    dependencyManagerCli.command("DEPEND a b");
    assertTrue(dependencyManager.isdepend("a", ImmutableSet.of("b")));
  }

  @Test
  public void testInstall() {
    dependencyManagerCli.command("INSTALL a");
    assertEquals(ImmutableSet.of("a"), dependencyManager.list());
  }

  @Test
  public void testRemove() {
    dependencyManagerCli.command("INSTALL a");
    dependencyManagerCli.command("REMOVE a");
    assertEquals(ImmutableSet.of(), dependencyManager.list());
  }

  @Test
  public void testList() {
    assertTrue(dependencyManagerCli.command("LIST").isEmpty());
  }
    
}