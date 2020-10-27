package minegame159.meteorclient;

import lombok.Getter;
import lombok.Setter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listenable;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.accounts.AccountManager;
import minegame159.meteorclient.commands.commands.Ignore;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.gui.GuiKeyEvents;
import minegame159.meteorclient.gui.screens.topbar.TopBarModules;
import minegame159.meteorclient.macros.MacroManager;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.misc.DiscordPresence;
import minegame159.meteorclient.rendering.MFont;
import minegame159.meteorclient.rendering.MyFont;
import minegame159.meteorclient.utils.Capes;
import minegame159.meteorclient.utils.KeyBinds;
import minegame159.meteorclient.waypoints.Waypoints;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;

@Getter
public enum Meteor implements Listenable {
    INSTANCE;

    Meteor() { }

    private MinecraftClient minecraft;

    private final EventBus eventBus = new EventManager();
    private final Logger logger = LogManager.getLogger();
    private final File folder = new File(FabricLoader.getInstance().getGameDir().toString(), "meteor-client");

    private MFont font, font2x;
    private MyFont guiFont, guiTitleFont;

    @Setter private boolean inGame;
    @Setter private Screen screenToOpen;

    public void init() {
        logger.info("Initializing Meteor.");
        minecraft = MinecraftClient.getInstance();
        eventBus.subscribe(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void reload() {
        logger.info("Configuring Meteor.");

        Config.INSTANCE.load();
        loadFont();

        initManagers();
        Ignore.load();
        Waypoints.loadIcons();
    }

    private void initManagers() {
        ModuleManager.INSTANCE.init();
        if (!ModuleManager.INSTANCE.load()) ModuleManager.INSTANCE.get(DiscordPresence.class).toggle(false);
        FriendManager.INSTANCE.load();
        MacroManager.INSTANCE.load();
        AccountManager.INSTANCE.load();
    }

    private void stop() {
        Config.INSTANCE.save();
        ModuleManager.INSTANCE.save();
        FriendManager.INSTANCE.save();
        MacroManager.INSTANCE.save();
        AccountManager.INSTANCE.save();

        Ignore.save();
    }

    private void openClickGui() {
        minecraft.openScreen(new TopBarModules());
    }

    @EventHandler
    private final Listener<TickEvent> onTick = new Listener<>(event -> {
        Capes.INSTANCE.tick();

        if (screenToOpen != null && minecraft.currentScreen == null) {
            minecraft.openScreen(screenToOpen);
            screenToOpen = null;
        }

        if (KeyBinds.OPEN_CLICK_GUI.isPressed() && minecraft.currentScreen == null && GuiKeyEvents.postKeyEvents()) {
            openClickGui();
        }

        minecraft.player.getActiveStatusEffects().values().removeIf(statusEffectInstance -> statusEffectInstance.getDuration() <= 0);
    });

    private void loadFont() {
        File[] files = folder.exists() ? folder.listFiles() : new File[0];
        File fontFile = null;
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".ttf") || file.getName().endsWith(".TTF")) {
                    fontFile = file;
                    break;
                }
            }
        }

        if (fontFile == null) {
            try {
                fontFile = new File(folder, "JetBrainsMono-Regular.ttf");
                fontFile.getParentFile().mkdirs();

                InputStream in = Meteor.class.getResourceAsStream("/assets/meteor-client/JetBrainsMono-Regular.ttf");
                OutputStream out = new FileOutputStream(fontFile);

                byte[] bytes = new byte[255];
                int read;
                while ((read = in.read(bytes)) > 0) out.write(bytes, 0, read);

                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            font = new MFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f), true, true);
            font2x = new MFont(Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f * 2), true, true);
            font2x.scale = 0.5;
            guiFont = new MyFont(fontFile, 18);
            guiTitleFont = new MyFont(fontFile, 22);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public void resetFont() {
        File[] files = folder.exists() ? folder.listFiles() : new File[0];
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".ttf") || file.getName().endsWith(".TTF")) {
                    file.delete();
                }
            }
        }
    }

    public void onKeyInMainMenu(int key) {
        if (key == KeyBindingHelper.getBoundKeyOf(KeyBinds.OPEN_CLICK_GUI).getCode()) {
            openClickGui();
        }
    }
}
