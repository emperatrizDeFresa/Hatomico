package emperatriz.hatomico2;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by ramon on 5/04/15.
 */
public class WhatsappNotification implements Notification{
    private String sender, text, app, group;
    private Bitmap bitmap;
    private Context ctx;
    public WhatsappNotification(String sender, String group, String text, Bitmap bitmap, Context ctx){
        this.sender=Sys.init().checkNumber(sender.replace("@", ctx.getString(R.string.at)), ctx);
        this.text=text;
        this.bitmap = bitmap;
        this.app = "Whatsapp";
        this.group=group;
        this.ctx = ctx;
    }



    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof WhatsappNotification) {
            WhatsappNotification normalNotification = (WhatsappNotification) object;
            boolean ret = normalNotification.text.trim().equals(this.text.trim());
            boolean sender=false;
            if (normalNotification.getSender().length()>this.getSender().length()){
                sender = normalNotification.getSender().indexOf(this.getSender())>=0;
            }else{
                sender = this.getSender().indexOf(normalNotification.getSender())>=0;
            }
            return ret&&sender;
        }

        return false;
    }


    @Override
    public String getSender() {
        return group!=null?sender+" "+ctx.getString(R.string.at)+" "+group:sender;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Bitmap getIcon() {
        return bitmap;
    }

    @Override
    public String getApp() {
        return app;
    }

    public String toString(){
        return sender+"|"+group+"|"+text;
    }

    public WhatsappNotification toGroupForm(){
        WhatsappNotification wn= new WhatsappNotification(this.group,null,this.sender+": "+this.text,null,ctx);
        return wn;
    }
}
