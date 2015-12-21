package util;

import android.support.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 24/08/15.
 */
public class TextUtils {

    public static boolean isValidText(@Nullable String text){
        return !(text == null || text.length() == 0 || text.equals("") || text.equals(" "));
    }

    public static double formatDoubleNumber(double number){

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat df = (DecimalFormat)nf;
        df.applyPattern("##.##");

        //String twoDigitFormat = String.format("%.2f", number);
        try {
            return Double.valueOf(df.format(number));
        }catch (NumberFormatException e){
            return .0;
        }
    }
}
