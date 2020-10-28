package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.stream.Stream;

public class Parkour extends ToggleModule {
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> {
        if (!mc.player.isOnGround() || mc.options.keyJump.isPressed()) {
            return;
        }

        if (mc.player.isSneaking() || mc.options.keySneak.isPressed()) {
            return;
        }

        Box box = mc.player.getBoundingBox();
        Box adjustedBox = box.offset(0, -0.5, 0).expand(-0.001, 0, -0.001);

        Stream<VoxelShape> blockCollisions = mc.world.getBlockCollisions(mc.player, adjustedBox);

        if (blockCollisions.findAny().isPresent()) {
            return;
        }

        mc.player.jump();
    });

    public Parkour() {
        super(Category.Movement, "parkour", "Automatically jumps at the edges of blocks.");
    }
}
