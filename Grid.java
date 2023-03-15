import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Grid {
    public final GridLayout layout;
    public final Map<List<Integer>, Integer> cellValues;

    public Grid(GridLayout layout, Map<List<Integer>, Integer> cellValues) {
        this.layout = layout;
        this.cellValues = cellValues;
    }

    public static Grid newEmpty(GridLayout layout) {
        Map<List<Integer>, Integer> cellValues = new HashMap<>();
        return new Grid(layout, cellValues);
    }

    public static Grid newRandomComplete(GridLayout layout) {
        Map<List<Integer>, Integer> cellValues = newRandomCompleteCellValues(layout);
        return new Grid(layout, cellValues);
    }

    private static Map<List<Integer>, Integer> newRandomCompleteCellValues(GridLayout layout) {
        final int base = layout.boxSideLength;
        final int side = layout.rowsCount;

        IntBinaryOperator pattern = (r, c) -> (base * (r % base) + Math.floorDiv(r, base) + c) % side;

        List<Integer> baseRange = IntStream.range(0, base).boxed().toList();
        List<Integer> rows = Util.shuffle(baseRange).stream()
                .flatMap(r -> Util.shuffle(baseRange).stream()
                        .map(g -> g * base + r))
                .toList();
        List<Integer> cols = Util.shuffle(baseRange).stream()
                .flatMap(c -> Util.shuffle(baseRange).stream()
                        .map(g -> g * base + c))
                .toList();
        List<Integer> valuesSample = Util.shuffle(layout.values);

        return Lists.cartesianProduct(rows, cols).stream()
                .collect(Collectors.toMap(cellIndex -> cellIndex, cellIndex -> valuesSample.get(pattern.applyAsInt(CellIndex.getRowIndex(cellIndex), CellIndex.getColumnIndex(cellIndex)))));
    }

    public boolean isComplete() {
        return this.cellValues.keySet().equals(this.layout.cellIndexes);
    }

    public Map<List<Integer>, Integer> filterInvalidCellValues() {
        return this.cellValues.entrySet().stream()
                .filter(cellValueEntry -> this.layout.cellPeers.get(cellValueEntry.getKey()).stream()
                        .map(this.cellValues::get)
                        .anyMatch(peerValue -> Objects.equals(peerValue, this.cellValues.get(cellValueEntry.getKey()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean isValid() {
        return this.filterInvalidCellValues().isEmpty();
    }

    public String toPrintableString() {
        StringBuilder sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < this.layout.rowsCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < this.layout.columnsCount; columnIndex++) {
                List<Integer> cellIndex = List.of(rowIndex, columnIndex);
                Optional<Integer> cellValue = Optional.ofNullable(cellValues.get(cellIndex));
                String cellValueView = cellValue.map(Object::toString).orElse(".");
                sb.append("%3s".formatted(cellValueView));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
