# Mental Poker
Distributed Algorithms (COMP90020) Project 2017.

## Source File Structure
```
.
+-- src/
|   +-- main/java/au/edu/unimelb/mentalpoker/
|   |   +-- algorithm/
|   |   +-- crypto/
|   |   +-- exceptions/
|   |   +-- net/
|   |   +-- poker/
|   |   +-- MentalPoker.java
|   |   +-- PokerGame.java
|   +-- test/java/au/edu/unimelb/mentalpoker/
|   +-- protos.proto
+-- tests/
```

The source code is organized as follows:
- The __algorithm__ package contains the implementation of the secure mental poker algorithm.
  - __MentalPokerEngine__ defines the interface for the operations the algorithm provides;
  - __SRAPokerEngine__ is an implementation of the SRA mental poker algorithm, modified to work with arbitraily many players.
 - The __net__ package contains a reliable communcation layer built on UDP and classes for creating, and managing connections with peers.
 - The __poker__ package provides utilities for representing and evaluating poker hands.
 - __MentalPoker.java__ is the main entry point to the application.
 - __PokerGame.java__ contains the main game loop which handles betting, game-logic and makes use of a MentalPokerEngine for secure dealing of cards.
 - __protos.proto__ contains protocol buffer definitions of network messages.
 - Source files in the __test/java/au/edu/unimelb/mentalpoker/__ directory contain unit tests.
 
