package mprot.core.analyzer.execptions;

public class IllegalSequenceElementException extends Exception{
     //default 
     private static final long serialVersionUID = 1L;
    
     public IllegalSequenceElementException(){
         super();
     }
 
     public IllegalSequenceElementException(String msg){
         super(msg);
     }
}