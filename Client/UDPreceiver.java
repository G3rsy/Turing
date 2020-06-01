
/*
 *   OVERVIEW:
 *   Implementata la connessione tramite multicast udp
 *   Quando lanciata una istanza di questo thread si mette in ascolto
 *   sulla porta passata in input e tutti i file ricevti li immagazzina
 *   all'interno dell'arraylist messaggi
 *
 *   l'arraylist messaggi e' una struttura condivisa con il client
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class UDPreceiver implements Runnable {
    int port;
    ArrayList<String> messaggi;
    public UDPreceiver(int p, ArrayList<String> list){
       port = p;
       messaggi = list;
    }
    public void receiveUDPMessage() throws IOException {

        //il limite massimo per ogni messaggio sara' di 1024 byte
        byte[] buffer = new byte[1024];

        //entro nel multicast gruop
        MulticastSocket socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);


        //mi metto in ascolto dei messaggi
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());


            messaggi.add(msg);


            if(Thread.interrupted())
                break;
        }

        socket.leaveGroup(group);
        socket.close();
    }

    public void run() {
        try {
            receiveUDPMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

