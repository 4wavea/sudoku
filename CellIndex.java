import java.util.Comparator;
import java.util.List;

public class CellIndex {
    public static final Comparator<List<Integer>> COMPARATOR = Comparator
            .comparingInt(CellIndex::getRowIndex)
            .thenComparingInt(CellIndex::getColumnIndex);

    public static int getRowIndex(List<Integer> cellIndex) {
        final int rowIndexElement = 0;
        return cellIndex.get(rowIndexElement);
    }

    public static int getColumnIndex(List<Integer> cellIndex) {
        final int columnIndexElement = 1;
        return cellIndex.get(columnIndexElement);
    }
}
