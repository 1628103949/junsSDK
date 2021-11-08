package com.game.chijun;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Keychain {

    //获取某个文件的内容
    public  static  String GetFileValue(Context context, String filename){
        String fileRootPath = getPath(context) +  GetFileRealPath(filename);
        String ret = FileUtils.readFile(fileRootPath);
        if(ret == null){
            ret = "";
        }
        return ret;
    }

    //保存文件到某个文件
    public static void   SaveFileValue(Context context, String filename, String content){
        String ExternalSdCardPath = getExternalSdCardPath() + GetFileRealPath(filename);
        FileUtils.writeFile(ExternalSdCardPath, content);
        String InnerPath = context.getFilesDir().getAbsolutePath() + GetFileRealPath(filename);
        FileUtils.writeFile(InnerPath,content);
    }
    /**
     *
     * @param filename String
     * @return String
     */
    public static  String GetFileRealPath(String filename){
        filename = filename.replaceAll("[^a-z^A-Z^0-9]", "");
        return  File.separator + "self" + File.separator + filename;
    }
    /**
     *
     * @param context Context
     * @return String
     */
    private static String getPath(Context context) {
        //首先判断是否有外部存储卡，如没有判断是否有内部存储卡，如没有，继续读取应用程序所在存储
        String phonePicsPath = getExternalSdCardPath();
        if (phonePicsPath == null) {
            phonePicsPath = context.getFilesDir().getAbsolutePath();
        }
        return phonePicsPath;
    }

    /**
     * 遍历 "system/etc/vold.fstab” 文件，获取全部的Android的挂载点信息
     * @return String
     */
    private static ArrayList<String> getDevMountList() {
        ArrayList<String> out = new ArrayList<>();
        String[] toSearch = FileUtils.readFile("/system/etc/vold.fstab").split(" ");
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }

    /**
     * 获取扩展SD卡存储目录
     * <p/>
     * 如果有外接的SD卡，并且已挂载，则返回这个外置SD卡目录
     * 否则：返回内置SD卡目录
     *
     * @return String
     */
    public static String getExternalSdCardPath() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdCardFile.getAbsolutePath();
        }

        String path = null;

        File sdCardFile = null;

        ArrayList<String> devMountList = getDevMountList();

        for (String devMount : devMountList) {
            File file = new File(devMount);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                } else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }

        return null;
    }

}