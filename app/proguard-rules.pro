# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# JNI wrapper: the native library looks these classes and members up by name
-keep class org.infradead.libopenconnect.** { *; }
-keep class org.stoken.** { *; }

# The app's own code is small. It is looked up by name (fragments from preference
# and menu XML, serialized log entries, the AIDL API), so only shrink the libraries.
-keep class net.openconnect_vpn.android.** { *; }
-keepattributes SourceFile,LineNumberTable
