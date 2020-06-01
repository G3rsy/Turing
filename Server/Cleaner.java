
/*
 *   OVERVIEW:
 *   Una istanza di questa classe scorre la struttura che viene passata tramite
 *   costruttore e ne ripulisce le eventuali lock mai rilasciate
 *
 *   Questo e' possibile grazie all'implementazione del sistema di mutua esclusione,
 *   vedere meglio la classe Metadata
 *
*/

import java.util.Iterator;
import java.util.Set;

public class Cleaner implements Runnable{
    DataBase db;
    public Cleaner(DataBase d){
        db = d;
    }
    public void run(){
        while(true){
            Set<String> s = db.getSet();
            Iterator<String> iter = s.iterator();

            //Scorro tutta la struttura per cercare
            //delle lock non valide
            while(iter.hasNext()){

                String tmp = iter.next();

                //e' scontato che qualsiasi oggetto restituito con
                //il key set sia sempre li' in quanto non ho alcuna possibilita'
                //di rimuovere i file dalla collezione
                db.clear(tmp);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
