package fi.bizhop.jassu.util;

public class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {
    private Pair( F f, S s ) {
        super( f, s );
    }

    public F getFirst() {
        return this.getKey();
    }

    public S getSecond() {
        return this.getValue();
    }

    public static <F, S> Pair<F, S> of(F f, S s) {
        return new Pair<>(f, s);
    }
}
