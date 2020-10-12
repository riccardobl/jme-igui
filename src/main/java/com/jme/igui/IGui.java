package com.jme.igui;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

public interface IGui{
    public static final float SIZE_AUTO=Float.MIN_VALUE;

    /**
     * Set color for subsequential calls to text()
     * @param color The color of the text
     */
    public IGui textColor(ColorRGBA color);

    /**
    * Set size for subsequential calls to text()
    * @param scale the size of the text
    */
    public IGui textSize(float size);

    /**
    * Set font for subsequential calls to text()
    * @param path The asset path of the font
    */
    public IGui textFont(String path);

    /**
     * Draw text on screen. The text will be drawn only for 1 frame and then destroyed.
     * @param text The string to draw
     * @param posX Y position on screen
     * @param posY Y position on screen
     */
    public IGuiComponent text(String text, float posX, float posY);

    /**
    * Draw text on screen 
    * @param text The string to draw
    * @param posX Y position on screen
    * @param posY Y position on screen
    * @param persistent If true the text will be drawn for every frame until .destroy() is called on the IGuiComponent instance.
    */
    public IGuiComponent text(String text, float posX, float posY, boolean persistent);

    /**
    * Set if subsequential calls of image() should draw a flipped image
    * @param v 
    */
    public IGui imageFlip(boolean v);

    /**
    * Set if subsequential calls of image() should draw an image with an alpha channel
    * @param v 
    */
    public IGui imageAlpha(boolean v);

    /**
    * Draw an image for 1 frame
    * @param img The asset path of the image
    * @param posX X position on the screen
    * @param posY Y position on the screen
    */
    public IGuiComponent image(String img, float posX, float posY);

    /**
    * Draw an image
    * @param img The asset path of the image
    * @param posX X position on the screen
    * @param posY Y position on the screen
    * @param persistent If true the image will be drawn for every frame until .destroy() is called on the IGuiComponent instance.
    */
    public IGuiComponent image(String img, float posX, float posY, boolean persistent);

    /**
     * Clear the gui
     * @param destroyAllPersistent if true persistent components will be destroyed as well
     */
    public void clear(boolean destroyAllPersistent);

    /**
     * Get current specified text color
     */
    public ColorRGBA textColor();

    /**
     * Get current specified text size
     */
    public float textSize();

    /**
     * Get current specified font
     */
    public BitmapFont textFont();

    
    public IGui textRighToLeft(boolean v);

    public boolean textRightToLeft();


    public IGui textHAlign(String align);
    public IGui textVAlign(String align);
    
    public String textHAlign();
    public String textVAlign();

    public IGui imageHAlign(String align);
    public IGui imageVAlign(String align);

    public String imageVAlign();
    public String imageHAlign();


    

    /**
     * Get current specified image flip status
     */
    public boolean imageFlip();

    /**
     * Get current specified image alpha status
     */
    public boolean imageAlpha();

    /**
     * Set an imageSize equivaled to the native size of the specified image for subsequential calls to image()
     * @param image The image
     */
    public IGui imageSize(String image);

    /**
     * Specify an image size for subsequential calls of image()
     * @param x the width or IGui.SIZE_AUTO
     * @param y the height or IGui.SIZE_AUTO
     */
    public IGui imageSize(float w, float h);

    /**
    * Specify an image size for subsequential calls to image()
    * @param size 
    */
    public IGui imageSize(Vector2f size);

    /**
     * Get current specified image size
     */
    public Vector2f imageSize();

    /**
     * Scale images drawn with subsequential calls to image()
     */
    public IGui imageScale(float s);

    /**
     * Scale images drawn with subsequential calls to image()
     */
    public IGui imageScale(float x, float y);

    /**
    * Scale images drawn with subsequential calls to image()
    */
    public IGui imageScale(Vector2f s);

    /**
     * Get current specified image scale
     */
    public Vector2f imageScale();

    /**
     * Destroy the gui and all the components
     */
    public void destroy();

   

}