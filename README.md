# Lizan - Connect-X Player

**Lizan** è un giocatore automatico per **Connect-X**, generalizzazione di Connect Four in cui il giocatore deve allineare `X` pedine su una griglia di dimensioni variabili.

Il giocatore è sviluppato in **Java** e utilizza l'algoritmo **Negascout** per analizzare le possibili mosse e scegliere quella con il miglior risultato stimato.

## 🧠 Algoritmo

Lizan utilizza **Negascout**, una variante ottimizzata di **Minimax** basata sulla potatura **Alpha-Beta**.

L'algoritmo esplora l'albero delle possibili mosse e utilizza una funzione di valutazione per stimare la bontà delle posizioni. Negascout sfrutta l'ipotesi che la prima mossa esplorata sia probabilmente la migliore, utilizzando finestre di ricerca più ristrette per ridurre il numero di nodi analizzati.

Questo permette a Lizan di esplorare più efficacemente l'albero di gioco entro il tempo a disposizione per ogni mossa.

## 🎮 Giocatori di riferimento

Il framework Connect-X include due giocatori utilizzabili come baseline per il confronto:

* **L0** — seleziona le mosse casualmente.
* **L1** — è leggermente più avanzato di L0: riconosce le situazioni in cui può vincere o perdere con una singola mossa; negli altri casi sceglie casualmente.

Lizan può essere eseguito e confrontato contro entrambi.

## 🚀 Esecuzione

### Compilazione

Dalla directory `connectx/`:

```bash
javac -cp ".." *.java */*.java
```

### Human vs Lizan

Per giocare contro Lizan:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan
```

### Lizan vs L0

Per far giocare Lizan contro il giocatore casuale:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan connectx.L0.L0
```

### Lizan vs L1

Per far giocare Lizan contro il giocatore che riconosce le vittorie e sconfitte immediate:

```bash
java -cp ".." connectx.CXGame 6 7 4 connectx.Lizan.Lizan connectx.L1.L1
```

In questi esempi la partita viene giocata su una griglia **6×7**, con l'obiettivo di allineare **4 pedine**.

## 🧪 Player Tester

`CXPlayerTester` permette di eseguire automaticamente più partite tra due giocatori e confrontarne i risultati.

### Solo punteggio

Ad esempio, per confrontare Lizan e L1:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1
```

### Output dettagliato

L'opzione `-v` abilita un output più dettagliato:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1 -v
```

### Timeout e numero di partite personalizzati

È possibile specificare il **timeout per ogni mossa** (`-t`) e il **numero di partite** (`-r`).

Ad esempio, per un timeout di **1 secondo per mossa** e **10 partite**:

```bash
java -cp ".." connectx.CXPlayerTester 6 7 4 connectx.Lizan.Lizan connectx.L1.L1 -v -t 1 -r 10
```

In tutti gli esempi, i parametri `6 7 4` indicano rispettivamente **6 righe**, **7 colonne** e **4 pedine da allineare**.
