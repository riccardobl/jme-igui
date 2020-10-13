package com.jme.igui.extras;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.jme.igui.IGuiInterface;
import com.jme.igui.IGuiMouseEvent;
import com.jme3.input.event.KeyInputEvent;

public interface IGuiInputFields extends IGuiInterface{
    public IGuiInputFields input(String content,float posX,float posY,BiFunction<IGuiMouseEvent,Object,Boolean> onMouseEvent, Consumer<KeyInputEvent> onKey,boolean persistent);
    public IGuiInputFields input(String content,float posX,float posY,BiFunction<IGuiMouseEvent,Object,Boolean> onMouseEvent, Consumer<KeyInputEvent> onKey);
    public IGuiInputFields input(String content,float posX,float posY, Consumer<KeyInputEvent> onKey,boolean persistent);
    public IGuiInputFields input(String content,float posX,float posY, Consumer<KeyInputEvent> onKey);
}