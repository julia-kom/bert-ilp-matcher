package bpm.ippm.profile;

public class Relation {
    public enum RelationType {
        BP_ORDER,
        BP_EXCLUSIVE,
        BP_INTERLEAVING,
        BP_REVERSE_ORDER,
        NONE,
        BPP_DIRECT_CAUSAL,
        BPP_REVERSE_DIRECT_CAUSAL,
        BPP_INDIRECT_CAUSAL,
        BPP_REVERSE_INDIRECT_CAUSAL,
        BPP_CONFLICT,
        BPP_SOMETIMES_CONCURRENT,
        BPP_ALWAYS_CONCURRENT,
        ALPHA_ORDER,
        ALPHA_EXCLUSIVE,
        ALPHA_INTERLEAVING,
        ALPHA_REVERSE_ORDER,
        LOG_DIRECTLY_FOLLOWS,
        LOG_EVENTUALLY_FOLLOWS
    }

    private RelationType type; // relation type
    private double frequency; //relative frequency

    public Relation(RelationType type, double frequency){
        this.frequency = frequency;
        this.type = type;
    }

    public Relation(RelationType type){
        this.frequency = 1;
        this.type = type;
    }

    public double getFrequency() {
        return frequency;
    }

    public RelationType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o){

        if(!(o instanceof Relation)){
            return false;
        }
        Relation r =(Relation) o;
        if(this.getType().equals(r.getType()) && Math.abs(this.getFrequency() -r.getFrequency())<= 0.0001){
            return true;
        }
        return false;
    }
}
