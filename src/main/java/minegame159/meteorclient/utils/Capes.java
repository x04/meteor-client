package minegame159.meteorclient.utils;

import minegame159.meteorclient.Meteor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public enum Capes {
    INSTANCE;

    private final String CAPE_OWNERS_URL = "http://api.meteorclient.com:8082/capeowners";
    private final String CAPE_FOLDER_URL = "http://api.meteorclient.com:8082/capes";

    private final Map<UUID, String> OWNERS = new HashMap<>();
    private final Map<String, String> URLS = new HashMap<>();
    private final Map<String, Cape> TEXTURES = new HashMap<>();

    private final List<Cape> TO_REGISTER = new ArrayList<>();
    private final List<Cape> TO_RETRY = new ArrayList<>();
    private final List<Cape> TO_REMOVE = new ArrayList<>();

    Capes() {
        MeteorExecutor.INSTANCE.execute(() -> HttpUtils.getLines(CAPE_OWNERS_URL, s -> {
            String[] split = s.split(" ");

            if (split.length >= 2) {
                OWNERS.put(UUID.fromString(split[0]), split[1]);
                if (!TEXTURES.containsKey(split[1])) {
                    TEXTURES.put(split[1], new Cape(split[1]));
                }
            }
        }));

        // Capes
        MeteorExecutor.INSTANCE.execute(() -> HttpUtils.getLines(CAPE_FOLDER_URL, s -> {
            String[] split = s.split(" ");

            if (split.length >= 2) {
                if (!URLS.containsKey(split[0])) {
                    URLS.put(split[0], split[1]);
                }
            }
        }));
    }

    public Identifier getCape(PlayerEntity player) {
        String capeName = OWNERS.get(player.getUuid());
        if (capeName != null) {
            Cape cape = TEXTURES.get(capeName);
            if (cape == null) {
                return null;
            } else if (cape.isDownloaded()) {
                return cape;
            }

            cape.download();
            return null;
        }

        return null;
    }

    public void tick() {
        synchronized (TO_REGISTER) {
            for (Cape cape : TO_REGISTER)
                cape.register();
            TO_REGISTER.clear();
        }

        synchronized (TO_RETRY) {
            TO_RETRY.removeIf(Cape::tick);
        }

        synchronized (TO_REMOVE) {
            for (Cape cape : TO_REMOVE) {
                URLS.remove(cape.name);
                TEXTURES.remove(cape.name);
                TO_REGISTER.remove(cape);
                TO_RETRY.remove(cape);
            }

            TO_REMOVE.clear();
        }
    }

    private class Cape extends Identifier {
        private final String name;

        private boolean downloaded;
        private boolean downloading;

        private NativeImage img;

        private int retryTimer;

        public Cape(String name) {
            super("meteor-client", "capes/" + name);

            this.name = name;
        }

        public void download() {
            if (downloaded || downloading || retryTimer > 0) {
                return;
            }
            downloading = true;

            MeteorExecutor.INSTANCE.execute(() -> {
                try {
                    String url = URLS.get(name);
                    if (url == null) {
                        synchronized (TO_RETRY) {
                            TO_REMOVE.add(this);
                            downloading = false;
                            return;
                        }
                    }

                    InputStream in = HttpUtils.get(url);
                    if (in == null) {
                        synchronized (TO_RETRY) {
                            TO_RETRY.add(this);
                            retryTimer = 10 * 20;
                            downloading = false;
                            return;
                        }
                    }

                    img = NativeImage.read(in);

                    synchronized (TO_REGISTER) {
                        TO_REGISTER.add(this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void register() {
            Meteor.INSTANCE.getMinecraft().getTextureManager().registerTexture(this, new NativeImageBackedTexture(img));
            img = null;

            downloading = false;
            downloaded = true;
        }

        public boolean tick() {
            if (retryTimer > 0) {
                retryTimer--;
            } else {
                download();
                return true;
            }

            return false;
        }

        public boolean isDownloaded() {
            return downloaded;
        }
    }
}
