package emperatriz.hatomico2;

import android.graphics.Bitmap;

/**
 * Created by ramon on 5/04/15.
 */
public interface Notification {
    public String getSender();
    public String getText();
    public Bitmap getIcon();
    public String getApp();
}
