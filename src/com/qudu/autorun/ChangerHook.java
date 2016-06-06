package com.qudu.autorun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Random;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ChangerHook implements IXposedHookLoadPackage {
	
	PhoneInfoModel mPhoneInfoModel;
	
	private String getPhoneInfo() {
		File configFile = new File(ConstConfig.infoPath);
		if (configFile == null || !configFile.exists()) {
			return null;
		}
		String phoneInfo = null;
		InputStream is;
		try {
			is = new FileInputStream(configFile);
			
			if (is != null) {
			    InputStreamReader inputreader = new InputStreamReader(is);
			    BufferedReader buffreader = new BufferedReader(inputreader);
			    try {
			    	phoneInfo = buffreader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}		    
			    is.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return phoneInfo;
	}

	private boolean needShake() {
		File configFile = new File(ConstConfig.shakePath);
		if (configFile == null || !configFile.exists()) {
			return false;
		}
		
		return true;
	}	
	
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
//		if (!lpparam.packageName.equals("com.tencent.android.qqdownloader")//应用宝
//				&& !lpparam.packageName.equals("com.baidu.appsearch")//百度助手
//				&& !lpparam.packageName.equals("com.qihoo.appstore")//360
//				&& !lpparam.packageName.equals("com.xiaomi.market")//小米应用商店
//				&& !lpparam.packageName.equals("com.huawei.appmarket")//华为商城
//				&& !lpparam.packageName.equals("com.aspire.mm")//移动mm商城
//				&& !lpparam.packageName.equals("com.suning")
//				) {
//			XposedBridge.log("handleLoadPackage() pacakge not need hook, return!");
//			return;
//		}
		
		mPhoneInfoModel = new PhoneInfoModel();
		String phoneInfo = getPhoneInfo();
		XposedBridge.log("phonInfo = " + phoneInfo);
		mPhoneInfoModel.init(phoneInfo);

        //hook TelephonyManager类里面的getDeviceId，修改手机imei码妥妥的
		XC_MethodHook hook = new HookCallback(lpparam);
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getDeviceId",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getDeviceSoftwareVersion",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getLine1Number",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getNetworkCountryIso",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getNetworkOperator",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getNetworkOperatorName",
				new Object[] { hook });
//		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getNetworkType",
//				new Object[] { hook });
//		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getPhoneType",
//				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSimCountryIso",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSimOperator",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSimOperatorName",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSimSerialNumber",
				new Object[] { hook });
//		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSimState",
//				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getSubscriberId",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), lpparam.classLoader, "getVoiceMailNumber",
				new Object[] { hook });

		XposedHelpers.findAndHookMethod(WifiInfo.class.getName(), lpparam.classLoader, "getMacAddress",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(WifiInfo.class.getName(), lpparam.classLoader, "getSSID",
				new Object[] { hook });
		XposedHelpers.findAndHookMethod(WifiInfo.class.getName(), lpparam.classLoader, "getBSSID",
				new Object[] { hook });

		XposedHelpers.findAndHookMethod(Build.class.getName(), lpparam.classLoader, "getRadioVersion",
				new Object[] { hook });
		//android_id
		XposedHelpers.findAndHookMethod(Settings.Secure.class.getName(), lpparam.classLoader, "getString",
				new Object[] { ContentResolver.class, String.class, hook });
		//序列号
	    XposedHelpers.findAndHookMethod("android.os.SystemProperties", lpparam.classLoader, "get",
	    		new Object[] { String.class, String.class, hook });

	    //NetExtraInfo
	    XposedHelpers.findAndHookMethod(NetworkInfo.class.getName(),  lpparam.classLoader, "getExtraInfo",
	    		new Object[] { hook });

	    //ipv6
	    XposedHelpers.findAndHookMethod(InetAddress.class.getName(),  lpparam.classLoader, "getHostAddress",
	    		new Object[] { hook });
	    XposedHelpers.findAndHookMethod(InetAddress.class.getName(),  lpparam.classLoader, "getHostName",
	    		new Object[] { hook });
	    
	    if (lpparam.packageName.equals(ConstConfig.PACKAGE_WEIXIN)) {
			XposedHelpers.findAndHookMethod("android.hardware.SystemSensorManager$SensorEventQueue",  lpparam.classLoader, "dispatchSensorEvent",
		    		new Object[] { hook });
		}
	    
	    //分辨率
//	    XposedHelpers.findAndHookMethod(Display.class.getName(), lpparam.classLoader, "getWidth",
//	    		new Object[] { hook });
//	    XposedHelpers.findAndHookMethod(Display.class.getName(), lpparam.classLoader, "getHeight",
//	    		new Object[] { hook });
//	    XposedHelpers.findAndHookMethod(Resources.class.getName(), lpparam.classLoader, "getDisplayMetrics",
//	    		new Object[] { hook });
//	    XposedHelpers.findAndHookMethod(Display.class.getName(), lpparam.classLoader, "getMetrics",
//	    		new Object[] { DisplayMetrics.class, hook });

//	    XposedHelpers.findAndHookMethod(Activity.class.getName(), lpparam.classLoader, "onCreate", 
//	    		new Object[] { Bundle.class, hook });

		XposedHelpers.setStaticObjectField(Build.VERSION.class, "RELEASE", mPhoneInfoModel.release);
		XposedHelpers.setStaticObjectField(Build.VERSION.class, "SDK", mPhoneInfoModel.sdk);
		XposedHelpers.setStaticObjectField(Build.class, "CPU_ABI", mPhoneInfoModel.cpu_abi);
		XposedHelpers.setStaticObjectField(Build.class, "CPU_ABI2", mPhoneInfoModel.CPU_ABI2);
		XposedHelpers.setStaticObjectField(Build.class, "BRAND", mPhoneInfoModel.brand);
		XposedHelpers.setStaticObjectField(Build.class, "MODEL", mPhoneInfoModel.model);
		XposedHelpers.setStaticObjectField(Build.class, "PRODUCT", mPhoneInfoModel.product);
		XposedHelpers.setStaticObjectField(Build.class, "MANUFACTURER", mPhoneInfoModel.manufacture);
		XposedHelpers.setStaticObjectField(Build.class, "HARDWARE", mPhoneInfoModel.hardware);
		XposedHelpers.setStaticObjectField(Build.class, "FINGERPRINT", mPhoneInfoModel.fingerprint);
		XposedHelpers.setStaticObjectField(Build.class, "SERIAL", mPhoneInfoModel.serial);
		XposedHelpers.setStaticObjectField(Build.class, "DISPLAY", mPhoneInfoModel.DISPLAY);
    }
    
    public class HookCallback extends XC_MethodHook {
    	private LoadPackageParam mLpparam = null;
    	private String mPackageName = null;
    	
    	public HookCallback(LoadPackageParam param) {
    		mLpparam = param;
    		if (param != null) {
				mPackageName = param.packageName;
			}
    	}
    	
    	private boolean needHook() {
    		if (!mPackageName.equals(ConstConfig.PACKAGE_YingYongBao)//应用宝
					&& !mPackageName.equals(ConstConfig.PACKAGE_BaiDu)//百度助手
					&& !mPackageName.equals(ConstConfig.PACKAGE_360)//360
					&& !mPackageName.equals(ConstConfig.PACKAGE_XiaoMi)//小米应用商店
					&& !mPackageName.equals(ConstConfig.PACKAGE_HuaWei)//华为商城
					&& !mPackageName.equals(ConstConfig.PACKAGE_YiDongMM)//移动mm商城
					&& !mPackageName.equals("com.suning")
					&& !mPackageName.equals(ConstConfig.PACKAGE_SMREADER)
					&& !mPackageName.equals(ConstConfig.PACKAGE_TAOBAO)
					&& !mPackageName.equals(ConstConfig.PACKAGE_WEIXIN)
					) {
				return false;
			}
    		
    		return true;
    	}
    	
    	@Override
    	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    		if (mPackageName.equals(ConstConfig.PACKAGE_WEIXIN) && param.method.getName().equals("dispatchSensorEvent")) {
    			if (needShake()) {
                    ((float[])param.args[1])[0] = (125.0F + 1200.0F * new Random().nextFloat());
                    try {
                    	new File(ConstConfig.shakePath).delete();
					} catch (Exception e) {
						XposedBridge.log(e.getMessage());
					}
                    return;
				}
			}
    		super.beforeHookedMethod(param);    		
    	}
    	
    	@Override
    	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    		if (!TextUtils.isEmpty(mPackageName) && !param.method.getName().equals("getDeviceId")) {
        		if (!needHook()) {
    				XposedBridge.log("handleLoadPackage() pacakge not need hook, return!");
    				return;
				}
			}
            XposedBridge.log("开始劫持啦~");
            XposedBridge.log("劫持方法：param.method.getName()" + param.method.getName());
            if (mPhoneInfoModel == null || mPhoneInfoModel.getDeviceId == null) {
                XposedBridge.log("mPhoneInfoModel == null");
                return;
			} else {
                XposedBridge.log("mPhoneInfoModel != null");
			}
            //将返回得imei值设置为我想要的值
            if (param.method.getName().equals("getDeviceId")) {
            	if (!needHook()) {
					if (param.getResult() == null || "".equals(param.getResult())) {
						param.setResult("869894025139771");
					}
					return;
				}
                param.setResult(mPhoneInfoModel.getDeviceId);
			} else if (param.method.getName().equals("getDeviceSoftwareVersion")) {
                param.setResult(mPhoneInfoModel.getDeviceSoftwareVersion);
			} else if (param.method.getName().equals("getLine1Number")) {
                param.setResult(mPhoneInfoModel.getLine1Number);
			} else if (param.method.getName().equals("getNetworkCountryIso")) {
                param.setResult(mPhoneInfoModel.getNetworkCountryIso);
			} else if (param.method.getName().equals("getNetworkOperator")) {
                param.setResult(mPhoneInfoModel.getNetworkOperator);
			} else if (param.method.getName().equals("getNetworkOperatorName")) {
                param.setResult(mPhoneInfoModel.getNetworkOperatorName);
			} else if (param.method.getName().equals("getNetworkType")) {
                param.setResult(12);
			} else if (param.method.getName().equals("getPhoneType")) {
                param.setResult(1);
			} else if (param.method.getName().equals("getSimCountryIso")) {
                param.setResult(mPhoneInfoModel.getSimCountryIso);
			} else if (param.method.getName().equals("getSimOperator")) {
                param.setResult(mPhoneInfoModel.getSimOperator);
			} else if (param.method.getName().equals("getSimOperatorName")) {
                param.setResult(mPhoneInfoModel.getSimOperatorName);
			} else if (param.method.getName().equals("getSimSerialNumber")) {
                param.setResult(mPhoneInfoModel.getSimSerialNumber);
			} else if (param.method.getName().equals("getSimState")) {
                param.setResult(4);
			} else if (param.method.getName().equals("getSubscriberId")) {
                param.setResult(mPhoneInfoModel.getSubscriberId);
			} else if (param.method.getName().equals("getVoiceMailNumber")) {
                param.setResult(mPhoneInfoModel.getVoiceMailNumber);
			} else if (param.method.getName().equals("getMacAddress")) {
				param.setResult(mPhoneInfoModel.getMacAddress);
			}  else if (param.method.getName().equals("getSSID")) {
				param.setResult(mPhoneInfoModel.getSSID);
			}  else if (param.method.getName().equals("getBSSID")) {
				param.setResult(mPhoneInfoModel.getBSSID);
			} else if (param.method.getName().equals("getString")) {//android_id
				//对应原方法为:Settings.Secure.getString(resolver, name);
				//String androidId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
				if (param.args[1] == Settings.Secure.ANDROID_ID) {
					param.setResult(mPhoneInfoModel.android_id);
				} 
			} else if (param.method.getName().equals("get")) {//序列号
				//对应原方法为:android.os.SystemProperties.get("ro.serialno", "Unknown");
				if (param.args[0] == "ro.serialno") {
					param.setResult(mPhoneInfoModel.serial_no);
				} 
			} else if (param.method.getName().equals("getWidth")) {
				param.setResult(mPhoneInfoModel.getWidth);
			} else if (param.method.getName().equals("getHeight")) {
				param.setResult(mPhoneInfoModel.getHeight);
			} else if (param.method.getName().equals("getDisplayMetrics")) {
				DisplayMetrics metrics = new DisplayMetrics();
				metrics.widthPixels = mPhoneInfoModel.getWidth;
				metrics.heightPixels = mPhoneInfoModel.getHeight;
				param.setResult(metrics);
			} else if (param.method.getName().equals("getMetrics")) {
				if (param.args[0] instanceof DisplayMetrics) {
					((DisplayMetrics)param.args[0]).widthPixels = mPhoneInfoModel.getWidth;
					((DisplayMetrics)param.args[0]).heightPixels = mPhoneInfoModel.getHeight;				
				}
			} else if (param.method.getName().equals("onCreate")) {
				XposedHelpers.setStaticObjectField(Build.VERSION.class, "RELEASE", mPhoneInfoModel.release);
				XposedHelpers.setStaticObjectField(Build.VERSION.class, "SDK", mPhoneInfoModel.sdk);
				XposedHelpers.setStaticObjectField(Build.class, "CPU_ABI", mPhoneInfoModel.cpu_abi);
				XposedHelpers.setStaticObjectField(Build.class, "CPU_ABI2", mPhoneInfoModel.CPU_ABI2);
				XposedHelpers.setStaticObjectField(Build.class, "BRAND", mPhoneInfoModel.brand);
				XposedHelpers.setStaticObjectField(Build.class, "MODEL", mPhoneInfoModel.model);
				XposedHelpers.setStaticObjectField(Build.class, "PRODUCT", mPhoneInfoModel.product);
				XposedHelpers.setStaticObjectField(Build.class, "MANUFACTURER", mPhoneInfoModel.manufacture);
				XposedHelpers.setStaticObjectField(Build.class, "HARDWARE", mPhoneInfoModel.hardware);
				XposedHelpers.setStaticObjectField(Build.class, "FINGERPRINT", mPhoneInfoModel.fingerprint);
				XposedHelpers.setStaticObjectField(Build.class, "SERIAL", mPhoneInfoModel.serial);
				XposedHelpers.setStaticObjectField(Build.class, "DISPLAY", mPhoneInfoModel.DISPLAY);
			} else if (param.method.getName().equals("getExtraInfo")) {
				param.setResult("\"" + mPhoneInfoModel.getSSID + "\"");
			} else if (param.method.getName().equals("getHostAddress") || param.method.getName().equals("getHostName")) {
				if (param.getResult() != null && param.getResult() instanceof String) {
					String ipv6 = (String) param.getResult();
					if (ipv6.toLowerCase().startsWith("fe80::")) {
						if (ipv6.toLowerCase().endsWith("%wlan0")) {
							mPhoneInfoModel.Ipv6 += "%wlan0";
						}
						if (ipv6.toLowerCase().endsWith("%eth0")) {
							mPhoneInfoModel.Ipv6 += "%eth0";
						}
						param.setResult(mPhoneInfoModel.Ipv6.toLowerCase());
					}
				}
			}

            XposedBridge.log("劫持结束啦~");
    	}
    }
}