public class Main {
    public static void main(String[] args) {
//        GridLayout gridLayout = new GridLayout(4, 4, 4, IntStream.rangeClosed(1, 4).boxed().collect(Collectors.toSet()));
        GridLayout gridLayout = GridLayout.newClassic();
        Grid grid = Grid.newRandomComplete(gridLayout);
        System.out.printf("Layout: %s\n", gridLayout.toString());
        System.out.println("Values:");
        System.out.println(grid.toPrintableString());
        System.out.printf("Is complete: %s\n", grid.isComplete());
        System.out.printf("Is valid: %s\n", grid.isValid());
    }
}
