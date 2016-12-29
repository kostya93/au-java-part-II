import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by kostya on 07.09.16.
 */
public class LazyFactoryTest {
    @Test
    public void createLazyOne() throws Exception {
        Random random = new Random();
        Lazy<Integer> lazy = LazyFactory.createLazyOne(random::nextInt);
        int res = lazy.get();
        for (int i = 0; i < 10; i++) {
            int new_res = lazy.get();
            assertEquals(res, new_res);
        }
    }

    @Test
    public void createLazyTwo() throws Exception {
        Random random = new Random();
        Lazy<Integer> lazy = LazyFactory.createLazyOne(random::nextInt);
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                int j = lazy.get();
                synchronized (list) {
                    list.add(j);
                }
            }).start();
        }

        int first = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(first, (int)list.get(i));
        }
    }

    @Test
    public void createLazyThree() throws Exception {
        Random random = new Random();
        Lazy<Integer> lazy = LazyFactory.createLazyThree(random::nextInt);
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                int j = lazy.get();
                synchronized (list) {
                    list.add(j);
                }
            }).start();
        }

        int first = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(first, (int)list.get(i));
        }
    }

}