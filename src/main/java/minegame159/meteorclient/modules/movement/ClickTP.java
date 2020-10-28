package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ClickTP extends ToggleModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder().name("max-distance").description("Maximum distance.").defaultValue(5).build());
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> {
        if (mc.player.isUsingItem()) {
            return;
        }

        if (mc.options.keyUse.isPressed()) {
            HitResult hitResult = mc.player.raycast(maxDistance.get(), 1f / 20f, false);

            if (hitResult.getType() == HitResult.Type.ENTITY && mc.player.interact(((EntityHitResult) hitResult).getEntity(), Hand.MAIN_HAND) != ActionResult.PASS) {
                return;
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                Direction side = ((BlockHitResult) hitResult).getSide();

                if (mc.world.getBlockState(pos).onUse(mc.world, mc.player, Hand.MAIN_HAND, (BlockHitResult) hitResult) != ActionResult.PASS) {
                    return;
                }

                mc.player.updatePosition(pos.getX() + 0.5 + side.getOffsetX(), pos.getY() + side.getOffsetY(), pos.getZ() + 0.5 + side.getOffsetZ());
            }
        }
    });

    public ClickTP() {
        super(Category.Movement, "click-tp", "Teleports you to the block you are looking at.");
    }
}
