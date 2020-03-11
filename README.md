"pure-java" dependency manager w/sample cli front-end

# OVERVIEW

1. gradle + junit5 (no spring boot)
1. tested on ubuntu 18.04 and macos 10.something (not windows)

# CHECKOUT

use your favorite git client and clone https://github.com/rrizun/DepMan.git, e.g.,

<pre>
git clone https://github.com/rrizun/DepMan.git
</pre>

# to run the tests from a command line

from the "DepMan" folder:
<pre>
./gradlew clean && ./gradlew test
</pre>

# to run the tests from vscode ide (recommended!)

1. open the "DepMan" folder in vscode (be sure u install the vscode "java extension pack" by microsoft!)
1. wait for vscode to settle down while it downloads and installs gradle dependencies
1. go to the "test" perspective (the science beaker icon) and then click the "run all tests" toolbar button at the tolp

# to run the tests from eclipse ide

untested!

# DELIVERABLES

as per the 'deliverables' section of the assignment,
the part that 'takes the input of the commands and provides the output as the response' is

<pre>
DependencyManagerCli.batch()
</pre>

1. the input is the contents of input.txt
1. the output can be compared w/output.txt
   1. this is what the cli unit tests do

# DESIGN NOTES

1. DependencyManager is the center of the design.
   1. DependencyManager does not know anything about the cli interface and/or input/output files
   1. DependencyManager can be thought of as effectively a pure-java dependency manager
   1. static dependencies are stored in a google guava graph
1. DependencyManagerCli is the command line interface that wraps DependencyManager
   1. in the same way, as per the assignment, a REST interface could also wrap dependency manager
1. remove(): the 'still needed' exception has been enhanced to indicate the components that still need the component
   1. however, this output had to be 'dumbed down' for the cli interface in order to be compatible with the cli input and output sample files
1. the design uses LinkedHashSets to preserve insertion order in order to provide stable api results
   1. i.e., using HashSets (or even TreeSets) would introduce a level of uncertainty/unstableness to the api wrt clients and unit tests
   1. for example, list() returns the list of installed components in the order they were installed
1. there is a EventBus that lets clients listen in on the internal workings of the DependencyManager
   1. the cli leverages this
   1. events are either before-the-fact or after-the-fact events
   1. the event names are a little bit funny because they were influenced by the cli requirements.. this can be cleaned up
1. ClientException.. the intent here is that this is a base class for errors that are the client's fault (IllegalArgumentException is also included in this)
   1. any exception thrown by dependency manager that does not derive from ClientException can be considered not the client's fault
   1. these could be operational runtime errors and/or dependency manager bugs
1. perhaps there could be a 'DependencyManagerException' abstract base class that would help differentiate between client exceptions and not client exceptions

# KNOWN ISSUES

1. DependencyManager is not thread-safe

# NOT DONE/NEXT STEPS

1. package up as a true .jar library
   1. publish to maven repo?