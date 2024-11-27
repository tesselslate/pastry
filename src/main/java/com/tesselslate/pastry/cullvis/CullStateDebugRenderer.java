package com.tesselslate.pastry.cullvis;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

public class CullStateDebugRenderer implements DebugRenderer.Renderer {
    private static final int FLOW_LINE_LENGTH = 4;

    private static final int FLOW_LINE_OFFSET = 6;

    private BufferBuilder bufferBuilder;

    private CullState state;

    private Vec3d cameraPos;

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            double cameraX,
            double cameraY,
            double cameraZ) {
        assert this.state != null;

        this.cameraPos = new Vec3d(cameraX, cameraY, cameraZ);
        this.bufferBuilder = Tessellator.getInstance().getBuffer();

        this.drawLines();
        this.drawText();
    }

    public void setState(CullState state) {
        this.state = state;
    }

    private void buildLine(double ax, double ay, double az, double bx, double by, double bz, Vector3f color) {
        Vec3d c = this.cameraPos;

        this.bufferBuilder
                .vertex(ax - c.x, ay - c.y, az - c.z)
                .color(color.getX(), color.getY(), color.getZ(), 1.0f)
                .next();
        this.bufferBuilder
                .vertex(bx - c.x, by - c.y, bz - c.z)
                .color(color.getX(), color.getY(), color.getZ(), 1.0f)
                .next();
    }

    private void buildLine(Vector3f a, Vector3f b, Vector3f aColor, Vector3f bColor) {
        Vec3d c = this.cameraPos;

        this.bufferBuilder
                .vertex(a.getX() - c.x, a.getY() - c.y, a.getZ() - c.z)
                .color(aColor.getX(), aColor.getY(), aColor.getZ(), 1.0f)
                .next();
        this.bufferBuilder
                .vertex(b.getX() - c.x, b.getY() - c.y, b.getZ() - c.z)
                .color(bColor.getX(), bColor.getY(), bColor.getZ(), 1.0f)
                .next();
    }

    private void buildVisibleSubchunkFlow() {
        Vector3f innerColor = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f outerColor = new Vector3f(0.0f, 0.0f, 1.0f);

        this.state.data.forEach((pos, subchunk) -> {
            int cx = pos.getX() * 16 + 8;
            int cy = pos.getY() * 16 + 8;
            int cz = pos.getZ() * 16 + 8;

            for (int i = 0; i < subchunk.flowDirections.length; i++) {
                if (!subchunk.flowDirections[i]) {
                    continue;
                }

                Vector3f inner = Direction.byId(i).getUnitVector();
                inner.scale(FLOW_LINE_OFFSET);
                inner.add(cx, cy, cz);

                Vector3f outer = Direction.byId(i).getUnitVector();
                outer.scale(FLOW_LINE_LENGTH);
                outer.add(inner);

                this.buildLine(inner, outer, innerColor, outerColor);
            }
        });
    }

    private void buildVisibleSubchunkOutlines() {
        Vector3f color = new Vector3f(1.0f, 0.0f, 1.0f);

        for (Vec3i pos : this.state.visible) {
            int ax = pos.getX() * 16;
            int ay = pos.getY() * 16;
            int az = pos.getZ() * 16;

            int bx = ax + 16;
            int by = ay + 16;
            int bz = az + 16;

            // X facing lines
            this.buildLine(ax, ay, az, bx, ay, az, color);
            this.buildLine(ax, by, az, bx, by, az, color);
            this.buildLine(ax, ay, bz, bx, ay, bz, color);
            this.buildLine(ax, by, bz, bx, by, bz, color);

            // Y facing lines
            this.buildLine(ax, ay, az, ax, by, az, color);
            this.buildLine(bx, ay, az, bx, by, az, color);
            this.buildLine(ax, ay, bz, ax, by, bz, color);
            this.buildLine(bx, ay, bz, bx, by, bz, color);

            // Z facing lines
            this.buildLine(ax, ay, az, ax, ay, bz, color);
            this.buildLine(bx, ay, az, bx, ay, bz, color);
            this.buildLine(ax, by, az, ax, by, bz, color);
            this.buildLine(bx, by, az, bx, by, bz, color);
        }
    }

    @SuppressWarnings("deprecation")
    private void drawLines() {
        RenderSystem.enableDepthTest();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        RenderSystem.lineWidth(3.0f);
        this.bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        this.buildVisibleSubchunkOutlines();
        Tessellator.getInstance().draw();

        RenderSystem.lineWidth(2.0f);
        this.bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        this.buildVisibleSubchunkFlow();
        Tessellator.getInstance().draw();

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.lineWidth(1.0f);
    }

    private void drawText() {
        this.state.data.forEach((pos, subchunk) -> {
            int cx = pos.getX() * 16 + 8;
            int cy = pos.getY() * 16 + 8;
            int cz = pos.getZ() * 16 + 8;

            StringBuilder cullingState = new StringBuilder("Blocked: ");
            for (Direction dir : Direction.values()) {
                if ((subchunk.cullingState & (1 << dir.getId())) != 0) {
                    cullingState.append(Character.toUpperCase(dir.getName().charAt(0)));
                }
            }

            DebugRenderer.drawString(cullingState.toString(), cx, cy, cz, -1, 0.05f, true, 0, false);
        });
    }
}
