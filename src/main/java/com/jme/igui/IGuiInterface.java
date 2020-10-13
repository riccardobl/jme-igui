package com.jme.igui;

public interface IGuiInterface{
    /**
    * Clear the gui
    * @param destroyAllPersistent if true persistent components will be destroyed as well
    */
    public void clear(boolean destroyAllPersistent);

    /**
     * Destroy the gui and all the components
     */
    public void destroy();
}