# Add project specific ProGuard rules here.
  # By default, the flags in this file are appended to flags specified
  # in D:\ADT\sdk/tools/proguard/proguard-android.txt
  # You can edit the include path and order by changing the proguardFiles
  # directive in build.gradle.
  #
  # For more details, see
  #   http://developer.android.com/guide/developing/tools/proguard.html

  # Add any project specific keep options here:

  # If your project uses WebView with JS, uncomment the following
  # and specify the fully qualified class name to the JavaScript interface
  # class:
  #-keepclassmembers class fqcn.of.javascript.interface.for.webview {
  #   public *;
  #}

 -ignorewarnings

# juns sdk proguard config
#--------------- start -------------

# juns sdk
-keepclassmembers class com.juns.sdk.core.own.fw.TNUserActivity$WebInterface{
   public *;
}
-keepclassmembers class com.juns.sdk.core.sdk.common.NoticeDialog$WebInterface{
   public *;
}
-keepattributes *JavascriptInterface*

-keep class com.juns.sdk.core.api.JunSConstants{*;}
-keep class com.juns.sdk.core.sdk.IOaidAdapter{*;}
-keep class com.juns.sdk.core.api.JunSConstants$**{*;}

-keep class com.juns.sdk.core.api.JunSSdkApplication{*;}
-keep class com.juns.sdk.core.api.JunSSdkApi{*;}
-keep class com.juns.sdk.core.sdk.common.InitInfoCallBack{*;}
-keep class com.juns.sdk.core.sdk.common.InitInfoDialog{*;}
-keep class com.juns.sdk.core.api.**{*;}
-keep class com.juns.sdk.core.sdk.ads.**{*;}
-keep class com.juns.sdk.core.sdk.SDKData{*;}
-keep class com.juns.sdk.core.sdk.config.**{*;}
-keep class com.juns.sdk.core.platform.**{*;}
-keep class com.juns.sdk.core.sdk.SDKApplication{*;}
-keep class com.juns.sdk.core.http.params.MPayQueryParam{*;}
-keep class com.juns.sdk.core.http.params.ReferParam{*;}
-keep class com.juns.sdk.core.http.JunSResponse{*;}
-keepclassmembers @com.juns.sdk.framework.xutils.db.annotation.* class * {*;}
-keepclassmembers @com.juns.sdk.framework.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @com.juns.sdk.framework.xutils.view.annotation.Event <methods>;
}

-keep class com.bun.miitmdid.core.** {*;}

#--------------- End ---------------











