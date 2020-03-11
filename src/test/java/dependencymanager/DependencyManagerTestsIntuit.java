package dependencymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dependencymanager.exceptions.AlreadyInstalledException;
import dependencymanager.exceptions.DependsOnException;
import dependencymanager.exceptions.NotInstalledException;
import dependencymanager.exceptions.StillNeededByException;

/**
 * this is the "pure-java" (non-cli) version of the sample input and output
 */
public class DependencyManagerTestsIntuit {

  private EventBus eventBus;
  private DependencyManager dependencyManager;

  @BeforeEach
  public void setUp() {
    eventBus = new EventBus();
    dependencyManager = new DependencyManager(eventBus);
  }

  @Test
  public void testIntuit() {

    dependencyManager.depend("telnet", ImmutableSet.of("tcpip", "netcard"));
    assertTrue(dependencyManager.isdepend("telnet", ImmutableSet.of("tcpip", "netcard")));

    dependencyManager.depend("tcpip", ImmutableSet.of("netcard"));
    assertTrue(dependencyManager.isdepend("tcpip", ImmutableSet.of("netcard")));
    
    // test circular dependency
    log(assertThrows(DependsOnException.class, () -> {
      dependencyManager.depend("netcard", ImmutableSet.of("tcpip"));
    }).getMessage());
    assertFalse(dependencyManager.isdepend("netcard", ImmutableSet.of("tcpip")));
    
    dependencyManager.depend("dns", ImmutableSet.of("tcpip", "netcard"));
    assertTrue(dependencyManager.isdepend("dns", ImmutableSet.of("tcpip", "netcard")));
    
    dependencyManager.depend("browser", ImmutableSet.of("tcpip", "html"));
    assertTrue(dependencyManager.isdepend("browser", ImmutableSet.of("tcpip", "html")));

    assertEquals(ImmutableSet.of("netcard"), dependencyManager.install("netcard"));
    assertEquals(ImmutableSet.of("netcard"), dependencyManager.list());

    assertEquals(ImmutableSet.of("tcpip", "telnet"), dependencyManager.install("telnet"));
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet"), dependencyManager.list());

    assertEquals(ImmutableSet.of("foo"), dependencyManager.install("foo"));
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet", "foo"), dependencyManager.list());

    log(assertThrows(StillNeededByException.class, () -> {
      dependencyManager.remove("netcard");
    }).getMessage());
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet", "foo"), dependencyManager.list());

    assertEquals(ImmutableSet.of("html", "browser"), dependencyManager.install("browser"));
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet", "foo", "html", "browser"), dependencyManager.list());

    assertEquals(ImmutableSet.of("dns"), dependencyManager.install("dns"));
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet", "foo", "html", "browser", "dns"), dependencyManager.list());

    // LIST
    assertEquals(ImmutableSet.of("netcard", "tcpip", "telnet", "foo", "html", "browser", "dns"), dependencyManager.list());

    dependencyManager.remove("telnet");
    assertEquals(ImmutableSet.of("html", "browser", "dns", "netcard", "foo", "tcpip"), dependencyManager.list());

    log(assertThrows(StillNeededByException.class, ()->{
      dependencyManager.remove("netcard");
    }).getMessage());
    assertEquals(ImmutableSet.of("html", "browser", "dns", "netcard", "foo", "tcpip"), dependencyManager.list());

    dependencyManager.remove("dns");
    assertEquals(ImmutableSet.of("html", "browser", "netcard", "foo", "tcpip"), dependencyManager.list());

    log(assertThrows(StillNeededByException.class, ()->{
      dependencyManager.remove("netcard");
    }).getMessage());
    assertEquals(ImmutableSet.of("html", "browser", "netcard", "foo", "tcpip"), dependencyManager.list());

    log(assertThrows(AlreadyInstalledException.class, ()->{
      dependencyManager.install("netcard");
    }).getMessage());
    assertEquals(ImmutableSet.of("html", "browser", "netcard", "foo", "tcpip"), dependencyManager.list());

    log(assertThrows(StillNeededByException.class, ()->{
      dependencyManager.remove("tcpip");
    }).getMessage());
    assertEquals(ImmutableSet.of("html", "browser", "netcard", "foo", "tcpip"), dependencyManager.list());
    
    assertEquals(ImmutableSet.of("browser", "html", "tcpip"), dependencyManager.remove("browser"));
    assertEquals(ImmutableSet.of("netcard", "foo"), dependencyManager.list());
    
    assertThrows(NotInstalledException.class, ()->{
      dependencyManager.remove("tcpip");
    });
    
    // LIST
    assertEquals(ImmutableSet.of("netcard", "foo"), dependencyManager.list());
  }

  private void log(String... args) {
    System.out.println(String.join(" ", args));
  }

}