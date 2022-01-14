package com.juliusgithaiga.flutter_sms_inbox;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import com.juliusgithaiga.flutter_sms_inbox.permissions.Permissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import static io.flutter.plugin.common.MethodChannel.Result;
import static io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

enum SmsQueryRequest {
	Inbox,
	Sent,
	Draft;

	Uri toUri() {
		if (this == Inbox) {
			return Uri.parse("content://sms/inbox");
		} else if (this == Sent) {
			return Uri.parse("content://sms/sent");
		} else {
			return Uri.parse("content://sms/draft");
		}
	}
}

class SmsQueryHandler {
	private final Context context;
	private final String[] permissionsList = new String[]{Manifest.permission.READ_SMS};
	private MethodChannel.Result result;
	private SmsQueryRequest request;
	private int start = 0;
	private int count = -1;
	private int threadId = -1;
	private String address = null;

	SmsQueryHandler(Context context, MethodChannel.Result result, SmsQueryRequest request,
					int start, int count, int threadId, String address) {
		this.context = context;
		this.result = result;
		this.request = request;
		this.start = start;
		this.count = count;
		this.threadId = threadId;
		this.address = address;
	}

	void handle() {
//		if (permissions.checkAndRequestPermission(permissionsList, Permissions.SEND_SMS_ID_REQ)) {
		querySms();
//		}
	}

	private JSONObject readSms(Cursor cursor) {
		JSONObject res = new JSONObject();
		for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
			try {
				if (cursor.getColumnName(idx).equals("address") || cursor.getColumnName(idx).equals("body")) {
					res.put(cursor.getColumnName(idx), cursor.getString(idx));
				}
				else if (cursor.getColumnName(idx).equals("date") || cursor.getColumnName(idx).equals("date_sent")) {
					res.put(cursor.getColumnName(idx), cursor.getLong(idx));
				}
				else {
					res.put(cursor.getColumnName(idx), cursor.getInt(idx));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	private void querySms() {
		ArrayList<JSONObject> list = new ArrayList<>();
		Cursor cursor = context.getContentResolver().query(this.request.toUri(), null, null, null, null);
		if (cursor == null) {
			result.error("#01", "permission denied", null);
			return;
		}
		if (!cursor.moveToFirst()) {
			cursor.close();
			result.success(list);
			return;
		}
		do {
			JSONObject obj = readSms(cursor);
			try {
				if (threadId >= 0 && obj.getInt("thread_id") != threadId) {
					continue;
				}
				if (address != null && !obj.getString("address").equals(address)) {
					continue;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (start > 0) {
				start--;
				continue;
			}
			list.add(obj);
			if (count > 0) {
				count--;
			}
		} while (cursor.moveToNext() && count != 0);
		cursor.close();
		result.success(list);
	}
}