package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;

public class FullBright extends ToggleModule {
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> {
        if (event.getType() != TickEvent.Type.POST) {
            return;
        }

        mc.options.gamma = 16;
    });
    private double preGamma;

    public FullBright() {
        super(Category.Render, "full-bright", "No more darkness.");
    }

    @Override
    public void onActivate() {
        preGamma = mc.options.gamma;
        mc.options.gamma = 16;
    }

    @Override
    public void onDeactivate() {
        mc.options.gamma = preGamma;
    }
}
