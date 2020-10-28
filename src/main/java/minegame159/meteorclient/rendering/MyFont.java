package minegame159.meteorclient.rendering;

import minegame159.meteorclient.utils.ByteTexture;
import minegame159.meteorclient.utils.Color;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MyFont {
    public final AbstractTexture texture;
    private final MeshBuilder mb = new MeshBuilder(16384);
    private final int height;
    private final float scale;
    private final float ascent;
    private final CharData[] charData;

    public MyFont(File file, int height) {
        this.height = height;

        // Read file
        byte[] bytes = Utils.readBytes(file);
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes);
        buffer.flip();

        // Initialize font
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(fontInfo, buffer);

        // Allocate STBTTPackedchar buffer
        charData = new CharData[128];
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(charData.length);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(512 * 512);

        // Create font texture
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin(packContext, bitmap, 512, 512, 0, 1);
        STBTruetype.stbtt_PackSetOversampling(packContext, 2, 2);
        STBTruetype.stbtt_PackFontRange(packContext, buffer, 0, height, 32, cdata);
        STBTruetype.stbtt_PackEnd(packContext);

        // Create texture object and get font scale
        texture = new ByteTexture(512, 512, bitmap, true);
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height);

        // Get font vertical ascent
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascent = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, null, null);
            this.ascent = ascent.get(0);
        }

        // Populate charData array
        for (int i = 0; i < charData.length; i++) {
            STBTTPackedchar packedChar = cdata.get(i);

            float ipw = 1f / 512;
            float iph = 1f / 512;

            charData[i] = new CharData(packedChar.xoff(), packedChar.yoff(), packedChar.xoff2(), packedChar.yoff2(), packedChar.x0() * ipw, packedChar.y0() * iph, packedChar.x1() * ipw, packedChar.y1() * iph, packedChar.xadvance());
        }
    }

    public double getWidth(String string, int length) {
        double width = 0;

        for (int i = 0; i < length; i++) {
            int cp = string.charAt(i);
            if (cp < 32 || cp > 128) {
                cp = 32;
            }
            CharData c = charData[cp - 32];

            width += c.xAdvance;
        }

        return width;
    }

    public double getWidth(String string) {
        return getWidth(string, string.length());
    }

    public int getHeight() {
        return height;
    }

    public void begin() {
        mb.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE);
    }

    public boolean isBuilding() {
        return mb.isBuilding();
    }

    public void end() {
        texture.bindTexture();
        mb.end(true);
    }

    public void render(String string, double x, double y, Color color) {
        boolean wasBuilding = isBuilding();
        if (!isBuilding()) {
            begin();
        }

        y += ascent * scale;

        for (int i = 0; i < string.length(); i++) {
            int cp = string.charAt(i);
            if (cp < 32 || cp > 128) {
                cp = 32;
            }
            CharData c = charData[cp - 32];

            mb.pos(x + c.x0, y + c.y0, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1, y + c.y0, 0).color(color).texture(c.u1, c.v0).endVertex();
            mb.pos(x + c.x1, y + c.y1, 0).color(color).texture(c.u1, c.v1).endVertex();

            mb.pos(x + c.x0, y + c.y0, 0).color(color).texture(c.u0, c.v0).endVertex();
            mb.pos(x + c.x1, y + c.y1, 0).color(color).texture(c.u1, c.v1).endVertex();
            mb.pos(x + c.x0, y + c.y1, 0).color(color).texture(c.u0, c.v1).endVertex();

            x += c.xAdvance;
        }

        if (!wasBuilding) {
            end();
        }
    }

    private static class CharData {
        public final float x0, y0, x1, y1;
        public final float u0, v0, u1, v1;
        public final float xAdvance;

        public CharData(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float xAdvance) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.xAdvance = xAdvance;
        }
    }
}
