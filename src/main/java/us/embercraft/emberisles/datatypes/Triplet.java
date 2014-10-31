package us.embercraft.emberisles.datatypes;

import java.io.Serializable;

/**
 * A triplet of three elements. If the elements are immutable this triplet becomes immutable too.
 * 
 * Implements {@link Comparable} and {@link Serializable}.
 * 
 * @author Catalin Ionescu
 * 
 * @param <X> First element
 * @param <Y> Second element
 * @param <Z> Third element
 */
public class Triplet<X, Y, Z> implements Comparable<Triplet<X, Y, Z>>, Serializable {
    private static final long serialVersionUID = 1L;
    private final X first;
    private final Y second;
    private final Z third;

    /**
     * Creates a triplet of three elements.
     * 
     * @param first First element
     * @param second Second element
     * @param third Third element
     */
    public Triplet(X first, Y second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Gets the first element of the triplet.
     * 
     * @return First element
     */
    public X getFirst() {
        return first;
    }

    /**
     * Gets the second element of the triplet.
     * 
     * @return Second element
     */
    public Y getSecond() {
        return second;
    }

    /**
     * Gets the third element of the triplet.
     * 
     * @return
     */
    public Z getThird() {
        return third;
    }

    /**
     * Returns a String representation of this triplet using the format (first, second, third).
     */
    @Override
    public String toString() {
        return String.format("(%s,%s,%s)", first.toString(), second.toString(), third.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return ((((Triplet<?, ?, ?>) other).first == null && this.first == null) || ((Triplet<?, ?, ?>) other).first.equals(this.first)) &&
                ((((Triplet<?, ?, ?>) other).second == null && this.second == null) || ((Triplet<?, ?, ?>) other).second.equals(this.second)) &&
                ((((Triplet<?, ?, ?>) other).third == null && this.third == null) || ((Triplet<?, ?, ?>) other).third.equals(this.third));
    }

    @Override
    public int hashCode() {
        int result = 31 + ((first == null) ? 0 : first.hashCode());
        result = 31 * result + ((second == null) ? 0 : second.hashCode());
        result = 31 * result + ((third == null) ? 0 : third.hashCode());
        return result;
    }

    /**
     * Assumes X, Y and Z are of comparable type and non null. If not, throws {@link ClassCastException} or {@link NullPointerException} as per
     * {@link Comparable#compareTo(T o)}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Triplet<X, Y, Z> other) {
        int result = ((Comparable<X>) this.getFirst()).compareTo(other.getFirst());
        if (result == 0) {
            result = ((Comparable<Y>) this.getSecond()).compareTo(other.getSecond());
            if (result == 0) {
                return ((Comparable<Z>) this.getThird()).compareTo(other.getThird());
            }
        }
        return result;
    }
}
