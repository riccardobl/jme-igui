

import com.jme3.app.SimpleApplication;
import com.jme.igui.IGui;
import com.jme.igui.IGuiAppState;
import com.jme.igui.IGuiComponent;
import com.jme3.math.ColorRGBA;

public class IGuiTest extends SimpleApplication{
    private IGui gui;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        gui=IGuiAppState.newRelative(assetManager,stateManager,guiNode, cam.getWidth(),cam.getHeight());
        // gui.destroy();
        gui.textSize(0.05f).textColor(ColorRGBA.Red).textHAlign("center").textVAlign("top");
        IGuiComponent text=gui.text("Test IGui",.5f,1f,true); // persistent. stays for ever
        // text.destroy();   
    }
    
    @Override
    public void simpleUpdate(float tpf){
        gui.textSize(0.02f);
        gui.textColor(ColorRGBA.White);
        gui.textHAlign("left");
        gui.textVAlign("top");

        float spacing=0.03f;
        float line=1;
        gui.text("Line1",0f,line);
        gui.text("Line2",0f,line-=spacing);
        gui.text("Line3",0f,line-=spacing);

        float fps=timer.getFrameRate();
        gui.textColor(fps>=59?ColorRGBA.Green:ColorRGBA.Red);
        gui.textHAlign("right");
        gui.text("FPS: "+((int)fps),1f,1f);

        gui.imageSize(0.3f,IGui.SIZE_AUTO);
        gui.imageHAlign("center");
        gui.imageVAlign("center");
        gui.imageScale(0.5f);

        gui.image("igui/goldmonkey_head.png", .5f, .5f);
    }    

    public static void main(String[] args) {
        new IGuiTest().start();
    }
}