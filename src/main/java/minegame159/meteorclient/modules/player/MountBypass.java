package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.packets.SendPacketEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.modules.misc.AutoMountBypassDupe;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class MountBypass extends ToggleModule {
    private boolean dontCancel;
    @EventHandler private final Listener<SendPacketEvent> onSendPacket = new Listener<>(event -> {
        if (ModuleManager.INSTANCE.isActive(AutoMountBypassDupe.class)) {
            return;
        }

        onSendPacket(event);
    });

    public MountBypass() {
        super(Category.Player, "mount-bypass", "Allows you to bypass illegal stacks and put chests on donkeys.");
    }

    public void onSendPacket(SendPacketEvent event) {
        if (dontCancel) {
            dontCancel = false;
            return;
        }

        if (!(event.packet instanceof PlayerInteractEntityC2SPacket)) {
            return;
        }
        PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) event.packet;

        if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT && packet.getEntity(mc.world) instanceof AbstractDonkeyEntity) {
            event.cancel();
        }
    }

    public void dontCancel() {
        if (isActive()) {
            dontCancel = true;
        }
    }
}
