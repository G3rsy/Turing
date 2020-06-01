import java.rmi.Remote;
import java.rmi.RemoteException;

public interface userDBInterface extends Remote {
    //Operazione che permette la registrazione al database
    //restituisce un intero in caso di successo, eccezione altrimenti
    public int register(String name, String passw) throws RemoteException, InvalidUserCredential;

    //usata dal client per stampare le notifiche
    public String printNotify(String user) throws RemoteException;

    //usata dal client per la stampa dei documenti condivisi
    public String[] showFile(String user) throws RemoteException;
}
