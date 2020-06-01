
/*
 *   OVERVIEW:
 *   Questa classe racchiude le proprieta' di ogni utente, viene usata nella hash map dell'oggetto RMI
 *
 */

import java.util.ArrayList;

public class Properties {

    private String pwd;

    //int log;

    private ArrayList<String> fileDisponibili;

    private ArrayList<String> notifies;

    public Properties(String p){
        pwd = p;
        //log = 0;
        fileDisponibili = new ArrayList<String>();
        notifies = new ArrayList<String>();
    }

    public boolean login(String password){
        if(password.equals(pwd)){
            //log = 1;
            return true;
        }
        return false;
    }
    /*private boolean logged(){
        if(log == 0)
            return false;
        else
            return true;
    }*/

    /*public void logout(){
        log = 0;
    }*/

    public void addFile(String path){
        fileDisponibili.add(path);
    }

    //Serve per effettuare la list da parte del client
    public String[] showFile(){
        String[] out = new String[fileDisponibili.size()];

        for(int i=0; i<fileDisponibili.size(); i++){
            out[i] = fileDisponibili.get(i);
        }
        return out;
    }

    //verifica se l'utente puo' accedere a quel file
    public boolean accessFile(String fileN){
        return fileDisponibili.contains(fileN);
    }

    //aggiunge una notifica alla lista
    public void addNotify(String note){
        notifies.add(note);
    }

    //usato dal thread PrintNotifies lanciato dal client
    public String printNotifies(){
        if(notifies.size() > 0) {
            String tmp = notifies.get(0);
            notifies.remove(0);
            return tmp;
        }
        return null;
    }

}
