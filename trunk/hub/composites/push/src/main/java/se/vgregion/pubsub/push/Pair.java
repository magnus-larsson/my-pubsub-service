package se.vgregion.pubsub.push;

import java.io.Serializable;


public class Pair<T1, T2> implements Serializable  {

    private static final long serialVersionUID = -5960253394163751856L;

    private T1 first;
    private T2 second;
    
    public Pair(T1 value1, T2 value2) {
        this.first = value1;
        this.second = value2;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        @SuppressWarnings("rawtypes")
        Pair other = (Pair) obj;
        if (first == null) {
            if (other.first != null) return false;
        } else if (!first.equals(other.first)) return false;
        
        if (second == null) {
            if (other.second != null) return false;
        } else if (!second.equals(other.second)) return false;
        
        return true;
    }
}