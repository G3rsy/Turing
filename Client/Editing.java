
/*
*   OVERVIEW:
*   Questa classe e' stata pensata per bloccare il client nello stato di editing
*   nel quale ha potenzialita' limitate al ricevere ed inviare messaggi, e terminare l'editing
 */

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class Editing {
    public static void edit(String file, String path, SocketChannel sok, String port, String user) throws IOException {

        ComSupport.saveFile(file, path);

        //Lista di messaggi ricevuti dalla chat
        ArrayList<String> messages = new ArrayList<String>();

        Scanner in = new Scanner(System.in);

        boolean done = false;

        //lancio il thread ce si mette in ascolto della chat
        Thread t = new Thread(new UDPreceiver(new Integer(port), messages));
        t.start();

        //Lancio un ciclo che termina nel momento in cui finisco di editare
        while(!done) {
            String op = in.nextLine();
            String[] splitted = op.split(" ");

            if(splitted[0].equals("turing") && splitted.length > 1){
                switch(splitted[1]){
                    case "send":
                        op = op.replaceFirst("turing send ", user+": ");
                        UDPsender.send(op, new Integer(port));
                        System.out.println("Inviato");
                        break;

                    case "receive":
                        while(messages.size() != 0){
                            System.out.println(messages.get(0));
                            messages.remove(0);
                        }
                        break;

                    case "end-edit":
                        if(splitted.length == 4) {
                            try{
                                int verify = new Integer(splitted[3]);
                                ComSupport.sendStr(splitted[1] + " " + splitted[2] + " " + splitted[3], sok);

                                String tmp = ComSupport.receiveStr(sok);

                                if (tmp.equals("OK")) {
                                    ComSupport.sendFile(path, sok);
                                    System.out.println("Hai finito di editare il file correttamente");
                                    done = true;
                                    t.interrupt();
                                } else {
                                    System.out.println(tmp);
                                }
                            }catch(NumberFormatException e){
                                System.out.println("Formato input errato\nFor the command list try with \nturing --help");
                            }
                        }else{
                            System.out.println("For the command list try with \nturing --help");
                        }

                        break;

                    default:
                        System.out.println("Stai editando, puoi solo effettuare le seguenti operazioni:");
                        System.out.println(
                                "turing end-edit < doc > < sec >          fine modifica della sezione del doc\n"+
                                "turing send < msg >                      invia un msg sulla chat\n" +
                                "turing receive                           visualizza i msg ricevuti sulla chat");
                        break;
                }
            }else{
                System.out.println("Stai editando, puoi solo effettuare le seguenti operazioni:");
                System.out.println(
                        "turing end-edit < doc > < sec >          fine modifica della sezione del doc\n"+
                        "turing send < msg >                      invia un msg sulla chat\n" +
                        "turing receive                           visualizza i msg ricevuti sulla chat");
            }

        }
    }
}
