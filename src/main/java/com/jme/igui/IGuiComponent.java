package com.jme.igui;

import java.util.function.BiFunction;

import com.jme3.scene.Spatial;

public class IGuiComponent{
    private IGui gui;
    public  Spatial sp;
    public boolean persistent;
    public BiFunction<IGuiMouseEvent,Object,Boolean> action;

    IGuiComponent(IGui gui){
        this.gui=gui;
    }

    public IGui gui(){
        return gui;
    }

    public void destroy() {
        persistent=false;
    }

    public Spatial getSpatial() {
        return sp;
    }

}