package kr.toxicity.hud.api.popup;

/**
 * Represents popup's updater.
 */
public interface PopupUpdater {
    /**
     * Tries to update.
     * @return whether updating is success
     */
    boolean update();

    /**
     * Gets index.
     * @return index
     */
    int getIndex();

    /**
     * Sets index priority.
     * @param index new index
     */
    void setIndex(int index);

    /**
     * Removes this popup.
     */
    void remove();
}
