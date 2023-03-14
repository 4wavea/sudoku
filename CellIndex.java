import java.util.Comparator;
import java.util.List;

public class CellIndex {
    public static final Comparator<List<Integer>> COMPARATOR = Comparator
            .comparingInt(CellIndex::getRowIndex)
            .thenComparingInt(CellIndex::getColumnIndex);

    public static int getRowIndex(List<Integer> cellIndex) {
        return cellIndex.get(0);
    }

    public static int getColumnIndex(List<Integer> cellIndex) {
        return cellIndex.get(1);
    }
}
