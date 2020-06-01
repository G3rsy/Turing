
/*
*   OVERVIEW:
*   Metadata e' pensata per contenere le proprieta' del file a cui e' associato nell'oggetto DataBase
*
 */
import java.nio.channels.SelectionKey;
import java.util.ArrayList;

public class Metadata {
    //Di quanti elementi si compone il file
    int segmenti;

    //la porta al quale si connetteranno per la chat(statico)
    int port;

    //proprietario del file
    String owner;

    //lista di persone con sui e' condiviso il file
    ArrayList<String> shared;

    //loc e' un array di SelectionKey utilizzato per accedere in mutua esclusione al segmento
    //i-esima posizione corrisponde a i-esimo segmento
    //Se i-esima posizione e' null, allora il segmento e' disponibile
    //Se i-esima posizione contiene una key valida, allora qualcuno sta lavorando a quel segmento
    ArrayList<SelectionKey> loc;

    public Metadata(int seq, String own, int p){
        segmenti = seq;
        owner = own;
        shared = new ArrayList<String>();
        loc = new ArrayList<SelectionKey>(seq);
        for(int i = 0; i<seq; i++){
            loc.add(i, null);
        }
        port = p;
        System.out.println("Metadata port-> " +port);

    }

    //Aggiungo name a shared
    public void addShared(String name){
        if(!shared.contains(name)){
            shared.add(name);
        }
    }

    //inserisce nell'i-esima posizione di loc, user
    //se la posizione non e' gia' occupata
    public boolean setLoc(int index, SelectionKey user) {

        if (index < loc.size() && loc.get(index) == null) {
            loc.set(index, user);
            return true;
        }
        return false;
    }

    //Svuota la loc alla posizione index, facendola tornare a null
    public boolean resetLoc(int index, SelectionKey user){
        if(index < loc.size() && (loc.get(index) != null && loc.get(index).equals(user))) {
            loc.set(index, null);
            return true;
        }
        return false;
    }

    //ritorno la porta per la connessione alla chat
    public int getPort(){
        return port;
    }

    //Funzione che permette di sbloccare le loc di utenti non piu' attivi
    //verifica che la selectionKey sia ancora valida
    //Se lo e' qualcuno sta ancora lavorando a quel segmento
    //Se non lo e' fa tornare il valore a null
    public void clear(){
        for(int i=0; i<loc.size(); i++){
            if (loc.get(i) != null && !loc.get(i).isValid()){
                loc.set(i, null);
            }
        }
    }
}
