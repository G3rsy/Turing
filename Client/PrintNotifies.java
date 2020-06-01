
/*
 *   OVERVIEW:
 *   Pensato per la stampa della notifiche, fa una ricerca attiva sull'oggetto RMI
 *   per verificare la presenza di eventuali nuove notifiche e le smalisce
 *
 *   Vedere la classe Parser, nella sezione login, per capire quando viene lanciato
 */

import java.rmi.RemoteException;

public class PrintNotifies implements Runnable {
    userDBInterface structure;
    String owner;

    public PrintNotifies(userDBInterface s, String own){
        structure = s;
        owner = own;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()){
            try {
                String tmp = null;
                if(structure!= null)
                    tmp  = structure.printNotify(owner);
                if(tmp!=null){
                    System.out.println(tmp);
                }
            } catch (RemoteException e) {
                break;
            }

            Thread.yield();
        }
        return;
    }
}
