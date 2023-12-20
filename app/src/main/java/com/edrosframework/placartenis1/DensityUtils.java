//==================================================================================================
package com.edrosframework.placartenis1;

import android.content.Context;
import android.util.DisplayMetrics;

//--------------------------------------------------------------------------------------------------
public class DensityUtils {

    //----------------------------------------------------------------------------------------------
    public static int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return Math.round(dp * density) ;//(density / 160f));
    }
}
//==================================================================================================