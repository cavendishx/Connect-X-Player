# Lizan - Connect-X Player

**Lizan** is an automated player for **Connect-X**, a generalization of Connect Four in which the player must align `X` pieces on a grid of variable dimensions.

The player is developed in **Java** and uses the **Negascout** algorithm to analyze possible moves and select the one with the best estimated outcome.

## 🧠 Algorithm

Lizan uses **Negascout**, an optimized variant of **Minimax** based on **Alpha-Beta pruning**.

The algorithm explores the tree of possible moves and uses an evaluation function to estimate the quality of the positions. Negascout relies on the assumption that the first move explored is likely to be the best one, using narrower search windows to reduce the number of nodes analyzed.

This allows Lizan to explore the game tree more efficiently within the time available for each move.

## 🎮 Reference Players

The Connect-X framework includes two players that can be used as baselines for comparison:

* **L0**: selects moves randomly.
* **L1**: is slightly more advanced than L0, as it recognizes situations in which it can win or lose with a single move; otherwise, it selects moves randomly.

Lizan can be run and compared against both players.

## 🚀 Running the Project

### Compilation

From the `connectx/` directory:

```bash
javac -cp ".." *.java */*.java
```

### Human vs Lizan

To play against Lizan:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan
```

### Lizan vs L0

To have Lizan play against the random player:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan connectx.L0.L0
```

### Lizan vs L1

To have Lizan play against the player that recognizes immediate wins and losses:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan connectx.L1.L1
```

In these examples, the game is played on a **6×7** grid, with the goal of aligning **4 pieces**.

## 🧪 Player Tester

`CXPlayerTester` allows multiple games to be run automatically between two players and their results to be compared.

### Score only

For example, to compare Lizan and L1:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1
```

### Detailed output

The `-v` option enables more detailed output:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1 -v
```

### Custom timeout and number of games

It is possible to specify the **timeout for each move** (`-t`) and the **number of games** (`-r`).

For example, to set a **1-second timeout per move** and run **10 games**:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1 -v -t 1 -r 10
```

In all examples, the parameters `6 7 4` represent **6 rows**, **7 columns**, and **4 pieces to align**, respectively.
