package com.jme.igui;

import com.jme3.app.LegacyApplication;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.font.BitmapText;
import com.jme3.scene.Node;
import com.jme3.texture.plugins.AWTLoader;

/** CPU-only regressions; no display, mouse, keyboard, or OpenGL is required. */
public class IGuiHeadlessRegressionTest {
    public static void main(String[] args) {
        checkGui(800, 600, false);
        checkGui(600, 800, false);
        checkGui(0, 0, true);
        System.out.println("PASS: optional input lifecycle, relative conversions, and absolute sizing");
    }

    private static void checkGui(int width, int height, boolean absolute) {
        LegacyApplication app = new LegacyApplication();
        Node root = new Node();
        DesktopAssetManager assets = new DesktopAssetManager(true);
        assets.registerLoader(AWTLoader.class, "png");
        IGui gui = absolute
                ? IGuiAppState.newAbsolute(assets, app.getStateManager(), root)
                : IGuiAppState.newRelative(assets, app.getStateManager(), root, width, height);
        float scale = absolute ? 1f : Math.min(width, height);
        float defaultPixels = assets.loadFont("Interface/Fonts/Default.fnt")
                .getCharSet().getRenderedSize();
        assertClose(defaultPixels / scale, gui.getTextSize(), "default virtual font size");
        float requestedSize = absolute ? 24f : 0.1f;
        IGuiComponent text = gui.textSize(requestedSize).text("Audit", 0.5f, 0.5f, true);
        assertClose(requestedSize * scale,
                ((BitmapText) text.getSpatial()).getSize(), "rendered font size");
        if (root.getQuantity() != 1) {
            throw new AssertionError("Text must be attached");
        }
        app.getStateManager().update(0.016f);
        gui.destroy();
        app.getStateManager().update(0.016f);
        if (root.getQuantity() != 0) {
            throw new AssertionError("Destroyed text must be detached");
        }
    }

    private static void assertClose(float expected, float actual, String description) {
        if (Math.abs(expected - actual) > 0.001f) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
