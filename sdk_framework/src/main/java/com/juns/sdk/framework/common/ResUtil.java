package com.juns.sdk.framework.common;

import android.content.Context;
import android.util.Log;

/**
 * Data：28/09/2018-4:29 PM
 * Author: ranger
 */
public class ResUtil {

    private static String ANIMATION = "anim";
    private static String COLOR = "color";
    private static String DRAWABLE = "drawable";
    private static String LAYOUT = "layout";
    private static String MENU = "menu";
    private static String STRING = "string";
    private static String STYLE = "style";
    private static String DIMEN = "dimen";
    private static String ATTR = "attr";
    private static String ID = "id";

    private static int getResourcesID(String name, String type, Context context) {
        int id = 0x7f000000;
        try {
            id = context.getResources().getIdentifier(name, type, context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("JunS", "get resources exception. resources no found -->" +
                    "\nname --> " + name +
                    "\ntype --> " + type);
        }
        return id;
    }

    public static int getAnimationID(String name, Context ctx) {
        return getResourcesID(name, ANIMATION, ctx);
    }

    public static int getColorID(String name, Context ctx) {
        return getResourcesID(name, COLOR, ctx);
    }

    public static int getDrawableID(String name, Context ctx) {
        return getResourcesID(name, DRAWABLE, ctx);
    }

    public static int getLayoutID(String name, Context ctx) {
        return getResourcesID(name, LAYOUT, ctx);
    }

    public static int getMenuID(String name, Context ctx) {
        return getResourcesID(name, MENU, ctx);
    }

    public static int getStringID(String name, Context ctx) {
        return getResourcesID(name, STRING, ctx);
    }

    public static int getStyleID(String name, Context ctx) {
        return getResourcesID(name, STYLE, ctx);
    }

    public static int getDimenID(String name, Context ctx) {
        return getResourcesID(name, DIMEN, ctx);
    }

    public static int getAttrID(String name, Context ctx) {
        return getResourcesID(name, ATTR, ctx);
    }

    public static int getID(String name, Context ctx) {
        return getResourcesID(name, ID, ctx);
    }

}
