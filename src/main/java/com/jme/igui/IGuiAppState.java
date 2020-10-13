
package com.jme.igui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple minimalistic imediate GUI 
 */
public class IGuiAppState extends BaseAppState implements IGui,ActionListener{
    private final Collection<IGuiComponent> entries=new LinkedList<IGuiComponent>();

    private final Node root;
    private final AssetManager assetManager;
    private final AppStateManager stateManager;
    private final Vector2f screenSize=new Vector2f(0,0);

    private final LinkedList<IGuiState> stateStack=new LinkedList<IGuiState>();
    private IGuiState currentState;

    private final List<IGuiComponent> componentsUnderMouse=new LinkedList<IGuiComponent>();
    private final List<IGuiMouseEvent> mouseEventsQueue=new LinkedList<IGuiMouseEvent>();

    private final String mouseMapping[]={"_igui_MouseLeft","_igui_MouseRight"};

    private final TTFConverter ttfConverter=new TTFConverter();


    public static IGui newRelative(AssetManager assetManager, AppStateManager sm, Node root, int screenW, int screenH) {
        return newRelative(assetManager,sm,null,root,screenW,screenH);
    }

    /**
     * Create a new gui that uses relative coordinates
     * All the positions and sizes used in this gui should be relative to the specified screenW and screenH
     * ie. [0;1] range
     */
    public static IGui newRelative(AssetManager assetManager, AppStateManager sm, InputManager inputManager, Node root, int screenW, int screenH) {
        IGuiAppState ig=new IGuiAppState(assetManager,sm,inputManager,root,screenW,screenH);
        sm.attach(ig);
        return ig;
    }

    public static IGui newAbsolute(AssetManager assetManager, AppStateManager sm, Node root) {
        return newAbsolute(assetManager,sm,null,root);
    }

    /**
    * Create a new gui that uses absolute coordinates
    */
    public static IGui newAbsolute(AssetManager assetManager, AppStateManager sm, InputManager inputManager, Node root) {
        IGuiAppState ig=new IGuiAppState(assetManager,sm,inputManager,root,0,0);
        sm.attach(ig);
        return ig;
    }

    private final InputManager inputManager;

    IGuiAppState(AssetManager assetManager,AppStateManager stateManager,InputManager inputManager,Node root,int w,int h){
        this.assetManager=assetManager;
        this.root=root;
        this.stateManager=stateManager;
        this.screenSize.set(w,h);
        this.inputManager=inputManager;

        push();
    }

    @Override
    public IGui push() {
        return push(true);
    }

    @Override
    public IGui push(boolean inherit) {
        IGuiState oldState=currentState;
        currentState=new IGuiState();
        textFont("Interface/Fonts/Default.fnt");
        textSize(toVirtualSize(currentState.textFont.getCharSet().getRenderedSize()));
        textColor(ColorRGBA.Gray);

        if(inherit && oldState != null) currentState.inheritFrom(oldState);

        stateStack.addFirst(currentState);
        return this;
    }

    @Override
    public IGui pop() {
        if(stateStack.size() <= 1) return this;
        stateStack.removeFirst();
        currentState=stateStack.getFirst();
        return this;
    }

    @Override
    protected void initialize(Application app) {
        this.inputManager.addMapping(this.mouseMapping[0],new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        this.inputManager.addMapping(this.mouseMapping[1],new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        this.inputManager.addListener(this,mouseMapping);
    }

    @Override
    protected void cleanup(Application app) {
        for(String m:this.mouseMapping)
            this.inputManager.deleteMapping(m);
        this.inputManager.removeListener(this);
    }

    @Override
    public void render(RenderManager rm) {
        if(inputManager == null) return;

        Vector2f mousePos=inputManager.getCursorPosition();
        float x=mousePos.x;
        float y=mousePos.y;

        for(IGuiComponent c:this.entries){
            if(c.action == null) continue;
            Spatial sp=c.getSpatial();
            BoundingBox spbox=(BoundingBox)sp.getWorldBound();
            float xex=spbox.getXExtent();
            float yex=spbox.getYExtent();
            if(sp instanceof BitmapText){
                yex=((BitmapText)sp).getLineHeight() / 2f;
            }
            float bx=spbox.getCenter().x - xex;
            float by=spbox.getCenter().y - yex;
            float bx2=spbox.getCenter().x + xex;
            float by2=spbox.getCenter().y + yex;
            if(x > bx && x < bx2 && y > by && y < by2) componentsUnderMouse.add(c);
            else c.action.apply(IGuiMouseEvent.MOUSE_OUT,null);
        }

        componentsUnderMouse.sort((a, b) -> {
            float z1=a.sp.getWorldTranslation().z;
            float z2=b.sp.getWorldTranslation().z;
            if(z1 > z2) return -1;
            if(z2 > z1) return 1;
            return 0;
        });

        for(IGuiComponent c:componentsUnderMouse){
            boolean consumed=c.action.apply(IGuiMouseEvent.MOUSE_IN,null);
            if(consumed) break;
        }

        for(IGuiMouseEvent e:this.mouseEventsQueue){
            if(e == IGuiMouseEvent.MOUSE_RELEASED_LEFT || e == IGuiMouseEvent.MOUSE_RELEASED_RIGHT){
                for(IGuiComponent c:entries){
                    if(c.action != null) c.action.apply(e,null);
                }
            }else{
                for(IGuiComponent c:componentsUnderMouse){
                    boolean consumed=c.action.apply(e,null);
                    if(consumed) break;
                }
            }
        }

        this.componentsUnderMouse.clear();
        this.mouseEventsQueue.clear();

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
        if(destroyAllPersistent){
            ttfConverter.clearCache();
        }
    }

    @Override
    public void destroy() {
        clear(true);
        stateManager.detach(this);
    }

    private float toRealSize(float v) {
        if(v == SIZE_AUTO) return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float min=screenSize.x;
        if(screenSize.y < min) screenSize.y=screenSize.y;
        return v * min;
    }

    private float toRealSize(float v, boolean w) {
        if(v == SIZE_AUTO) return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float vv=w?screenSize.x:screenSize.y;
        return v * vv;
    }

    private float toVirtualSize(float v, boolean w) {
        if(v == SIZE_AUTO) return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float vv=w?screenSize.x:screenSize.y;
        return v / vv;
    }

    private float toVirtualSize(float v) {
        if(v == SIZE_AUTO) return v;
        if(screenSize.x == 0 && screenSize.y == 0) return v;
        float min=screenSize.x;
        if(screenSize.y < min) screenSize.y=screenSize.y;
        return v / min;
    }

    // Text
    @Override
    public IGui textColor(ColorRGBA color) {
        this.currentState.textColor.set(color);
        return this;
    }

    @Override
    public ColorRGBA getTextColor() {
        return this.currentState.textColor;
    }

    @Override
    public IGui textRighToLeft(boolean v) {
        this.currentState.textRightToLeft=v;
        return this;
    }

    @Override
    public boolean isTextRightToLeft() {
        return this.currentState.textRightToLeft;
    }

    @Override
    public IGui textSize(float scale) {
        this.currentState.textSize=scale;
        return this;
    }

    @Override
    public float getTextSize() {
        return this.currentState.textSize;
    }

    @Override
    public IGui textFontStyle(String v) {
        this.currentState.fontStyle=v;
        return this;
    }

    @Override
    public IGui textFont(String path) {
        if(path.endsWith(".ttf")){ 
            this.currentState.textFont=null;
            this.currentState.textTTFFont=path;            
        }else{
            this.currentState.textFont=assetManager.loadFont(path);
            this.currentState.textTTFFont=null;
        }
        return this;
    }

    @Override
    public Object getTextFont() {
        if(this.currentState.textFont!=null)return this.currentState.textFont;
        else{
            float realFontSize=toRealSize(this.currentState.textSize);
            String fontPath=this.currentState.textTTFFont;
            return this.ttfConverter.convertTTF(assetManager, fontPath, this.currentState.fontStyle, realFontSize);
        }
    }


    @Override
    public IGui textHAlign(String align) {
        this.currentState.textHAlign=align;
        return this;
    }

    @Override
    public IGui textVAlign(String align) {
        this.currentState.textVAlign=align;
        return this;
    }

    @Override
    public IGui imageHAlign(String align) {
        this.currentState.imageHAlign=align;
        return this;
    }

    @Override
    public IGui imageVAlign(String align) {
        this.currentState.imageVAlign=align;
        return this;
    }

    @Override
    public String getTextHAlign() {
        return this.currentState.textHAlign;
    }

    @Override
    public String getTextVAlign() {
        return this.currentState.textVAlign;
    }

    @Override
    public String getImageHAlign() {
        return this.currentState.imageHAlign;
    }

    @Override
    public String getImageVAlign() {
        return this.currentState.imageVAlign;
    }


    @Override
    public IGuiComponent text(String text, float posX, float posY) {
        return text(text,posX,posY,false,null);
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY,BiFunction<IGuiMouseEvent,Object,Boolean> onClick) {
        return text(text,posX,posY,false,onClick);
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY, boolean persistent) {
        return text(text,posX,posY,persistent,null);
    }

    public BitmapText getBitmapText(String text) {
        BitmapFont font=this.currentState.textFont;
        if(font == null){
            float realFontSize=toRealSize(this.currentState.textSize);
            String fontPath=this.currentState.textTTFFont;
            font=ttfConverter.convertTTF(assetManager,fontPath,this.currentState.fontStyle,realFontSize);
        }         

        BitmapText fake=new BitmapText(font,this.currentState.textRightToLeft);
        fake.setText("A"); 
        BitmapText btext=new BitmapText(font,this.currentState.textRightToLeft){
            @Override
            public float getLineHeight() {
               float v=super.getLineHeight();
               if(fake.getLineHeight()>v)v=fake.getLineHeight(); /**Minimum height = the height of A */
               return v;
            }
        };
        btext.setSize(toRealSize(this.currentState.textSize));
        btext.setText(text);
        return btext;
    }
    
    @Override
    public IGuiComponent text(String text, float posX, float posY, boolean persistent,BiFunction<IGuiMouseEvent,Object,Boolean> onClick) {
        BitmapText btext=getBitmapText(text);


        float lw=btext.getLineWidth();
        float lh=btext.getLineHeight();

        float x=toRealSize(posX,true);
        float y=toRealSize(posY,false);

        switch(this.currentState.textVAlign){
            case "bottom":
                y+=lh;
                break;
            case "center":
            case "middle":
                y+=lh / 2f;
        }

        switch(this.currentState.textHAlign){
            case "right":
                x-=lw;
                break;
            case "center":
            case "middle":
                x-=lw / 2f;
        }

        btext.setLocalTranslation(x,y,this.currentState.zIndex);
        btext.setColor(this.currentState.textColor.clone());
        root.attachChild(btext);
        IGuiComponent tx=new IGuiComponent(this);
        tx.sp=btext;
        tx.action=onClick;
        tx.persistent=persistent;
        entries.add(tx);
        return tx;
    }

    // Image

    @Override
    public IGui imageFlip(boolean v) {
        this.currentState.imageFlip=v;
        return this;
    }

    @Override
    public boolean getImageFlip() {
        return this.currentState.imageFlip;
    }

    @Override
    public IGui imageAlpha(boolean v) {
        this.currentState.imageAlpha=v;
        return this;
    }

    @Override
    public boolean getImageAlpha() {
        return this.currentState.imageAlpha;
    }

    @Override
    public IGui imageSize(String image) {
        TextureKey key=new TextureKey(image,true);
        Texture tx=assetManager.loadTexture(key);
        Image img=tx.getImage();
        this.currentState.imageSize.set(toVirtualSize(img.getWidth(),true),toVirtualSize(img.getHeight(),false));
        return this;
    }

    @Override
    public IGui imageSize(float w, float h) {
        this.currentState.imageSize.set(w,h);
        return this;
    }

    @Override
    public IGui imageSize(Vector2f size) {
        this.currentState.imageSize.set(size);
        return this;
    }

    @Override
    public Vector2f getImageSize() {
        return this.currentState.imageSize;
    }

    @Override
    public IGui imageScale(float s) {
        this.currentState.imageScale.set(s,s);
        return this;
    }

    @Override
    public IGui imageScale(float x, float y) {
        this.currentState.imageScale.set(x,y);
        return this;
    }

    @Override
    public IGui imageScale(Vector2f s) {
        this.currentState.imageScale.set(s);
        return this;
    }

    @Override
    public Vector2f getImageScale() {
        return this.currentState.imageScale;
    }

    @Override
    public IGui imageColor(ColorRGBA color){
        this.currentState.imageColor.set(color);
        return this;
    }

    @Override
    public ColorRGBA getImageColor(){
        return this.currentState.imageColor;
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY) {
        return image(img,posX,posY,false,null);
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY,BiFunction<IGuiMouseEvent,Object,Boolean> onClick) {
        return image(img,posX,posY,false,onClick);
    }


    
    @Override
    public IGuiComponent image(String img, float posX, float posY, boolean persistent) {
        return  image( img,  posX,  posY,  persistent,null);
    }
    
    @Override
    public IGuiComponent image(String img, float posX, float posY, boolean persistent,BiFunction<IGuiMouseEvent,Object,Boolean> onClick) {
        TextureKey key=new TextureKey(img,true);
        Texture2D txx=(Texture2D)assetManager.loadTexture(key);

        float imgW=this.currentState.imageSize.x;
        float imgH=this.currentState.imageSize.y;
        imgW=toRealSize(imgW,true);
        imgH=toRealSize(imgH,false);

        if(imgW == SIZE_AUTO && imgH == SIZE_AUTO) this.currentState.imageSize.set(txx.getImage().getWidth(),txx.getImage().getHeight());
        else{
            if(imgW == SIZE_AUTO){
                float r=(float)txx.getImage().getWidth() / txx.getImage().getHeight();
                imgW=imgH * r;
            }else if(imgH == SIZE_AUTO){
                float r=(float)txx.getImage().getHeight() / txx.getImage().getWidth();
                imgH=imgW * r;
            }
        }

        imgW*=this.currentState.imageScale.x;
        imgH*=this.currentState.imageScale.y;

        float x=toRealSize(posX,true);
        float y=toRealSize(posY,false);

        switch(this.currentState.imageVAlign){
            case "top":
                y-=imgH;
                break;
            case "bottom":
                break;
            case "center":
            case "middle":
                y-=imgH / 2f;
        }

        switch(this.currentState.imageHAlign){
            case "right":
                x-=imgW;
                break;
            case "center":
            case "middle":
                x-=imgW / 2f;
        }

        Picture p=new Picture(img,this.currentState.imageFlip);
        p.setTexture(assetManager,txx,this.currentState.imageAlpha);
        p.getLocalTranslation().z=this.currentState.zIndex;
        p.setPosition(x,y);
        p.setWidth(imgW);
        p.setHeight(imgH);
        p.getMaterial().setColor("Color", this.currentState.imageColor);
        root.attachChild(p);

        IGuiComponent tx=new IGuiComponent(this);
        tx.sp=p;
        tx.action=onClick;
        tx.persistent=persistent;
        entries.add(tx);
        return tx;
    }

    @Override
    public float getZIndex() {
        return this.currentState.zIndex;
    }

    @Override
    public IGui zIndex(float v) {
        this.currentState.zIndex=v;
        return this;
    }


    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals(this.mouseMapping[0])){
            IGuiMouseEvent event=isPressed?IGuiMouseEvent.MOUSE_PRESSED_LEFT:IGuiMouseEvent.MOUSE_RELEASED_LEFT;
            mouseEventsQueue.add(event);
        }else if(name.equals(this.mouseMapping[1])){
            IGuiMouseEvent event=isPressed?IGuiMouseEvent.MOUSE_PRESSED_RIGHT:IGuiMouseEvent.MOUSE_RELEASED_RIGHT;
            mouseEventsQueue.add(event);
        }
    }

    @Override
    public float getTextLineWidth(String text) {
        return toVirtualSize(getBitmapText(text).getLineWidth(),true);
    }

    @Override
    public float getTextLineHeight(String text) {
        return toVirtualSize(getBitmapText(text).getLineHeight(),false);
    }





}