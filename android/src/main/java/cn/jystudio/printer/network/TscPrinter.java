package cn.jystudio.printer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import cn.jystudio.printer.network.command.TscCommand;
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
public class TscPrinter {
    private Socket mmSocket;
    private final ReadableMap options;
    private String labelSize;
    public TscPrinter(Socket connectedSocket, final ReadableMap options, String labelSize) {
        this.mmSocket = connectedSocket;
        this.options = options;
        this.labelSize = labelSize;
    }

    public boolean printLabel() {
        TscCommand tsc = new TscCommand();
        ReadableArray commands = options.hasKey("commands")? options.getArray("commands"):null;
        for (int idxCommand = 0;commands!=null&& idxCommand < commands.size(); idxCommand++) {
            ReadableMap command = commands.getMap(idxCommand);
            ReadableArray texts = command.hasKey("text")? command.getArray("text"):null;
           String lbSize = command.hasKey("size")? command.getString("size"):"SIZE 40mm,30mm";
            String lbGap = command.hasKey("gap")? command.getString("gap"):"GAP 3mm";
            String lbDirection = command.hasKey("direction")? command.getString("direction"):"DIRECTION 1";
            String fontEncoding = command.hasKey("fontEncoding")? command.getString("fontEncoding"):"GB2312";
            String delay = command.hasKey("delay")? command.getString("delay"):"N";
            String totalRep = command.hasKey("totalRep")? command.getString("totalRep") : "1";
            String remainder = command.hasKey("remainder")? command.getString("remainder") : "1";
            
            tsc.addStartCommand(lbSize,lbGap,lbDirection,fontEncoding,remainder);
            for (int i = 0;texts!=null&& i < texts.size(); i++) {
                ReadableMap text = texts.getMap(i);
                String t = text.getString("text");
                String fontType = text.getString("fontType");
                String x = text.getString("x");
                String y = text.getString("y");
                String fieldBlock = text.hasKey("fieldBlock") ? text.getString("fieldBlock") : "";
                String fontMultiplier = text.hasKey("fontMultiplier") ? text.getString("fontMultiplier"): "1,1";
                String useCounter = text.hasKey("useCounter") ? text.getString("useCounter") : "N";
                try {
                    byte[] temp = t.getBytes(fontEncoding);
                    String temStr = new String(temp, fontEncoding);
                    t = new String(temStr.getBytes(fontEncoding), fontEncoding);//打印的文字
                } catch (Exception e) {
                    //promise.reject("INVALID_TEXT", e);
                    //mmSocket.close();
                    return false;
                }

                if (fieldBlock!=""){
                    tsc.addFieldBlock(fieldBlock,x,y,fontType,fontMultiplier,t,fontEncoding);
                } else {
                    tsc.addText(x,y,fontType,fontMultiplier,t,fontEncoding,useCounter);
                }
                
            }
            tsc.addEndCommand(fontEncoding, totalRep);
        }
        
        Vector<Byte> bytes = tsc.getCommand();
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
