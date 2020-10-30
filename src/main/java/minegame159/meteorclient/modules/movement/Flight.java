package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.events.packets.SendPacketEvent;
import minegame159.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Flight extends ToggleModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").description("Mode.").defaultValue(Mode.Vanilla).build());
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder().name("speed").description("Speed.").defaultValue(0.1).min(0.0).build());
    private boolean flip;
    private float lastYaw;
    @EventHandler private final Listener<TickEvent> onTick = new Listener<>(event -> {
        if (event.getType() == TickEvent.Type.PRE) {
            float currentYaw = mc.player.yaw;
            if (mc.player.fallDistance >= 3f && currentYaw == lastYaw && mc.player.getVelocity().length() < 0.003d) {
                mc.player.yaw += flip ? 1 : -1;
                flip = !flip;
            }
            lastYaw = currentYaw;
        } else {
            if (mc.player.yaw != lastYaw) {
                mc.player.yaw = lastYaw;
            }

            if (mode.get() == Mode.Vanilla && !mc.player.isSpectator()) {
                mc.player.abilities.setFlySpeed(speed.get().floatValue());
                mc.player.abilities.flying = true;
                if (mc.player.abilities.creativeMode) {
                    return;
                }
                mc.player.abilities.allowFlying = true;
            }
        }
    });
    private long lastModifiedTime = 0;
    private double lastY = Double.MAX_VALUE;
    /**
     * @see net.minecraft.server.network.ServerPlayNetworkHandler#onPlayerMove(PlayerMoveC2SPacket)
     */
    @EventHandler private final Listener<SendPacketEvent> onSendPacket = new Listener<>(event -> {
        if (!(event.packet instanceof PlayerMoveC2SPacket)) {
            return;
        }

        PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.packet;
        long currentTime = System.currentTimeMillis();
        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            // maximum time we can be "floating" is 80 ticks, so 4 seconds max
            // we'll be safe and modify every second or what we can assume is 20 ticks
            if (currentTime - lastModifiedTime > 250 && lastY != Double.MAX_VALUE && mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {
                // actual check is for >= -0.03125D but we have to do a bit more than that
                // probably due to compression or some shit idk
                ((IPlayerMoveC2SPacket) packet).setY(lastY - 0.03130D);
                lastModifiedTime = currentTime;
            } else {
                lastY = currentY;
            }
        }
    });

    public Flight() {
        super(Category.Movement, "flight", "FLYYYY! You will take fall damage so enable no fall.");
    }

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Vanilla && !mc.player.isSpectator()) {
            mc.player.abilities.flying = true;
            if (mc.player.abilities.creativeMode) {
                return;
            }
            mc.player.abilities.allowFlying = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (mode.get() == Mode.Vanilla && !mc.player.isSpectator()) {
            mc.player.abilities.flying = false;
            mc.player.abilities.setFlySpeed(0.05f);
            if (mc.player.abilities.creativeMode) {
                return;
            }
            mc.player.abilities.allowFlying = false;
        }
    }

    public enum Mode {
        Vanilla
    }
}
