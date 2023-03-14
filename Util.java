import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
    public static <E> List<E> sample(Collection<E> c, int n, Random r) {
        ArrayList<E> list = new ArrayList<>(c);
        int length = list.size();
        if (length < n) return null;
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    public static <E> List<E> sample(Collection<E> list, int n) {
        return sample(list, n, ThreadLocalRandom.current());
    }

    public static <E> List<E> shuffle(Collection<E> list) {
        return sample(list, list.size());
    }
}
