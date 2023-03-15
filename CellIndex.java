import java.util.List;

public class CellIndex {
    public static int getRowIndex(List<Integer> cellIndex) {
        return cellIndex.get(0);
    }

    public static int getColumnIndex(List<Integer> cellIndex) {
        return cellIndex.get(1);
    }
}
