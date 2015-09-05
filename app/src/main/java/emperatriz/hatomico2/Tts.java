package emperatriz.hatomico2;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Tts implements OnInitListener{

	private TextToSpeech tts;
	static final int TTS_CHECK_CODE = 0;
	private int ready = 0;
	//private ReentrantLock waitForInitLock = new ReentrantLock();

	
	private Tts(Activity screen){
		ready = 0;
	    tts = new TextToSpeech( screen, this );
//	    waitForInitLock.lock();
	}
	
	private static Tts instance=null;
	
	public static Tts getInstance(Activity screen){
		if (instance==null){
			instance = new Tts(screen);
		}
		return instance;
	}
	
	public static void destroy(){
		try {
			instance.finalize();
		} catch (Throwable e) {
		}
	}

	@Override
	public void onInit(int status) {
		 if (status == TextToSpeech.SUCCESS)
		    {
		        ready = 1;
		    }
//		    waitForInitLock.unlock();		
	}
	
	public int speak( String text )
	{
//	        if (waitForInitLock.isLocked())
//	        {
//	            try
//	            {
//	                waitForInitLock.tryLock(180, TimeUnit.SECONDS);
//	            }
//	            catch (InterruptedException e)
//	            {
//	                
//	            }
//	            //unlock it here so that it is never locked again
//	            waitForInitLock.unlock();
//	        }

	    if( ready == 1 )
	    {
	        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	        return 1;
	    }
	    else
	    {
	        return 0;
	    }   
	}

	
}
