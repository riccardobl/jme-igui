

import com.jme3.app.SimpleApplication;
import com.jme.igui.IGui;
import com.jme.igui.IGuiAppState;
import com.jme.igui.IGuiComponent;
import com.jme.igui.IGuiMouseEvent;
import com.jme3.math.ColorRGBA;

public class IGuiTest extends SimpleApplication{
    private IGui gui;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        gui=IGuiAppState.newRelative(assetManager,stateManager,inputManager,guiNode, cam.getWidth(),cam.getHeight());
        // gui.destroy();
        gui.textFont("igui/vera/VeraBd.ttf");
        gui.textFontStyle("bold");
        gui.textSize(0.05f).textColor(ColorRGBA.Red).textHAlign("center").textVAlign("top");
        IGuiComponent text=gui.text("Test IGui",.5f,1f,true); // persistent. stays for ever
        // text.destroy();   
    }

    boolean mouseHover=false;
    boolean mousePressedL=false;
    boolean mousePressedR=false;
    
    @Override
    public void simpleUpdate(float tpf){
        gui.push(false);
        gui.textFont("igui/vera/Vera.ttf");

        gui.textSize(0.02f);
        gui.textColor(mouseHover?ColorRGBA.Red:ColorRGBA.White);
        gui.textHAlign("left");
        gui.textVAlign("top");


        float spacing=0.03f;
        float line=1;
        gui.text("Line1",0f,line,(event,arg)->{
            if(event==IGuiMouseEvent.MOUSE_IN){
                mouseHover=true;
            }else if(event==IGuiMouseEvent.MOUSE_OUT){
                mouseHover=false;
            }
            return true;
        });

        gui.textColor(mousePressedL?ColorRGBA.Green:ColorRGBA.White);
        gui.text("Line2",0f,line-=spacing,(event,arg)->{
            if(event==IGuiMouseEvent.MOUSE_PRESSED_LEFT){
                mousePressedL=true;
            }else if(event==IGuiMouseEvent.MOUSE_RELEASED_LEFT){
                mousePressedL=false;
            }
            return true;
        });

        gui.textColor(mousePressedR?ColorRGBA.Green:ColorRGBA.White);
        gui.text("Line3",0f,line-=spacing,(event,arg)->{
            if(event==IGuiMouseEvent.MOUSE_PRESSED_RIGHT){
                mousePressedR=true;
            }else if(event==IGuiMouseEvent.MOUSE_RELEASED_RIGHT){
                mousePressedR=false;
            }
            return true;
        });


        gui.textColor(ColorRGBA.White);
        float fps=timer.getFrameRate();
        gui.textColor(fps>=59?ColorRGBA.Green:ColorRGBA.Red);
        gui.textHAlign("right");
        gui.text("FPS: "+((int)fps),1f,1f);
        gui.imageSize(0.3f,IGui.SIZE_AUTO);
        gui.imageHAlign("center");
        gui.imageVAlign("center");
        gui.imageScale(0.5f);
        gui.image("igui/goldmonkey_head.png", .5f, .5f);
        gui.pop();

        gui.push(false);
        gui.imageSize(IGui.SIZE_AUTO,1f);
        gui.imageHAlign("center");
        gui.imageVAlign("center");
        gui.zIndex(-1);
        gui.image("igui/wall.jpg", .5f, .5f);
        gui.pop();

    }    

    public static void main(String[] args) {
        new IGuiTest().start();
    }
}