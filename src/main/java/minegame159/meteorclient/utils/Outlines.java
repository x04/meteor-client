package minegame159.meteorclient.utils;

import minegame159.meteorclient.Meteor;
import minegame159.meteorclient.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class Outlines {
    public static boolean loadingOutlineShader;
    public static boolean renderingOutlines;

    public static Framebuffer outlinesFbo;
    public static OutlineVertexConsumerProvider vertexConsumerProvider;
    private static ShaderEffect outlinesShader;

    public static void load() {
        try {
            MinecraftClient mc = Meteor.INSTANCE.getMinecraft();

            if (outlinesShader != null) {
                outlinesShader.close();
            }

            loadingOutlineShader = true;
            outlinesShader = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new Identifier("meteor-client", "shaders/post/my_entity_outline.json"));
            outlinesShader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
            outlinesFbo = outlinesShader.getSecondaryTarget("final");
            vertexConsumerProvider = new OutlineVertexConsumerProvider(mc.getBufferBuilders().getEntityVertexConsumers());
            loadingOutlineShader = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void beginRender() {
        outlinesFbo.clear(MinecraftClient.IS_SYSTEM_MAC);
        Meteor.INSTANCE.getMinecraft().getFramebuffer().beginWrite(false);
    }

    public static void endRender(float tickDelta) {
        WorldRenderer worldRenderer = Meteor.INSTANCE.getMinecraft().worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor) worldRenderer;

        Framebuffer fbo = worldRenderer.getEntityOutlinesFramebuffer();
        wra.setEntityOutlinesFramebuffer(outlinesFbo);
        vertexConsumerProvider.draw();
        wra.setEntityOutlinesFramebuffer(fbo);

        outlinesShader.render(tickDelta);
        Meteor.INSTANCE.getMinecraft().getFramebuffer().beginWrite(false);
    }

    public static void renderFbo() {
        MinecraftClient mc = Meteor.INSTANCE.getMinecraft();

        outlinesFbo.draw(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), false);
    }

    public static void onResized(int width, int height) {
        if (outlinesShader != null) {
            outlinesShader.setupDimensions(width, height);
        }
    }
}
