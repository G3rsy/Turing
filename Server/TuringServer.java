public class TuringServer {
    public static int DEFAULT_PORT = 5000;
    public static void main(String[] args){

        //credo una istanza della classe Run selector e la avvio
        RunSelector selly = new RunSelector(DEFAULT_PORT);

        Thread workingSelly = new Thread(selly);
        workingSelly.run();
    }
}
