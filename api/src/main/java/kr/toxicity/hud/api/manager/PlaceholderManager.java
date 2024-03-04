package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.placeholder.PlaceholderContainer;
import org.jetbrains.annotations.NotNull;

public interface PlaceholderManager {
    @NotNull PlaceholderContainer<Number> getNumberContainer();
    @NotNull PlaceholderContainer<Boolean> getBooleanContainer();
    @NotNull PlaceholderContainer<String> getStringContainer();
}
