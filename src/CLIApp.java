import java.util.Scanner;

public class CLIApp {
    public static void main(String[] args) {
        try {
            IModel model = new Model(true);
            View view = new View(false);
            Controller controller = new Controller(model, view, false);
            view.setController(controller);
            controller.startGame();
        } catch (Exception e) {
            System.err.println("Game error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
