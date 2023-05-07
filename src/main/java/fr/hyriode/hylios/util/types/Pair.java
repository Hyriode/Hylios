package fr.hyriode.hylios.util.types;

public record Pair<T>(T left, T right) {

    public static <T> Pair<T> of(T left, T right) {
        return new Pair<>(left, right);
    }
}
