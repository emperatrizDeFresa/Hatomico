package emperatriz.hatomico2;

import android.content.Context;
import android.graphics.Bitmap;

public class NormalNotification implements Notification{
	private String sender, text, app;
	private Bitmap bitmap;
	public NormalNotification(String sender, String text, Bitmap bitmap, String app, Context ctx){
		this.sender=sender.replace("@", ctx.getString(R.string.at));
		this.text=text;
		this.bitmap = bitmap;
		this.app = app;
	}
	
	public NormalNotification(String sender, String text){
		this.sender=sender;
		this.text=text;
		this.bitmap = null;
		this.app="";
		
	}

    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof NormalNotification) {
            NormalNotification normalNotification = (NormalNotification) object;
            boolean ret = normalNotification.text.trim().equals(this.text.trim())&& normalNotification.app.equals(this.app);
            boolean sender=false;
            if (normalNotification.sender.length()>this.sender.length()){
                sender = normalNotification.sender.indexOf(this.sender)>=0;
            }else{
                sender = this.sender.indexOf(normalNotification.sender)>=0;
            }
            return ret&&sender;
        }

        return false;
	}


    @Override
    public String getSender() {
        return sender;
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
}
