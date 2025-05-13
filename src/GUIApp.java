import javax.swing.SwingUtilities;

public class GUIApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                IModel model = new Model(false);
                View view = new View(true);
                Controller controller = new Controller(model, view, true);
                view.setController(controller);
                controller.startGame();
            } catch (Exception e) {
                System.err.println("Game error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
