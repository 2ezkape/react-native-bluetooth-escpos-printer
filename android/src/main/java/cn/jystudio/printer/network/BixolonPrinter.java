package cn.jystudio.printer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import cn.jystudio.printer.network.command.BixolonCommand;
import com.facebook.react.bridge.*;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Created by kape on 2021/11/17.
 */
public class BixolonPrinter {
    private Socket mmSocket;
    private final ReadableMap options;
    private String labelSize;
    public BixolonPrinter(Socket connectedSocket, final ReadableMap options, String labelSize) {
        this.mmSocket = connectedSocket;
        this.options = options;
        this.labelSize = labelSize;
    }

    public boolean printLabel() {
        BixolonCommand bixolon = new BixolonCommand();
        ReadableArray texts = options.hasKey("text")? options.getArray("text"):null;
        // String lbSize = options.hasKey("size")? options.getString("size"):"SIZE 40mm,30mm";
        // String lbGap = options.hasKey("gap")? options.getString("gap"):"GAP 3mm";
        // String lbDirection = options.hasKey("direction")? options.getString("direction"):"DIRECTION 1";
        String fontEncoding = options.hasKey("fontEncoding")? options.getString("fontEncoding"):"GB2312";
        //bixolon.addStartCommand(lbSize,lbGap,lbDirection,fontEncoding);
        //bixolon.addLabelPositioning("252","210","20");
        String delay = options.hasKey("delay")? options.getString("delay"):"N";
        
        if(delay.equals("Y")){    
            Log.d("DELAY_GOES_HERE",delay);
            try{
                Log.d("MASUK_DELAY","MASUK_DELAY_NIH");
                long timeToSleep = 1L;
                TimeUnit time = TimeUnit.SECONDS;
                time.sleep(timeToSleep);
            }catch(InterruptedException e){
                Log.d("INTERUPTED_WHILE_SLEEPING","INTERUPTED_WHILE_SLEEPING");
            }
            
        }
        for (int i = 0;texts!=null&& i < texts.size(); i++) {
            ReadableMap text = texts.getMap(i);
            String t = text.getString("text");
            String fontType = text.getString("fontType");
            String x = text.getString("x");
            String y = text.getString("y");
            boolean pageBreak = text.hasKey("pageBreak") ? text.getBoolean("pageBreak"): false;
            //String fieldBlock = text.hasKey("fieldBlock") ? text.getString("fieldBlock") : "";
            String fontMultiplier = text.hasKey("fontMultiplier") ? text.getString("fontMultiplier"): "1,1";
            try {
                byte[] temp = t.getBytes(fontEncoding);
                String temStr = new String(temp, fontEncoding);
                t = new String(temStr.getBytes(fontEncoding), fontEncoding);//打印的文字
            } catch (Exception e) {
                //promise.reject("INVALID_TEXT", e);
                //mmSocket.close();
                return false;
            }

            // if (fieldBlock!=""){
            //     bixolon.addFieldBlock(fieldBlock,x,y,fontType,fontMultiplier,t,fontEncoding);
            // } else {
            //     bixolon.addText(x,y,fontType,fontMultiplier,t,fontEncoding);
            // }
            bixolon.addText(x,y,fontType,fontMultiplier,t,fontEncoding);
            if(pageBreak){
                bixolon.addEndCommand(fontEncoding);
            };
            
        }
        bixolon.addEndCommand(fontEncoding);
        Vector<Byte> bytes = bixolon.getCommand();
        byte[] tosend = new byte[bytes.size()];
        for(int i=0;i<bytes.size();i++){
            tosend[i]= bytes.get(i);
        }
        try{
            if(sendDataByte(tosend)){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        
    }

    private boolean sendDataByte(byte[] data) {
        try {
            if(mmSocket.isConnected()) {
                OutputStream outputStream = mmSocket.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
                mmSocket.close();
                return true;
            } else {
                return false;
            }
        } catch(IOException e) {
            return false;
        }
        
        
    }
}
