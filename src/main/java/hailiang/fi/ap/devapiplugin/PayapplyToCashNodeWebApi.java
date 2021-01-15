package hailiang.fi.ap.devapiplugin;

import java.util.Map;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.api.ApiResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 是否到达出纳节点
 * @author TonyQ
 *
 */
public class PayapplyToCashNodeWebApi  implements IBillWebApiPlugin{
	
	private static Log logger = LogFactory.getLog(PayapplyToCashNodeWebApi.class);

	private static final String data = "data" ;
	private static final String data_billno  = "billno" ;
	private static final String ap_payapply = "ap_payapply";
	
	private static final String ISCASHIER = "hl01_iscashier";
	
	@Override
	public ApiResult doCustomService(Map<String, Object> params) {

		ApiResult result = new ApiResult();
		//设置接口返回结果为成功
		result.setSuccess(true);
		result.setMessage(APIConstant.MESSAGE_SUCCESS);
		try {
			//解析传入参数
			String jsonStr = JSONUtils.toJSONString(params);
			//记录传入参数日志
			logger.info("OA传入参数："+jsonStr);
			JSONObject rootObj = JSONObject.parseObject(jsonStr);
			//获取传入参数data数据
			JSONObject jsonData = (JSONObject)rootObj.get(data);	
			String billno = jsonData.getString(data_billno);
			QFilter filter = new QFilter("billno", QCP.equals, billno);
			DynamicObject payapply = QueryServiceHelper.queryOne(ap_payapply,CommonConstant.ID,filter.toArray());
			if(payapply!=null) {
				payapply = BusinessDataServiceHelper.loadSingle(payapply.getString(CommonConstant.ID),ap_payapply);
				boolean iscashier = payapply.getBoolean(ISCASHIER);
				if(!iscashier) {
					payapply.set(ISCASHIER, true);
				}
				SaveServiceHelper.update(payapply);
			}else {
				result.setSuccess(false);
				result.setMessage("找不到对应的付款申请单据");
				result.setErrorCode(APIConstant.ERRORCODE);
			}
			
		}
		catch (Exception e) {
			//异常情况设置接口返回失败
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			result.setErrorCode(APIConstant.ERRORCODE);
			//记录错误日志信息
			logger.error("异常："+ExceptionUtils.getExceptionStackTraceMessage(e));
		}	
		return result;
	}

}
