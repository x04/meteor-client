package minegame159.meteorclient.rendering;

import lombok.Getter;
import minegame159.meteorclient.events.RenderEvent;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;

@Getter
public enum Renderer {
    INSTANCE;

    private final MeshBuilder triangles = new MeshBuilder();
    private final MeshBuilder lines = new MeshBuilder();

    private boolean building;

    public void begin(RenderEvent event) {
        if (!building) {
            triangles.begin(event, GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR);
            lines.begin(event, GL11.GL_LINES, VertexFormats.POSITION_COLOR);

            building = true;
        }
    }

    public void end(boolean texture) {
        if (building) {
            triangles.end(texture);
            lines.end(false);

            building = false;
        }
    }

    public void end() {
        end(false);
    }
}
