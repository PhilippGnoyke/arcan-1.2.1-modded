package it.unimib.disco.essere.main.graphmanager;

public class EmptyProjectException extends Exception {
    
    public EmptyProjectException(){}
    
    public EmptyProjectException(String message){
         super(message);
    }
    
    public EmptyProjectException(Throwable cause){
        super(cause);
    }
    
    public EmptyProjectException(String message, Throwable cause){
        super(message, cause);
    }
}

