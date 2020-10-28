package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.mixininterface.IMinecraftClient;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;

public class FastUse extends ToggleModule {
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> ((IMinecraftClient) mc).setItemUseCooldown(0));

    public FastUse() {
        super(Category.Player, "fast-use", "Fast item use.");
    }
}
