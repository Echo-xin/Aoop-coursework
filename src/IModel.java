/**
 * 游戏模型接口，定义了游戏的核心逻辑和状态管理
 */
public interface IModel {
    /**
     * 检查两个单词是否只相差一个字母
     * 
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 如果两个单词只相差一个字母返回true，否则返回false
     */
    boolean isOneLetterDifferent(String word1, String word2);

    /**
     * 检查输入的单词是否有效（是否在字典中且符合游戏规则）
     * 
     * @param word 要检查的单词
     * @return 如果单词有效返回true，否则返回false
     */
    boolean isValidWord(String word);

    /**
     * 处理玩家输入的单词，验证并更新游戏状态
     * 
     * @param input 玩家输入的单词
     */
    void processWord(String input);

    /**
     * 获取游戏的起始单词
     * 
     * @return 起始单词
     */
    String getStartWord();

    /**
     * 获取游戏的目标单词
     * 
     * @return 目标单词
     */
    String getTargetWord();

    /**
     * 添加游戏观察者，用于接收游戏状态更新
     * 
     * @param observer 要添加的观察者
     */
    void addGameObserver(GameObserver observer);

    /**
     * 移除游戏观察者
     * 
     * @param observer 要移除的观察者
     */
    void removeGameObserver(GameObserver observer);

    /**
     * 通知所有观察者游戏状态更新
     * 
     * @param message 更新消息
     */
    void notifyGameObservers(String message);

    /**
     * 获取当前尝试次数
     * 
     * @return 尝试次数
     */
    int getAttempts();
}

/**
 * 游戏观察者接口，用于接收游戏状态更新
 */
interface GameObserver {
    /**
     * 当游戏状态更新时被调用
     * 
     * @param message 更新消息
     */
    void onGameUpdate(String message);
}
