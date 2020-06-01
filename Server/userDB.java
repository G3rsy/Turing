
/*
 *   OVERVIEW:
 *
 *   Struttura RMI creata per immagazzinare tutti gli utenti e le relative proprieta'
 */

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class userDB extends RemoteServer implements userDBInterface {
    private ConcurrentHashMap<String, Properties> utenti;

    public userDB() {
        utenti = new ConcurrentHashMap<String, Properties>();
    }

    //usato dal client per eseguire la registrazione
    @Override
    public int register(String name, String passw) throws RemoteException, InvalidUserCredential {
        //verifico se l'utente esiste gia nel database
        if (utenti.containsKey(name))
            throw new InvalidUserCredential();

        //Inserisco il nuovo utente nella struttura
        Properties tmp = new Properties(passw);
        utenti.put(name, tmp);
        System.out.println("Registrato "+name);
        return 0;
    }

    //Usato dal server per tener conto degli utenti online
    public String login(String name, String passw) throws RemoteException{
        if(utenti.containsKey(name)){
            boolean tmp = utenti.get(name).login(passw);
            if(tmp) {
                System.out.println("login -> " + name + " " + tmp);
                return "OK";
            }
        }
        return "Credenziali non valide";
    }

    /*//usato dal client per effetture il logout
    public int logout(String user)throws RemoteException{
        if(utenti.containsKey(user)){
            utenti.get(user).logout();
        }
        return 0;
    }*/

    public boolean contains(String user){
        return utenti.containsKey(user);
    }

    //Usata esclusivamente dal lato server
    public boolean addNotify(String user, String note){
        if(utenti.containsKey(user)){
            Properties tmp = utenti.get(user);
            tmp.addNotify(note);
            return true;
        }
        return false;
    }

    //usata dal client per stampare le notifiche
    public String printNotify(String user) throws RemoteException{
        if(utenti.containsKey(user)){
            String tmp = utenti.get(user).printNotifies();

            return tmp;

        }
        return null;
    }

    public boolean permission(String fileN, String user) throws RemoteException{
        return utenti.get(user).accessFile(fileN);
    }

    //usato dal client per la list
    public String[] showFile(String user)throws RemoteException{
        String[] out = null;
        if(utenti.containsKey(user)){
            out = utenti.get(user).showFile();
        }
        return out;
    }

    public void addFile(String user, String fileN){
        utenti.get(user).addFile(fileN);
    }
    public void overview(){
        System.out.println("Gli utenti sono:");
        Set<String> key = utenti.keySet();
        Iterator<String> it = key.iterator();

        while(it.hasNext()){
            String k = it.next();
            System.out.println(k + " " + utenti.get(k));
        }
    }
}