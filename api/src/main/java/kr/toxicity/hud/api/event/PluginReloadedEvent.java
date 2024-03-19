package kr.toxicity.hud.api.event;

import kr.toxicity.hud.api.plugin.ReloadResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PluginReloadedEvent extends Event implements BetterHudEvent {
    private final ReloadResult result;
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
