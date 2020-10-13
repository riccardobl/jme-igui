
import com.jme.igui.IGui;
import com.jme.igui.IGuiAppState;
import com.jme.igui.extras.IGuiInputFields;
import com.jme.igui.extras.IGuiInputFieldsAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;

public class IGuiInputTest extends SimpleApplication{
    private IGui igui;
    private IGuiInputFields iinput;

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        igui=IGuiAppState.newRelative(assetManager,stateManager,inputManager,guiNode,cam.getWidth(),cam.getHeight());
        iinput=IGuiInputFieldsAppState.newPlugin(igui,inputManager,stateManager);
        igui.push();
        igui.textSize(0.05f).textColor(ColorRGBA.Red).textHAlign("center").textVAlign("top");
        igui.text("Test Input",0.5f,1,true);
        igui.pop();
    }

    private String inputText="";
    private String confirmedText="";

    @Override
    public void simpleUpdate(float tpf) {
        viewPort.setBackgroundColor(ColorRGBA.Gray);
        igui.push();
        String label="Input some Text: ";
        String description="(move the mouse over the line, write some text and press enter to confirm)";
        igui.textColor(ColorRGBA.White);
        igui.text(label,0,0.5f);
        igui.text(description,0,0.46f);

        String inputPlaceHolder=inputText;
        while(inputPlaceHolder.length() < 10)
            inputPlaceHolder+="_";

        iinput.input(inputPlaceHolder,igui.getTextLineWidth(label) + 0.02f,.5f,key -> {
            if(key.getKeyCode() == KeyInput.KEY_BACK && inputText.length() > 0) inputText=inputText.substring(0,inputText.length() - 1);
            else if(key.getKeyCode() == KeyInput.KEY_RETURN) confirmedText=inputText;
            else if(key.getKeyChar() != '\0') inputText+=key.getKeyChar();
        });

        igui.text("Your input was: " + confirmedText,0,0.54f);

        igui.pop();
    }

    public static void main(String[] args) {
        new IGuiInputTest().start();
    }
}