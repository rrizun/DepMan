package dependencymanager.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dependencymanager.DependencyManager;

/**
 * this is the test case for running the sample input.txt and verifying against the sample output.txt
 */
public class DependencyManagerTestsCliBatch {

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
  public void testBatch() throws IOException {
    List<String> inputLines = Files.asCharSource(new File("input.txt"), Charsets.UTF_8).readLines();
    List<String> expectedOutputLines = Files.asCharSource(new File("output.txt"), Charsets.UTF_8).readLines();
    List<String> actualOutputLines = dependencyManagerCli.batch(inputLines);

              // for debug
              Files.asCharSink(new File("actual.txt"),Charsets.UTF_8).writeLines(actualOutputLines);

    assertEquals(trimLines(expectedOutputLines), trimLines(actualOutputLines));
  }

  // ignores leading/trailing spaces/tabs
  private List<String> trimLines(List<String> input) {
    List<String> output = new ArrayList<>();
    for (String line : input)
      output.add(line.trim());
    return output;
  }

}