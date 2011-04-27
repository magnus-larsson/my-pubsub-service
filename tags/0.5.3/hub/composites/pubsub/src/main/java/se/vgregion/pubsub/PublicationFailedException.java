package se.vgregion.pubsub;

public class PublicationFailedException extends Exception {

    public PublicationFailedException() {
        super();
    }

    public PublicationFailedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PublicationFailedException(String arg0) {
        super(arg0);
    }

    public PublicationFailedException(Throwable arg0) {
        super(arg0);
    }

    
}
