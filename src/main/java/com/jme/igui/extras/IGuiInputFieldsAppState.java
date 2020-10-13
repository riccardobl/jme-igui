package com.jme.igui.extras;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.jme.igui.IGui;
import com.jme.igui.IGuiComponent;
import com.jme.igui.IGuiMouseEvent;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.renderer.RenderManager;

public class IGuiInputFieldsAppState extends BaseAppState implements IGuiInputFields,RawInputListener{

    public static IGuiInputFields newPlugin(IGui igui, InputManager inputManager, AppStateManager stateManager) {
        IGuiInputFieldsAppState ig=new IGuiInputFieldsAppState(igui,inputManager,stateManager);
        stateManager.attach(ig);
        return ig;
    }

    // public static IGui newAbsolute(AssetManager assetManager, AppStateManager sm,  Node root) {
    //     return newAbsolute(assetManager,sm,null,root);
    // }

    class InputFieldComponent{
        public Consumer<KeyInputEvent> onKey;
        public  IGuiComponent textComponent;
        public boolean mouseOver;
        public boolean persistent;
    }

    private IGui igui;
    private InputManager inputManager;
    private List<InputFieldComponent> components=new LinkedList<InputFieldComponent>();
    private List<KeyInputEvent> keyEventsQueue=new LinkedList<KeyInputEvent>();
    private AppStateManager stateManager;

    IGuiInputFieldsAppState(IGui igui,InputManager inputManager,AppStateManager stateManager){
        this.igui=igui;
        this.stateManager=stateManager;
        this.inputManager=inputManager;
    }

    
    @Override
    public IGuiInputFields input(String content, float posX, float posY, BiFunction<IGuiMouseEvent,Object,Boolean> onMouseEvent, Consumer<KeyInputEvent> onKey) {
        return input(content,posX,posY,onMouseEvent,onKey,false);
    }

    

    @Override
    public IGuiInputFields input(String content, float posX, float posY, Consumer<KeyInputEvent> onKey) {
        return input(content,posX,posY,null,onKey,false);
    }

    @Override
    public IGuiInputFields input(String content, float posX, float posY, Consumer<KeyInputEvent> onKey,boolean persistent) {
        return input(content,posX,posY,null,onKey,persistent);
    }


    @Override
    public IGuiInputFields input(String content, float posX, float posY, BiFunction<IGuiMouseEvent,Object,Boolean> onMouseEvent, Consumer<KeyInputEvent> onKey,boolean persistent) {
        InputFieldComponent icomponent=new InputFieldComponent();
        BiFunction<IGuiMouseEvent,Object,Boolean> onMouseEventWrapper=(ev, args) -> {
            if(ev == IGuiMouseEvent.MOUSE_IN){
                icomponent.mouseOver=true;
            }else if(ev == IGuiMouseEvent.MOUSE_OUT){
                icomponent.mouseOver=false;
            }
            return onMouseEvent==null?true:onMouseEvent.apply(ev,args);
        };
        igui.push(true);
        icomponent.textComponent=igui.text(content,posX,posY,onMouseEventWrapper);
        icomponent.onKey=onKey;
        icomponent.persistent=persistent;
        components.add(icomponent);
        igui.pop();
        return null;
    }

    @Override
    public void render(RenderManager rm){
        for(KeyInputEvent evt:keyEventsQueue){
            for(InputFieldComponent c:components){
                if(c.mouseOver)c.onKey.accept(evt);
            }
        }
        keyEventsQueue.clear();
    }


    @Override
    public void postRender() {
        clear(false);
    }

    @Override
    public void clear(boolean destroyAllPersistent) {
        Iterator<InputFieldComponent> entries_i=components.iterator();
        while(entries_i.hasNext()){
            InputFieldComponent t=entries_i.next();
            if(!t.persistent || destroyAllPersistent){
                entries_i.remove();
            }
        }
    }

    @Override
    public void destroy() {
        clear(true);
        stateManager.detach(this);
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        if(evt.isPressed())keyEventsQueue.add(evt);
    }

    @Override
    protected void cleanup(Application app) {
        inputManager.removeRawInputListener(this);
    }


    @Override
    protected void initialize(Application app) {
        this.inputManager.addRawInputListener(this);

    }


    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void beginInput() {

    }

    @Override
    public void endInput() {

    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {

    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {

    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {

    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {

    }

  

    @Override
    public void onTouchEvent(TouchEvent evt) {

    }
}