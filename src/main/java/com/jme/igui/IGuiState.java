package com.jme.igui;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

public class IGuiState{
    BitmapFont textFont;
    boolean imageFlip=false;
    boolean imageAlpha=true;
    float textSize;
    boolean textRightToLeft=false;

    final Vector2f imageSize=new Vector2f(IGui.SIZE_AUTO,IGui.SIZE_AUTO);
    final ColorRGBA textColor=new ColorRGBA();
    final Vector2f imageScale=new Vector2f(1,1);

    String textHAlign="left";
    String textVAlign="top";
    String imageHAlign="left";
    String imageVAlign="top";

    float zIndex=0;

    void inheritFrom(IGuiState b){
        zIndex=b.zIndex;
        imageVAlign=b.imageVAlign;
        imageHAlign=b.imageHAlign;
        textVAlign=b.textVAlign;
        textHAlign=b.textHAlign;
        imageScale.set(b.imageScale);
        textColor.set(b.textColor);
        imageSize.set(b.imageSize);
        textRightToLeft=b.textRightToLeft;
        textSize=b.textSize;
        imageAlpha=b.imageAlpha;
        imageFlip=b.imageFlip;
        textFont=b.textFont;        
    }
}