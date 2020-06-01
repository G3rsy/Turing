
/*
 *   OVERVIEW:
 *   La struttura DATABASE e' strutturata in questo modo:
 *
 *   HASH MAP : <String,  Metadata>
 *
 *   Pensata per racchiudere tutti i file del server
 *   La chiave stringa, e' appunto il nome del file(univoco)
 *
 *   Il valore della chiave, e' un oggetto che ne estende le proprieta'
 */

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Set;

public class DataBase {
    private HashMap<String, Metadata> DB;

    //Serve per assegnare porte diverse per avviare la chat
    //una per ogni file (assegnate in modo statico)
    int portCount;

    public DataBase(){
        DB = new HashMap<String, Metadata>();
        portCount = 5005;
    }

    //Inserisco un file nella struttura
    public String addFile(String fileN, String owner, int seq) throws IOException {
        //creao una stringa che mi garantisce l'univocita' tra utenti diversi
        String path = fileN + "_" + owner;

        //verifico che non sia gia' contenuta nella struttura
        if(!DB.containsKey(path)){

            //incremento il contatore per passare alla prossima porta
            portCount++;

            //costruisco un nuovo metadata con i dati relativi
            Metadata tmp = new Metadata(seq, owner, portCount);

            //inserisco
            DB.put(path, tmp);

            //Genero in locale dei file vuoti con i nomi codificati in questo modo
            //FileName_OwnerName_NumeroDiSequenza.txt
            for(Integer i=0; i<seq; i++){
                String str = path+"_"+i.toString()+".txt";

                ComSupport.saveFile("", str);
            }
            return "OK";

        }else{
            return "File gia' esistente";
        }
    }

    //Aggiunge shared alla lista relativa al fileN, solo se sono Owner di quel file
    public String share(String fileN, String user, String shared){
        String tmp = isOwner(fileN, user);

        if(tmp.equals("OK"))
            DB.get(fileN).addShared(shared);

        return tmp;

    }


    public String isOwner(String name, String user) {
        if (DB.containsKey(name)) {
            if (DB.get(name).owner.equals(user)) {
                return "OK";
            }else{
                return "ERRORE: Non puoi condividere il file, non sei owner";
            }
        }
        return "ERRORE: Controlla che il file sia nella tua lista";
    }

    public boolean isShared(String name, String user){
        if(DB.containsKey(name)){
            if(DB.get(name).shared.contains(user))
                return true;
        }
        return false;
    }

    public boolean contains(String fileN){
        return DB.containsKey(fileN);
    }

    //Definita per verificare l'esistenza di un determinato segmento
    public boolean contains(String fileN, int dim){
        if(DB.containsKey(fileN))
            if(DB.get(fileN).segmenti > dim)
                return true ;
        return false;
    }

    public Integer length(String fileN){
        return DB.get(fileN).segmenti;
    }

    //Setta la loc di un segmento di un determinato file
    public boolean take(String fileN, SelectionKey user, int index){
        if(DB.containsKey(fileN)){
            Metadata x = DB.get(fileN);
            return x.setLoc(index,user);
        }
        return false;
    }

    //Rilascia la loc sul segmento del file passato come parametro
    public boolean release(String fileN, int index, SelectionKey key){
        if(DB.containsKey(fileN)){
            Metadata x = DB.get(fileN);
            return x.resetLoc(index, key);
        }
        return false;
    }


    public int getPort(String fileN){
        return DB.get(fileN).getPort();
    }

    //Restituisce un set di chiavi, utilizzato dal thread Clear
    public Set<String> getSet(){
        return DB.keySet();
    }


    public void clear(String fileN){
        DB.get(fileN).clear();
    }
}
