import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GridLayout {
    public final int rowsCount;
    public final int columnsCount;
    public final int boxesCount;
    public final int cellsCount;
    public final int boxCellsCount;
    public final int boxSideLength;

    public final Set<Integer> rowIndexes;
    public final Set<Integer> columnIndexes;
    public final Set<Integer> boxIndexes;
    public final Set<List<Integer>> cellIndexes;

    public final Set<Set<List<Integer>>> groups;
    public final Map<List<Integer>, Set<Set<List<Integer>>>> cellGroups;
    public final Map<List<Integer>, Set<List<Integer>>> cellPeers;

    public final Set<Integer> values;

    public GridLayout(int rowsCount, int columnsCount, int boxesCount, Set<Integer> values) {
        this.rowsCount = rowsCount;
        this.columnsCount = columnsCount;
        this.boxesCount = boxesCount;
        this.cellsCount = rowsCount * columnsCount;
        this.boxCellsCount = cellsCount / boxesCount;
        this.boxSideLength = (int) Math.sqrt(this.boxCellsCount);

        this.rowIndexes = IntStream.range(0, rowsCount).boxed().collect(Collectors.toSet());
        this.columnIndexes = IntStream.range(0, columnsCount).boxed().collect(Collectors.toSet());
        this.boxIndexes = IntStream.range(0, boxesCount).boxed().collect(Collectors.toSet());
        this.cellIndexes = Sets.cartesianProduct(rowIndexes, columnIndexes);

        Map<Integer, Set<List<Integer>>> rows = cellIndexes.stream()
                .collect(Collectors.groupingBy(CellIndex::getRowIndex,
                        Collectors.toSet()));
        Map<Integer, Set<List<Integer>>> columns = cellIndexes.stream()
                .collect(Collectors.groupingBy(CellIndex::getColumnIndex,
                        Collectors.toSet()));
        Map<Integer, Set<List<Integer>>> boxes = cellIndexes.stream()
                .collect(Collectors.groupingBy(this::getCellBoxIndex,
                        Collectors.toSet()));
        this.groups = Stream.of(rows, columns, boxes)
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        this.cellGroups = cellIndexes.stream()
                .collect(Collectors.toMap(cellIndex -> cellIndex, cellIndex -> this.groups.stream()
                        .filter(group -> group.contains(cellIndex))
                        .collect(Collectors.toSet())));
        this.cellPeers = cellGroups.entrySet().stream()
                .flatMap(cellGroupEntry -> cellGroupEntry.getValue().stream()
                        .flatMap(Collection::stream)
                        .map(peer -> new AbstractMap.SimpleEntry<>(cellGroupEntry.getKey(), peer)))
                .filter(Predicate.not(cellPeerEntry -> cellPeerEntry.getKey().equals(cellPeerEntry.getValue())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue,
                                Collectors.toSet())));

        this.values = Set.copyOf(values);
    }

    public static GridLayout newClassic() {
        final int classicRowsCount = 9;
        final int classicColumnsCount = 9;
        final int classicBoxesCount = 9;
        final Set<Integer> classicValues = IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toSet());
        return new GridLayout(classicRowsCount, classicColumnsCount, classicBoxesCount, classicValues);
    }

    public int getCellBoxIndex(List<Integer> cellIndex) {
        int boxRowIndex = CellIndex.getRowIndex(cellIndex) / boxSideLength;
        int boxColumnIndex = CellIndex.getColumnIndex(cellIndex) / boxSideLength;
        return boxRowIndex * boxSideLength + boxColumnIndex;
    }

    public Optional<List<Integer>> getTopCellIndex(List<Integer> cellIndex) {
        return this.cellIndexes.stream()
                .filter(index ->
                        CellIndex.getRowIndex(index) == CellIndex.getRowIndex(cellIndex) - 1
                                && CellIndex.getColumnIndex(index) == CellIndex.getColumnIndex(cellIndex))
                .findAny();
    }

    public Optional<List<Integer>> getBottomCellIndex(List<Integer> cellIndex) {
        return this.cellIndexes.stream()
                .filter(index ->
                        CellIndex.getRowIndex(index) == CellIndex.getRowIndex(cellIndex) + 1
                                && CellIndex.getColumnIndex(index) == CellIndex.getColumnIndex(cellIndex))
                .findAny();
    }

    public Optional<List<Integer>> getLeftCellIndex(List<Integer> cellIndex) {
        return this.cellIndexes.stream()
                .filter(index ->
                        CellIndex.getRowIndex(index) == CellIndex.getRowIndex(cellIndex)
                                && CellIndex.getColumnIndex(index) == CellIndex.getColumnIndex(cellIndex) - 1)
                .findAny();
    }

    public Optional<List<Integer>> getRightCellIndex(List<Integer> cellIndex) {
        return this.cellIndexes.stream()
                .filter(index ->
                        CellIndex.getRowIndex(index) == CellIndex.getRowIndex(cellIndex)
                                && CellIndex.getColumnIndex(index) == CellIndex.getColumnIndex(cellIndex) + 1)
                .findAny();
    }

    @Override
    public String toString() {
        return "rows %d, ".formatted(this.rowsCount)
                + "columns %d, ".formatted(this.columnsCount)
                + "boxes %d, ".formatted(this.boxesCount)
                + "values %s".formatted(this.values);
    }
}
