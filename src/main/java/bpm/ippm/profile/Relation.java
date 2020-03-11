package bpm.ippm.profile;

/**
 * Relations have a relation type and a frequency.
 * For binary relations frequency is set to 1.
 */
public class Relation {
    /**
     * Different type of relations.
     */
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

    /**
     * Create a new Relation with type and frequency
     * @param type Type of the relation
     * @param frequency Frequency of the relation
     */
    public Relation(RelationType type, double frequency){
        this.frequency = frequency;
        this.type = type;
    }

    /**
     * Create new Relation with type only. Frequency is set to binary (therefore 1)
     * @param type Type of the relation
     */
    public Relation(RelationType type){
        this.frequency = 1;
        this.type = type;
    }

    /**
     * Get the frequency
     * @return frequency or 1 if binary profile
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Get Type of relation
     * @return type
     */
    public RelationType getType() {
        return type;
    }

    /**
     * Equals.
     * @param o Object to compare to
     * @return true if same.
     */
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
