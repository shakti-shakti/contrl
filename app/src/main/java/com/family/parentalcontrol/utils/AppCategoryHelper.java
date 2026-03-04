package com.family.parentalcontrol.utils;

import java.util.Locale;

public class AppCategoryHelper {
    public static String categorize(String packageName) {
        String pkg = packageName.toLowerCase(Locale.US);
        if (pkg.contains("game") || pkg.contains("play")) {
            return "Games";
        }
        if (pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat")) {
            return "Social";
        }
        if (pkg.contains("edu") || pkg.contains("study") || pkg.contains("khan") || pkg.contains("coursera")) {
            return "Education";
        }
        return "Other";
    }
}