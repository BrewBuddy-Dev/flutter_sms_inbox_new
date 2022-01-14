package com.juliusgithaiga.flutter_sms_inbox;

import android.content.Context;

import androidx.annotation.NonNull;

import com.juliusgithaiga.flutter_sms_inbox.permissions.Permissions;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterSmsInboxPlugin */
public class FlutterSmsInboxPlugin implements FlutterPlugin, MethodCallHandler {
  private static final String CHANNEL_QUERY = "plugins.juliusgithaiga.com/querySMS";
  private MethodChannel channel;
  private Context context;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_sms_inbox");
    channel.setMethodCallHandler(new FlutterSmsInboxPlugin());

//    registrar.addRequestPermissionsResultListener(Permissions.getRequestsResultsListener());

    /// SMS query
    final MethodChannel querySmsChannel = new MethodChannel(registrar.messenger(), CHANNEL_QUERY, JSONMethodCodec.INSTANCE);
    querySmsChannel.setMethodCallHandler(new FlutterSmsInboxPlugin());
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    this.context = binding.getApplicationContext();
    channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL_QUERY, JSONMethodCodec.INSTANCE);
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    int start = 0;
    int count = -1;
    int threadId = -1;
    String address = null;
    SmsQueryRequest request;
    switch (call.method) {
      case "getInbox":
        request = SmsQueryRequest.Inbox;
        break;
      case "getSent":
        request = SmsQueryRequest.Sent;
        break;
      case "getDraft":
        request = SmsQueryRequest.Draft;
        break;
      default:
        result.notImplemented();
        return;
    }
    if (call.hasArgument("start")) {
      start = call.argument("start");
    }
    if (call.hasArgument("count")) {
      count = call.argument("count");
    }
    if (call.hasArgument("thread_id")) {
      threadId = call.argument("thread_id");
    }
    if (call.hasArgument("address")) {
      address = call.argument("address");
    }
    SmsQueryHandler handler = new SmsQueryHandler(context, result, request, start, count, threadId, address);
//		this.registrar.addRequestPermissionsResultListener(handler);
    handler.handle();
  }
}
