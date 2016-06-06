package com.qudu.autorun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("autorun", "收到系统启动信息，运行MainActivity");
		Toast.makeText(context, "已經自動啓動了！！！！！！！！！！！！！！！！！！！！！！！", Toast.LENGTH_LONG).show();
		context.startActivity(new Intent(context, MainActivity.class));
	}

}
