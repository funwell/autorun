package com.qudu.autorun;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	final static int STEP_START = 0;
	final static int STEP_STOP_ANJIAN = 100;
	final static int STEP_RESTART = 200;
	final static int REBOOT = 300;
	final static int UPDATE_AUTORUN = 400;
	final static int SHOW_VERSION_INFO = 500;
	final static String ANJIAN_PACKAGE_NAME = "com.cyjh.mobileanjian";
	final static String TAG = "autorun";
	RootShellCmd cmd = new RootShellCmd();
	static boolean isPC = true;
	static String scriptVer = "0.1";
	static String scriptPath = "/storage/emulated/0/MobileAnjian/Script/";
	static String zipVer = "0.1";
	static String autorunVer = "0.1";
	static String checkAppExistVer = "0.1";
	private TextView mTextView;
	private int anjianVerCode = 0;
	private String unzipPath;
		
	public static final int ThemeColor = Color.rgb(255, 106, 106);
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			
			handleAnjian3_0_0(msg);
		};
	};
/*	
	private void handleAnjian2_6_2(android.os.Message msg) {
		switch (msg.what) {
		case STEP_START:
			Log.e(TAG, "启动按键精灵");
			Utils.runApp(this, ANJIAN_PACKAGE_NAME, null);
//			cmd.runApp(ANJIAN_PACKAGE_NAME, "com.cyjh.mobileanjian.activity.SplashActivity");
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 15 * 1000);
			break;
		case STEP_START + 1:
			// 点击“取消更新”
			cmd.tap(425, 815);
			// 点击“未分类”
			Log.e(TAG, "点击“未分类”");
			cmd.tap(200, 200);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_START + 2:
			// 点击对应的脚本，如“手机版百助脚本”
			Log.e(TAG, "点击对应的脚本，如“手机版百助脚本”");
			cmd.tap(200, 200);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_START + 3:
			// 点击底部的“运行”按钮
			Log.e(TAG, "点击底部的“运行”按钮");
			cmd.tap(200, 1230);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 5 * 1000);
			break;
		case STEP_START + 4:
			// 选择“无限循环”
			Log.e(TAG, "选择“无限循环”");
			if (isPC) {
				cmd.tap(620, 675);
			} else {
				cmd.tap(600, 700);
			}
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 2 * 1000);
			break;
		case STEP_START + 5:
			// 点击“确定”
			Log.e(TAG, "点击“确定”");
			if (isPC) {
				cmd.tap(610, 750);
			} else {
				cmd.tap(600, 800);
			}
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 8 * 1000);
			break;
		case STEP_START + 6:
			// 点击按键精灵缩在右侧的图标
			Log.e(TAG, "点击按键精灵缩在右侧的图标");				
			cmd.tap(700, 400);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 500);
			break;
		case STEP_START + 7:
			// 点击运行图标
			Log.e(TAG, "点击运行图标");
			if (isPC) {
				cmd.tap(550, 375);
			} else {
				cmd.tap(500, 400);
			}				
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 500);
			break;
		case STEP_START + 8:
			// 退回到桌面
			Log.e(TAG, "退回到桌面");
			goHome();
			//3分钟后开始下一循环
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 60 * 1000);
			break;
		case STEP_START + 9:
			// 重新下一个回合
			start(true);
			break;
		case STEP_START + 10:
			break;
		case STEP_START + 11:
			break;
		case STEP_START + 12:
			break;
		case STEP_START + 13:
			break;
		
		case STEP_STOP_ANJIAN:
			cmd.killProcess(ANJIAN_PACKAGE_NAME);
			mHandler.removeMessages(STEP_START);
			mHandler.sendEmptyMessageDelayed(STEP_START, 3 * 1000);
			break;
		case SHOW_VERSION_INFO:
			if (mTextView != null) {
				mTextView.setText(" 脚本版本号：" + scriptVer + "\n autorun版本号：" + autorunVer 
						+ "\n zip包版本号：" + zipVer);
			}
			break;
		case UPDATE_AUTORUN:
			break;
		default:
			break;
		}
		mHandler.removeMessages(msg.what);
		if (msg.what != SHOW_VERSION_INFO) {
			mHandler.sendEmptyMessage(SHOW_VERSION_INFO);
		}
	}
*/
	private void handleAnjian3_0_0(android.os.Message msg) {
		switch (msg.what) {
		case STEP_START:
			toast("启动按键精灵");
			Log.e(TAG, "启动按键精灵");
			Utils.runApp(this, ANJIAN_PACKAGE_NAME, null);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 15 * 1000);
			break;
		case STEP_START + 1:
			Log.e(TAG, "点击“我的”");
			cmd.tap(625, 1225);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_START + 2:
			// 点击“未分类”
			Log.e(TAG, "点击“未分类”");
			cmd.tap(200, 385);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_START + 3:
			// 点击对应的脚本，如“手机版百助脚本”
			Log.e(TAG, "点击对应的脚本，如“手机版百助脚本”");
			cmd.tap(200, 230);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_START + 4:
			// 选择“无限循环”
			Log.e(TAG, "选择“重复运行”");
			if (isPC) {
				cmd.tap(620, 675);
			} else {
				cmd.tap(665, 460);
			}
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 1 * 1000);
			break;
		case STEP_START + 5:
			// 点击底部的“运行”按钮
			Log.e(TAG, "点击底部的“加载”按钮");
			cmd.tap(350, 1210);
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 5 * 1000);
			break;
		case STEP_START + 6:
			// 点击运行图标
			Log.e(TAG, "点击运行图标");
			cmd.tap(360, 840);			
			mHandler.sendEmptyMessageDelayed(msg.what + 1, 1000);
			break;
		case STEP_START + 7:
			// 退回到桌面
			Log.e(TAG, "退回到桌面");
			goHome();
			break;
		case STEP_START + 8:
			break;
		case STEP_START + 9:
			break;
		case STEP_START + 10:
			break;
		case STEP_START + 11:
			break;
		case STEP_START + 12:
			break;
		case STEP_START + 13:
			break;
		
		case STEP_STOP_ANJIAN:
			Utils.reboot();
//			startActivity(new Intent(MainActivity.this, MainActivity.class));
//			mHandler.sendEmptyMessageDelayed(msg.what + 1, 3 * 1000);
			break;
		case STEP_STOP_ANJIAN + 1:
			toast("关闭按键精灵");
			cmd.killProcess(ANJIAN_PACKAGE_NAME);
			mHandler.removeMessages(STEP_START);
			mHandler.sendEmptyMessageDelayed(STEP_START, 3 * 1000);
			break;
		case SHOW_VERSION_INFO:
			if (mTextView != null) {
				mTextView.setText(" 脚本版本号：" + scriptVer + "\n autorun版本号：" + autorunVer 
						+ "\n zip包版本号：" + zipVer);
			}
			break;
		case STEP_RESTART:
			// 重新下一个回合
			start(true);
			break;
		case REBOOT:
			//重启系统
			Utils.reboot();
			break;
		case UPDATE_AUTORUN:
			updateAutorun((Bundle)msg.obj);
			break;
		default:
			break;
		}
		mHandler.removeMessages(msg.what);
		if (msg.what != SHOW_VERSION_INFO) {
			mHandler.sendEmptyMessage(SHOW_VERSION_INFO);
		}
	}
	
	private void toast(String msg) {
//		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	private void updateAutorun(Bundle data) {
		Utils.runApp(this, ConstConfig.PACKAGE_Update, data);
		//更新完关闭自己
		finish();
	}

	
	private void goHome() {
//		Intent home = new Intent(Intent.ACTION_MAIN);
//		home.addCategory(Intent.CATEGORY_HOME);
//		startActivity(home);
		
		startService(new Intent(this, FxService.class));
	}

	private void init() {
		File checkFile = new File("/mnt/shared/");
		if (checkFile.isDirectory()) {
			isPC = true;
		} else {
			isPC = false;
		}
		scriptPath = isPC ? "/storage/emulated/legacy/MobileAnjian/script/" : "/storage/emulated/0/MobileAnjian/Script/";
		//"/sdcard/MobileAnjian/script/" : "/storage/emulated/0/MobileAnjian/Script/";

		initVerInfo();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textview);
		mTextView.setTextColor(ThemeColor);

		stopService(new Intent(this, FxService.class));
				
		wakeUpAndUnlock(MainActivity.this);
		init();
		start(false);
		mHandler.sendEmptyMessage(SHOW_VERSION_INFO);

		rebootHalfDayLater();
	}
	
	/**
	 * 12小时后重启
	 */
	private void rebootHalfDayLater() {
	    Calendar ca = Calendar.getInstance(); 
	    ca.add(Calendar.HOUR, 6);
	    ca.set(Calendar.MINUTE, 3);//整点后的第三分钟才开始重启，以防上一个小时任务未执行完就被重启终止了
	    ca.set(Calendar.SECOND, 0);
	    
	    long rebootTime = ca.getTimeInMillis();
	    long now = System.currentTimeMillis();
	    long delayTime = rebootTime - now;

		mHandler.sendEmptyMessageDelayed(REBOOT, delayTime);
	}
	
	private void start(boolean runOnlyVerChanged) {
		StartRunnable runnable = new StartRunnable();
		runnable.runOnlyVerChanged = runOnlyVerChanged;
		new Thread(runnable).start();
	}
	
	private class StartRunnable implements Runnable {
		public boolean runOnlyVerChanged = false;

		@Override
		public void run() {
			String verScript = MainActivity.scriptVer;

			CountDownLatch latch = null;
			/************ *********************/
			latch = new CountDownLatch(1);
			checkVersion(latch, checkAppExistVer, ConstConfig.UpdateType_CheckAppExist);
			try {
				latch.await();
			} catch (Exception e) {
				e.printStackTrace();
			}		
			
			/************ 检测各apk的zip包更新 ***************/
			latch = new CountDownLatch(1);
			checkVersion(latch, zipVer, ConstConfig.UpdateType_Zip);
			try {
				latch.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			/************ 检测autorun的更新 *************/
			latch = new CountDownLatch(1);
			checkVersion(latch, autorunVer, ConstConfig.UpdateType_AutorunApk);
			try {
				latch.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			/************ 检测脚本更新 *************/
			latch = new CountDownLatch(1);
			checkVersion(latch, scriptVer, isPC ? ConstConfig.UpdateType_PcScript : ConstConfig.UpdateType_PhoneScript);
			try {
				latch.await();
			} catch (Exception e) {
				e.printStackTrace();
			}

			RunningAppProcessInfo processInfo = getMobileAnjianProcessInfo();
			boolean isAnjianRunning = processInfo != null;
			if (!isAnjianRunning || (verScript != null && !verScript.equals(scriptVer)) || !runOnlyVerChanged) {
				if (!TextUtils.isEmpty(verScript) && !verScript.equals(scriptVer)) {
					mHandler.sendEmptyMessageDelayed(STEP_STOP_ANJIAN, 50);
				} else {
					runMobileAnjian();
				}
			}

			//1分钟后开始下一循环
			mHandler.sendEmptyMessageDelayed(STEP_RESTART, 60 * 1000);
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			goHome();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
		
	private String getUpdateRequestUrl(String originVer, int type) {
		final String requestUrl = String.format(ConstConfig.UpdateRequestUrl, originVer, type, getLocalIPAddress());
		
		return requestUrl;
	}
	
	private void initVerInfo() {
		scriptVer = getScriptVer();
		zipVer = getZipVer();
		autorunVer = getAutorunVer();
		checkAppExistVer = getCheckAppExistVer();
	}
	
	/**
	 * 检测是否需要版本更新
	 * @param latch		
	 * @param originVer	原始版本号
	 * @param type		更新类型，见ConstConfig.UpdateType_***
	 */
	private void checkVersion(final CountDownLatch latch, final String originVer, final int type) {
		final String requestUrl = getUpdateRequestUrl(originVer, type);
		new Thread(new Runnable() {

			@Override
			public void run() {
				String jsonData = executeHttpGet(requestUrl);
				if (!TextUtils.isEmpty(jsonData)) {
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(jsonData);
						if (jsonData.contains("Ver")) {
							try {
								String Ver = jsonObject.getString("Ver");
								if (!originVer.equals(Ver)) {
									if (type == ConstConfig.UpdateType_CheckAppExist) {
										checkAppExist();
										refreshVer(type, Ver);
									}
									else if (jsonData.contains("DownUrl")) {
										String downloadUrl = jsonObject.getString("DownUrl");
										if (!TextUtils.isEmpty(downloadUrl) && !downloadUrl.equals("null")) {
											if (type == ConstConfig.UpdateType_Zip) {
												unzipPath = jsonObject.getString("ExtCfg");
											}
											boolean result = doDownloadUpdateFile(type, downloadUrl, Ver);
											if (result) {
												doSomethingWhenDownloaded(type, Ver);
											}
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								LogWriter.log(MainActivity.class, "解析json出错:\n" + e.getMessage() + "\n");
							}
						} else {
							LogWriter.log(MainActivity.class, "没有Ver参数\n");
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
						LogWriter.log(MainActivity.class, "不是json格式\n");
					}
				} else {
					LogWriter.log(MainActivity.class, "服务端返回数据为空\n");
				}
				latch.countDown();
			}
		}).start();
		
	}
	
	private void doSomethingWhenDownloaded(final int type, final String ver) {
		
		if (type == ConstConfig.UpdateType_AutorunApk) {
			if (!ver.equals(autorunVer)) {
				Bundle data = new Bundle();
				data.putString("path", getDownlaodPath(type, ver));
				data.putString("packageName", ConstConfig.PACKAGE_Autorun);
				Message msg = Message.obtain(mHandler, UPDATE_AUTORUN);
				msg.what = UPDATE_AUTORUN;
				msg.obj = data;
				mHandler.sendMessage(msg);
			}
			refreshVer(type, ver);
		} else {
			refreshVer(type, ver);
			if (type == ConstConfig.UpdateType_Zip) {
				if (ConstConfig.installPath.equals(this.unzipPath)) {
					File installFolderFile = new File(ConstConfig.installPath);
					if (installFolderFile != null && installFolderFile.exists()) {
						File[] files = installFolderFile.listFiles();
						for (int i = 0; files != null && i < files.length; i++) {
							if (files[i] != null && files[i].exists()) {
								Utils.installAppWhenRoot(this, files[i].getAbsolutePath());
							}
						}
						
						DataCleanManager.deleteFolderFile(ConstConfig.installPath, true, true);
					}
				}
			}
		}

	}
	
	private String getDownlaodPath(final int type, final String ver) {
		String path = ConstConfig.rootPath;
		switch (type) {
		case ConstConfig.UpdateType_PhoneScript:
		case ConstConfig.UpdateType_PcScript:
			path = scriptPath + "script_" + ver + ".zip";
			break;
		case ConstConfig.UpdateType_AutorunApk:
			path = ConstConfig.apksPath + "autorun.apk"; 
			break;
		case ConstConfig.UpdateType_Zip:
			path = ConstConfig.apksPath + "apks_" + ver + ".zip"; 
			break;
		default:
			break;
		}
		return path;
	}
	
	private String getUnzipPath(final int type) {
		String path = ConstConfig.rootPath;
		switch (type) {
		case ConstConfig.UpdateType_PhoneScript:
		case ConstConfig.UpdateType_PcScript:
			path = scriptPath;
			break;
		case ConstConfig.UpdateType_AutorunApk:
		case ConstConfig.UpdateType_Zip:
			path = ConstConfig.apksPath;
			if (!TextUtils.isEmpty(this.unzipPath)) {
				if (!this.unzipPath.endsWith("/")) {
					this.unzipPath += "/";					
				}
				path = this.unzipPath;
			}
			break;
		default:
			break;
		}
		return path;
	}
	
	private boolean doDownloadUpdateFile(final int type, final String downloadUrl, final String ver) {
		boolean result = false;
		String filePath = getDownlaodPath(type, ver);
		String unzipPath = getUnzipPath(type);
		result = downloadFile(downloadUrl, filePath);
		if (result) {
			File zipFile = new File(filePath);
			if (zipFile != null && zipFile.exists()) {
				if (type == ConstConfig.UpdateType_AutorunApk) {
					result = true;
				} else {
					try {
						ZipControl.readByApacheZipFile(filePath, unzipPath);
	
						result = true;
					} catch (Exception e) {
						result = false;
						e.printStackTrace();
					}
				}
			} else {
				result = false;
			}
		}
		
		return result;
	}
	
	private void refreshVer(final int type, final String ver) {
		// 下载并解压成功，则更新本地版本号
		switch (type) {
		case ConstConfig.UpdateType_PhoneScript:
		case ConstConfig.UpdateType_PcScript:
			scriptVer = ver;
			setScriptVer(ver);
			break;
		case ConstConfig.UpdateType_AutorunApk:
			autorunVer = ver;
			setAutorunVer(ver);
			break;
		case ConstConfig.UpdateType_Zip:
			zipVer = ver;
			setZipVer(ver);
			break;
		case ConstConfig.UpdateType_CheckAppExist:
			checkAppExistVer = ver;
			setCheckAppExistVer(ver);
			break;
		default:
			break;
		}
	}
	
	private void checkScriptVersion(final CountDownLatch latch) {
		scriptVer = getStringValue(MainActivity.this, "Ver");
		final String requestUrl = String.format(ConstConfig.ServerUrlHost + "/Stat.ashx?act=update&ver=%s&pc=%d&host=%s",
				scriptVer, isPC ? 1 : 0, getLocalIPAddress());
		new Thread(new Runnable() {

			@Override
			public void run() {
				// final String proxyHost = getProxyHost();
				String jsonData = executeHttpGet(requestUrl);
//				jsonData = "{\"Ver\":null,\"DownUrl\":null}";
				if (!TextUtils.isEmpty(jsonData)) {
					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(jsonData);
						if (jsonData.contains("Ver")) {
							try {
								String Ver = jsonObject.getString("Ver");
								if (!scriptVer.equals(Ver)) {
									//Toast.makeText(MainActivity.this, "发现新版本："+Ver+", 当前版本："+version, Toast.LENGTH_LONG).show();
									if (jsonData.contains("DownUrl")) {
										String filePath = scriptPath + "script" + Ver + ".zip";
										String downloadUrl = jsonObject.getString("DownUrl");
										downloadFile(downloadUrl, filePath);
										File zipFile = new File(filePath);
										if (zipFile != null && zipFile.exists()) {
											try {
												ZipControl.readByApacheZipFile(filePath, scriptPath);

												// 下载并解压成功，则更新本地版本号
												scriptVer = Ver;
												setStringValue(MainActivity.this, "Ver", Ver);
												//Toast.makeText(MainActivity.this, "更新版本成功, 当前版本："+version, Toast.LENGTH_LONG).show();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
							} catch (Exception e) {
//								Toast.makeText(MainActivity.this, "解析json出错", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
								LogWriter.log(MainActivity.class, "解析json出错:\n" + e.getMessage() + "\n");
							}
						} else {
							//Toast.makeText(MainActivity.this, "没有Ver参数", Toast.LENGTH_SHORT).show();
							LogWriter.log(MainActivity.class, "没有Ver参数\n");
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
						//Toast.makeText(MainActivity.this, "不是json格式", Toast.LENGTH_SHORT).show();
						LogWriter.log(MainActivity.class, "不是json格式\n");
					}
				} else {
//					Toast.makeText(MainActivity.this, "服务端返回数据为空", Toast.LENGTH_SHORT).show();
					LogWriter.log(MainActivity.class, "服务端返回数据为空\n");
				}
				latch.countDown();
			}
		}).start();
	}

	private void runMobileAnjian() {
		mHandler.sendEmptyMessage(STEP_START);
	}

	/*
	 * root权限执行Linux下的Shell指令
	 */
	public class RootShellCmd {

		private OutputStream os;

		/**
		 * 执行shell指令
		 * 
		 * @param cmd
		 *            指令
		 */
		public final void exec(String cmd) {
			try {
				if (os == null) {
					os = Runtime.getRuntime().exec("su").getOutputStream();
				}
				os.write(cmd.getBytes());
				os.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 后台模拟全局按键
		 * 
		 * @param keyCode
		 *            键值
		 */
		public final void keydown(int keyCode) {
			exec("input keyevent " + keyCode + "\n");
		}

		/**
		 * 后台模拟屏幕点击
		 * 
		 * @param x
		 * @param y
		 */
		public final void tap(int x, int y) {
			exec("input tap " + x + " " + y + "\n");
		}

		/**
		 * 后台模拟屏幕滑动
		 * 
		 * @param fromX
		 * @param fromY
		 * @param toX
		 * @param toY
		 * @param swipeTime
		 */
		public final void swipe(int fromX, int fromY, int toX, int toY, int swipeTime) {
			exec("input swipe " + fromX + " " + fromY + " " + toX + " " + toY + " " + swipeTime + "\n");
		}

		/**
		 * 后台模拟文本输入
		 * 
		 * @param content
		 */
		public final void inputText(String content) {
			exec("input text " + content + "\n");
		}

		/**
		 * 启动app
		 * 
		 * @param packageName
		 * @param activityName
		 */
		public final void runApp(String packageName, String activityName) {
			// Intent launchIntent =
			// getPackageManager().getLaunchIntentForPackage(packageName);
			// startActivity(launchIntent);
			exec("am start -n " + packageName + "/" + activityName + "\n");
		}

		/**
		 * 结束进程
		 */
		public final void killProcess(String packageName) {
			exec("am force-stop " + packageName + " \n");
		}
	}

	/**
	 * 获取字符串内容，支持http重定向
	 * 
	 * @param srcUrl
	 * @return
	 */
	private String executeHttpGet(String srcUrl) {
		String result = null;
		URL url = null;
		HttpURLConnection connection = null;
		InputStreamReader in = null;
		try {
			url = new URL(srcUrl);
			int responseCode = HttpStatus.SC_MOVED_PERMANENTLY;
			int redirectCount = 0;
			while (responseCode == HttpStatus.SC_MOVED_PERMANENTLY || responseCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				connection = (HttpURLConnection) url.openConnection();
				connection.setInstanceFollowRedirects(true);
				connection.setConnectTimeout(10 * 1000);// 10秒超时
				connection.connect();
				responseCode = connection.getResponseCode();
				url = connection.getURL();
				if (redirectCount++ >= 10) {
					break;
				}
			}
			if (HttpStatus.SC_OK == responseCode) {
				in = new InputStreamReader(connection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(in);
				StringBuffer strBuffer = new StringBuffer();
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					strBuffer.append(line);
				}
				result = strBuffer.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return result;
	}

	// 获取本地IP函数
	public static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface.getNetworkInterfaces(); mEnumeration
					.hasMoreElements();) {
				NetworkInterface intf = mEnumeration.nextElement();
				for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIPAddr.nextElement();
					// 如果不是回环地址
					if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
						// 直接返回本地IP地址
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			System.err.print("error");
		}
		return null;
	}

	public String getHostName() {
		try {
			InetAddress host = InetAddress.getLocalHost();
			String hostName = host.getHostName();
			String hostAddr = host.getHostAddress();
			String tCanonicalHostName = host.getCanonicalHostName();
			return hostName;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	private String getScriptVer() {
		return getStringValue(this, ConstConfig.scriptVerPath);
	}
	
	private void setScriptVer(String ver) {
		setStringValue(this, ConstConfig.scriptVerPath, ver);		
	}


	private String getZipVer() {
		return getStringValue(this, ConstConfig.zipVerPath);
	}
	
	private void setZipVer(String ver) {
		setStringValue(this, ConstConfig.zipVerPath, ver);
	}

	private String getAutorunVer() {
		return getStringValue(this, ConstConfig.autorunVerPath);
	}
	
	private void setAutorunVer(String ver) {
		setStringValue(this, ConstConfig.autorunVerPath, ver);
	}
	
	private String getCheckAppExistVer() {
		return getStringValue(this, ConstConfig.checkAppExistVerPath);
	}
	
	public void setCheckAppExistVer(String checkAppExistVer) {
		setStringValue(this, ConstConfig.checkAppExistVerPath, checkAppExistVer);
	}
	
	private static String[] getStringsFromFile(String filePath) {
		File configFile = new File(filePath);
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
	
	public static String getStringValue(Context context, String strKey) {
		String[] values = getStringsFromFile(strKey);
		if (values != null && values.length > 0 && !TextUtils.isEmpty(values[0])) {
			return values[0];
		}
		return "";
	}

	public static void setStringValue(Context context, String strKey, String strValue) {
//		SharedPreferences settingPreferences = context.getSharedPreferences("config", 0);
//		SharedPreferences.Editor editor = settingPreferences.edit();
//		editor.putString(strKey, strValue);
//		editor.commit();
		Utils.saveStringToFile(strValue, strKey, true);
	}

	public boolean downloadFile(String urlStr, String filePath) {
		boolean result = false;
		OutputStream output = null;
		try {
			/*
			 * 通过URL取得HttpURLConnection 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
			 * <uses-permission android:name="android.permission.INTERNET" />
			 */
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 取得inputStream，并将流中的信息写入SDCard

			/*
			 * 写前准备 1.在AndroidMainfest.xml中进行权限配置 <uses-permission
			 * android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
			 * 取得写入SDCard的权限 2.取得SDCard的路径：
			 * Environment.getExternalStorageDirectory() 3.检查要保存的文件上是否已经存在
			 * 4.不存在，新建文件夹，新建文件 5.将input流中的信息写入SDCard 6.关闭流
			 */
			// String SDCard = Environment.getExternalStorageDirectory() + "";
			// String pathName = SDCard + "/" + path + "/" + fileName;// 文件存储路径

			File file = new File(filePath);
			InputStream input = conn.getInputStream();
			if (file.exists()) {
				file.delete();
				System.out.println("exits");
			}

			String dir = filePath;
			file.getParentFile().mkdirs();
			file.createNewFile();// 新建文件
			output = new FileOutputStream(file);
			// 读取大文件
			byte[] buffer = new byte[4 * 1024];
			int hasRead = 0;
			while ((hasRead = input.read(buffer)) > 0) {
				output.write(buffer, 0, hasRead);
			}
			output.flush();
			
			result = true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
				System.out.println("success");
			} catch (IOException e) {
				System.out.println("fail");
				e.printStackTrace();
			}
		}
		
		return result;
	}

	/**
	 * gzip解压
	 * 
	 * @param srcBytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] gzipDecompress(byte[] srcBytes) throws IOException {
		if (srcBytes == null) {
			return null;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(srcBytes);
		GZIPInputStream gunzip = new GZIPInputStream(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int hasRead;
		while ((hasRead = gunzip.read(buffer)) > 0) {
			out.write(buffer, 0, hasRead);
		}
		out.flush();
		byte[] dstBytes = out.toByteArray();
		buffer = null;
		out = null;
		return dstBytes;
	}

	/**
	 * gzip压缩
	 * 
	 * @param srcBytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] gzipCompress(byte[] srcBytes) throws IOException {
		if (srcBytes == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(srcBytes);
		gzip.close();
		byte[] result = out.toByteArray();
		out.close();
		return result;
	}

	public void LogDeviceInfo() {
		TelephonyManager tm = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder();
		sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
		sb.append("\nLine1Number = " + tm.getLine1Number());
		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
		sb.append("\nNetworkType = " + tm.getNetworkType());
		sb.append("\nPhoneType = " + tm.getPhoneType());
		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
		sb.append("\nSimOperator = " + tm.getSimOperator());
		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
		sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
		sb.append("\nSimState = " + tm.getSimState());
		sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
		sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
		sb.append("\nBuild.MODEL = " + Build.MODEL);
		sb.append("\nBuild.VERSION.SDK = " + Build.VERSION.SDK);
		sb.append("\nBuild.VERSION.RELEASE = " + Build.VERSION.RELEASE);
		sb.append("\nBuild.BRAND = " + Build.BRAND);
		sb.append("\nBuild.MANUFACTURER = " + Build.MANUFACTURER);

		Log.e("info", sb.toString());
	}

	private String getProxyHost() {
		String proxyhost = null;
		int proxyport = -1;

		int version = Build.VERSION.SDK_INT;
		if (version > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

			proxyhost = System.getProperty("http.proxyHost");

			String port = System.getProperty("http.proxyPort");

			if (!TextUtils.isEmpty(port)) {
				proxyport = Integer.parseInt(port);
			}
		} else {
			proxyhost = android.net.Proxy.getHost(this);
			proxyport = android.net.Proxy.getPort(this);
		}

		if (TextUtils.isEmpty(proxyhost) || proxyport == -1) {
			// 如果没有发现代理设置，则不继续了。
			return null;
		}

		return proxyhost + ":" + proxyport;
	}

	private RunningAppProcessInfo getMobileAnjianProcessInfo() {
		ActivityManager _ActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> list = _ActivityManager.getRunningAppProcesses();
		int i = list.size();
		Log.i("tag", String.valueOf(i));
		RunningAppProcessInfo processInfo = null;
		for (int j = 0; j < list.size(); j++) {
			Log.i("tag", list.get(j).processName);
			if (list.get(j).processName.equals(ANJIAN_PACKAGE_NAME)) {
				processInfo = list.get(j);
			}
		}
		
		return processInfo;
	}
	
	private boolean isAnjianRunning() {
		return getMobileAnjianProcessInfo() != null;
	}
	
	private void killProcess(String packageName) {
		try {
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.killBackgroundProcesses(packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void wakeUpAndUnlock(Context context){
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
		// 解锁
		kl.disableKeyguard();
		// 获取电源管理器对象
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		// 点亮屏幕
		wl.acquire();
		// 释放
		wl.release();
    }

    private void log(String msg) {
    	Log.d(TAG, msg);

        File logf = new File(Environment.getExternalStorageDirectory()
        		+ File.separator + "autorun_log.txt");
        
        LogWriter mLogWriter = null; 
        try {
        	mLogWriter = LogWriter.open(logf.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
		}
    	try {
			mLogWriter.print(MainActivity.class, msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
		}
    	try {
			mLogWriter.close();
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
    }

    private void checkAppExist() {
    	StringBuilder sb = new StringBuilder();
    	String apks[] = new String[] {
        		"/sdcard/system/app/" + "360.apk",
        		"/sdcard/system/app/" + "baizhu.apk",
        		"/sdcard/system/app/" + "Hispace.apk",
        		"/sdcard/system/app/" + "xiaomi.apk",
        		"/sdcard/system/app/" + "yidongmm.apk",
        		"/sdcard/system/app/" + "yingyongbao.apk",
        		"/sdcard/system/app/" + "taobaozhushou.apk",    		
    	};
    	
    	for (int i = 0; i < apks.length; i++) {
    		File file = new File(apks[i]);
    		if (file == null || !file.exists()) {
				sb.append(apks[i]);
				sb.append('\n');
			}
		}
    	
    	String[] packages = new String[] {
    		"com.autostart",
    		"com.qudu.autorun_update",
    	};
    	for (int i = 0; i < packages.length; i++) {
			if (!Utils.isAppInstalled(MainActivity.this, packages[i])) {
				sb.append(packages[i]);
				sb.append('\n');
			}
		}
    	
    	if (sb.length() > 0) {
        	uploadCheckAppExistResult(sb.toString());
		}
    }

	private String getCheckApkExistUrl() {
		return String.format(ConstConfig.UploadLogUrl, "CheckAppExist");
	}
	
	private synchronized void uploadCheckAppExistResult(String error) {
		String path = ConstConfig.scriptLogPath + Utils.getLocalIPAddress() + " " + FxService.getTimeString() + ".txt";
		LogWriter.log(FxService.class, "uploading file : " + path);
		Utils.saveStringToFile(error, path, true);
		final File file = new File(path);
		if (file != null && file.exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					int tryCount = 5;
					while (tryCount-- > 0) {
						boolean result = Utils.uploadFile(getCheckApkExistUrl(), file);
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
}
