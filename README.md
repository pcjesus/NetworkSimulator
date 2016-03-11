# Network Simulator

This project provide a Java implementation of a high-level network simulator
that can be used to evaluate and compare distributed algorithms. The simulator
uses an even-driven implementation to simulate the execution of algorithms in
both synchronous and asynchronous network models. Even in synchronous
settings, the simulation level is detailed enough to observe the effect of
concurrent message exchanges between nodes (i.e., unlike PeerSim [1] which
abstracts message exchange in cycle-based simulations). Synchronous
simulations follow the synchronous execution model defined in Chapter 2 of
[2], with all processes executing message-generation and state-transition
events in lock-step. In asynchronous simulations, the transmission time of
each messages can be set to varies, according to a predefined message latency
distribution, leading each process to execute at different points in time.

The simulator includes the implementation of a few distributed aggregation 
algorithms that can be used as examples, in particular:
* Flow Updating [3,4] {msm.simulator.apps.ApplicationFlowUpdatingDynamic}
* Push-Synopses (with shares of 0.5 => Push-Sum protocol) [5]
  {msm.simulator.apps.ApplicationPushSynopse}
* Push-Pull Gossiping [6] {msm.simulator.apps.ApplicationPushPullGossiping}
* DRG (Distributed Random Grouping) [7] {msm.simulator.apps.ApplicationDRG}


## How-to try it

1 - Generate the bytecode classes from the source code:

```shell
shell> javac -sourcepath src -classpath classes:libs/commons-math-2.2.jar:libs/jython.jar -d classes src/msm/simulator/*.java src/msm//simulator/apps/*.java src/msm/simulator/exceptions/*.java src/msm/simulator/network/*.java src/msm/simulator/util/*.java
```

>**Note:** you might get some warnings (use of deprecated API and unchecked
operation), but don't worry...

2 - Run the network simulator (main classe: msm.simulator.NetworkSimulator)
using one of the available example configuration files as argument, correctly
specifying the classpath and library dependencies: 

```shell
shell> java -classpath classes:libs/commons-math-2.2.jar:libs/jython.jar -Xmx1024M -Xms512M -Dmsm.simulator.ComEngine.DEBUG=false msm.simulator.NetworkSimulator ./config/COMP-RAND-100-3.conf
```

or

```shell
shell> java -classpath classes:libs/commons-math-2.2.jar:libs/jython.jar -Xmx1024M -Xms512M -Dmsm.simulator.ComEngine.DEBUG=false msm.simulator.NetworkSimulator ./config/FU.CHURN_LOSS-2D-100-1.8.conf
```

or

```shell
shell> java -classpath classes:libs/commons-math-2.2.jar:libs/jython.jar -Xmx1024M -Xms512M -Dmsm.simulator.ComEngine.DEBUG=false msm.simulator.NetworkSimulator ./config/ASYNC_DATA-RAND-100-3-20LOSS.conf
```

>**Note:** In alternative, you can generate a jar file (for example into the jar
folder) and use the -jar option to run the simulator, for example (assuming
that the libs are referred correctly in the MANIFEST.MF file):
```shell
shell> java -Xmx1024M -Xms512M -Dmsm.simulator.ComEngine.DEBUG=false -jar ./jar/NetworkSimulator.jar ./config/COMP-RAND-100-3.conf
```

3 - The progress of the simuation will be displayed in the standard ouput and
the resulting simulation files (graphs) will be generated in the "output"
folder.

4 - Go to the "output" folder and use gnuplot to show one of the generated
graph (extension: .gp), for example:

```shell
shell> gnuplot -p Random-100-3_NormRMSEByIter_5.gp
```

or 

```shell
shell> gnuplot -p 2D-100-1.85_EstimateValue_5.gp
```

or 

```shell
shell> gnuplot -p Random-100-3_MsgSendByNormRMSE_8.gp
```

## Dependencies

The simulator implementation uses two 3rd party libraries included in the
"libs" folder:
* [Commons Math](https://commons.apache.org/proper/commons-math/): The Apache Commons Mathematics Library version 2.2
  * File: commons-math-2.2.jar
  * License: Apache 2.0
* [Jython](http://www.jython.org): Python for the Java Platform version 2.5.2
  * File: jython.jar
  * License: Apache 2.0

## Recommended auxiliary software

The following auxiliary software is recommended in order to show the generated
simulation result graphs and plot the network graphs:
* gnuplot 
* graphviz (neato)

## Compatibility

This simulator was initially target to run with Java 1.5, however it is know
to work with the latest version (Java 1.8).

## References:

[1] Márk Jelasity, Alberto Montresor, Gian Paolo Jesi, and Spyros Voulgaris.
    The Peersim simulator. http://peersim.sf.net (Last accessed: Aug. 2011).

[2] Nancy A Lynch. Distributed Algorithms. Morgan Kaufmann Publishers Inc.,
    1996.

[3] Paulo Jesus, Carlos Baquero, and Paulo Sérgio Almeida. Fault-Tolerant
    Aggregation by Flow Updating. In 9th IFIP International Conference on 
    Distributed Applications and Interoperable Systems (DAIS), pages 73–86,
    2009.

[4] Paulo Jesus, Carlos Baquero, and Paulo Sérgio Almeida. Fault-Tolerant
    Aggregation for Dynamic Networks. In 29th IEEE Symposium on Reliable
    Distributed Systems, pages 37–43, 2010.

[5] D Kempe, A Dobra, and J Gehrke. Gossip-Based Computation of Aggregate
    Information. In 44th Annual IEEE Symposium on Foundations of Computer
    Science, pages 482–491, 2003.

[6] M Jelasity, A Montresor, and O Babaoglu. Gossip-based aggregation in
    large dynamic networks. ACM Transactions on Computer Systems,
    23(3):219–252, 2005.

[7] Jen-Yeu Chen, G Pandurangan, and Dongyan Xu. Robust Computation of 
    Aggregates in Wireless Sensor Networks: Distributed Randomized Algorithms
    and Analysis. IEEE Transactions on Parallel and Distributed Systems,
    17(9):987– 1000, 2006.
