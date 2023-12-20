//==============================================================================
package com.edrosframework.placartenis1;
import android.os.Handler;

//------------------------------------------------------------------------------
public class NTimer {
    private final Handler handler;
    private final Runnable runnable;
    private final int index;
    public int Tag;

    //--------------------------------------------------------------------------
    public NTimer( NTimerListener listener, int index) {
        Tag = 0; this.index = index;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Perform your desired action here when the timer expires
                if(listener != null){
                    listener.onTimeout(NTimer.this);
                }
            }
        };
    }

    //--------------------------------------------------------------------------
    public void startTimer(int Milliseconds) {
        handler.postDelayed(runnable, Milliseconds); // 1000 milliseconds = 1 second
    }

    //--------------------------------------------------------------------------
    public void cancelTimer() {
        handler.removeCallbacks(runnable);
    }

    //--------------------------------------------------------------------------
    public int getIndex(){ return (index);}

    //--------------------------------------------------------------------------
    public void onTimeout() {
        // This method will be called when the timer expires
        // Implement your logic here
    }
}
//==============================================================================