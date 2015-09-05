package emperatriz.hatomico2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class About extends Activity implements OnClickListener{

	int times=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.about); 
		
		LinearLayout al = (LinearLayout)findViewById(R.id.aboutLayout);
		al.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		if (times<10) times++;
		else{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=ddp70SjsiNw")));
		}
		
	}
	
	
}
