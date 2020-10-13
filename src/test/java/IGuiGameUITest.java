import java.util.HashMap;
import java.util.Map;

import com.jme.igui.IGui;
import com.jme.igui.IGuiAppState;
import com.jme.igui.IGuiMouseEvent;
import com.jme.igui.extras.IGuiInputFields;
import com.jme.igui.extras.IGuiInputFieldsAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.system.AppSettings;

public class IGuiGameUITest extends SimpleApplication{
    private IGui igui;
    private IGuiInputFields iinput;
    private String username="";
    private boolean userNameFieldSelected=false;
    private Map<String,Integer> buttonStatus=new HashMap<String,Integer>();

    private int targetState=0;
    private int currentState=0;
    private float stateTime=0;
    private int optionMinIndex=0;

    //  -- Helper methods
    // Draw some text with a shadow
    void textWithShadow(String text, float posX, float posY, float shadowSize, ColorRGBA shadowColor, boolean persistent) {
        // Shadow
        igui.push();
        igui.textColor(shadowColor);
        igui.textFontStyle("bold");
        float shadowOffset=shadowSize;
        for(float xoffset=-shadowOffset;xoffset <= shadowOffset;xoffset+=shadowOffset){
            for(float yoffset=-shadowOffset;yoffset <= shadowOffset;yoffset+=shadowOffset){
                igui.text(text,posX + yoffset,posY + xoffset,persistent);
            }
        }
        igui.pop();

        //Text
        igui.text(text,posX,posY,persistent);
    }

    // Draw a button
    void button(String text, float posX, float posY, boolean persistent, Runnable onClick) {
        igui.push();
        igui.textFontStyle("bold");
        if(buttonStatus.getOrDefault(text,0).intValue() == 1){
            igui.textColor(ColorRGBA.Gray.mult(igui.getTextColor()));
        }else if(buttonStatus.getOrDefault(text,0).intValue() == 2){
            igui.textColor(ColorRGBA.Red.mult(igui.getTextColor()));
        }

        igui.text(text,posX,posY,persistent,(ev, args) -> {
            if(ev == IGuiMouseEvent.MOUSE_IN){
                if(buttonStatus.getOrDefault(text,0) != 2){
                    buttonStatus.put(text,1);
                }
            }else if(ev == IGuiMouseEvent.MOUSE_OUT){
                buttonStatus.put(text,0);
            }else if(ev == IGuiMouseEvent.MOUSE_PRESSED_LEFT){
                buttonStatus.put(text,2);
                onClick.run();
            }else if(ev == IGuiMouseEvent.MOUSE_RELEASED_LEFT){
                buttonStatus.put(text,0);
            }
            return true;
        });

        igui.pop();

    }

    // Make every below darker
    void overlay(boolean persistent) {
        igui.push(false);
        igui.imageSize(1,1);
        igui.image("igui/blackAlpha1.png",0,1,persistent);
        igui.pop();
    }

    // Draw a window
    void window(float scale, float closeButtonSize, Runnable onExit) {
        igui.push();

        igui.imageHAlign("center");
        igui.imageVAlign("center");
        igui.imageSize(scale,scale);

        for(int i=0;i < 2;i++) // make it darker.
            igui.image("igui/blackAlpha1.png",0.5f,0.5f);

        igui.textHAlign("right");
        igui.textVAlign("top");
        igui.textSize(closeButtonSize);

        float d=(1.f - scale) / 2f;
        // Close button
        button("X",scale + d - 0.01f * scale,scale + d - 0.01f * scale,false,onExit);

        igui.pop();
    }

    // Set next state
    void setState(int id) {
        currentState=targetState;
        targetState=id;
        stateTime=0;
    }

    // -- States
    void mainMenuState() {

        // Main menu
        float mainMenuBlendValue=targetState == 0?stateTime:currentState == 0?1f - stateTime:0;
        if(mainMenuBlendValue > 0){
            igui.push();

            float relOpacityScale=FastMath.clamp(mainMenuBlendValue,0,1);
            igui.textColor(new ColorRGBA(1,1,1,relOpacityScale));
            igui.imageColor(new ColorRGBA(1,1,1,relOpacityScale));
            float posY=0.7f;
            float posX=0.02f;
            float spacing=0.095f;
            igui.textHAlign("left");
            button("> P  L  A  Y",posX,posY-=spacing,false,() -> {
                setState(2);
            });
            button("> O  P  T  I  O  N  S",posX,posY-=spacing,false,() -> {
                setState(1);
            });
            button("> Q  U  I  T",posX,posY-=spacing,false,() -> {
                System.out.println("Bye");
                stop();
            });
            igui.pop();
        }

    }

    void optionsState() {

        float optionMenuBlendValue=targetState == 1?stateTime:currentState == 1?1f - stateTime:0;
        if(optionMenuBlendValue > 0){
            igui.push();

            float relOpacityScale;
            if(targetState == 1){
                relOpacityScale=FastMath.clamp((optionMenuBlendValue - 0.5f),0f,1f);
            }else{
                relOpacityScale=1.f - FastMath.clamp((1.f - optionMenuBlendValue) * 4f,0,1);
            }
            igui.textColor(new ColorRGBA(1,1,1,relOpacityScale));
            igui.imageColor(new ColorRGBA(1,1,1,relOpacityScale));

            // Draw window
            igui.push();
            igui.imageColor(new ColorRGBA(1,1,1,1));
            float relScale=FastMath.clamp(optionMenuBlendValue * 2,0f,1f);
            window(relScale * 0.8f,0.02f * relScale,() -> {
                setState(0);
            });
            igui.pop();

            // Draw title
            igui.push();
            igui.textSize(0.04f);
            igui.textVAlign("center");
            igui.text("O p t i o n s",.5f,.85f);
            igui.pop();

            // List
            String options[]=new String[20];
            for(int i=0;i < options.length;i++)
                options[i]="Option" + i;

            // Draw scroll bar
            igui.push();
            igui.imageHAlign("right");
            igui.imageSize(0.04f,IGui.SIZE_AUTO);

            igui.imageVAlign("bottom");
            igui.image("igui/font-awesome/sort-up.png",.89f,.75f,(ev, args) -> {
                if(ev == IGuiMouseEvent.MOUSE_PRESSED_LEFT){
                    optionMinIndex--;
                    if(optionMinIndex < 0) optionMinIndex=0;
                }
                return true;
            });

            igui.imageVAlign("top");
            igui.image("igui/font-awesome/sort-down.png",.89f,.2f,(ev, args) -> {
                if(ev == IGuiMouseEvent.MOUSE_PRESSED_LEFT){
                    optionMinIndex++;
                    if(optionMinIndex > options.length) optionMinIndex=options.length;
                }
                return true;
            });

            float topToBottomRowD=.75f - .2f;
            float cursorLength=topToBottomRowD / options.length;
            float cursorPos=cursorLength * optionMinIndex;

            igui.imageVAlign("center");
            igui.imageSize(0.02f,cursorLength);
            igui.image("igui/white1.png",.89f - 0.01f,.75f - cursorPos);

            igui.pop();

            // Draw options
            igui.push();
            igui.textHAlign("left");
            igui.textVAlign("top");
            float spacing=0.06f;
            float posY=0.8f;
            for(int i=optionMinIndex;i < options.length;i++){
                igui.text(options[i],0.13f,posY-=spacing);
                if(posY <= 0.3f) break;
            }
            igui.pop();

            igui.pop();

        }
    }

    void playState() {

        float playMenuBlendValue=targetState == 2?stateTime:currentState == 2?1f - stateTime:0;
        if(playMenuBlendValue > 0){
            igui.push();

            float relOpacityScale;
            if(targetState == 1){
                relOpacityScale=FastMath.clamp((playMenuBlendValue - 0.5f),0f,1f);
            }else{
                relOpacityScale=1.f - FastMath.clamp((1.f - playMenuBlendValue) * 4f,0,1);
            }
            igui.textColor(new ColorRGBA(1,1,1,relOpacityScale));
            igui.imageColor(new ColorRGBA(1,1,1,relOpacityScale));

            // Draw window
            igui.push();
            igui.imageColor(new ColorRGBA(1,1,1,1));
            float relScale=FastMath.clamp(playMenuBlendValue * 2,0f,1f);
            window(relScale * 0.8f,0.02f * relScale,() -> {
                setState(0);
            });
            igui.pop();

            // Draw title
            igui.push();
            igui.textSize(0.04f);
            igui.textVAlign("center");
            igui.text("P l a y",.5f,.85f);
            igui.pop();

            String placeholder=username;
            int d=(24 - placeholder.length()) / 2;
            if(d > 0){
                for(int i=0;i < d;i++)
                    placeholder+="_";
                for(int i=0;i < d;i++)
                    placeholder="_" + placeholder;
            }

            placeholder="~" + placeholder + "~";
            igui.text("Chose an username",0.5f,0.7f);

            igui.push();
            if(userNameFieldSelected) igui.textColor(ColorRGBA.Green);
            iinput.input(placeholder,0.5f,0.5f,(ev, args) -> {
                if(ev == IGuiMouseEvent.MOUSE_IN) userNameFieldSelected=true;
                else if(ev == IGuiMouseEvent.MOUSE_OUT) userNameFieldSelected=false;
                return true;
            },(key) -> {
                if(key.getKeyCode() == KeyInput.KEY_BACK && username.length() > 0) username=username.substring(0,username.length() - 1);
                else if(key.getKeyChar() != '\0') username+=key.getKeyChar();
            });
            igui.pop();

            button("[CONFIRM]",0.5f,0.4f,false,() -> {
                setState(3);
            });

            igui.pop();

        }
    }

    void gameState() {

        float gameBlendValue=targetState == 3?stateTime:currentState == 3?1f - stateTime:0;
        if(gameBlendValue > 0){
            igui.push();

            float relOpacityScale;
            if(targetState == 1){
                relOpacityScale=FastMath.clamp((gameBlendValue - 0.5f),0f,1f);
            }else{
                relOpacityScale=1.f - FastMath.clamp((1.f - gameBlendValue) * 4f,0,1);
            }
            igui.textColor(new ColorRGBA(1,1,1,relOpacityScale));
            igui.imageColor(new ColorRGBA(1,1,1,relOpacityScale));

            // Draw window
            igui.push();
            igui.imageColor(new ColorRGBA(1,1,1,1));
            float relScale=FastMath.clamp(gameBlendValue * 2,0f,1f);
            window(relScale * 0.8f,0.02f * relScale,() -> {
                setState(0);
            });
            igui.pop();

            // Draw title
            igui.push();
            igui.textSize(0.04f);
            igui.textVAlign("center");
            igui.text("4 0 4 - Game Not Found",.5f,.85f);
            igui.pop();

            igui.text("Hello ",0.5f,0.7f);
            igui.text(username,0.5f,0.7f - 0.06f);
            igui.text("This is just a demo :(",0.5f,0.7f - 0.20f);

            igui.pop();

        }
    }

    // -- Initialization and static UI
    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);

        igui=IGuiAppState.newRelative(assetManager,stateManager,inputManager,guiNode,cam.getWidth(),cam.getHeight());
        iinput=IGuiInputFieldsAppState.newPlugin(igui,inputManager,stateManager);

        // Global settings
        igui.textSize(0.03f);
        igui.textColor(ColorRGBA.White);
        igui.textFont("igui/vera/Vera.ttf");
        igui.textHAlign("center");
        igui.textVAlign("top");

        // Draw background
        igui.push();
        igui.imageSize(IGui.SIZE_AUTO,1);
        igui.image("igui/bg.jpg",0,1,true);
        igui.pop();

        overlay(true);

        // Draw title
        igui.push();
        igui.textSize(0.05f);
        igui.textFont("igui/vera/VeraBd.ttf");
        igui.textColor(ColorRGBA.White);
        textWithShadow(" G  A  M  E",0.5f,0.9f,0.003f,ColorRGBA.Black,true);
        igui.pop();

    }

    // -- Dynamic UI
    @Override
    public void simpleUpdate(float tpf) {
        stateTime+=tpf;

        mainMenuState();
        optionsState();
        playState();
        gameState();

    }

    public static void main(String[] args) {
        AppSettings settings=new AppSettings(true);
        settings.setVSync(true);
        IGuiGameUITest a=new IGuiGameUITest();
        a.setSettings(settings);
        a.start();
    }

}