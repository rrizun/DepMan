package dependencymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dependencymanager.events.DependEvent;
import dependencymanager.exceptions.AlreadyInstalledException;
import dependencymanager.exceptions.DependsOnException;
import dependencymanager.exceptions.NotInstalledException;
import dependencymanager.exceptions.StillNeededByException;

public class DependencyManagerTests {

  private EventBus eventBus;
  private DependencyManager dependencyManager;
  private SystemOutSubscriber subscriber;

  @BeforeEach
  public void setUp() {
    eventBus = new EventBus();
    dependencyManager = new DependencyManager(eventBus);
    subscriber = new SystemOutSubscriber();
    eventBus.register(subscriber);
  }

  @AfterEach
  public void tearDown() {
    eventBus.unregister(subscriber);
  }

  @Test
  public void testZeroDependencies() {
    // a component w/zero dependencies is ok
    dependencyManager.depend("foo", ImmutableSet.of());
    assertTrue(dependencyManager.isdepend("foo", ImmutableSet.of()));
  }

  @Test
  public void testOneDependency() {
    dependencyManager.depend("foo", ImmutableSet.of("bar"));
    assertTrue(dependencyManager.isdepend("foo", ImmutableSet.of("bar")));
  }

  @Test
  public void testTwoDependencies() {
    dependencyManager.depend("foo", ImmutableSet.of("bar", "baz"));
    assertTrue(dependencyManager.isdepend("foo", ImmutableSet.of("bar")));
    assertTrue(dependencyManager.isdepend("foo", ImmutableSet.of("baz")));
    assertTrue(dependencyManager.isdepend("foo", ImmutableSet.of("bar", "baz")));
  }

  @Test
  public void testDependsOnException() {
    dependencyManager.depend("foo", ImmutableSet.of("bar"));
    assertThrows(DependsOnException.class, ()->{
      dependencyManager.depend("bar", ImmutableSet.of("foo"));
    });
  }

  @Test
  public void testInstallNull() {
    assertThrows(IllegalArgumentException.class, ()->{
      dependencyManager.install(null);
    });
  }

  @Test
  public void testInstallZeroLength() {
    // zero-length package name is ok
    assertEquals(ImmutableSet.of(""), dependencyManager.install(""));
    assertEquals(ImmutableSet.of(""), dependencyManager.list());
  }

  @Test
  public void testBasicInstall() {
    assertEquals(ImmutableSet.of("foo"), dependencyManager.install("foo"));
    assertEquals(ImmutableSet.of("foo"), dependencyManager.list());
  }

  @Test
  public void testReinstall() {
    assertEquals(ImmutableSet.of("foo"), dependencyManager.install("foo"));
    assertEquals(ImmutableSet.of("foo"), dependencyManager.list());
    assertThrows(AlreadyInstalledException.class, ()->{
      dependencyManager.install("foo");
    });
    assertEquals(ImmutableSet.of("foo"), dependencyManager.list());
  }

  @Test
  public void testInstallTransitiveDependency1() {
    dependencyManager.depend("a", ImmutableSet.of("b"));
    dependencyManager.depend("b", ImmutableSet.of("c"));

    assertEquals(ImmutableSet.of("c", "b", "a"), dependencyManager.install("a"));
    assertEquals(ImmutableSet.of("c", "b", "a"), dependencyManager.list());
  }

  @Test
  public void testInstallTransitiveDependency2() {
    dependencyManager.depend("a", ImmutableSet.of("b"));
    dependencyManager.depend("b", ImmutableSet.of("c"));

    assertEquals(ImmutableSet.of("c", "b"), dependencyManager.install("b"));
    assertEquals(ImmutableSet.of("c", "b"), dependencyManager.list());
    assertEquals(ImmutableSet.of("a"), dependencyManager.install("a"));
    assertEquals(ImmutableSet.of("c", "b", "a"), dependencyManager.list());
  }

  @Test
  public void testInstallTransitiveDependency3() {
    dependencyManager.depend("a", ImmutableSet.of("b"));
    dependencyManager.depend("b", ImmutableSet.of("c"));

    assertEquals(ImmutableSet.of("c"), dependencyManager.install("c"));
    assertEquals(ImmutableSet.of("c"), dependencyManager.list());
    assertEquals(ImmutableSet.of("b"), dependencyManager.install("b"));
    assertEquals(ImmutableSet.of("c", "b"), dependencyManager.list());
    assertEquals(ImmutableSet.of("a"), dependencyManager.install("a"));
    assertEquals(ImmutableSet.of("c", "b", "a"), dependencyManager.list());
  }

  @Test
  public void testRemoveNull() {
    assertThrows(IllegalArgumentException.class, ()->{
      dependencyManager.remove(null);
    });
  }

  @Test
  public void testRemove() {
    dependencyManager.install("foo");
    assertEquals(ImmutableSet.of("foo"), dependencyManager.list());
    dependencyManager.remove("foo");
    assertEquals(ImmutableSet.of(), dependencyManager.list());
  }

  @Test
  public void testRemoveImplicitDependencies() {
    dependencyManager.depend("a", ImmutableSet.of("b"));
    dependencyManager.install("a");
    assertEquals(ImmutableSet.of("a", "b"), dependencyManager.list());
    
    // both a and b should be removed
    assertEquals(ImmutableSet.of("a", "b"), dependencyManager.remove("a"));
    
    // "b" should be removed too because it was implicitly installed
    assertEquals(ImmutableSet.of(), dependencyManager.list());
  }

  @Test
  public void testInstallRemoveExplicit() {
    dependencyManager.depend("a", ImmutableSet.of("b"));
    dependencyManager.install("b");
    dependencyManager.install("a");
    dependencyManager.remove("a");
    // "b" should not be removed because it was explicitly installed
    assertEquals(ImmutableSet.of("b"), dependencyManager.list());
  }

  @Test
  public void testRemoveNotInstalledException() {
    assertThrows(NotInstalledException.class, ()->{
      dependencyManager.remove("foo");
    });
  }

  @Test
  public void testRemoveStillNeededByException() {
    dependencyManager.depend("foo", ImmutableSet.of("bar"));
    dependencyManager.install("foo");
    assertThrows(StillNeededByException.class, ()->{
      dependencyManager.remove("bar");
    });
  }

  // ----------------------------------------------------------------------
  // events
  //
  // 
  // 

  // ----------------------------------------------------------------------

  /**
   * events are first class parts of the dependency manager's public interface
   * therefore events should be unit tested
   * 
   * here is one example of how to test the depend event
   */
  @Test
  public void testDependEvent() {

    boolean gotEvent[] = new boolean[1];

    dependencyManager.eventBus.register(new Object() {
      @Subscribe
      void handle(DependEvent event) {
        gotEvent[0] = true;
      }
    });

    dependencyManager.depend("foo", ImmutableSet.of());
    assertTrue(gotEvent[0]);

    //###TODO for completeness.. unregister event bus subscriber

  }

  //###TODO test more events here

}