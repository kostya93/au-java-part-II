import java.util.function.Supplier;

/**
 * Created by kostya on 07.09.16.
 */
public class Proxy<K> {
    private final K res;
    Proxy(Supplier<K> supplier) {
        res = supplier.get();
    }

    public K getRes() {
        return res;
    }
}
