package kr.toxicity.hud.api.popup;

public interface PopupUpdater {
    boolean update();
    int getIndex();
    void setIndex(int index);
    void remove();
}
