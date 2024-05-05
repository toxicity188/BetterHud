package kr.toxicity.hud.api.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CreateShaderEvent extends Event implements BetterHudEvent {
    @NotNull
    private final List<String> lines = new ArrayList<>();

    public CreateShaderEvent() {
        super(true);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
