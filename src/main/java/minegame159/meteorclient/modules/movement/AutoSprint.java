package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

public class AutoSprint extends ToggleModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").description("Mode.").defaultValue(Mode.Legit).build());
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> {
        switch (mode.get()) {
            case Legit: {
                if (mc.player.isSprinting()) {
                    return;
                }

                mc.player.setSprinting(canSprint());
                break;
            }
            case Always: {
                mc.player.setSprinting(true);
                break;
            }
        }
    });

    public AutoSprint() {
        super(Category.Movement, "auto-sprint", "Automatically sprints.");
    }

    @Override
    public void onDeactivate() {
        mc.player.setSprinting(false);
    }

    /**
     * @return If player can sprint.
     * @see ClientPlayerEntity#tickMovement()
     */
    public boolean canSprint() {
        boolean bl5 = (float) mc.player.getHungerManager().getFoodLevel() > 6.0F || mc.player.abilities.allowFlying;
        return (mc.player.isOnGround() || mc.player.isSubmergedInWater()) && !mc.player.isSneaking() && !mc.player.input.jumping && mc.player.forwardSpeed > 0f && !mc.player.isSprinting() && bl5 && !mc.player.isUsingItem() && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    public enum Mode {
        Legit, Always
    }
}
