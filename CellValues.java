import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CellValues {
    // create

    public static Map<List<Integer>, Integer> newRandomCompleted(GridLayout layout) {
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

    public static Map<List<Integer>, Integer> newRandomPartiallyCompleted(GridLayout gridLayout, Map<List<Integer>, Integer> completedCellValues, int cluesCount) {
        if (cluesCount < 0 || cluesCount > gridLayout.cellsCount) {
            throw new IllegalArgumentException("Clues count is greater than layout or negative");
        }
        Set<List<Integer>> cellsSample = Set.copyOf(Util.sample(gridLayout.cellIndexes, cluesCount));
        return completedCellValues.entrySet().stream()
                .filter(cellValueEntry -> cellsSample.contains(cellValueEntry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // analyse

    public static Map<List<Integer>, Integer> filterInvalid(GridLayout gridLayout, Map<List<Integer>, Integer> cellValues) {
        return cellValues.entrySet().stream()
                .filter(cellValueEntry -> gridLayout.cellPeers.get(cellValueEntry.getKey()).stream()
                        .map(cellValues::get)
                        .anyMatch(peerValue -> Objects.equals(peerValue, cellValues.get(cellValueEntry.getKey()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static boolean isValid(GridLayout gridLayout, Map<List<Integer>, Integer> cellValues) {
        return CellValues.filterInvalid(gridLayout, cellValues).isEmpty();
    }

    public static boolean isCompleted(GridLayout gridLayout, Map<List<Integer>, Integer> cellValues) {
        return cellValues.keySet().equals(gridLayout.cellIndexes);
    }

    public static String toPrintableString(GridLayout gridLayout, Map<List<Integer>, Integer> cellValues) {
        StringBuilder sb = new StringBuilder();
        for (int rowIndex = 0; rowIndex < gridLayout.rowsCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < gridLayout.columnsCount; columnIndex++) {
                List<Integer> cellIndex = List.of(rowIndex, columnIndex);
                Optional<Integer> cellValue = Optional.ofNullable(cellValues.get(cellIndex));
                String cellValueView = cellValue.map(Object::toString).orElse(".");
                sb.append("%3s".formatted(cellValueView));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // solve

    // todo: backtracking, assign/merge

    // convert

    // todo: values <-> value sets, split, merge
}
