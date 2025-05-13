import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class View extends JFrame implements GameObserver {
    private Controller controller;

    // 图形界面组件
    private JPanel gamePanel;
    private JTextField[] inputFields;
    private JButton submitButton;
    private JLabel[] startWordLabels;
    private JLabel[] targetWordLabels;
    private List<JLabel[]> historyLabels;
    private JPanel historyPanel;
    private JPanel keyboardPanel;
    private JTextArea feedbackArea;
    private Color appleBlue = new Color(0, 122, 255);
    private Color appleGray = new Color(142, 142, 147);
    private Color appleLightGray = new Color(229, 229, 234);
    private Color appleGreen = new Color(52, 199, 89);
    private Color appleRed = new Color(255, 59, 48);

    // 命令行界面组件
    private Scanner scanner;

    public View(boolean isGUI) {
        if (isGUI) {
            historyLabels = new ArrayList<>();
            setupGUI();
            setVisible(true);
        } else {
            createCLI();
        }
    }

    private void setupGUI() {
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(500, 800);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gamePanel = new JPanel();
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gamePanel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setMaximumSize(new Dimension(450, 40));
        topPanel.setPreferredSize(new Dimension(450, 40));

        JLabel titleLabel = new JLabel("Weaver Game");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Add restart button
        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("SF Pro Display", Font.PLAIN, 14));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(appleBlue);
        restartButton.setBorderPainted(false);
        restartButton.setFocusPainted(false);
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restartButton.addActionListener(e -> restartGame());
        restartButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                restartButton.setBackground(appleBlue.darker());
            }

            public void mouseExited(MouseEvent e) {
                restartButton.setBackground(appleBlue);
            }
        });
        topPanel.add(restartButton, BorderLayout.EAST);

        gamePanel.add(topPanel);
        gamePanel.add(Box.createVerticalStrut(20));

        // 使用GridLayout确保所有单元格大小一致
        // 创建起始单词行
        JPanel startWordPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        startWordPanel.setBackground(Color.WHITE);
        startWordPanel.setMaximumSize(new Dimension(450, 70));
        startWordPanel.setMinimumSize(new Dimension(450, 70));
        startWordPanel.setPreferredSize(new Dimension(450, 70));
        startWordLabels = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            startWordLabels[i] = createLetterLabel("");
            startWordLabels[i].setBackground(Color.WHITE);
            startWordLabels[i].setForeground(Color.BLACK);
            startWordPanel.add(startWordLabels[i]);
        }
        gamePanel.add(startWordPanel);
        gamePanel.add(Box.createVerticalStrut(20));

        // 创建输入行
        JPanel inputPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setMaximumSize(new Dimension(450, 70));
        inputPanel.setMinimumSize(new Dimension(450, 70));
        inputPanel.setPreferredSize(new Dimension(450, 70));
        inputFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            inputFields[i] = createInputField();
            inputPanel.add(inputFields[i]);
        }
        gamePanel.add(inputPanel);
        gamePanel.add(Box.createVerticalStrut(20));

        // 创建目标单词行
        JPanel targetWordPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        targetWordPanel.setBackground(Color.WHITE);
        targetWordPanel.setMaximumSize(new Dimension(450, 70));
        targetWordPanel.setMinimumSize(new Dimension(450, 70));
        targetWordPanel.setPreferredSize(new Dimension(450, 70));
        targetWordLabels = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            targetWordLabels[i] = createLetterLabel("");
            targetWordLabels[i].setBackground(Color.WHITE);
            targetWordLabels[i].setForeground(Color.BLACK);
            targetWordPanel.add(targetWordLabels[i]);
        }
        gamePanel.add(targetWordPanel);
        gamePanel.add(Box.createVerticalStrut(30));

        // 确保所有面板在垂直方向上居中对齐
        startWordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        targetWordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 创建提交按钮
        submitButton = createAppleButton("Submit");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> {
            if (controller != null) {
                handleInput();
            }
        });
        gamePanel.add(submitButton);
        gamePanel.add(Box.createVerticalStrut(30));

        // 创建历史记录面板
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(Color.WHITE);
        JScrollPane historyScrollPane = new JScrollPane(historyPanel);
        historyScrollPane.setPreferredSize(new Dimension(400, 200));
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gamePanel.add(historyScrollPane);
        gamePanel.add(Box.createVerticalStrut(20));

        // 创建虚拟键盘
        createVirtualKeyboard();

        add(gamePanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);

        // 初始焦点设置到第一个输入框
        SwingUtilities.invokeLater(() -> {
            if (inputFields != null && inputFields.length > 0) {
                inputFields[0].requestFocus();
            }
        });
    }

    private void createVirtualKeyboard() {
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new GridLayout(4, 10, 5, 5));
        keyboardPanel.setBackground(Color.WHITE);
        keyboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] letters = {
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                "a", "s", "d", "f", "g", "h", "j", "k", "l",
                "z", "x", "c", "v", "b", "n", "m"
        };

        // 添加字母键
        for (String letter : letters) {
            JButton keyButton = createKeyButton(letter);
            keyboardPanel.add(keyButton);
        }

        // 添加删除键
        JButton deleteButton = new JButton("⌫");
        deleteButton.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        deleteButton.setForeground(Color.BLACK);
        deleteButton.setBackground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(appleLightGray, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteButton.addActionListener(e -> {
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused instanceof JTextField) {
                handleBackspace((JTextField) focused);
            } else {
                // 如果没有焦点，尝试选择最后一个有内容的字段
                for (int i = inputFields.length - 1; i >= 0; i--) {
                    if (!inputFields[i].getText().isEmpty()) {
                        inputFields[i].requestFocus();
                        handleBackspace(inputFields[i]);
                        break;
                    } else if (i == 0) {
                        // 如果所有字段都为空，选择第一个
                        inputFields[0].requestFocus();
                    }
                }
            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                deleteButton.setBackground(appleLightGray);
            }

            public void mouseReleased(MouseEvent e) {
                deleteButton.setBackground(Color.WHITE);
            }
        });

        keyboardPanel.add(deleteButton);

        gamePanel.add(keyboardPanel);
    }

    private JButton createKeyButton(String letter) {
        JButton button = new JButton(letter.toUpperCase());
        button.setFont(new Font("SF Pro Display", Font.BOLD, 16));
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(appleLightGray, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            // 找到目前活跃的输入框或第一个空的输入框
            JTextField targetField = null;

            // 先查看是否有焦点的输入框
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused instanceof JTextField && Arrays.asList(inputFields).contains(focused)) {
                targetField = (JTextField) focused;
            } else {
                // 如果没有，找第一个空的输入框
                for (JTextField field : inputFields) {
                    if (field.getText().isEmpty() && field.isEnabled()) {
                        targetField = field;
                        break;
                    }
                }

                // 如果没有空的，使用第一个未禁用的
                if (targetField == null) {
                    for (JTextField field : inputFields) {
                        if (field.isEnabled()) {
                            targetField = field;
                            break;
                        }
                    }
                }
            }

            // 如果找到了目标框，输入字母并跳转
            if (targetField != null) {
                targetField.requestFocus();
                targetField.setText(letter);
                moveToNextField(targetField);
            }
        });

        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                button.setBackground(appleLightGray);
            }

            public void mouseReleased(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void moveToNextField(JTextField currentField) {
        for (int i = 0; i < inputFields.length; i++) {
            if (inputFields[i] == currentField && i < inputFields.length - 1) {
                inputFields[i + 1].requestFocus();
                break;
            }
        }
    }

    private void handleDeleteKey() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused instanceof JTextField) {
            handleBackspace((JTextField) focused);
        }
    }

    private void handleBackspace(JTextField currentField) {
        if (currentField.getText().isEmpty()) {
            // 如果当前字段为空，移动到前一个字段并清空它
            for (int i = 0; i < inputFields.length; i++) {
                if (inputFields[i] == currentField && i > 0) {
                    inputFields[i - 1].requestFocus();
                    inputFields[i - 1].setText("");
                    break;
                }
            }
        } else {
            // 如果当前字段有内容，清空当前字段
            currentField.setText("");
        }
    }

    private JLabel createLetterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        label.setPreferredSize(new Dimension(60, 60));
        label.setMaximumSize(new Dimension(60, 60));
        label.setMinimumSize(new Dimension(60, 60));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(appleLightGray, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField(1);
        field.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setPreferredSize(new Dimension(60, 60));
        field.setMaximumSize(new Dimension(60, 60));
        field.setMinimumSize(new Dimension(60, 60));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(appleLightGray, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        field.setBackground(Color.WHITE);
        field.setCaretColor(Color.BLACK);
        field.setSelectedTextColor(Color.WHITE);
        field.setSelectionColor(appleBlue);

        // 禁用自动调整大小
        field.setColumns(1);

        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isLetter(c)) {
                    e.consume(); // 阻止默认的输入行为
                    field.setText(String.valueOf(Character.toLowerCase(c)));
                    moveToNextField(field);
                } else {
                    e.consume();
                }
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    e.consume(); // 阻止默认的删除行为
                    handleBackspace(field);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleInput();
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(appleBlue, 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            public void focusLost(FocusEvent e) {
                if (field.getBackground() == Color.WHITE) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(appleLightGray, 2),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                }
            }
        });

        return field;
    }

    private JButton createAppleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SF Pro Display", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(appleBlue);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
        button.setMaximumSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(appleBlue.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(appleBlue);
            }

            public void mousePressed(MouseEvent e) {
                button.setBackground(appleBlue.darker().darker());
            }

            public void mouseReleased(MouseEvent e) {
                button.setBackground(appleBlue.darker());
            }
        });

        return button;
    }

    private void handleInput() {
        StringBuilder word = new StringBuilder();
        boolean hasEmptyField = false;

        // 检查所有输入框
        for (JTextField field : inputFields) {
            String text = field.getText().trim().toLowerCase();
            if (text.isEmpty()) {
                hasEmptyField = true;
                break;
            }
            if (text.length() != 1 || !Character.isLetter(text.charAt(0))) {
                return;
            }
            word.append(text);
        }

        if (hasEmptyField) {
            return;
        }

        String inputWord = word.toString();
        if (inputWord.length() == 4) {
            if (controller != null) {
                controller.handleUserInput(inputWord);
            }
        }
    }

    private void createCLI() {
        scanner = new Scanner(System.in);
        historyLabels = new ArrayList<>(); // 确保CLI模式下也初始化historyLabels
    }

    public void displayGameStart() {
        if (scanner != null) {
            System.out.println("\n=== Weaver Game ===");
            System.out.println("Game Rules:");
            System.out.println("1. You can only change one letter at a time");
            System.out.println("2. Words must exist in the dictionary");
            System.out.println("3. Feedback explanation:");
            System.out.println("   G - Correct letter in correct position");
            System.out.println("   ✗ - Incorrect letter");
            System.out.println("4. Type 'restart' to start a new game anytime");
            System.out.println("\nStart word: " + controller.getStartWord());
        System.out.println("Target word: " + controller.getTargetWord());
            System.out.println("\nEnter your guess:");
        } else {
            String startWord = controller.getStartWord();
            String targetWord = controller.getTargetWord();

            // 显示起始单词
            for (int i = 0; i < 4; i++) {
                startWordLabels[i].setText(String.valueOf(startWord.charAt(i)));
                startWordLabels[i].setBackground(Color.WHITE);
                startWordLabels[i].setForeground(Color.BLACK);
            }

            // 显示目标单词
            for (int i = 0; i < 4; i++) {
                targetWordLabels[i].setText(String.valueOf(targetWord.charAt(i)));
                targetWordLabels[i].setBackground(Color.WHITE);
                targetWordLabels[i].setForeground(Color.BLACK);
            }

            // 清空输入框
            for (JTextField field : inputFields) {
                field.setText("");
                field.setBackground(Color.WHITE);
                field.setForeground(Color.BLACK);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(appleLightGray, 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            // 清空反馈和历史记录
            clearFeedback();
        }
    }

    public String getUserInput() {
        if (scanner != null) {
            System.out.print("\nEnter your word (type 'restart' to start a new game): ");
            String input = scanner.nextLine().trim().toLowerCase();

            // 检查输入有效性，避免误输入反馈消息
            if (input.startsWith("feedback:") || input.startsWith("Feedback:")) {
                System.out.println("Please enter a valid 4-letter word.");
                return getUserInput(); // 重新获取输入
            }

            return input;
        } else {
            StringBuilder word = new StringBuilder();
            for (JTextField field : inputFields) {
                word.append(field.getText().trim().toLowerCase());
            }
            return word.toString();
        }
    }

    @Override
    public void onGameUpdate(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.equals("Game Won!")) {
                displayWinner();
            } else {
                displayFeedback(message);
            }
        });
    }

    public void displayFeedback(String feedback) {
        if (scanner != null) {
            System.out.println(feedback);
        } else {
            if (feedback.startsWith("Feedback:")) {
                String result = feedback.substring(9).trim();
                updateInputFieldsWithFeedback(result);
                addWordToHistory(result);
            }
        }
    }

    private void updateInputFieldsWithFeedback(String feedback) {
        for (int i = 0; i < feedback.length(); i++) {
            char result = feedback.charAt(i);
            JTextField field = inputFields[i];
            if (result == 'G') {
                // 如果字母正确，设置为绿色并禁用
                field.setBackground(appleGreen);
                field.setForeground(Color.WHITE);
                field.setEnabled(false);
            } else {
                // 如果字母不存在或位置不对，统一设置为灰色
                field.setBackground(appleGray);
                field.setForeground(Color.WHITE);
            }
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(field.getBackground(), 2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            field.repaint();
        }
    }

    private void addWordToHistory(String feedback) {
        // 创建单词面板
        JPanel wordPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        wordPanel.setBackground(Color.WHITE);
        wordPanel.setMaximumSize(new Dimension(450, 70));
        wordPanel.setMinimumSize(new Dimension(450, 70));
        wordPanel.setPreferredSize(new Dimension(450, 70));

        JLabel[] letterLabels = new JLabel[4];

        for (int i = 0; i < 4; i++) {
            String letter = inputFields[i].getText();
            letterLabels[i] = createLetterLabel(letter);
            char result = feedback.charAt(i);
            if (result == 'G') {
                letterLabels[i].setBackground(appleGreen);
                letterLabels[i].setForeground(Color.WHITE);
            } else {
                // 如果字母不存在或位置不对，统一设置为灰色
                letterLabels[i].setBackground(appleGray);
                letterLabels[i].setForeground(Color.WHITE);
            }
            wordPanel.add(letterLabels[i]);
        }

        // 添加连接线
        if (!historyLabels.isEmpty()) {
            JPanel linePanel = new JPanel();
            linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.Y_AXIS));
            linePanel.setBackground(Color.WHITE);

            JLabel line = new JLabel("↓");
            line.setFont(new Font("SF Pro Display", Font.PLAIN, 20));
            line.setAlignmentX(Component.CENTER_ALIGNMENT);
            linePanel.add(line);

            historyPanel.add(linePanel);
        }

        historyPanel.add(wordPanel);
        historyPanel.add(Box.createVerticalStrut(5));
        historyLabels.add(letterLabels);
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    public void displayWinner() {
        if (scanner != null) {
            System.out.println("\nCongratulations! You found the correct word!");
            System.out.println("Number of attempts: " + controller.getAttempts());
            System.out.println("Type 'restart' to start a new game");
        } else {
            // 将最后一步设置为目标单词
            String targetWord = controller.getTargetWord();
            for (int i = 0; i < 4; i++) {
                inputFields[i].setText(String.valueOf(targetWord.charAt(i)));
                inputFields[i].setBackground(appleGreen);
                inputFields[i].setForeground(Color.WHITE);
                inputFields[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(appleGreen, 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                inputFields[i].setEnabled(false);
            }

            // 添加最后一步到历史记录
            addFinalWordToHistory();

            // 创建胜利提示对话框
            JDialog winDialog = new JDialog(this, "Game Won!", true);
            winDialog.setLayout(new BorderLayout());

            // 创建胜利消息面板
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            messagePanel.setBackground(Color.WHITE);

            // 添加胜利图标
            JLabel iconLabel = new JLabel("🎉");
            iconLabel.setFont(new Font("SF Pro Display", Font.PLAIN, 48));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(iconLabel);
            messagePanel.add(Box.createVerticalStrut(20));

            // 添加胜利消息
            JLabel messageLabel = new JLabel("Congratulations! You found the correct word!");
            messageLabel.setFont(new Font("SF Pro Display", Font.BOLD, 18));
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(messageLabel);

            // 添加尝试次数
            JLabel attemptsLabel = new JLabel("Number of attempts: " + controller.getAttempts());
            attemptsLabel.setFont(new Font("SF Pro Display", Font.PLAIN, 16));
            attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(Box.createVerticalStrut(10));
            messagePanel.add(attemptsLabel);

            // 添加确定按钮
            JButton okButton = createAppleButton("OK");
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            okButton.addActionListener(e -> winDialog.dispose());
            messagePanel.add(Box.createVerticalStrut(20));
            messagePanel.add(okButton);

            winDialog.add(messagePanel, BorderLayout.CENTER);
            winDialog.pack();
            winDialog.setLocationRelativeTo(this);

            // 禁用游戏输入
            submitButton.setEnabled(false);

            // 显示对话框
            winDialog.setVisible(true);
        }
    }

    private void addFinalWordToHistory() {
        // 如果历史记录不为空，添加连接线
        if (!historyLabels.isEmpty()) {
            JPanel linePanel = new JPanel();
            linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.Y_AXIS));
            linePanel.setBackground(Color.WHITE);

            JLabel line = new JLabel("↓");
            line.setFont(new Font("SF Pro Display", Font.PLAIN, 20));
            line.setAlignmentX(Component.CENTER_ALIGNMENT);
            linePanel.add(line);

            historyPanel.add(linePanel);
        }

        // 创建目标单词的面板
        JPanel wordPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        wordPanel.setBackground(Color.WHITE);
        wordPanel.setMaximumSize(new Dimension(450, 70));
        wordPanel.setMinimumSize(new Dimension(450, 70));
        wordPanel.setPreferredSize(new Dimension(450, 70));

        JLabel[] letterLabels = new JLabel[4];
        String targetWord = controller.getTargetWord();

        for (int i = 0; i < 4; i++) {
            letterLabels[i] = createLetterLabel(String.valueOf(targetWord.charAt(i)));
            letterLabels[i].setBackground(appleGreen);
            letterLabels[i].setForeground(Color.WHITE);
            wordPanel.add(letterLabels[i]);
        }

        historyPanel.add(wordPanel);
        historyPanel.add(Box.createVerticalStrut(5));
        historyLabels.add(letterLabels);
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    public void setController(Controller controller) {
        this.controller = controller;
        // 确保View被注册为Model的观察者
        if (controller != null && controller.getModel() != null) {
            controller.getModel().addGameObserver(this);
        }
    }

    public void setStartWord(String word) {
        for (int i = 0; i < 4; i++) {
            startWordLabels[i].setText(String.valueOf(word.charAt(i)));
        }
    }

    public void setTargetWord(String word) {
        for (int i = 0; i < 4; i++) {
            targetWordLabels[i].setText(String.valueOf(word.charAt(i)));
        }
    }

    public void setAttempts(int attempts) {
        // Implementation needed
    }

    public void clearFeedback() {
        historyPanel.removeAll();
        historyLabels.clear();
        historyPanel.revalidate();
        historyPanel.repaint();
    }

    private void restartGame() {
        // 请求新游戏
        if (controller != null) {
            // 重置界面
            for (JTextField field : inputFields) {
                field.setText("");
                field.setBackground(Color.WHITE);
                field.setForeground(Color.BLACK);
                field.setEnabled(true);
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(appleLightGray, 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            }

            // 启用提交按钮
            submitButton.setEnabled(true);

            // 清空历史记录
            clearFeedback();

            // 调用控制器重新开始游戏
            controller.startNewGame();
        }
    }
}
