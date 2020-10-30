package minegame159.meteorclient.mixin;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.events.EventStore;
import minegame159.meteorclient.events.OpenScreenEvent;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.gui.GuiKeyEvents;
import minegame159.meteorclient.gui.WidgetScreen;
import minegame159.meteorclient.mixininterface.IMinecraftClient;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.player.AutoEat;
import minegame159.meteorclient.modules.player.AutoGap;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.net.Proxy;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements IMinecraftClient {
    @Shadow private static int currentFps;
    @Shadow public ClientWorld world;
    @Shadow public Mouse mouse;
    @Shadow @Nullable public Screen currentScreen;
    @Shadow private int itemUseCooldown;
    @Shadow private Window window;

    @Shadow
    @Final
    private Proxy netProxy;

    @Shadow private Session session;

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    protected abstract void doAttack();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo info) {
        Meteor.INSTANCE.reload();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        if (Utils.canUpdate()) {
            world.getProfiler().swap("meteor-client_pre_update");
            Meteor.INSTANCE.getEventBus().post(EventStore.tickEvent(TickEvent.Type.PRE));
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onPostTick(CallbackInfo info) {
        if (Utils.canUpdate()) {
            world.getProfiler().swap("meteor-client_post_update");
            Meteor.INSTANCE.getEventBus().post(EventStore.tickEvent(TickEvent.Type.POST));
        }
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen) {
            screen.mouseMoved(mouse.getX() * window.getScaleFactor(), mouse.getY() * window.getScaleFactor());
        }

        OpenScreenEvent event = EventStore.openScreenEvent(screen);
        Meteor.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            info.cancel();
            return;
        }

        GuiKeyEvents.resetPostKeyEvents();
    }

    @Redirect(method = "doItemUse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal = 1))
    private HitResult doItemUseMinecraftClientCrosshairTargetProxy(MinecraftClient client) {
        if (ModuleManager.INSTANCE.get(AutoEat.class).rightClickThings() && ModuleManager.INSTANCE.get(AutoGap.class).rightClickThings()) {
            return client.crosshairTarget;
        }
        return null;
    }

    @Override
    public void leftClick() {
        doAttack();
    }

    @Override
    public void rightClick() {
        doItemUse();
    }

    @Override
    public void setItemUseCooldown(int cooldown) {
        itemUseCooldown = cooldown;
    }

    @Override
    public Proxy getProxy() {
        return netProxy;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public int getFps() {
        return currentFps;
    }
}
