package com.jme.igui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

/**
 * Based on https://github.com/jMonkeyEngine/sdk/blob/master/jme3-angelfont/src/com/jme3/gde/angelfont/FontCreator.java
 * And  https://github.com/jMonkeyEngine/jmonkeyengine/blob/64bfe42878bbd33c18b875e05923887554f395cd/jme3-core/src/plugins/java/com/jme3/font/plugins/BitmapFontLoader.java
 * See respective licenses.
 * 
 * TODO: Replace this with something that is not based in awt.
 */
public class TTFConverter{
    private final Map<TTFEntry,BitmapFont> ttfCache=new HashMap<TTFEntry,BitmapFont>();

    private static class TTFEntry{
        String font;
        String style;
        double mult;

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof TTFEntry)) return false;
            TTFEntry e=(TTFEntry)o;
            return e.font.equals(font) && e.style.equals(style) && mult == e.mult;
        }

        @Override
        public int hashCode() {
            return Objects.hash(font,style,mult);
        }
    }

    public void clearCache(){
        ttfCache.clear();
    }

    public BitmapFont convertTTF(AssetManager assetManager, String font, String style, float size) {
        double m=Math.floor( (size / 16.)*10.) /10. ;
        TTFEntry entry=new TTFEntry();
        entry.font=font;
        entry.style=style;
        entry.mult=m;
        BitmapFont out=ttfCache.get(entry);
        if(out != null){
            return out;
        }
        try{
            int fontSize=(int)(16 * m);
            if(fontSize<=0)fontSize=1;
            int bitmapSize=(int)(256 * m);
            if(bitmapSize<2)bitmapSize=2;
            AssetInfo info=assetManager.locateAsset(new AssetKey(font));
            InputStream is=info.openStream();
            out=TTFConverter.convert(assetManager,is,bitmapSize,fontSize,style);
            ttfCache.put(entry,out);
            is.close();
            return out;
        }catch(Exception e){
            throw new AssetLoadException("Can't load " + font,e);
        }
    }

    private static final Color OPAQUE_WHITE=new Color(0xFFFFFFFF,true);
    private static final Color TRANSPARENT_BLACK=new Color(0x00000000,true);

    private static BitmapFont load(AssetManager assetManager, String charLocs, BufferedImage fontImage) throws IOException {
        MaterialDef spriteMat=(MaterialDef)assetManager.loadAsset(new AssetKey("Common/MatDefs/Misc/Unshaded.j3md"));
        BitmapCharacterSet charSet=new BitmapCharacterSet();
        Material[] matPages=null;
        BitmapFont font=new BitmapFont();

        BufferedReader reader=new BufferedReader(new StringReader(charLocs));
        String regex="[\\s=]+";
        font.setCharSet(charSet);
        String line;
        while((line=reader.readLine()) != null){
            String[] tokens=line.split(regex);
            if(tokens[0].equals("info")){
                // Get rendered size
                for(int i=1;i < tokens.length;i++){
                    if(tokens[i].equals("size")){
                        charSet.setRenderedSize(Integer.parseInt(tokens[i + 1]));
                    }
                }
            }else if(tokens[0].equals("common")){
                // Fill out BitmapCharacterSet fields
                for(int i=1;i < tokens.length;i++){
                    String token=tokens[i];
                    if(token.equals("lineHeight")){
                        charSet.setLineHeight(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("base")){
                        charSet.setBase(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("scaleW")){
                        charSet.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("scaleH")){
                        charSet.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("pages")){
                        // number of texture pages
                        matPages=new Material[Integer.parseInt(tokens[i + 1])];
                        font.setPages(matPages);
                    }
                }
            }else if(tokens[0].equals("page")){
                int index=-1;
                Texture tex=null;

                for(int i=1;i < tokens.length;i++){
                    String token=tokens[i];
                    if(token.equals("id")){
                        index=Integer.parseInt(tokens[i + 1]);
                    }else if(token.equals("file")){
                        String file=tokens[i + 1];
                        if(file.startsWith("\"")){
                            file=file.substring(1,file.length() - 1);
                        }

                        Image img=new AWTLoader().load(fontImage,true);

                        tex=new Texture2D(img);
                        tex.setMagFilter(Texture.MagFilter.Bilinear);
                        tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
                    }
                }
                // set page
                if(index >= 0 && tex != null){
                    Material mat=new Material(spriteMat);
                    mat.setTexture("ColorMap",tex);
                    mat.setBoolean("VertexColor",true);
                    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                    matPages[index]=mat;
                }
            }else if(tokens[0].equals("char")){
                // New BitmapCharacter
                BitmapCharacter ch=null;
                for(int i=1;i < tokens.length;i++){
                    String token=tokens[i];
                    if(token.equals("id")){
                        int index=Integer.parseInt(tokens[i + 1]);
                        ch=new BitmapCharacter();
                        charSet.addCharacter(index,ch);
                    }else if(token.equals("x")){
                        ch.setX(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("y")){
                        ch.setY(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("width")){
                        ch.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("height")){
                        ch.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("xoffset")){
                        ch.setXOffset(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("yoffset")){
                        ch.setYOffset(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("xadvance")){
                        ch.setXAdvance(Integer.parseInt(tokens[i + 1]));
                    }else if(token.equals("page")){
                        ch.setPage(Integer.parseInt(tokens[i + 1]));
                    }
                }
            }else if(tokens[0].equals("kerning")){
                // Build kerning list
                int index=0;
                int second=0;
                int amount=0;

                for(int i=1;i < tokens.length;i++){
                    if(tokens[i].equals("first")){
                        index=Integer.parseInt(tokens[i + 1]);
                    }else if(tokens[i].equals("second")){
                        second=Integer.parseInt(tokens[i + 1]);
                    }else if(tokens[i].equals("amount")){
                        amount=Integer.parseInt(tokens[i + 1]);
                    }
                }

                BitmapCharacter ch=charSet.getCharacter(index);
                ch.addKerning(second,amount);
            }
        }
        return font;
    }

    public static BitmapFont convert(AssetManager assetManager, InputStream font, int bitmapSize, int fontSize, String style) throws IOException {
        int awtstyle=Font.PLAIN;
        switch(style){
            case "bold":
                awtstyle=Font.BOLD;
                break;
            case "italic":
                awtstyle=Font.ITALIC;
                break;
        }

        return buildFont(assetManager,font,bitmapSize,fontSize,awtstyle);
    }

    private static BitmapFont buildFont(AssetManager assetManager, InputStream font, int bitmapSize, int fontSize, int style) throws IOException {
        return buildFont(assetManager,font,bitmapSize,fontSize,Font.PLAIN,false);
    }

    private static BitmapFont buildFont(AssetManager assetManager, InputStream font, int bitmapSize, int fontSize, int style, boolean debug) throws IOException {
        return buildFont(assetManager,font,bitmapSize,fontSize,style,2,2,4,debug);
    }

    private static BitmapFont buildFont(AssetManager assetManager, InputStream font, int bitmapSize, int fontSize, int style, int paddingX, int paddingY, int letterSpacing, boolean debug) throws IOException {
        return buildFont(assetManager,font,bitmapSize,fontSize,style,paddingX,paddingY,letterSpacing,0,256,debug);
    }

    private static BitmapFont buildFont(AssetManager assetManager, InputStream istream, int bitmapSize, int fontSize, int style, int paddingX, int paddingY, int letterSpacing, int firstChar, int lastChar, boolean debug) throws IOException {
        Font font=null;
        try{
            font=Font.createFont(Font.TRUETYPE_FONT,istream);
            font=font.deriveFont(style,(float)fontSize);
        }catch(Exception e){
            throw new IOException(e);
        }

        BufferedImage fontImage;

        String charLocs="";

        // use BufferedImage.TYPE_4BYTE_ABGR to allow alpha blending
        fontImage=new BufferedImage(bitmapSize,bitmapSize,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g=(Graphics2D)fontImage.getGraphics();

        g.setFont(font);
        if(!debug){
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setColor(OPAQUE_WHITE);
        g.setBackground(TRANSPARENT_BLACK);
        FontRenderContext frc=g.getFontRenderContext();

        FontMetrics fm=g.getFontMetrics();

        if(debug){
            // g.fillRect(0,0,bitmapSize ,bitmapSize);
        }

        if(debug){
            g.setColor(Color.WHITE);
            g.drawRect(0,0,256 - 1,256 - 1);
        }

        int xPos=0;
        int height=fm.getDescent() + fm.getAscent();
        int yPos=height + (paddingY * 2);

        for(int i=firstChar;i <= lastChar;i++){
            char ch[]={(char)i};
            String temp=new String(ch);

            if(!font.canDisplay((char)i)){
                continue;
            }

            TextLayout tl=new TextLayout(temp,font,frc);
            Rectangle2D pixelBounds=tl.getPixelBounds(frc,xPos,yPos);

            int width=(int)Math.ceil(pixelBounds.getWidth());

            int advance=(int)Math.ceil(tl.getAdvance());

            int xOffset=(int)Math.round(pixelBounds.getX()) - xPos;

            if(xPos + width + (paddingX * 2) > bitmapSize){
                xPos=0;
                yPos+=height + (paddingY * 2);
            }

            g.drawString(temp,xPos + paddingX - xOffset,yPos + paddingY);
            if(debug){
                g.setColor(Color.BLUE);
                g.drawRect(xPos,yPos - fm.getAscent(),width + (paddingX * 2),height + (paddingY * 2));
                g.setColor(Color.WHITE);
            }
            charLocs=charLocs + "char id=" + i + "    x=" + xPos + "    y=" + (yPos - fm.getAscent()) + "    width=" + (width + (paddingX * 2)) + "    height=" + (fm.getHeight() + (paddingY * 2)) + "    xoffset=" + (xOffset) + "    yoffset=0" + "    xadvance=" + ((advance + letterSpacing) - 1) + " " + "    page=0" + "    chnl=0\n";
            xPos+=width + (paddingX * 2);
        }
        charLocs="info face=null " + "size=" + fontSize + " " + "bold=0 " + "italic=0 " + "charset=ASCII " + "unicode=0 " + "stretchH=100 " + "smooth=1 " + "aa=1 " + "padding=0,0,0,0 " + "spacing=1,1 " + "\n" + "common lineHeight=" + height + " " + "base=26 " + "scaleW=" + bitmapSize + " " + "scaleH=" + bitmapSize + " " + "pages=1 " + "packed=0 " + "\n" + "page id=0 file=\"" + "font.png\"\n" + "chars count=255\n" + charLocs;

        return load(assetManager,charLocs,fontImage);
    }

}