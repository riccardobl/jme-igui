
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
    private final AssetManager assetManager;
    private final AppStateManager stateManager;
    private final Vector2f screenSize=new Vector2f(0,0);

    private final LinkedList<IGuiState> stateStack=new LinkedList<IGuiState>();
    private IGuiState currentState;


    @Override
    public IGui push(){
        return push(true);
    }

    @Override
    public IGui push(boolean inherit){
        IGuiState oldState=currentState;
        currentState= new IGuiState();
        textFont("Interface/Fonts/Default.fnt");
        textSize(currentState.textFont.getCharSet().getRenderedSize());
        textColor(ColorRGBA.Gray);

        if(inherit&&oldState!=null)currentState.inheritFrom(oldState);

        stateStack.addFirst(currentState);
        return this;
    }

    @Override
    public IGui pop(){
        if(stateStack.size()<=1)return this;
        stateStack.removeFirst();
        currentState=stateStack.getFirst();
        return this;
    }

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
        this.screenSize.set(w,h);
        push();
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
        this.currentState.textColor.set(color);
        return this;
    }

    @Override
    public ColorRGBA textColor() {
        return this.currentState.textColor;
    }


    @Override
    public IGui textRighToLeft(boolean v) {
        this.currentState.textRightToLeft=v;
        return this;
    }

    @Override
    public boolean textRightToLeft(){
        return this.currentState.textRightToLeft;
    }

    @Override
    public IGui textSize(float scale) {
        this.currentState.textSize=scale;
        return this;
    }

    @Override
    public float textSize() {
        return this.currentState.textSize;
    }

    @Override
    public IGui textFont(String path) {
        this.currentState.textFont=assetManager.loadFont(path);
        return this;
    }

    @Override
    public BitmapFont textFont() {
        return this.currentState.textFont;
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY) {
        return text(text,posX,posY,false);
    }

    @Override
    public IGui textHAlign(String align){
        this.currentState.textHAlign=align;
        return this;
    }

    @Override
    public IGui textVAlign(String align){
        this.currentState.textVAlign=align;
        return this;
    }

    @Override
    public IGui imageHAlign(String align){
        this.currentState.imageHAlign=align;
        return this;
    }

    @Override
    public IGui imageVAlign(String align){
        this.currentState.imageVAlign=align;
        return this;
    }


    @Override
    public String textHAlign(){
        return this.currentState.textHAlign;
    }

    @Override
    public String textVAlign(){
        return this.currentState.textVAlign;
    }


    @Override
    public String imageHAlign(){
        return this.currentState.imageHAlign;
    }

    @Override
    public String imageVAlign(){
        return this.currentState.imageVAlign;
    }

    @Override
    public IGuiComponent text(String text, float posX, float posY, boolean persistent) {
        BitmapText btext=new BitmapText(this.currentState.textFont,this.currentState.textRightToLeft);
        btext.setSize(toReal(this.currentState.textSize));
        btext.setText(text);
        

        float lw=btext.getLineWidth();
        float lh=btext.getLineHeight();
    
        float x=toReal(posX,true);
        float y=toReal(posY,false);

        switch(this.currentState.textVAlign){
            case "bottom":
                y+=lh;
                break;
            case "center":
            case "middle":
                y+=lh/2f;
        }

        switch(this.currentState.textHAlign){
            case "right":
                x-=lw;
                break;
            case "center":
            case "middle":
                x-=lw/2f;
        }


        btext.setLocalTranslation(x,y,this.currentState.zIndex);
        btext.setColor(this.currentState.textColor.clone());
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
        this.currentState.imageFlip=v;
        return this;
    }

    @Override
    public boolean imageFlip() {
        return this.currentState.imageFlip;
    }

    @Override
    public IGui imageAlpha(boolean v) {
        this.currentState.imageAlpha=v;
        return this;
    }

    @Override
    public boolean imageAlpha() {
        return this.currentState.imageAlpha;
    }

    @Override
    public IGui imageSize(String image) {
        TextureKey key=new TextureKey(image,true);
        Texture tx=assetManager.loadTexture(key);
        Image img=tx.getImage();
        this.currentState.imageSize.set(toVirtual(img.getWidth(),true),toVirtual(img.getHeight(),false));
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
    public Vector2f imageSize() {
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
    public Vector2f imageScale() {
        return this.currentState.imageScale;
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY) {
        return image(img,posX,posY,false);
    }

    @Override
    public IGuiComponent image(String img, float posX, float posY, boolean persistent) {
        TextureKey key=new TextureKey(img,true);
        Texture2D txx=(Texture2D)assetManager.loadTexture(key);

        float imgW=this.currentState.imageSize.x;
        float imgH=this.currentState.imageSize.y;
        imgW=toReal(imgW,true);
        imgH=toReal(imgH,false);
        
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

        imgW*= this.currentState.imageScale.x;
        imgH*= this.currentState.imageScale.y;

        float x=toReal(posX,true);
        float y=toReal(posY,false);


        switch(this.currentState.imageVAlign){
            case "top":
                y-=imgH;
                break;
            case "bottom":
                break;
            case "center":
            case "middle":
                y-=imgH/2f;
        }

        switch(this.currentState.imageHAlign){
            case "right":
                x-=imgW;
                break;
            case "center":
            case "middle":
                x-=imgW/2f;
        }

        Picture p=new Picture(img,this.currentState.imageFlip);
        p.setTexture(assetManager,txx,this.currentState.imageAlpha);
        p.getLocalTranslation().z=this.currentState.zIndex;
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

    public float zIndex(){
        return this.currentState.zIndex;
    }

    public IGui zIndex(float v){
        this.currentState.zIndex=v;
        return this;
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