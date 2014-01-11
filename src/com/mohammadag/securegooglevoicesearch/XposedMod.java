package com.mohammadag.securegooglevoicesearch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	private static boolean mIsVoiceSearch = false;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.google.android.googlequicksearchbox"))
			return;

		XposedHelpers.findAndHookMethod("com.google.android.velvet.ui.VelvetActivity", 
				lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				boolean wasScreenOn = true;
				final Activity activity = (Activity) param.thisObject;
				PowerManager pwrmgr = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
				wasScreenOn = pwrmgr.isScreenOn();
				Window window = activity.getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

				if (!wasScreenOn && mIsVoiceSearch) {
					mIsVoiceSearch = false;

					new Handler().postDelayed(new Runnable() {
						public void run() {
							Intent intent = new Intent();
							intent.setComponent(new ComponentName("com.google.android.googlequicksearchbox",
									"com.google.android.googlequicksearchbox.VoiceSearchActivity"));
							activity.startActivity(intent);
						}
					}, 500);
				}
			}
		});

		XposedHelpers.findAndHookMethod("com.google.android.googlequicksearchbox.VoiceSearchActivity",
				lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mIsVoiceSearch = true;
			}
		});
	}
}
