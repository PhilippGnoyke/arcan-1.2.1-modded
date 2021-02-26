package it.unimib.disco.essere.main.graphmanager;

public class TypeVertexException extends Exception {
    
    public TypeVertexException(){}
    
    public TypeVertexException(String message){
         super(message);
    }
    
    public TypeVertexException(Throwable cause){
        super(cause);
    }
    
    public TypeVertexException(String message, Throwable cause){
        super(message, cause);
    }
}
