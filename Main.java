import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
//        GridLayout gridLayout = new GridLayout(4, 4, 4, IntStream.rangeClosed(1, 4).boxed().collect(Collectors.toSet()));
        GridLayout gridLayout = GridLayout.newClassic();
        System.out.printf("Layout: %s\n", gridLayout);
        System.out.print("\n");

        Map<List<Integer>, Integer> completedCellValues = CellValues.newRandomCompleted(gridLayout);
        System.out.println("Completed cell values:");
        System.out.print(CellValues.toPrintableString(gridLayout, completedCellValues));
        System.out.printf("Is completed: %s\n", CellValues.isCompleted(gridLayout, completedCellValues));
        System.out.printf("Is valid: %s\n", CellValues.isValid(gridLayout, completedCellValues));
        System.out.print("\n");

        final int cluesCount = (int) (gridLayout.cellsCount * 0.4);
        Map<List<Integer>, Integer> partiallyCompletedCellValues = CellValues.newRandomPartiallyCompleted(gridLayout, completedCellValues, cluesCount);
        System.out.printf("Partially completed cell values (%d clues):\n", cluesCount);
        System.out.print(CellValues.toPrintableString(gridLayout, partiallyCompletedCellValues));
        System.out.printf("Is completed: %s\n", CellValues.isCompleted(gridLayout, partiallyCompletedCellValues));
        System.out.printf("Is valid: %s\n", CellValues.isValid(gridLayout, partiallyCompletedCellValues));
        System.out.print("\n");

        Map<List<Integer>, Integer> partiallyCompletedCellValuesCopy = new HashMap<>(partiallyCompletedCellValues);
        boolean ok = CellValues.backtracking(gridLayout, partiallyCompletedCellValuesCopy);
        if (ok) {
            System.out.println("Solved cell values:");
            System.out.print(CellValues.toPrintableString(gridLayout, partiallyCompletedCellValuesCopy));
            System.out.printf("Is completed: %s\n", CellValues.isCompleted(gridLayout, partiallyCompletedCellValuesCopy));
            System.out.printf("Is valid: %s\n", CellValues.isValid(gridLayout, partiallyCompletedCellValuesCopy));
        } else {
            System.out.println("Solution not found");
        }
    }
}
