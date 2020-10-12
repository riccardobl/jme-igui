
package com.jme.igui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * Simple minimalistic imediate GUI 
 */
public class IGuiAppState extends BaseAppState implements IGui{
    private final Collection<IGuiComponent> entries=new LinkedList<IGuiComponent>();

    private final Node root;
    private AssetManager assetManager;
    private BitmapFont textFont;
    private boolean imageFlip=false,imageAlpha=true;
    private float textSize;
    private boolean textRightToLeft=false;

    private final Vector2f imageSize=new Vector2f(SIZE_AUTO,SIZE_AUTO);
    private final ColorRGBA textColor=new ColorRGBA();
    private final Vector2f imageScale=new Vector2f(1,1);
    private final Vector2f screenSize=new Vector2f(0,0);

    private String textHAlign="left";
    private String textVAlign="top";
    private String imageHAlign="left";
    private String imageVAlign="top";
    private final AppStateManager stateManager;

    /**
     * Create a new gui that uses relative coordinates
     * All the positions and sizes used in this gui should be relative to the specified screenW and screenH
     * ie. [0;1] range
     */
    public static IGui newRelative(AssetManager assetManager, AppStateManager sm,Node root, int screenW, int screenH) {
        IGuiAppState ig= new IGuiAppState(assetManager,sm,root,screenW,screenH);
        sm.attach(ig);
        return ig;
    }

    /**
    * Create a new gui that uses absolute coordinates
    */
    public static IGui newAbsolute(AssetManager assetManager, AppStateManager sm,Node root) {
        IGuiAppState ig= new IGuiAppState(assetManager,sm,root,0,0);
        sm.attach(ig);
        return ig;
    }

 
    IGuiAppState(AssetManager assetManager,AppStateManager stateManager,Node root,int w,int h){
        this.assetManager=assetManager;
        this.root=root;
        this.stateManager=stateManager;
        textFont("Interface/Fonts/Default.fnt");
        textSize(textFont.getCharSet().getRenderedSize());
        textColor(ColorRGBA.Gray);
        screenSize.set(w,h);
    }

    @Override
    public void postRender() {
        clear(false);
    }

    @Override
    public void clear(boolean destroyAllPersistent) {
        Iterator<IGuiComponent> entries_i=entries.iterator();
        while(entries_i.hasNext()){
            IGuiComponent t=entries_i.next();
            if(!t.persistent || destroyAllPersistent){
                t.sp.removeFromParent();
                entries_i.remove();
            }
        }
    }

    @Override
    public void destroy(){
        clear(true);
        stateManager.detach(this);
    }

    private float toReal(float v) {
        if(v==SIZE_AUTO)return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float min=screenSize.x;
        if(screenSize.y < min) screenSize.y=screenSize.y;
        return v * min;
    }

    private float toReal(float v, boolean w) {
        if(v==SIZE_AUTO)return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float vv=w?screenSize.x:screenSize.y;
        return v * vv;
    }

    private float toVirtual(float v, boolean w) {
        if(v==SIZE_AUTO)return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float vv=w?screenSize.x:screenSize.y;
        return v / vv;
    }

    // Text
    @Override
    public IGui textColor(ColorRGBA color) {
        this.textColor.set(color);
        return this;
    }

    @Override
    public ColorRGBA textColor() {
        return this.textColor;
    }


    @Override
    public IGui textRighToLeft(boolean v) {
        this.textRightToLeft=v;
        return this;
    }

    @Override
    public boolean textRightToLeft(){
        return textRightToLeft;
    }

    @Override
    public IGui textSize(float scale) {
        textSize=scale;
        return this;
    }

    @Override
    public float textSize() {
        return textSize;
    }

    @Override
    public IGui textFont(String path) {
        textFont=assetManager.loadFont(path);
        return this;
    }

    @Override
    public BitmapFont textFont() {
        return textFont;
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY) {
        return text(text,posX,posY,false);
    }

    @Override
    public IGui textHAlign(String align){
        this.textHAlign=align;
        return this;
    }

    @Override
    public IGui textVAlign(String align){
        this.textVAlign=align;
        return this;
    }

    @Override
    public IGui imageHAlign(String align){
        this.imageHAlign=align;
        return this;
    }

    @Override
    public IGui imageVAlign(String align){
        this.imageVAlign=align;
        return this;
    }


    @Override
    public String textHAlign(){
        return this.textHAlign;
    }

    @Override
    public String textVAlign(){
        return this.textVAlign;
    }


    @Override
    public String imageHAlign(){
        return this.imageHAlign;
    }

    @Override
    public String imageVAlign(){
        return this.imageVAlign;
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY, boolean persistent) {
        BitmapText btext=new BitmapText(textFont,textRightToLeft);
        btext.setSize(toReal(textSize));
        btext.setText(text);

        float lw=btext.getLineWidth();
        float lh=btext.getLineHeight();
    
        float x=toReal(posX,true);
        float y=toReal(posY,false);

        switch(textVAlign){
            case "bottom":
                y+=lh;
                break;
            case "center":
            case "middle":
                y+=lh/2f;
        }

        switch(textHAlign){
            case "right":
                x-=lw;
                break;
            case "center":
            case "middle":
                x-=lw/2f;
        }


        btext.setLocalTranslation(x,y,0);
        btext.setColor(textColor.clone());
        root.attachChild(btext);
        IGuiComponent tx=new IGuiComponent(this);
        tx.sp=btext;
        tx.persistent=persistent;
        entries.add(tx);
        return tx;
    }

    // Image

    @Override
    public IGui imageFlip(boolean v) {
        this.imageFlip=v;
        return this;
    }

    @Override
    public boolean imageFlip() {
        return imageFlip;
    }

    @Override
    public IGui imageAlpha(boolean v) {
        this.imageAlpha=v;
        return this;
    }

    @Override
    public boolean imageAlpha() {
        return imageAlpha;
    }

    @Override
    public IGui imageSize(String image) {
        TextureKey key=new TextureKey(image,true);
        Texture tx=assetManager.loadTexture(key);
        Image img=tx.getImage();
        imageSize.set(toVirtual(img.getWidth(),true),toVirtual(img.getHeight(),false));
        return this;
    }

    @Override
    public IGui imageSize(float w, float h) {
        imageSize.set(w,h);
        return this;
    }

    @Override
    public IGui imageSize(Vector2f size) {
        imageSize.set(size);
        return this;
    }

    @Override
    public Vector2f imageSize() {
        return imageSize;
    }

    @Override
    public IGui imageScale(float s) {
        imageScale.set(s,s);
        return this;
    }

    @Override
    public IGui imageScale(float x, float y) {
        imageScale.set(x,y);
        return this;
    }

    @Override
    public IGui imageScale(Vector2f s) {
        imageScale.set(s);
        return this;
    }

    @Override
    public Vector2f imageScale() {
        return imageScale;
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY) {
        return image(img,posX,posY,false);
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY, boolean persistent) {
        TextureKey key=new TextureKey(img,true);
        Texture2D txx=(Texture2D)assetManager.loadTexture(key);

        float imgW=imageSize.x;
        float imgH=imageSize.y;
        imgW=toReal(imgW,true);
        imgH=toReal(imgH,false);
        
        if(imgW == SIZE_AUTO && imgH == SIZE_AUTO) imageSize.set(txx.getImage().getWidth(),txx.getImage().getHeight());
        else{
            if(imgW == SIZE_AUTO){
                float r=txx.getImage().getWidth() / txx.getImage().getHeight();
                imgW=imgH * r;
            }else if(imgH == SIZE_AUTO){
                float r=txx.getImage().getHeight() / txx.getImage().getWidth();
                imgH=imgW * r;
            }
        }

        imgW*= imageScale.x;
        imgH*= imageScale.y;

        float x=toReal(posX,true);
        float y=toReal(posY,false);


        switch(imageVAlign){
            case "top":
                y-=imgH;
                break;
            case "bottom":
                break;
            case "center":
            case "middle":
                y-=imgH/2f;
        }

        switch(imageHAlign){
            case "right":
                x-=imgW;
                break;
            case "center":
            case "middle":
                x-=imgW/2f;
        }

        Picture p=new Picture(img,imageFlip);
        p.setTexture(assetManager,txx,imageAlpha);
        p.setPosition(x,y);
        p.setWidth(imgW );
        p.setHeight(imgH);
        root.attachChild(p);

        IGuiComponent tx=new IGuiComponent(this);
        tx.sp=p;
        tx.persistent=persistent;
        entries.add(tx);
        return tx;
    }

    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

}