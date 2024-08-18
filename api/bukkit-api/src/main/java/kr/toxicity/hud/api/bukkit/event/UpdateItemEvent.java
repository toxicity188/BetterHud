package kr.toxicity.hud.api.bukkit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * player's inventory change event.
 */
public class UpdateItemEvent extends PlayerEvent implements BetterHudEvent {
    @Getter
    private final @NotNull ItemStack itemStack;
    @Getter
    private final @NotNull Event original;
    private ItemMeta itemMeta;
    public UpdateItemEvent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull Event original) {
        super(player);
        this.itemStack = itemStack;
        this.original = original;
    }

    public synchronized @NotNull ItemMeta getItemMeta() {
        if (itemMeta == null) itemMeta = itemStack.getItemMeta();
        return Objects.requireNonNull(itemMeta);
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
