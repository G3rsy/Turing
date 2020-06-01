
/*
 *   OVERVIEW:
 *   Classe che implementa alcuni metodi in comune a client e server per la comunicazione
 *   e la gestione dei file
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ComSupport{

    //Invia toSend sul socket sok
    //restituisce true se l'invio e' andato a buon fine, false altrimenti
    public static boolean sendStr(String toSend, SocketChannel sok) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(256);
        byte[] out = toSend.getBytes();
        buf.put(out);
        buf.flip();


        //mando richiesta
        sok.write(buf);
        buf.clear();

        return true;
    }

    //Si mette in ricezione su sok e restituisce una stringa
    public static String receiveStr(SocketChannel sok) throws IOException {
        ByteBuffer in = ByteBuffer.allocateDirect(256);
        int readen = 0;

        readen = sok.read(in);


        in.flip();
        byte[] res = new byte[readen];
        in.get(res);
        in.flip();

        String str = new String(res);
        return str;
    }

    //Invia il file rappresentato dal file path passato come parametro sulla socket
    //restituisce true se tutto e' andato bene, false altrimenti
    public static boolean sendFile(String path, SocketChannel sok) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(8000);

        //utilizzo un array di byte per prendere i byte dal buffer
        //il buffer legge dal fileChannel 8000 byte alla volta tranne l'ultima lettura che potra' avere meno byte
        try(FileChannel inChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ)){
            //System.out.println(inChannel.size());

            int readen = inChannel.read(buffer);
            while(readen!=-1) {
                buffer.flip();

                sok.write(buffer);

                buffer.flip();

                readen = inChannel.read(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        buffer.clear();
        //Invio la stringa finale del file
        //che mi permette di capire di essere arrivato alla fine
        buffer.put(new String("-_-END-_-").getBytes());
        buffer.flip();
        sok.write(buffer);
        buffer.clear();


        return true;

        //stringa che contiene l'intero JSON
        //String questa = new String(built);
    }

    //Si mette in ascolto sulla sok e restituisce una stringa che contiene il file
    public static String receiveFile(SocketChannel sok) throws IOException {
        //utilizzo un StringBuilder che mi permette di creare una stringa
        //alla quale posso fare l'append, questo perche' la creazione della stringa
        //come nella consegna effettuata creava un collo di bottiglia che ritardava
        //notevolmente l'esecuzione del programma
        StringBuilder built = new StringBuilder();
        ByteBuffer in = ByteBuffer.allocateDirect(8000);
        byte[] lilBuf;
        int readen = 0;
        String tmp = "";
        do {
            built.append(tmp);


            readen = sok.read(in);

            in.flip();
            lilBuf = new byte[readen];
            in.get(lilBuf);
            in.clear();

            tmp = new String(lilBuf);
            //System.out.println(tmp);

        }while(!tmp.contains("-_-END-_-"));
        tmp = tmp.replace("-_-END-_-", "");
        built.append(tmp);

        String output = new String(built);
        return output;
    }


    public static void saveFile(String file, String path) throws IOException {
        //System.out.println(Paths.get(path));

        //inizialmente verifico l'esistenza del file stesso e nel caso lo elimino
        if(Files.exists(Paths.get(path))){
            Files.delete(Paths.get(path));
        }

        //Salvo il nuovo file in locale
        try (FileChannel open = FileChannel.open(Paths.get(path), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            open.write(ByteBuffer.wrap(file.getBytes()));
        }
    }
}
