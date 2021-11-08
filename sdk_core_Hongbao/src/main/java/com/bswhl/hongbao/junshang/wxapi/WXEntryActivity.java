package com.bswhl.hongbao.junshang.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.framework.common.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;



public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WXBind.api.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq baseReq) {

	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				Log.i("WXTest","onResp OK");

				if(resp instanceof SendAuth.Resp){
					SendAuth.Resp newResp = (SendAuth.Resp) resp;
					//获取微信传回的code
					String code = newResp.code;
					SDKData.setCODE(code);
					Log.i("WXTest","onResp code = "+code);
					//Log.i("WXTest","onResp url = "+SDKData.getRedUrl());
					//Log.i("WXTest","onResp url = "+JunSUtils.buildCommonWebUrl(SDKData.getRedUrl(),true));
					//Log.i("WXTest","onResp url = "+SDKData.getRedUrl());
					//ToastUtil.show(SDKCore.getMainAct(),"绑定成功code="+code+JunSUtils.buildCommonWebUrl(SDKData.getRedUrl(),true), Toast.LENGTH_LONG);
					NoticeDialog hongbaoDialog = new NoticeDialog(SDKCore.getMainAct(), JunSUtils.buildCommonWebUrl(SDKData.getRedUrl(), true), new NoticeDialog.NoticeCallback() {
						@Override
						public void onFinish() {

						}
					});
					hongbaoDialog.show();
				}

				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				Log.i("WXTest","onResp ERR_USER_CANCEL ");
				//发送取消
				ToastUtil.show(SDKCore.getMainAct(),"绑定失败", Toast.LENGTH_LONG);
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				Log.i("WXTest","onResp ERR_AUTH_DENIED");
				ToastUtil.show(SDKCore.getMainAct(),"绑定失败", Toast.LENGTH_LONG);
				//发送被拒绝
				break;
			default:
				Log.i("WXTest","onResp default errCode " + resp.errCode);
				//发送返回
				break;
		}
		finish();
	}
}