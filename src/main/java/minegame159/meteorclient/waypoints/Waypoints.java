package minegame159.meteorclient.waypoints;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listenable;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.events.EventStore;
import minegame159.meteorclient.events.GameDisconnectedEvent;
import minegame159.meteorclient.events.GameJoinedEvent;
import minegame159.meteorclient.events.RenderEvent;
import minegame159.meteorclient.rendering.Matrices;
import minegame159.meteorclient.rendering.ShapeBuilder;
import minegame159.meteorclient.utils.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.CompoundTag;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.*;

public class Waypoints extends Savable<Waypoints> implements Listenable, Iterable<Waypoint> {
    public static final Map<String, AbstractTexture> ICONS = new HashMap<>();
    public static final Waypoints INSTANCE = new Waypoints();

    private static final String[] BUILTIN_ICONS = {"Square", "Circle", "Triangle", "Star", "Diamond"};

    private static final Color BACKGROUND = new Color(0, 0, 0, 75);
    private static final Color TEXT = new Color(255, 255, 255);
    @EventHandler private final Listener<GameJoinedEvent> onGameJoined = new Listener<>(event -> load());
    @EventHandler private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        for (Waypoint waypoint : this) {
            if (!waypoint.visible || !checkDimension(waypoint)) {
                continue;
            }

            Camera camera = Meteor.INSTANCE.getMinecraft().gameRenderer.getCamera();

            // Compute scale
            double dist = Utils.distanceToCamera(waypoint.x, waypoint.y, waypoint.z);
            if (dist > waypoint.maxVisibleDistance) {
                continue;
            }
            double scale = 0.04;
            if (dist > 15) {
                scale *= dist / 15;
            }

            double a = 1;
            if (dist < 10) {
                a = dist / 10;
                if (a < 0.1) {
                    continue;
                }
            }

            int preBgA = BACKGROUND.a;
            int preTextA = TEXT.a;
            BACKGROUND.a *= a;
            TEXT.a *= a;

            double x = waypoint.x;
            double y = waypoint.y;
            double z = waypoint.z;

            double maxViewDist = Meteor.INSTANCE.getMinecraft().options.viewDistance * 16;
            if (dist > maxViewDist) {
                double dx = waypoint.x - camera.getPos().x;
                double dy = waypoint.y - camera.getPos().y;
                double dz = waypoint.z - camera.getPos().z;

                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                dx /= length;
                dy /= length;
                dz /= length;

                dx *= maxViewDist;
                dy *= maxViewDist;
                dz *= maxViewDist;

                x = camera.getPos().x + dx;
                y = camera.getPos().y + dy;
                z = camera.getPos().z + dz;

                scale /= dist / 15;
                scale *= maxViewDist / 15;
            }

            // Setup the rotation
            Matrices.push();
            Matrices.translate(x + 0.5 - event.offsetX, y - event.offsetY, z + 0.5 - event.offsetZ);
            Matrices.translate(0, -0.5 + waypoint.scale - 1, 0);
            Matrices.rotate(-camera.getYaw(), 0, 1, 0);
            Matrices.rotate(camera.getPitch(), 1, 0, 0);
            Matrices.translate(0, 0.5, 0);
            Matrices.scale(-scale * waypoint.scale, -scale * waypoint.scale, scale);

            String distText = Math.round(dist) + " blocks";

            // Render background
            double i = Meteor.INSTANCE.getFont2x().getStringWidth(waypoint.name) / 2.0;
            double i2 = Meteor.INSTANCE.getFont2x().getStringWidth(distText) / 2.0;
            ShapeBuilder.begin(null, GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
            ShapeBuilder.quad(-i - 1, -1 - Meteor.INSTANCE.getFont2x().getHeight(), 0, -i - 1, 8 - Meteor.INSTANCE.getFont2x().getHeight(), 0, i + 1, 8 - Meteor.INSTANCE.getFont2x().getHeight(), 0, i + 1, -1 - Meteor.INSTANCE.getFont2x().getHeight(), 0, BACKGROUND);
            ShapeBuilder.quad(-i2 - 1, -1, 0, -i2 - 1, 8, 0, i2 + 1, 8, 0, i2 + 1, -1, 0, BACKGROUND);
            ShapeBuilder.end();

            waypoint.renderIcon(-8, 9, 0, a, 16);

            // Render name text
            Meteor.INSTANCE.getFont2x().begin();
            Meteor.INSTANCE.getFont2x().renderString(waypoint.name, -i, -Meteor.INSTANCE.getFont2x().getHeight(), TEXT);
            Meteor.INSTANCE.getFont2x().renderString(distText, -i2, 0, TEXT);
            Meteor.INSTANCE.getFont2x().end();

            Matrices.pop();

            BACKGROUND.a = preBgA;
            TEXT.a = preTextA;
        }
    });
    private List<Waypoint> waypoints = new ArrayList<>();
    @EventHandler private final Listener<GameDisconnectedEvent> onGameDisconnected = new Listener<>(event -> {
        save();
        waypoints.clear();
    });

    private Waypoints() {
        super(null);
        Meteor.INSTANCE.getEventBus().subscribe(this);
    }

    public static void loadIcons() {
        File iconsFolder = new File(new File(Meteor.INSTANCE.getFolder(), "waypoints"), "icons");
        iconsFolder.mkdirs();

        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + ".png");
            if (!iconFile.exists()) {
                copyIcon(iconFile);
            }
        }

        File[] files = iconsFolder.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".png")) {
                try {
                    String name = file.getName().replace(".png", "");
                    AbstractTexture texture = new NativeImageBackedTexture(NativeImage.read(new FileInputStream(file)));
                    ICONS.put(name, texture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void copyIcon(File file) {
        try {
            InputStream in = Waypoints.class.getResourceAsStream("/assets/meteor-client/waypoint-icons/" + file.getName());
            OutputStream out = new FileOutputStream(file);

            byte[] bytes = new byte[256];
            int read;
            while ((read = in.read(bytes)) > 0)
                out.write(bytes, 0, read);

            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(Waypoint waypoint) {
        waypoints.add(waypoint);
        Meteor.INSTANCE.getEventBus().post(EventStore.waypointListChangedEvent());
        save();
    }

    public void remove(Waypoint waypoint) {
        if (waypoints.remove(waypoint)) {
            Meteor.INSTANCE.getEventBus().post(EventStore.waypointListChangedEvent());
            save();
        }
    }

    private boolean checkDimension(Waypoint waypoint) {
        Dimension dimension = Utils.getDimension();

        if (waypoint.overworld && dimension == Dimension.Overworld) {
            return true;
        }
        if (waypoint.nether && dimension == Dimension.Nether) {
            return true;
        }
        return waypoint.end && dimension == Dimension.End;
    }

    @Override
    public File getFile() {
        return new File(new File(Meteor.INSTANCE.getFolder(), "waypoints"), Utils.getWorldName() + ".nbt");
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("waypoints", NbtUtils.listToTag(waypoints));
        return tag;
    }

    @Override
    public Waypoints fromTag(CompoundTag tag) {
        waypoints = NbtUtils.listFromTag(tag.getList("waypoints", 10), tag1 -> new Waypoint().fromTag((CompoundTag) tag1));

        return this;
    }

    @Override
    public Iterator<Waypoint> iterator() {
        return waypoints.iterator();
    }
}
