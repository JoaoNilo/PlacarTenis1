//==============================================================================
package com.edrosframework.placartenis1;

//------------------------------------------------------------------------------
public class TennisSets {
    public final int SETS_MAX = 9;
    private int set;
    private int sets;

    public TennisSets(int sets, int set) {
        this.sets = sets;
        this.set = set;
    }

    public TennisSets() {
        this.sets = 7;
        this.set = 0;
    }

    public TennisSets(int sets) {
        this.sets = sets;
        this.set = 0;
    }

    public int getSet() { return(this.set);  }
    public void setSet(int new_set) {
        if(new_set > this.sets){ new_set = this.sets;}
        this.set = new_set;
    }

    public int getSets() { return(this.sets);  }
    public void setSets(int new_sets) {
        if(new_sets > SETS_MAX){ new_sets = this.sets;}
        this.set = new_sets;
    }

    @Override
    public String toString() {
        return (String.format("%01d", set));
    }
}

//==============================================================================