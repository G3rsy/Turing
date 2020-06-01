
/*
 *   OVERVIEW:
 *   Questo e' il core del Server
 *
 *   Crea le strutture principali che permettono il funzionamento del programma
 *   - userDB rmi: contiene i nomi di tutti gli utenti e i relativi dati personali
 *   - DataBase DB: Contiene tutti i file e i relativi dati
 *   - Selector: per soddisfare le richieste dei client
 *
 *   Nel selector, viene effettuato il parsing della stringa data in input dal client
 *   e a seconda della richiesta (rappresentata dalla prima parola), vengono effettuate
 *   le varie operazioni
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;

public class RunSelector implements Runnable{
    int port;
    userDB rmi;
    DataBase DB;

    public RunSelector(int in){
        port = in;
        rmi = null;
    }
    @Override
    public void run() {
        //creo una istanza dell'oggetto userDB in RMI
        rmi = RunRMI.nuovo(port+1);

        ServerSocketChannel serverChannel;
        Selector selector;

        try {
            //apro la connessione TCP per le richieste dei client
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);

            //configuro il socket non bloccante
            serverChannel.configureBlocking(false);

            //Apro un nuovo selector e lo registro al canale
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }



        System.out.println("Listening for connections on port " + port);

        DB = new DataBase();

        //Thread che permette una pulizia delle loc residue che si possono avere
        //in caso di crash da parte del client
        Thread c = new Thread(new Cleaner(DB));
        c.start();

        //parte il ciclo di lettura delle richieste sul canale
        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                try {

                    // rimuove la chiave dal Selected Set, ma non dal registered Set
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    //Verifico cosa sta chiedendo il client, se connettersi oppure se
                    //vuole inviare una richiesta
                    if (key.isAcceptable()) {
                        //accetto le richieste di connessione da Client verso il server
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = null;

                        client = server.accept();

                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);

                        //registro la chiave relatica al nuovo client connesso, all'interno del selector
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE);

                    } else if (key.isReadable()) {
                        //Leggo le richieste del client

                        SocketChannel client = (SocketChannel) key.channel();
                        try {

                            String[] splitted = ComSupport.receiveStr(client).split(" ");

                            switch (splitted[0]) {

                                case "login":
                                    String replay = null;
                                    try {
                                        replay = rmi.login(splitted[1], splitted[2]);
                                        if(replay.equals("OK")) {
                                            key.attach(splitted[1]);
                                        }
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        ComSupport.sendStr(replay, client);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                break;

                                case "create":
                                    try {

                                        String user = (String) key.attachment();

                                        String tmp = DB.addFile(splitted[1], user, new Integer(splitted[2]));

                                        if(tmp.equals("OK"))
                                            rmi.addFile(user, splitted[1]+"_"+user);

                                        ComSupport.sendStr(tmp, client);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "share":
                                    try {
                                        String tmp = null;
                                        String own = (String) key.attachment();
                                        //verifico se esiste l'utente identificato da splitted[2]
                                        //che sia diverso da chi effettua la richiesta (non voglio che un utente possa condividere un file con se stesso)
                                        //verifico che l'utente con cui devo condividere non abbia gia' i permessi per questo file
                                        if(rmi.contains(splitted[2]) && !own.equals(splitted[2]) && !DB.isShared(splitted[1], splitted[2])) {

                                            //lo condivido
                                            tmp = DB.share(splitted[1], own, splitted[2]);

                                            //invio la risposta al client
                                            if (tmp.equals("OK")) {
                                                rmi.addNotify(splitted[2], own + " ha condiviso " + splitted[1] + " con te!");
                                                rmi.addFile(splitted[2], splitted[1]);
                                            }

                                        }else{
                                            tmp = "Impossibile condividere con "+splitted[2];
                                        }
                                        ComSupport.sendStr(tmp, client);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                case "show":

                                    String own = (String) key.attachment();

                                    if(splitted.length == 3){
                                        boolean esiste = DB.contains(splitted[1], new Integer(splitted[2]));
                                        boolean sonoOwner = DB.isOwner(splitted[1], own).equals("OK");
                                        boolean meLoHannoCondiviso = DB.isShared(splitted[1], own);

                                        //DEBUG
                                        //System.out.println("Risultati esist, sonoOwn, cond "+ esiste + sonoOwner + meLoHannoCondiviso);
                                        if( esiste && (sonoOwner || meLoHannoCondiviso)) {
                                            ComSupport.sendStr("OK", client);

                                            /*
                                                QUESTO pezzo viene aggiunto per rispettare le specifiche, in quanto
                                                richiedono che sia visualizzato se qualcuno sta lavorando su una sezione
                                                oppure no
                                                if()

                                             */
                                            //riformulo la creazione del path del file
                                            String toSend = splitted[1]+"_"+new Integer(splitted[2])+".txt";
                                            ComSupport.sendFile(toSend, client);
                                            System.out.println("Inviato segmento a "+ own);

                                        }else{
                                            ComSupport.sendStr("Non e' possibile effettuare l'operazione, controlla che il file sia nella tua lista", client);
                                            System.out.println("Qualcosa e' andato storto");
                                        }

                                    }else if(splitted.length == 2){
                                        boolean esiste = DB.contains(splitted[1]);
                                        boolean sonoOwner = DB.isOwner(splitted[1], own).equals("OK");
                                        boolean meLoHannoCondiviso = DB.isShared(splitted[1], own);

                                        if(esiste && (sonoOwner || meLoHannoCondiviso)) {

                                            Integer dim = DB.length(splitted[1]);

                                            //invio prima la quantita' di segmenti da ricevere
                                            ComSupport.sendStr(dim.toString(), client);

                                            //successivamente invio il file
                                            for (Integer i = 0; i<dim; i++) {
                                                String toSend = splitted[1] + "_" + i.toString() + ".txt";
                                                ComSupport.sendFile(toSend, client);
                                            }
                                            System.out.println("Inviato file a "+ own);
                                        }else{
                                            System.out.println("Qualcosa andato storto");
                                            ComSupport.sendStr("-1", client);
                                        }
                                    }
                                    break;

                                case "edit":
                                    String own2 = (String) key.attachment();

                                    boolean esiste = DB.contains(splitted[1], new Integer(splitted[2]));
                                    boolean sonoOwner = DB.isOwner(splitted[1], own2).equals("OK");
                                    boolean meLoHannoCondiviso = DB.isShared(splitted[1], own2);

                                    if( esiste && (sonoOwner || meLoHannoCondiviso)) {

                                        //Compongo la stringa che rappresenta il file salvato su server
                                        String toSend = splitted[1] + "_" + splitted[2] + ".txt";

                                        //cerco di bloccare il file per garantire la mutua esclusione
                                        //sul suo editing
                                        if(DB.take(splitted[1], key, new Integer(splitted[2]))){

                                            //rappresenta la porta sulla quale deve connettersi il client per avere accesso alla chat
                                            String port = new String(String.valueOf(DB.getPort(splitted[1])));

                                            //System.out.println("run selector port -> "+port);
                                            //Invio la porta al client
                                            ComSupport.sendStr(port, client);

                                            //E solo successivamente il file
                                            ComSupport.sendFile(toSend, client);
                                            System.out.println(own2 + " sta editando " + toSend);

                                        }else{
                                            ComSupport.sendStr("File non disponibile", client);
                                        }

                                    }else{
                                        ComSupport.sendStr("File non disponibile", client);
                                    }
                                    break;

                                case "end-edit":
                                    String fileN = splitted[1];
                                    int segment = new Integer(splitted[2]);

                                    if(DB.release(fileN, segment, key)){
                                        //comunico al server che sono pronto per la ricezione del file
                                        ComSupport.sendStr("OK", client);

                                        //e mi metto in attesa
                                        String tmp = ComSupport.receiveFile(client);

                                        String path = fileN+"_"+segment+".txt";
                                        //salvo il nuovo file editato
                                        ComSupport.saveFile(tmp, path);

                                    }else{
                                        ComSupport.sendStr("Impossibile rilasciare la risorsa", client);
                                    }
                                    break;
                                default:
                                    System.out.println("C'e' qualcosa che non va!");
                            }

                        } catch (IOException e) {
                            key.channel().close();
                        }/* catch (NotBoundException e) {
                            e.printStackTrace();
                        }*/
                    } else if (!key.isValid()) {
                        key.channel().close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
