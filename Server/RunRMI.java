
/*
 *   OVERVIEW:
 *   Usata dal server per creare una istanza dell'oggetto RMI
 */
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RunRMI {
    public static userDB nuovo(int port){

        //Creo un nuovo oggetto RMI che rendo disponibile sulla porta in input
        try{
            userDB oggetto = new userDB();
            userDBInterface stub = (userDBInterface) UnicastRemoteObject.exportObject(oggetto, 0);
            LocateRegistry.createRegistry(port);
            Registry reg = LocateRegistry.getRegistry(port);
            reg.rebind("myRmi", stub);
            return oggetto;
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
