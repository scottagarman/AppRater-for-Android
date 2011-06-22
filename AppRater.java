
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AppRater.java by @scottagarman
 *
 * Use this to get users to rate Android Market apps
 * Don't use this if using Amazon Market or other non Google Marketplaces
 *
 * Params:
 * Context:     Activity context for edit prefs (use getApplicationContext())
 * days:        Number of days AFTER install required before displaying dialog
 * uses:        Number of uses required to display dialog. Use increaseAppUsed() as needed,
 *              This is a separate call from displayAsk2Rate for controlling uses(ex: events instead of sessions)
 * reminder:    True: If user decides not to rate the app display the dialog on next call of displayAsk2Rate,
 *              false, only show dialog once ever
 */
public class AppRater {
    private static final String ALL_PREFS = ".a2r_ALL";
    private static final String INSTALL_DATE = ".a2r_INSTALL_DATE";
    private static final String HAS_RATED = ".a2r_HAS_RATED";
    private static final String APP_USES = ".a2r_APP_USES";

    public static void displayAsk2Rate(Context ctx, int days, int uses, boolean reminder){

        // Get Package Name
        String packageName = getPackageName(ctx);

        if(packageName == null) return; // no package, no way to find in market

        SharedPreferences sp = ctx.getSharedPreferences(packageName + ALL_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ctx.getSharedPreferences(packageName + ALL_PREFS, Context.MODE_PRIVATE).edit();
        long installDate = sp.getLong(packageName + INSTALL_DATE, 0);
        if(installDate == 0){
            editor.putLong(packageName + INSTALL_DATE, System.currentTimeMillis());
            apply(editor);
        }

        boolean hasRated = sp.getBoolean(packageName + HAS_RATED, false);
        int appUses = sp.getInt(packageName + APP_USES, 0);

        if(appUses < uses){ // only ask if we have met use requirement
            return;
        }else if(!hasRated){
            long currentTime = System.currentTimeMillis();
            if( ((currentTime - installDate) / (1000*60*60*24)) >= days){
                // SHOW
                createAsk2RateDialog(ctx);

                // don't let us show this dialog again
                if(!reminder) {
                    editor.putBoolean(packageName + HAS_RATED, true);
                    apply(editor);
                }
            }
        }
        return;
    }

    /**
     * increaseAppUsed()
     *
     * Use this method to increase the number uses. This is split from the displayAsk2Rate call
     * so you can have finer control over what constitutes a session / use. For example if you only
     * wanted to show the dialog after a user made a purchase, call this in onPurchaseComplete()
     * and not in onCreate.
     *
     * @param
     * ctx: Context from Activity, use getApplicationContext()
     */
    public static void increaseAppUsed(Context ctx){
        String packageName = getPackageName(ctx);

        if(packageName == null) return; // no package, no way to find in market

        SharedPreferences sp = ctx.getSharedPreferences(packageName + ALL_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ctx.getSharedPreferences(packageName + ALL_PREFS, Context.MODE_PRIVATE).edit();

        int uses = sp.getInt(packageName + APP_USES, 0);
        editor.putInt(packageName + APP_USES, ++uses);
        apply(editor);
    }

    private static String getPackageName(Context ctx){
        String pkgName = null;
        try {
            PackageManager manager = ctx.getPackageManager();
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            pkgName = info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            // Can't find package name, set null
            e.printStackTrace();
        }
        return pkgName;
    }

    private static String getApplicationName(Context ctx){
		String appName = "";
		try {
			PackageManager manager = ctx.getPackageManager();
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			appName = ctx.getResources().getString(info.applicationInfo.labelRes);
		}
		catch (Exception e) {
			// Can't find AppName, leave blank
            e.printStackTrace();
		}
        return appName;
    }

    private static void createAsk2RateDialog(final Context ctx){
        AlertDialog.Builder marketDialog = new AlertDialog.Builder(ctx);
        final String packageName = getPackageName(ctx);
        final String appName = getApplicationName(ctx);

        final String marketLink = "market://details?id=" + packageName;
        marketDialog.setTitle("Rate " + appName);
        marketDialog.setMessage("If you enjoy using " + appName + ", would you mind taking a moment to rate it? It won't take more then a minute. Thanks for your support!");
        marketDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Uri uri = Uri.parse(marketLink);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                ctx.startActivity(intent);
                dialog.dismiss();

                // Hide Dialog 4ever
                SharedPreferences.Editor editor = ctx.getSharedPreferences(packageName + ALL_PREFS, Context.MODE_PRIVATE).edit();
                editor.putBoolean(packageName + HAS_RATED, true);
                apply(editor);
            }
        }).setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        marketDialog.create().show();
    }

    // Faster pref saving for high performance
    private static final Method sApplyMethod = findApplyMethod();

    private static Method findApplyMethod() {
        try {
            Class cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } catch (NoSuchMethodException unused) {
            // fall through
        }
        return null;
    }

    public static void apply(SharedPreferences.Editor editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor);
                return;
            } catch (InvocationTargetException unused) {
                // fall through
            } catch (IllegalAccessException unused) {
                // fall through
            }
        }
        editor.commit();
    }
}
