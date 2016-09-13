import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by kostya on 07.09.16.
 */
public class LazyFactory {
    public static <T> Lazy<T> createLazyOne(Supplier<T> supplier) {
        return new Lazy<T>() {
            private T res;
            private Supplier<T> spl = supplier;
            @Override
            public T get() {
                if (spl != null) {
                    res = spl.get();
                    spl = null;
                }
                return res;
            }
        };
    }
    public static <T> Lazy<T> createLazyTwo(Supplier<T> supplier) {
        return new Lazy<T>() {
            private T res;
            private Supplier<T> spl = supplier;
            @Override
            public T get() {
                synchronized (this) {
                    if (spl != null) {
                        res = spl.get();
                        spl = null;
                    }
                }
                return res;
            }
        };
    }
    public static <T> Lazy<T> createLazyThree(Supplier<T> supplier) {
        class CreateLazyThree implements Lazy<T> {
            private final AtomicReferenceFieldUpdater<CreateLazyThree, Proxy> atomicReferenceFieldUpdater =
                    AtomicReferenceFieldUpdater.newUpdater(CreateLazyThree.class, Proxy.class, "proxy");

            private final Supplier<T> spl;
            private volatile Proxy<T> proxy = null;
            private CreateLazyThree(Supplier<T> spl) {
                this.spl = spl;
            }

            @Override
            public T get() {
                while (proxy == null) {
                    atomicReferenceFieldUpdater.compareAndSet(this, null, new Proxy<T>(spl));
                }
                return proxy.getRes();
            }
        }
        return new CreateLazyThree(supplier);
    }
}
