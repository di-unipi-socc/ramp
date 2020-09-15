package analyzer;

import model.*;

//element of the sequence that we have to analyze

public class SequenceElement {

    private String rule;
    private Node targetNode;
    private Requirement req;
    private NodeInstance targetInstance;
    private NodeInstance servingInstance;
    private String op;

    public SequenceElement(String rule) throws NullPointerException, IllegalArgumentException {
        if (rule == null)
            throw new NullPointerException("rule null");
        if (rule.isEmpty() == true)
            throw new IllegalArgumentException("rule empty");

        this.targetInstance = null;
        this.req = null;
        this.servingInstance = null;
        this.targetNode = null;
        this.op = null;

        this.rule = rule;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op)
        throws
            NullPointerException
    {
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new NullPointerException("op empty");

        this.op = op;
    }

    public NodeInstance getServingInstance() {
        return servingInstance;
    }

    public void setServingInstance(NodeInstance servingInstance)
        throws 
            NullPointerException
    {
        if(servingInstance == null)
            throw new NullPointerException("servingInstance null");
        this.servingInstance = servingInstance;
    }

    public Requirement getReq() {
        return req;
    }

    public void setReq(Requirement req) 
        throws 
            NullPointerException
    {        
        if(req == null)
            throw new NullPointerException("req null");
        this.req = req;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) 
        throws
            NullPointerException
    {
        if(targetNode == null)
            throw new NullPointerException("targetNode null");
        this.targetNode = targetNode;
    }

    public String getRule() {
        return rule;
    }

    public NodeInstance getTargetInstance() {
        return targetInstance;
    }

    public void setTargetInstance(NodeInstance targetInstance)
        throws
            NullPointerException     
    {
        if(targetInstance == null)
            throw new NullPointerException("targetInstance null");
        
        this.targetInstance = targetInstance;
    }
    
    public boolean wellFormattedOpSequence(){
        if(this.op.equals("opStart"))
            return this.wellFormattedOpStart();
        
        if(this.op.equals("opEnd"))
            return this.wellFormattedOpEnd();
        
        if(this.op.equals("scaleIn"))
            return this.wellFormattedScaleIn();
        
        if(this.op.equals("scaleOut1"))
            return this.wellFormattedScaleOut1();
        
        if(this.op.equals("scaleOut2"))
            return this.wellFormattedScaleOut2();
        
        return false;
    }


    private boolean wellFormattedOpStart(){

        if(this.targetInstance == null)
            return false;

        if(this.op == null || this.op.isEmpty() == true)
            return false;

        if(this.req != null)
            return false;

        if(this.servingInstance != null)
            return false;
        
        if(this.targetNode != null)
            return false;

        return true;
    }

    private boolean wellFormattedOpEnd(){
        if(this.targetInstance == null)
            return false;

        if(this.op == null || this.op.isEmpty() == true)
            return false;

        if(this.req != null)
            return false;

        if(this.servingInstance != null)
            return false;
        
        if(this.targetNode != null)
            return false;   

        return true;
    }

    private boolean wellFormattedScaleIn(){
        if(this.targetInstance == null)
            return false;
        
        if(this.op != null)
            return false;

        if(this.req != null)
            return false;

        if(this.servingInstance != null)
            return false;
        
        return true;
    }

    private boolean wellFormattedScaleOut1(){
        if(this.targetNode == null)
            return false;

        if(this.targetInstance != null)
            return false;
        if(this.op != null)
            return false;
        if(this.servingInstance != null)
            return false;
        if(this.req != null)
            return false;
        
        return true; 
    }

    private boolean wellFormattedScaleOut2(){
        if(this.targetNode == null)
            return false;
        if(this.req == null)
            return false;
        if(this.servingInstance == null)
            return false;
        
        if(this.op != null)
            return false;
        
        if(this.targetInstance != null)
            return false;
        
        return true;
    }

}