import java.io.IOException;

public class Client {
    public static int DEFAULT_PORT = 5000;
    public static void main(String[] args){
        try {
            Parser.par(DEFAULT_PORT);
        }catch(IOException e){
            System.out.println("Bad news, il server e' offline :(");
        }catch (Exception e){
            System.out.println("Ops, qualcosa e' andato storto");
        }
        return;
    }
}
