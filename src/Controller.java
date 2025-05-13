public class Controller {
    private IModel model;
    private View view;
    private boolean isGUI;

    public Controller(IModel model, View view, boolean isGUI) {
        this.model = model;
        this.view = view;
        this.isGUI = isGUI;
    }

    public void startGame() {
        view.displayGameStart();
        if (!isGUI) {
            // CLI模式下的游戏循环
            while (true) {
                try {
                    String userInput = view.getUserInput();
                    if (userInput != null && !userInput.isEmpty()) {
                        handleUserInput(userInput);
                    }
                } catch (Exception e) {
                    System.err.println("Input processing error: " + e.getMessage());
                }
            }
        }
    }

    public void handleUserInput(String input) {
        if (input != null && !input.isEmpty()) {
            // 过滤掉误输入的反馈消息
            if (input.startsWith("feedback:") || input.startsWith("Feedback:")) {
                return; // 忽略误输入的反馈消息
            }

            if (input.equals("restart")) {
                startNewGame();
            } else {
                model.processWord(input);
            }
        }
    }

    public String getStartWord() {
        return model.getStartWord();
    }

    public String getTargetWord() {
        return model.getTargetWord();
    }

    public IModel getModel() {
        return model;
    }

    public int getAttempts() {
        return model.getAttempts();
    }

    public void startNewGame() {
        // 在Model中重新选择单词
        model = new Model(isGUI);
        // 重新设置视图
        view.setController(this);

        // 如果是CLI模式，添加重新开始的提示
        if (!isGUI) {
            System.out.println("\n=== Game Restarted ===");
        }

        // 显示新游戏
        view.displayGameStart();
    }
}
