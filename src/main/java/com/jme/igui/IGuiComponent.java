package com.jme.igui;

import com.jme3.scene.Spatial;

public class IGuiComponent{
    private IGui gui;
    Spatial sp;
    boolean persistent;

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