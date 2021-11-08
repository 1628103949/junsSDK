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

# juns sdk framework proguard config

# juns framework

# support library
-dontwarn android.**
-keep class android.**{*;}



# common
-keepattributes Signature,*Annotation*
-keep public class com.juns.sdk.framework.** {
    public protected *;
}
-keep public interface com.juns.sdk.framework.** {
    public protected *;
}
-keepclassmembers class * extends com.juns.sdk.framework.** {
    public protected *;
}

# webview
-keepattributes *JavascriptInterface*

# xutils
-keepclassmembers @com.juns.sdk.framework.xutils.db.annotation.* class * {*;}
-keepclassmembers @com.juns.sdk.framework.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @com.juns.sdk.framework.xutils.view.annotation.Event <methods>;
}











