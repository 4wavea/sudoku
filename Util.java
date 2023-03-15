import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
    private static <E> List<E> sample(Collection<E> population, int k, Random r) {
        int length = population.size();
        if (k < 0 || k > length) {
            throw new IllegalArgumentException("Sample larger than population or is negative");
        }
        ArrayList<E> list = new ArrayList<>(population);
        for (int i = length - 1; i >= length - k; --i) {
            Collections.swap(list, i, r.nextInt(i + 1));
        }
        return list.subList(length - k, length);
    }

    public static <E> List<E> sample(Collection<E> population, int k) {
        return sample(population, k, ThreadLocalRandom.current());
    }

    public static <E> List<E> shuffle(Collection<E> list) {
        return sample(list, list.size());
    }
}
