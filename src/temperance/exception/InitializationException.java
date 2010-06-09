package temperance.exception;

public class InitializationException extends RuntimeException {

    private static final long serialVersionUID = 3577623297559475890L;
    
    public InitializationException(String message){
        super(message);
    }

}
