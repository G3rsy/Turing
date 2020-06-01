public class InvalidUserCredential extends Exception {
    public InvalidUserCredential(){
        super();
    }

    public InvalidUserCredential(String in){
        super(in);
    }

    public InvalidUserCredential(String in, Throwable cause){
        super(in, cause);
    }

    public InvalidUserCredential(Throwable cause){
        super(cause);
    }
}
