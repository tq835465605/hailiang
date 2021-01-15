package hailiang.scmc.pm.formplugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.ExceptionUtils;
import kd.bos.util.HttpClientUtils;


/**
 * 采购申请回退理由
 * @author TonyQ
 *
 */
public class BackReasonPlugin extends AbstractBillPlugIn{

	private static Log logger = LogFactory.getLog(BackReasonPlugin.class);
	
	private static final String proback="bar_proback";
	private static final String reqback="bar_reqback";
	private static final String purback="bar_purback";
	private static final String audback="bar_audback";
	private static final String itemkey = "itemkey";
	private static final String KEY_POP_FORM = "hl01_back_bill";
	private final static String KEY_REASON = "hl01_reason";
	private final static String BILLSTATUS = "billstatus";
	private final static String ACTIONID = "backreson";
	
	private final static String URL = "";
	
	@Override
	public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		this.addItemClickListeners(proback,reqback,purback,audback);
	}


	@Override
	public void beforeItemClick(BeforeItemClickEvent evt) {
		// TODO Auto-generated method stub
		super.beforeItemClick(evt);
		
	}

	/**
	 * 控件点击事件触发，通过Key判断触发来源执行不同操作。
	 */
	@Override
	public void itemClick(ItemClickEvent evt) {
		String key = evt.getItemKey();
		// 保存操作
		if (proback.equals(key)||reqback.equals(key)||purback.equals(key)||audback.equals(key)) {
			//do save
			FormShowParameter ShowParameter = new FormShowParameter();
			//设置弹出页面的编码
			ShowParameter.setFormId(KEY_POP_FORM);
			//设置弹出页面标题
			ShowParameter.setCaption("回退理由");
			
			ShowParameter.setCustomParam(itemkey, key);
			//设置页面关闭回调方法
			//CloseCallBack参数：回调插件，回调标识
			ShowParameter.setCloseCallBack(new CloseCallBack(this, ACTIONID));
			//设置弹出页面打开方式，支持模态，新标签等
			ShowParameter.getOpenStyle().setShowType(ShowType.Modal);
			//ShowParameter.addCustPlugin(PopDetailForm.class.getName());
			//弹出页面对象赋值给父页面
			this.getView().showForm(ShowParameter);
		} 
	}

	@Override
	public void beforeDoOperation(BeforeDoOperationEventArgs args) {
		// TODO Auto-generated method stub
		super.beforeDoOperation(args);
        args.setCancel(true);
	}

	@Override
	public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
		super.closedCallBack(closedCallBackEvent);
		//判断标识是否匹配，并验证返回值不为空，不验证返回值可能会报空指针
		if (StringUtils.equals(closedCallBackEvent.getActionId(), ACTIONID) && null != closedCallBackEvent.getReturnData()) {
			//这里返回对象为Object，可强转成相应的其他类型，
			// 单条数据可用String类型传输，返回多条数据可放入map中，也可使用json等方式传输
			@SuppressWarnings("unchecked")
			Map<String, String> returnData = (Map<String, String>) closedCallBackEvent.getReturnData();
			//this.getModel().setValue("hl01_backreson", returnData.get(KEY_REASON));
			JSONObject result = BackToOa(returnData.get(KEY_REASON), returnData.get(itemkey));
			if(result!=null ) {
				if(result.getBooleanValue("operateResult")){
					this.getView().showSuccessNotification("回退成功");
					this.getModel().setValue(BILLSTATUS, "D");
				}
				else {
					this.getView().showErrorNotification("回退失败:"+result.getString("operateMsg"));
				}

			}else {
				this.getView().showErrorNotification("回退失败");
				//this.getModel().setValue(BILLSTATUS, "C");
			}
			DynamicObject dynamicObject  = this.getModel().getDataEntity();
			SaveServiceHelper.update(dynamicObject);
		}
	}
	
	private JSONObject BackToOa(String reason,String opkey)
	{
		String processid = (String) this.getModel().getValue("hl01_processid");
		String backreson  = reason;
		String CZLX  = "0";
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("requestid",processid);
		switch (opkey) {
		case proback:
			CZLX = "0";
			break;
		case reqback:
			CZLX = "1";
			break;
		case purback:
			CZLX = "2";
			break;
		case audback:
			CZLX = "3";
			break;
		default:
			CZLX = "0";
			break;
		}
		body.put("CZLX", CZLX);
		body.put("THYY", backreson);
		try {
			String result = HttpClientUtils.post(URL, null, body);
			JSONObject result_json = JSONObject.parseObject(result);
			return result_json;
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("异常："+ExceptionUtils.getExceptionStackTraceMessage(e1));
			return null;
		}
		
	}
}
