
/*
*   OVERVIEW:
*   Il Parser e' il core del client che scannerizza l'input ed effettua le relative operazioni
*   Si occupa anche di stabilire la connessione con la struttura RMI e con il server
 */
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Parser {

    public static String logged = null;
    public static SocketChannel sok = null;

    public static void par(int port) throws InterruptedException, IOException {

        InetSocketAddress P = new InetSocketAddress(port);
        //MI CONNETTO AL SERVER TRAMITE TCP

        boolean connected = false;
        while(!connected){
            try {
                sok = SocketChannel.open();
                connected=sok.connect(P);
            }catch(ConnectException e) {
                System.out.println("Aspetto che il Server sia online");
                sok.close();
                Thread.sleep(2000);
            }
        }

        //EFFETTUO LA CONNESSIONE ALLA STRUTTURA RMI
        userDBInterface dataBaseUtenti = null;
        try {
            Registry reg = LocateRegistry.getRegistry(5001);
            dataBaseUtenti = (userDBInterface) reg.lookup("myRmi");
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Sei connesso al server");

        //Thread che stampa le notifiche facendo una lookup alla struttura RMI
        Thread notify = null;

        Scanner in = new Scanner(System.in);
        //try{
            while(true){
                String op = in.nextLine();

                boolean done = true;

                //elaboro la stringa per poterci fare il parsing
                String[] splitted = op.split(" ");
                splitted[0].toLowerCase();
                if(splitted.length >1)
                    splitted[1].toLowerCase();

                if(splitted[0].equals("turing") && splitted.length >1) {
                    switch (splitted[1]) {


                        case "register":
                            if(splitted.length == 4 ){
                                if(logged == null) {
                                    if (splitted[2] == null || splitted[3] == null || dataBaseUtenti == null)
                                        throw new NullPointerException();

                                    //Mando la richiesta all'RMI
                                    try {
                                        if (dataBaseUtenti.register(splitted[2], splitted[3]) == 0)
                                            System.out.println("Registrato con successo " + splitted[2]);
                                    } catch (InvalidUserCredential e) {
                                        System.out.println("ERRORE: Credenziali non valide -> " + splitted[2]);

                                    }
                                }
                            }else{
                                done=false;
                            }
                            break;


                        case "login":
                            if(splitted.length == 4){
                                if(logged == null){

                                    //Invio la richiesta di login
                                    ComSupport.sendStr(new String(splitted[1]+" " + splitted[2]+ " "+splitted[3]), sok);

                                    String tmp = ComSupport.receiveStr(sok);

                                    if(tmp!= null && tmp.equals("OK")) {
                                        logged = splitted[2];

                                        //Questo thread mi permette di stampare le notifiche
                                        //riguardanti gli inviti che ha ricevuto il client
                                        //viene avviato solo in caso di login positivo
                                        notify = new Thread(new PrintNotifies(dataBaseUtenti, splitted[2]));
                                        notify.start();

                                        System.out.println(splitted[2] + " ora sei online");
                                    }else {
                                        if(tmp == null)
                                            System.out.println("ERRORE: Qualcosa e' andato storto");
                                        else
                                            System.out.println(tmp);
                                    }
                                }else{
                                    System.out.println("ERRORE: Sei gia' online come utente: "+logged);
                                }
                            }else
                                done = false;
                            break;


                        case "logout":
                            if(splitted.length == 2){
                                done = true;
                                if(isLogged()){

                                    //dataBaseUtenti.logout(logged);
                                    logged = null;
                                    if(notify!=null) {
                                        notify.interrupt();
                                        notify = null;
                                    }

                                    System.out.println("Sei disconnesso");
                                }
                            }else
                                done = false;
                            break;

                        case "create":
                            if(splitted.length == 4){
                                if(isLogged()){
                                    try{
                                        int verify = new Integer(splitted[3]);
                                        if(verify <= 30) {
                                            //invio la richiesta di creazione del client
                                            ComSupport.sendStr(new String(splitted[1] + " " + splitted[2] + " " + splitted[3]), sok);

                                            String tmp = ComSupport.receiveStr(sok);

                                            if (tmp.equals("OK"))
                                                System.out.println("File creato correttamente");
                                            else
                                                System.out.println(tmp);
                                        }else{
                                            System.out.println("ERRORE: Non possono essere create piu' di 30 sezioni per file");
                                        }
                                    }catch(NumberFormatException e){
                                        System.out.println("ERRORE: Formato input errato\nFor the command list try with \nturing --help");
                                    }
                                }

                            }else
                                done = false;
                            break;


                        case "share":
                            if(splitted.length == 4){
                                if(isLogged()){
                                    ComSupport.sendStr(new String(splitted[1]+" "+splitted[2]+" "+splitted[3]), sok);

                                    String tmp = ComSupport.receiveStr(sok);
                                    if(tmp.equals("OK")){
                                        System.out.println("File condiviso con successo con " + splitted[3]);
                                    }else
                                        System.out.println(tmp);
                                }
                            }else
                                done = false;
                            break;


                        case "show":
                            if(splitted.length == 4){

                                //Richiedo la visione di un segmento del documento
                                if(isLogged()){
                                    try {
                                        int verify = new Integer(splitted[3]);

                                        ComSupport.sendStr(new String(splitted[1] + " " + splitted[2] + " " + splitted[3]), sok);

                                        String out = ComSupport.receiveStr(sok);
                                        if (out.equals("OK")) {

                                            //mando in stampa il file ricevuto
                                            String tmp = ComSupport.receiveFile(sok);
                                            System.out.println("\\\\\\\\\\-----" + splitted[3] + " section-----/////");
                                            System.out.println(tmp);
                                            tmp = null;

                                        } else {
                                            System.out.println(out);
                                        }
                                    }catch(NumberFormatException e){
                                        System.out.println("ERRORE: Formato input errato\nFor the command list try with \nturing --help");
                                    }

                                }
                            }else if(splitted.length == 3){

                                //Richiedo la visione di un documento intero
                                if(isLogged()){
                                    ComSupport.sendStr(new String(splitted[1]+" "+splitted[2]), sok);

                                    //Aspetto di ricevere il numero dei segmenti che dovro' leggere
                                    Integer dim = new Integer(ComSupport.receiveStr(sok));

                                    //comincio il ciclo per avere tutti i pezzi del file
                                    Integer i =0;
                                    while(i<dim && dim!=-1){
                                        System.out.println("\\\\\\\\\\-----"+i.toString() + " section-----/////");
                                        String tmp = ComSupport.receiveFile(sok);
                                        System.out.println(tmp);
                                        tmp = null;
                                        i++;
                                    }

                                    if(dim == -1){
                                        System.out.println("ERRORE: Non e' possibile effettuare l'operazione, controlla che il file sia nella tua lista");
                                    }

                                }
                            }
                            break;


                        case "list":
                            if(splitted.length == 2){
                                if(isLogged()){

                                    //la list viene effettuata tramite tramite RMI
                                    String[] tmp = dataBaseUtenti.showFile(logged);
                                    for(int i =0; i< tmp.length; i++) {
                                        System.out.println(tmp[i]);
                                    }

                                    if(tmp.length <= 0){
                                        System.out.println("Non hai accesso ad alcun elemento");
                                    }
                                }
                            }else
                                done=false;
                            break;


                        case "edit":
                            if(splitted.length == 4 ){
                                if(isLogged()){
                                    try{
                                        int verify = new Integer(splitted[3]);
                                        ComSupport.sendStr(new String(splitted[1]+" "+splitted[2]+" "+splitted[3]), sok);

                                        //L'ack di risposta contiene il numero di porta a cui mi devo connettere
                                        String ack = ComSupport.receiveStr(sok);

                                        if(!ack.equals("File non disponibile")) {

                                            System.out.println("Ora puoi editare "+ splitted[2]);

                                            String tmp = ComSupport.receiveFile(sok);

                                            String path = splitted[2] + "_" + splitted[3] + ".txt";

                                            //Eseguo il metodo edit che mi obbliga ad avere delle opzioni limitate
                                            //fino alla fine dell'editing del documento
                                            Editing.edit(tmp, path, sok, ack, logged);
                                        }else{
                                            System.out.println(ack);
                                        }
                                    }catch(NumberFormatException e){
                                        System.out.println("ERRORE: Formato input errato\nFor the command list try with \nturing --help");
                                    }

                                }
                            }else
                                done=false;
                            break;

                        case "--help":
                            Help.help();
                            done=true;
                            break;
                        default:
                            System.out.println("For the command list try with \nturing --help");
                    }
                }else
                    System.out.println("For the command list try with \nturing --help");
                if(!done){
                    System.out.println("Try with:");
                    Help.help();
                }
            }

        /*}finally {
            if(logged!=null){
                dataBaseUtenti.logout(logged);
            }
        }*/
    }

    public static boolean isLogged(){
        if (logged == null) {
            System.out.println("Non hai ancora effettuato il login per compiere questa operazione");
            return false;
        }else{
            return true;
        }

    }

}

