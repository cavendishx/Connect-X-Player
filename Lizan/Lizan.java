package connectx.Lizan;

import connectx.CXPlayer;
import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.max;

public class Lizan implements CXPlayer {
    private int M, N, K; // Numero di righe, colonne e gettoni da allineare per vincere
    private CXGameState myWin, yourWin; // Game state che indica la vittoria del nostro giocatore oppure dell'avversario
    private CXCellState myCoin, yourCoin; // Cell state che differenzia il gettone del nostro giocatore da quello dell'avversario
    private boolean isTimeout; // True se è scaduto il tempo per la scelta della mossa migliore
    private int bestMove; // Variabile in cui viene salvata la colonna su cui effettuare la mossa
    private int TIMEOUT; // Variabile che contiene il tempo massimo per effettuare una mossa in secondi
    private long START; // Variabile che contiene il tempo di inizio per selezionare una mossa in millisecondi

    //VALORI COSTANTI PER L'EVALUATION DI UNO STATO TERMINALE
    private final static Long WIN = Long.MAX_VALUE - 1;
    private final static Long LOSE = Long.MIN_VALUE + 1;
    private final static Long DRAW = 0L;


    public Lizan() {
    }

    /**
     * Inizializzazione del player.
     * Costo: O(1)
     */
    public void initPlayer(int M, int N, int K, boolean first, int timeoutInSeconds) {
        this.M = M;
        this.N = N;
        this.K = K;
        this.TIMEOUT = timeoutInSeconds;
        this.myCoin = first ? CXCellState.P1 : CXCellState.P2;
        this.myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
        this.yourCoin = first ? CXCellState.P2 : CXCellState.P1;
        this.yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    }

    /**
     * Seleziona la mossa da effettuare, rappresentata dall'indice della colonna in cui si vuole giocare il gettone
     * Costo: O(M N^d K)
     * @return: indice della colonna da giocare
     */
    public int selectColumn(CXBoard B) {
        isTimeout = false;
        START = System.currentTimeMillis(); // Save starting time

        if (B.numOfMarkedCells() <= 1)
            return N / 2; // The best first move is always the middle column

        iterativeDeepening(B, 8);
        return bestMove;
    }

    /**
     * Ritorna il nome del nostro giocatore.
     * Cost: O(1)
     * @return: il nome del giocatore
     */
    public String playerName() {
        return "Lizan";
    }


    //  ------------------------------- SEZIONE PER LA VALUTAZIONE DELLA MOSSA MIGLIORE -----------------------------------


    /**
     * Iterative deepening che itera fino alla profondità maxDepth (nel nostro caso sarà 8)
     * Cost: O(M N^d K)
     */
    public void iterativeDeepening(CXBoard B, int maxDepth) {
        //utilizziamo la classe wrapper Long invece del tipo primitivo long per poterne sfruttare alcune funzionalità, come MIN_VALUE e MAX_VALUE
        Long alpha = Long.MIN_VALUE + 1;
        Long beta = Long.MAX_VALUE - 1;

        bestMove = -1; //inizializzazione della bestMove a un valore di default
        Long currEvaluation = evaluation(B, 0);

        for(int i = 1; i <= maxDepth; i++){
            if(isTimeout)
                break;
            int prevMove = bestMove;
            negaScout(B, currEvaluation, 0, i, alpha, beta, 1);
            //Controlliamo se il tempo è scaduto. In tal caso, consideriamo la bestMove trovata alla profondità precedente come mossa migliore.
            if(isTimeout)
                bestMove = prevMove; //Se è scattato il timeout significa che non abbiamo finito di esplorare l'albero di gioco e quindi non siamo riusciti a trovare una buona bestMove. Per questo ritorniamo quella trovata alla profondità precedente
        }
    }

    /**
     * Implementazione di un algoritmo NegaScout con potatura alpha-beta la cui principale caratteristica è di sfruttare un ordinamento dei nodi per poter
     * selezionare la mossa migliore visitando per primi i figli con uno score associato più vantaggioso.
     * L'algoritmo opera una serie di raffinazioni successive del risultato riducendo man mano la finestra di esplorazione dei valori possibili.
     * La mossa migliore sarà contenuta nell'attributo bestMove.
     * Cost: O(M N^d K), poiché:B^d O(M N K), dove B è il fattore di branching, ossia il numero medio di nodi figlio per ogni nodo, che noi sappiamo
     * essere al più il numero esatto delle colonne, ossia N. Dunque, il costo diventa N^d (M N K) = M N^(d+1) K = M N^d K (perché, siccome vogliamo calcolare
     * il caos pessimo, assumiamo d > 1).
     * @return: punteggio del nodo.
     */
    public Long negaScout(CXBoard B, Long currEvaluation, int currDepth, int maxDepth, Long alpha, Long beta, int sign) {
        //Stato terminale oppure abbiamo raggiunto la profondità massima
        if (currDepth == maxDepth || isLeaf(B))
            return sign*evaluation(B, currDepth);

        int bestMoveTmp = -1;

        //Move ordering: l'efficienza del NegaScout dipende fortemente dall'ordine in cui le mosse vengono esplorate.
        Integer[] L = B.getAvailableColumns();
        List<Pair> orderedMoves = new ArrayList<>(); //Una Pair è una coppia (Move, Score)
        for (int i : L) {
            Long childHeuristic = newStateEvaluation(B, currDepth, currEvaluation, i);
            orderedMoves.add(new Pair(i, childHeuristic));
        }
        //ordino le Pair in base all'attributo second. Se sign è 1 l'ordine sarà decrescente; altrimenti sarà crescente.
        orderedMoves.sort((a, b) -> sign*b.second.compareTo(a.second));

        boolean isFirstChild = true;

        for (Pair j : orderedMoves) {
            Integer i = j.first;

            if (isTimeRunningOut()) {
                this.isTimeout = true;
                return alpha;
            }

            B.markColumn(i);
            Long childVal = -negaScout(B, j.second, currDepth+1, maxDepth, -beta, -alpha, -sign);
            B.unmarkColumn();

            //L'algoritmo negaScout prevede di diversificare i casi in cui si sta valutando il primo figlio di un nodo (ossia il primo della lista orderMoves) rispetto a tutti gli altri.
            if(childVal > alpha && childVal < beta && !isFirstChild){
                B.markColumn(i);
                childVal = -negaScout(B, j.second, currDepth+1, maxDepth, -beta, -childVal, -sign); //cerco ancora, riducendo la finestra di esplorazione (caso in cui non sto valutando il primo figlio).
                B.unmarkColumn();
            }
            else{
                isFirstChild = false;
            }

            if(childVal > alpha) {
                alpha = childVal;
                bestMoveTmp = i; //ho trovato una mossa migliore
            }

            if(alpha >= beta) {
                break; //potatura
            }

        }
        bestMove = bestMoveTmp; //aggiorno la mossa migliore
        return alpha;
    }

    /**
     * Valutazione euristica dell'attuale configurazione della board di gioco.
     * Ci interessa studiare tutte le sottosequenze di K celle (visto che per vincere devo allineare K gettoni).
     * Tali sottosequenze possono essere orizzontali, verticali, diagonali ascendenti e diagonali discendenti.
     * Righe e colonne vuote vengono ignorate: ciò permette di risparmiare tempo durante l'evaluation di board molto grandi (che quindi soprattutto a inizio gioco presenteranno molte colonne e righe vuote).
     * Cost: O(MNK)
     * @return: l'evaluation euristica della configurazione attuale di B
     */
    public Long evaluation(CXBoard B, int depth) {

        //controllo innanzitutto se mi trovo in uno stato terminale del gioco
        if (B.gameState() != CXGameState.OPEN) {
            if (B.gameState() == myWin)
                return WIN - depth; //soluzioni a profondità minore (meno in profondità) sono migliori, perché ci permettono di raggiungere prima il nostro obiettivo. Per cui sottraiamo il valore di depth.
            else if (B.gameState() == yourWin)
                return LOSE + depth; //per l'avversario si ragiona con termini negativi; per cui aggiungiamo depth invece di sottrarla come nell'altro ramo dell'if.
            else
                return DRAW; //pareggio
        }

        Long verticalScore = 0L;
        Long horizontalScore = 0L;
        Long dDiagonalScore = 0L;
        Long aDiagonalScore = 0L;

        CXCellState[][] currBoard = B.getBoard(); //Matrice che rappresenta la Board di gioco
        CXCellState[] singleColumn = new CXCellState[M]; //Array che rappresenta una colonna della Board di gioco
        CXCellState[] singleRow = new CXCellState[N]; //Array che rappresenta una riga della Board di gioco
        CXCellState[] subDiagonal = new CXCellState[K]; //Array che rappresenta una porzione di una qualsiasi diagionale della Board di gioco

        boolean[] isRowEmpty = new boolean[M];
        for(int i = 0; i < M; i++)
            isRowEmpty[i] = true; //inizialmente ho tutte righe vuote

        //SEZIONE ALLINEAMENTI VERTICALI
        for (int i = 0; i < N; i++) { //Itero sulle colonne
            for (int j = 0; j < M; j++){
                singleColumn[j] = currBoard[j][i]; //colonna di indice i
                if(singleColumn[j] != CXCellState.FREE)
                    isRowEmpty[j] = false; //Se currBoard[j][i] non è vuota allora la riga j non sarà vuota perchè avrà almeno il gettoni in [j][i]. Questo vettore sarà utile durante la valutazione degli allineamenti orizzontali.
            }
            if(singleColumn[M-1] != CXCellState.FREE) {
                for (int row = 0; row < M - (K - 1); row++) { //Valuto ogni sottosequenza di K elementi a partire dal row-esimo indice
                    //se la cella che si trova più in basso nella sottosequenza di K celle incolonnate è vuota, possiamo ignorare tale sottosequenza perché significa che anche quelle sopra di essa saranno vuote.
                    if(singleColumn[row + K - 1] != CXCellState.FREE)
                        verticalScore += subsetEvaluation(singleColumn, row, row + K); //Aggiorno verticalScore aggiungendo il valore dell'evaluation della sottosequenza corrente
                }
            }
        }
        //FINE SEZIONE ALLINEAMENTI VERTICALI

        //SEZIONE ALLINEAMENTI ORIZZONTALI
        for (int i = 0; i < M; i++) { //Itero sulle righe
            if(!isRowEmpty[i]){ //le righe vuote vengono ignorate
                singleRow = currBoard[i]; //riga di indice i
                for (int col = 0; col < N - (K - 1); col++) { //Valuto ogni sottosequenza di K elementi a partire dal col-esimo indice
                    horizontalScore += subsetEvaluation(singleRow, col, col + K); //Aggiorno horizontalScore aggiungendo il valore dell'evaluation della sottosequenza corrente
                }
            }
        }
        //FINE SEZIONE ALLINEAMENTI ORIZZONTALI

        //SEZIONE ALLINEAMENTI DIAGONALI DISCENDENTI
        //Non vengono considerate le celle nell'andolo in alto a destra e in basso a sinistra perchè, in quelle posizione, non è possibile creare sottosequenze diagonali discendenti di K elementi
        for (int row = 0; row < M - (K - 1); row++) {
            for (int col = 0; col < N - (K - 1); col++) {
                for (int i = 0; i < K; i++)
                    subDiagonal[i] = currBoard[row + i][col + i]; 
                dDiagonalScore += subsetEvaluation(subDiagonal, 0, K); //Aggiorno dDiagonalScore aggiungendo il valore dell'evaluation della sottosequenza corrente
            }
        }
        //FINE SEZIONE ALLINEAMENTI DIAGONALI DISCENDENTI

        //SEZIONE ALLINEAMENTI DIAGONALI ASCENDENTI
        //Non vengono considerate le celle nell'andolo in alto a sinistra e in basso a destra perchè, in quelle posizione, non è possibile creare sottosequenze diagonali discendenti di K elementi
        for (int r = 0; r < M - (K - 1); r++) {
            for (int c = 0; c < N - (K - 1); c++) {
                for (int i = 0; i < K; i++)
                    subDiagonal[i] = currBoard[r + (K - 1) - i][c + i];
                aDiagonalScore += subsetEvaluation(subDiagonal, 0, K); //Aggiorno aDiagonalScore aggiungendo il valore dell'evaluation della sottosequenza corrente
            }
        }
        //FINE SEZIONE ALLINEAMENTI DIAGONALI ASCENDENTI

        //Il punteggio ritornato è la somma dei punteggi di tutte le valutazioni delle sottosequenze
        return verticalScore + horizontalScore + dDiagonalScore + aDiagonalScore;
    }

    /**
     * Valutazione dello score associato a una sottosequenza di cui sono specificati gli indici di inizio e di fine
     * Cost: θ(n), con n = lastIndex - firstIndex (assumendo che Math.pow() sia O(1))
     * @return: l'evaluation della sottosequenza
     */
    public Long subsetEvaluation(CXCellState []cellStateArray, int firstIndex, int lastIndex) {
        int countMyCoins = 0, countYourCoins = 0;

        Long score = 0L;

        //Non teniamo conto delle celle vuote.
        for (int i = firstIndex; i < lastIndex; i++) {
            if (cellStateArray[i] == myCoin)
                countMyCoins++;
            else if (cellStateArray[i] == yourCoin)
                countYourCoins++;
        }

        //Il punteggio della sottosequenza valutata è semplicemente il totale dei miei gettoni meno quelli dell'avversario.
        score += (countMyCoins - countYourCoins);

        //Nota: qua sotto viene fatto 2 elevato a qualcosa perché, rispetto ad esempio a un calcolo del tipo 10000*qualcosa,
        // tra una potenza e l'altra c'è molta più differenza (viene quindi dato un peso maggiore al fatto che vi sia un gettone in più o in meno).
        if (countYourCoins == 0)
            score *= (long) Math.pow(2, countMyCoins); //se ci sono solo miei gettoni nella sottosequenza, restituisco un punteggio molto elevato, ossia 2 elevato al numero dei miei gettoni
        if (countMyCoins == 0)
            score *= (long) Math.pow(2, countYourCoins); //se ci sono solo gettoni dell'avversario, devo comunque assegnare un punteggio alto perché dovrò cercare di bloccargli la vittoria

        return score;
    }

    /**
     * Aggiorna semplicemente l'evaluation già calcolata per l'ultima mossa (quindi per il nodo padre) modificando la valutazione delle sottosequenze
     * influenzate dalla mossa che ha generato il figlio.
     * Questa funzione viene richiamata al fine di studiare il punteggio di ogni possibile nodo figlio con l'aggiunta di un gettone nella colonna "col".
     * Successivamente, userò tali informazioni per ordinare i nodi figli mettendo per primi quelli con punteggio migliore.
     * Cost: θ(K^2)
     * @return: l'aggiornamento della vecchia evaluation
     */
    public Long newStateEvaluation(CXBoard B, int depth, Long prevEvaluation, int col) {
        B.markColumn(col);

        //If it's a terminal game state, let evaluation() calculate the evaluationuation in O(1) time
        if (isLeaf(B)) {
            Long score = evaluation(B, depth + 1);
            B.unmarkColumn();
            return score;
        }

        CXCell newMove = B.getLastMove(); //prendo l'ultima mossa effettuata
        int row = newMove.i; //prendo la riga su cui tale mossa è stata effettuata

        B.unmarkColumn();

        CXCellState[][] board = B.getBoard();
        //Mi interessano solo le sottosequenze di K gettoni che comprendono la cella considerata
        int horizontalFirstIndex = max(col - (K - 1), 0);
        int horizontalLastIndex = Math.min(col + (K - 1), N - 1);
        int verticalFirstIndex = max(row - (K - 1), 0);
        int verticalLastIndex = Math.min(row + (K - 1), M - 1);

        CXCellState[] currSubSet = new CXCellState[K];

        // SEZIONE 1: Rimuovo l'evaluation di tutte le sottosequenze che contengono la cella [row][col]
        //Sottosequenze verticali
        for (int i = verticalFirstIndex; i + K - 1 <= verticalLastIndex; i++) {
            for (int r = i; r < i + K; r++)
                currSubSet[r - i] = board[i][col];
            prevEvaluation -= subsetEvaluation(currSubSet, 0, K);
        }

        //Sottosequenze orizzontali
        for (int j = horizontalFirstIndex; j + K - 1 <= horizontalLastIndex; j++) {
            currSubSet = Arrays.copyOfRange(board[row], j, j + K); //Copia l'intervallo specificato dell'array specificato in un nuovo array
            prevEvaluation -= subsetEvaluation(currSubSet, 0, K);
        }

        //Sottosequenze diagonali
        for (int i = verticalFirstIndex; i + K - 1 <= verticalLastIndex; i++) {
            for (int j = horizontalFirstIndex; j + K - 1 <= horizontalLastIndex; j++) {
                for (int c = 0; c < K; c++)
                    currSubSet[c] = board[i + c][j + c];
                prevEvaluation -= subsetEvaluation(currSubSet, 0, K);
            }
        }

        //Sottosequenze antidiagonali
        for (int i = verticalLastIndex; i - K + 1 >= verticalFirstIndex; i--) {
            for(int j = horizontalFirstIndex; j+K-1 <= horizontalLastIndex; j++){
                for (int c = 0; c < K; c++)
                     currSubSet[c] = board[i - c][j + c];
                prevEvaluation -= subsetEvaluation(currSubSet, 0, K);
            }
        }

        //Nota: nel caso pessimo, dovranno essere valutate K sottosequenze sia per quelle verticali che per quelle orizzontali

        //FINE SEZIONE 1

        //SEZIONE 2: Inseriamo il nuovo gettone e aggiorniamo il valore della score aggiungendo l'evaluation di tutte le sottosequenze che contengono la cella [row][col]
        B.markColumn(col);

        //Sottosequenza verticale
        for(int i = verticalFirstIndex; i+K-1 <= verticalLastIndex; i++){
            for(int r = i; r < i+K; r++)
                currSubSet[r-i] = board[i][col];
            prevEvaluation += subsetEvaluation(currSubSet, 0, K);
        }

        //Sottosequenza orizzontale
        for(int j = horizontalFirstIndex; j+K-1 <= horizontalLastIndex; j++){
            currSubSet = Arrays.copyOfRange(board[row], j, j+K);
            prevEvaluation += subsetEvaluation(currSubSet, 0, K);
        }

        //Sottosequenza diagonale
        for (int i = verticalFirstIndex; i + K - 1 <= verticalLastIndex; i++) {
            for (int j = horizontalFirstIndex; j + K - 1 <= horizontalLastIndex; j++) {
                for (int c = 0; c < K; c++)
                    currSubSet[c] = board[i + c][j + c];
                prevEvaluation += subsetEvaluation(currSubSet, 0, K);
            }
        }

        //Sottosequenza antidiagonale
        for (int i = verticalLastIndex; i - K + 1 >= verticalFirstIndex; i--) {
            for (int j = horizontalFirstIndex; j + K - 1 <= horizontalLastIndex; j++) {
                for (int c = 0; c < K; c++)
                    currSubSet[c] = board[i - c][j + c];
                prevEvaluation += subsetEvaluation(currSubSet, 0, K);
            }
        }

        B.unmarkColumn();
        //FINE SEZIONE 2

        return prevEvaluation;
    }

    /**
     * True se lo stato attuale della board è uno terminale
     * Cost: O(1)
     * @return: un booleano che indica se lo stato attuale della board è terminale
     */
    public boolean isLeaf(CXBoard B) {
        if((B.gameState() != CXGameState.OPEN) || (B.numOfFreeCells() == 0))
            return true;
        else
            return false;
    }

    /**
     * True se il tempo trascorso è vicino al valore di TIMEOUT
     * Cost: O(1)
     * @return: un booleano che indica se si sta raggiungendo il timeout
     */
    private boolean isTimeRunningOut() {
        if ((System.currentTimeMillis() - START) / 1000.0 >= TIMEOUT * (95.0 / 100.0))
            return true;
        else
            return false;
    }


}