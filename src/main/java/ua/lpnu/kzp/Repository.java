package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Узагальнене сховище сутностей у пам'яті з підтримкою фільтрації та безпечного знімка стану.
 *
 * @param <T> тип збережених елементів
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Елемент не може бути null"));
    }

    public void addAll(Collection<? extends T> elements) {
        Objects.requireNonNull(elements, "Колекція елементів не може бути null");
        for (T element : elements) {
            add(element);
        }
    }

    /**
     * Повертає незмінний знімок усіх записів сховища.
     */
    public List<T> snapshot() {
        return List.copyOf(items);
    }

    /**
     * Знаходить усі елементи, що задовольняють умову.
     */
    public List<T> findAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Предикат пошуку не може бути null");
        return items.stream()
                .filter(predicate)
                .toList();
    }

    /**
     * Знаходить перший елемент, що задовольняє умову.
     */
    public Optional<T> findFirst(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Предикат пошуку не може бути null");
        return items.stream()
                .filter(predicate)
                .findFirst();
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}