#AppRater for Android
AppRate for android. Drop in your project and your ready to prompt users to rate your Android Market App.

##Install
Just add AppRater.java to your project.

##Docs
### displayAsk2Rate(Context ctx, int days, int uses, boolean reminder)
context:     Activity context for edit prefs (use getApplicationContext())
days:        Number of days AFTER install required before displaying dialog
uses:        Number of uses required to display dialog. Use increaseAppUsed() as needed,
           This is a separate call from displayAsk2Rate for controlling uses(ex: events instead of sessions)
reminder:    True: If user decides not to rate the app display the dialog on next call of displayAsk2Rate,
           false, only show dialog once ever
 
### increaseAppUsed(Context ctx)
Use this method to increase the number uses. This is split from the displayAsk2Rate call
so you can have finer control over what constitutes a session / use. For example if you only
wanted to show the dialog after a user made a purchase, call this in onPurchaseComplete()
and not in onCreate().

##Example
AppRater.displayAsk2Rate(this, 7, 1, false); // 7 days, 1 app use, don't remind if user dismisses dialog
AppRater.increaseAppUsed(getApplicationContext()); // App was used once

 
