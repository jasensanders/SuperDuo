package it.jaschke.alexandria;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.lang.Character;

/**
 * Created by Jasen Sanders on 002,03/02/16.
 */
public class Utility {
    //Checks for internet connectivity
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    //Checks if the String is numeric
    static public boolean isNum(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    //Checks if  the string is numeric and the right length for a ISBN barcode.
    static public boolean isValid(String s) {
        if(isNum(s)){
            if(s.length() == 10 || s.length() == 13){
                return true;
            }
        }
        return false;
    }
}
