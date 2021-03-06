package utils;

import android.view.View;

import java.util.Calendar;

/**
 * Created by Lvmoy on 2017/4/11 0011.
 * Mode: - - !
 */

public abstract class NoDoubleClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 4000;
    private long lastClickTime = 0;
    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(currentTime - lastClickTime > MIN_CLICK_DELAY_TIME){

        lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }

    protected abstract void onNoDoubleClick(View v);
}
