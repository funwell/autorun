package com.qudu.autorun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.R.integer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FxService extends Service 
{
	static final int MSG_TRACK_CMD_CHANGE = 0;//跟踪命令变化
	static final int MSG_TRACK_LOG_CHANGE = 1;//跟踪log变化
	static final int MSG_CLEAN_LOG_FILE = 2;//清理apkscript_log
	static final int MSG_UPLOAD_LOG_FILE = 3;//上传apkscript_log.txt文件
	
	//定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	
	TextView mFloatView;	
	private static final String TAG = "FxService";
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_TRACK_CMD_CHANGE:
				parseCMD();
				mHandler.sendEmptyMessageDelayed(MSG_TRACK_CMD_CHANGE, 300);
				break;
			case MSG_TRACK_LOG_CHANGE:
				refreshLogDisplay();
				mHandler.sendEmptyMessageDelayed(MSG_TRACK_LOG_CHANGE, 2500);
				break;

			case MSG_CLEAN_LOG_FILE:
				cleanLogFile();
				mHandler.sendEmptyMessageDelayed(MSG_CLEAN_LOG_FILE, 60 * 60 * 1000);
				break;
				
			case MSG_UPLOAD_LOG_FILE:
				uploadLogFile();
				mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_LOG_FILE, 20 * 60 * 1000);
				break;
				
			default:
				break;
			}
		};
	};
	
	private void cleanLogFile() {
		String path = ConstConfig.scriptLogPath;
		File rootFolder = new File(path);
		if (rootFolder != null && rootFolder.exists()) {
			File[] files = rootFolder.listFiles();
			String todayLogPrefix = getTodayLogNamePrefix();
			String lastdayLogPrefix = getLastDayLogNamePrefix();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.startsWith(ConstConfig.scriptLogPrefix) && fileName.endsWith(ConstConfig.scriptLogPostfix)) {
					if (!fileName.startsWith(todayLogPrefix) && !fileName.startsWith(lastdayLogPrefix)) {
						files[i].delete();
					}
				}
			}
		}
	}

	private String getUploadAutorunErrorUrl() {
		return String.format(ConstConfig.UploadLogUrl, "AutorunError");
	}
	
	private synchronized void uploadAutorunError(String error) {
		String path = ConstConfig.scriptLogPath + Utils.getLocalIPAddress() + " " + getTimeString() + ".txt";
		LogWriter.log(FxService.class, "uploading file : " + path);
		Utils.saveStringToFile(error, path, true);
		final File file = new File(path);
		if (file != null && file.exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int tryCount = 5;
					while (tryCount-- > 0) {
						boolean result = Utils.uploadFile(getUploadAutorunErrorUrl(), file);
						if (result) {
							LogWriter.log(FxService.class, "upload file success!");
							break;
						} else {
							LogWriter.log(FxService.class, "upload file failed!");
						}
					}
					file.delete();
				}
			}).start();
		}
	}
	
	private String getUploadUrl() {
		return String.format(ConstConfig.UploadLogUrl, Utils.getLocalIPAddress());
	}
	
	/**
	 * 把前一个小时的log文件上传到服务器上
	 */
	private void uploadLogFile() {
		String path = ConstConfig.scriptLogPath + getLastHourLogFileName();
		LogWriter.log(FxService.class, "uploading file : " + path);
		final File file = new File(path);
		if (file != null && file.exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int tryCount = 5;
					while (tryCount-- > 0) {
						boolean result = Utils.uploadFile(getUploadUrl(), file);
						if (result) {
							LogWriter.log(FxService.class, "upload file success!");
							break;
						} else {
							LogWriter.log(FxService.class, "upload file failed!");
						}
					}
				}
			}).start();
		}
	}
	
	//今天前缀
	private String getTodayLogNamePrefix() {
	    Calendar ca = Calendar.getInstance();
		int month = ca.get(Calendar.MONTH) + 1;// 获取月份
		int day = ca.get(Calendar.DATE);// 获取日
		
		return ConstConfig.scriptLogPrefix + month + "-" + day;
	}

	//昨天前缀
	private String getLastDayLogNamePrefix() {
	    Calendar ca = Calendar.getInstance(); 
	    ca.add(Calendar.DATE, -1);
		int month = ca.get(Calendar.MONTH) + 1;// 获取月份
		int day = ca.get(Calendar.DATE);// 获取日
		
		return ConstConfig.scriptLogPrefix + month + "-" + day;
	}
	
	//上一个小时的log名
	private String getLastHourLogFileName() {
	    Calendar ca = Calendar.getInstance(); 
	    ca.add(Calendar.HOUR, -1);
		int month = ca.get(Calendar.MONTH) + 1;// 获取月份
		int day = ca.get(Calendar.DATE);// 获取日
		int hour = ca.get(Calendar.HOUR_OF_DAY);
		
		return ConstConfig.scriptLogPrefix + month + "-" + day + "-" + hour + ".txt";
	}

	public static String getTimeString() {
	    Calendar ca = Calendar.getInstance(); 
		int month = ca.get(Calendar.MONTH) + 1;// 获取月份
		int day = ca.get(Calendar.DATE);// 获取日
		int hour = ca.get(Calendar.HOUR_OF_DAY);
		int min = ca.get(Calendar.MINUTE);
		int sec = ca.get(Calendar.SECOND);
		
		return month + "-" + day + "_" + hour + "-" + min + "-" + sec;
	}
	
	private void refreshLogDisplay() {
		String log = getAnjianLog();
		if (mFloatView != null && log != null) {
			mFloatView.setText(log);
			mFloatLayout.invalidate();
		}
	}
	
	/**
	 * 获取按键精灵脚本写的log，log路径为：/sdcard/system/steplog.txt
	 */
	private String getAnjianLog() {
		String result = " ";
		File configFile = new File(ConstConfig.anjianLogPath);
		if (configFile == null || !configFile.exists()) {
			return result;
		}
		InputStream is;
		try {
			is = new FileInputStream(configFile);
			
			if (is != null) {
			    InputStreamReader inputreader = new InputStreamReader(is);
			    BufferedReader buffreader = new BufferedReader(inputreader);
			    try {
			    	result = buffreader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}		    
			    is.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (TextUtils.isEmpty(result)) {
			result = "x";
		}
		return result;
	}
	
	private void parseCMD() {
		String[] values = getConfigValues();
		if (values == null) {
			return;
		}
		try {

			String cmdCode = values[0];//控制指令,0代表安装指令，读第1-3行； 1代表卸载指令，读第1行
			String packageName = values[1];
			String channelId = values[2];
			String apkPath = values[3];
			String phoneInfo = values[1];
			String chlPackageName = values[2];
			if (cmdCode != null) {
				try {
					int code = Integer.parseInt(cmdCode);
					switch (code) {
					case ConstConfig.Code_Install:
						install(packageName, apkPath, channelId);
						break;
					case ConstConfig.Code_Uninstall:
						uninstall(packageName);
						break;
					case ConstConfig.Code_ChangeInfo:
						changeInfo(phoneInfo);
						break;
					case ConstConfig.Code_ClearCache:
						clearCache(packageName);
						break;
					case ConstConfig.Code_CheckAppInstalledState:
						checkAppInstalledState(packageName);
						break;
					case ConstConfig.Code_CheckApkExist:
						checkApkExist(packageName, chlPackageName, false);
						break;
					case ConstConfig.Code_Chmod:
						checkApkExist(packageName, chlPackageName, true);
						break;
					case ConstConfig.Code_Reboot:
						Utils.reboot();
						break;
					case ConstConfig.Code_Shake:
						enableShake();
						break;
					default:
						return;
					}
					resetCMD();
				} catch (Exception e) {
				}
			}			
		} catch (Exception e) {
			Toast.makeText(this, "apk_config配置有误", 0).show();
//			e.printStackTrace();
		}	
	}
	
	private void enableShake() {
		File file = new File(ConstConfig.shakePath);
		if (file != null && !file.exists()) {
			Utils.saveStringToFile("1", ConstConfig.shakePath, true);
		}
	}
	
	private void chmodFile777(String path, boolean needChmod) {
		if (needChmod) {
			Utils.chmodFile777(path);
		}
	}

	private void checkApkExist(String packageName, String chlPackageName, boolean needChmod) {
		if (ConstConfig.PACKAGE_BaiDu.equals(chlPackageName)) {
			String prefix = "BaiduAs";
			File rootDir = Environment.getExternalStorageDirectory();
			File[] subFiles = rootDir.listFiles();
			for (File file : subFiles) {
				if (file.isDirectory() && file.getName().startsWith(prefix)) {
					File[] files = file.listFiles();
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
							if (packageName.equals(dst)) {
								chmodFile777(files[i].getAbsolutePath(), needChmod);
								Utils.setSuccessResult();
								return;
							}
						}
					}
				}
			}
			
			File bdasFile = new File(rootDir + "/bdas/downloads");
			if (bdasFile != null && bdasFile.exists()) {
				File[] files = bdasFile.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}			

			String downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_360.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "360Download";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_YingYongBao.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "Tencent/tassistant/apk";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}			

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
			
			downDir = rootDir.getAbsolutePath() + File.separator + "BaoDownload";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_HuaWei.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "/Android/data/com.huawei.appmarket/Appcache";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}		

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_YiDongMM.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "/mm/download";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}	

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_XiaoMi.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "/MiMarket/files";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}		

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		} else if (ConstConfig.PACKAGE_TAOBAO.equals(chlPackageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "appcenter/downloader/apk";
			File downFolderFile = new File(downDir);
			File[] files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}

			downDir = rootDir.getAbsolutePath() + File.separator + "Download";
			downFolderFile = new File(downDir);
			files = downFolderFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().endsWith(".apk")) {
						String dst = Utils.getApkPackageName(this, files[i].getAbsolutePath());
						if (packageName.equals(dst)) {
							chmodFile777(files[i].getAbsolutePath(), needChmod);
							Utils.setSuccessResult();
							return;
						}
					}
				}
			}
		}
		
		Utils.refreshResult(ConstConfig.Result_2);
	}
	
	
	private void checkAppInstalledState(String packageName) {
		boolean result = Utils.isAppInstalled(this, packageName);
		Utils.refreshResult(result ? ConstConfig.Result_Success : ConstConfig.Result_2);
	}
	
	private void changeInfo(String phoneInfo) {
		if (PhoneInfoManager.mPhoneInfoModel == null) {
			PhoneInfoManager.mPhoneInfoModel = new PhoneInfoModel();
		}
		Utils.saveStringToFile(phoneInfo, ConstConfig.infoPath, true);
		boolean result = PhoneInfoManager.mPhoneInfoModel.init(phoneInfo);
		Utils.refreshResult(result ? ConstConfig.Result_Success : ConstConfig.Result_Failed);
		Log.e(TAG, "change phone info " + (result ? "success" : "failed"));
	}
	
	private void clearCache(String packageName) {
		if (packageName.equals(ConstConfig.PACKAGE_BaiDu)) {
			clearBaiduCache();
		} else if (packageName.equals(ConstConfig.PACKAGE_360)) {
			clear360Cache();
		} else if (packageName.equals(ConstConfig.PACKAGE_YingYongBao)) {
			clearYingyongbaoCache();
		} else if (packageName.equals(ConstConfig.PACKAGE_YiDongMM)) {
			clearYidongmmCache();
		} else if (packageName.equals(ConstConfig.PACKAGE_HuaWei)) {
			clearHuaweiCache();
		} else if (packageName.equals(ConstConfig.PACKAGE_XiaoMi)) {
			clearXiaomiCache();
		} else if (packageName.equals(ConstConfig.PACKAGE_TAOBAO)) {
			clearTaobaoCache();
		}
		Utils.runRootCommand("cd /data/local/tmp\n" + "rm -r *");
		Utils.refreshResult(ConstConfig.Result_Success);
	}
	
	private void clearBaiduCache() {
		String[] folders = new String[] {
				"/data/data/com.baidu.appsearch/",
				"/storage/emulated/0/Android/data/com.baidu.appsearch/cache/",
				"/storage/emulated/0/backups/system/",
				"/storage/emulated/0/backups/.SystemConfig/",
				"/storage/emulated/0/baidu/AppSearch/", 
				"/storage/emulated/0/baidu/tempdata/",
				"/storage/emulated/0/.BD_SAPI_CACHE/",
				"/storage/emulated/0/bdas/",
				"/storage/emulated/0/.dlprovider/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			// Utils.runRootCommand("rm -r " + dir);
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}
		String[] files = new String[] { "/storage/emulated/0/baidu/.cuid", "/storage/emulated/0/appsearch.cfg", };
		for (String path : files) {
			File file = new File(path);
			if (file != null && file.exists()) {
				file.delete();
			}
		}
		
		String prefix = "BaiduAs";
		File rootDir = Environment.getExternalStorageDirectory();
		File[] subFiles = rootDir.listFiles();
		for (File file : subFiles) {
			if (file.isDirectory() && file.getName().startsWith(prefix)) {
				DataCleanManager.deleteFolderFile(file.getAbsolutePath(), true, true);
				// Utils.runRootCommand("rm -r " + file.getAbsolutePath());
			}
		}

		try {
			Settings.System.putString(getContentResolver(), "bd_setting_i", "");
			Settings.System.putString(getContentResolver(), "com.baidu.deviceid", "");
			Settings.System.putString(getContentResolver(), "com.baidu.deviceid.v2", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void clear360Cache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.qihoo.appstore/",
				"/storage/emulated/0/360Download/",
				"/storage/emulated/0/360Log/",
				"/storage/emulated/0/360/",
				"/storage/emulated/0/Android/data/com.qihoo.gameassist/",
				"/data/data/com.qihoo.appstore/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}	
	}
	
	private void clearYingyongbaoCache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.tencent.android.qqdownloader/",
				"/storage/emulated/0/tencent/tassistant/",
				"/storage/emulated/0/DCIM/tassistant/",
				"/storage/emulated/0/Download/",
				"/storage/emulated/0/BaoDownload/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}	
		String[] files = new String[] {
				"/storage/emulated/0/.qm_guid",
		};
		for (String path : files) {
			File file = new File(path);
			if (file != null && file.exists()) {
				file.delete();
			}
		}	
	}
	
	private void clearHuaweiCache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.huawei.appmarket2/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, false, false);
			}
		}		
	}
	
	private void clearXiaomiCache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.xiaomi.market/cache/",
				"/storage/emulated/0/MiMarket/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}		
	}
	
	private void clearYidongmmCache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.aspire.mm/cache/",
				"/storage/emulated/0/mm/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}		
	}

	private void clearTaobaoCache() {
		String[] folders = new String[] {
				"/storage/emulated/0/Android/data/com.taobao.appcenter/",
				"/storage/emulated/0/.UTSystemConfig/",
				"/storage/emulated/0/.DataStorage/",
				"/storage/emulated/0/Download/",
				};
		for (String dir : folders) {
			if (dir.equals("/storage/emulated/0/Download/")) {
				DataCleanManager.deleteFolderFile(dir, false, true);
			} else {
				DataCleanManager.deleteFolderFile(dir, true, true);
			}
		}				
	}
	
	private void install(String packageName, String apkPath, String channelId) {
		if (!TextUtils.isEmpty(channelId)) {
			Utils.modifySMReaderChannelId(this, channelId);
		}
		if (TextUtils.isEmpty(apkPath) || !new File(apkPath).exists()) {
			Toast.makeText(this, "找不到对应的apk文件", Toast.LENGTH_LONG).show();
			Utils.refreshResult(ConstConfig.Result_Failed);
		} else {
			Utils.runRootCommand("cd /data/data\n" + "rm -r " + packageName);
			Utils.runRootCommand("cd /data/local/tmp\n" + "rm -r *");
			boolean success = Utils.installAppWhenRoot(this, apkPath);
			if (!success) {
				success = Utils.isAppInstalled(this, packageName);
			}
			if (!success) {
				uploadAutorunError("未安装成功！");
			}
			if (ConstConfig.PACKAGE_HuaWei.equals(packageName)) {
				File rootDir = Environment.getExternalStorageDirectory();
				String downDir = rootDir.getAbsolutePath() + File.separator + "/Android/data/com.huawei.appmarket";
				String downDir2 = rootDir.getAbsolutePath() + File.separator + "/Android/data/com.huawei.appmarket2";
				String appCache2 = downDir2 + File.separator + "AppCache/";
				File file2 = new File(downDir2);
				File file = new File(downDir);
				File fileAppCache2 = new File(appCache2);
				try {
					if (file != null && file.exists() && file.isFile()) {
						file.delete();
					}
					if (file2 != null && file2.exists()) {
						if (fileAppCache2 != null && !fileAppCache2.exists()) {
							fileAppCache2.mkdir();
						}
						file2.renameTo(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Utils.refreshResult(success ? ConstConfig.Result_Success : ConstConfig.Result_Failed);
		}
	}
	
	private void uninstall(String packageName) {
		if (ConstConfig.PACKAGE_HuaWei.equals(packageName)) {
			File rootDir = Environment.getExternalStorageDirectory();
			String downDir = rootDir.getAbsolutePath() + File.separator + "/Android/data/com.huawei.appmarket";
			String downDir2 = rootDir.getAbsolutePath() + File.separator + "/Android/data/com.huawei.appmarket2";
			File file = new File(downDir);
			File file2 = new File(downDir2);
			if (file2 != null && file2.exists()) {
				file2.delete();
			}
			if (file != null && file.exists()) {
				file.renameTo(file2);
			}
		}
		if (Utils.isAppInstalled(this, packageName)) {
			boolean success = Utils.uninstallAppWhenRoot(this, packageName);
			if (!success) {
				success = !Utils.isAppInstalled(this, packageName);
			}
			if (!success) {
				uploadAutorunError("未卸载成功！");
			}
			//卸载之后清除干净
			Utils.runRootCommand("cd /data/data\n" + "rm -r " + packageName);
			Utils.runRootCommand("cd /data/local/tmp\n" + "rm -r *");
			Utils.refreshResult(success ? ConstConfig.Result_Success : ConstConfig.Result_Failed);
		} else {
			//卸载之后清除干净
			Utils.runRootCommand("cd /data/data\n" + "rm -r " + packageName);
			Utils.runRootCommand("cd /data/local/tmp\n" + "rm -r *");
			Utils.refreshResult(ConstConfig.Result_Success);
		}
	}
	
	private void resetCMD() {
		File configFile = new File(ConstConfig.configPath);
		if (configFile == null || !configFile.exists()) {
			return ;
		}
		Utils.saveStringToFile(String.valueOf(ConstConfig.Code_Unknown), ConstConfig.configPath, true);
	}
	
	private String[] getConfigValues() {
		File configFile = new File(ConstConfig.configPath);
		if (configFile == null || !configFile.exists()) {
			return null;
		}
		String[] resultValues = new String[20];
		InputStream is;
		try {
			is = new FileInputStream(configFile);
			
			if (is != null) {
			    InputStreamReader inputreader = new InputStreamReader(is);
			    BufferedReader buffreader = new BufferedReader(inputreader);
			    try {
			    	for (int i = 0; i < resultValues.length; i++) {
			    		resultValues[i] = buffreader.readLine();
			    		if (resultValues[i] == null) {
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}		    
			    is.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return resultValues;
	}
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		createFloatView();
		mHandler.sendEmptyMessageDelayed(MSG_TRACK_CMD_CHANGE, 100);
		mHandler.sendEmptyMessageDelayed(MSG_TRACK_LOG_CHANGE, 100);
		mHandler.sendEmptyMessageDelayed(MSG_CLEAN_LOG_FILE, 10 * 1000);
		mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_LOG_FILE, 20 * 1000);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void createFloatView()
	{
		wmParams = new WindowManager.LayoutParams();
		//获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//设置window type
		wmParams.type = LayoutParams.TYPE_PHONE; 
		//设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888; 
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = 
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
          LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
          ;
        //浮动窗口按钮
        TextView tv = new SelfTextView(getApplicationContext());
        tv.setSingleLine();
        tv.setEllipsize(TruncateAt.END);
        tv.setTextSize(8);
        tv.setText("正在测试神马看书下载正在测试神马看书下载正在测试神马看书下载正在测试神马看书下载正在测试神马看书下载");
        tv.setTextColor(MainActivity.ThemeColor);
        tv.setBackgroundColor(Color.BLACK);
        mFloatView = tv;

        // 设置悬浮窗口长宽数据
        wmParams.width = LayoutParams.WRAP_CONTENT;
        wmParams.height = LayoutParams.WRAP_CONTENT;
        
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM; 
        
        DisplayMetrics dm = getResources().getDisplayMetrics();
//        // 以屏幕左上角为原点，设置x、y初始值
//        wmParams.x = dm.widthPixels - wmParams.width;
//        wmParams.y = dm.heightPixels - wmParams.height;

        /*
        //设置悬浮窗口长宽数据  
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        */
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = new LinearLayout(this);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        
        Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());      

        mFloatLayout.setBackgroundColor(Color.BLACK);
        mFloatLayout.addView(new View(getApplicationContext()), new LinearLayout.LayoutParams(10, 10));
        mFloatLayout.addView(mFloatView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
//        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
//				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
//				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
        Log.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight()/2);
        /*
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new OnTouchListener() 
        {			
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
				wmParams.x = (int) event.getRawX();
				//Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
				Log.i(TAG, "RawX" + event.getRawX());
				Log.i(TAG, "X" + event.getX());
				//25为状态栏的高度
	            wmParams.y = (int) event.getRawY();
	           // Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
	            Log.i(TAG, "RawY" + event.getRawY());
	            Log.i(TAG, "Y" + event.getY());
	             //刷新
	            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				return false;
			}
		});	
		*/
        /*
        mFloatView.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				Toast.makeText(FxService.this, "onClick", Toast.LENGTH_SHORT).show();
			}
		});
		*/
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		if(mFloatLayout != null)
		{
			mWindowManager.removeView(mFloatLayout);
		}
	}
	
	
	private class SelfTextView extends TextView {

		public SelfTextView(Context context) {
			super(context);
		}
		
		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			return false;
		}
	}
}
