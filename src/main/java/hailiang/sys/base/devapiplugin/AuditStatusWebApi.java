package hailiang.sys.base.devapiplugin;

import java.text.MessageFormat;
import java.util.Map;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import hailiang.utils.HLCommonUtils;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 审核状态接口
 * @author TonyQ
 *
 */
public class AuditStatusWebApi implements IBillWebApiPlugin{

	private static Log logger = LogFactory.getLog(AuditStatusWebApi.class);

	private static final String data = "data" ;
	private static final String data_formid  = "formid" ;
	private static final String data_number  = "number" ;
	private static final String data_state = "state" ;


	private static final String BIDDOCUMENT = "bid_biddocument_edit";
	private static final String DECISION = "bid_decision";
	private static final String SOUCOMPARE = "sou_compare";
	private static final String CONMPURCONTRACT = "conm_purcontract";
	private static final String PMPURORDERBILL = "pm_purorderbill";
	private static final String PURRECEIVEBILL = "im_purreceivebill";
	private static final String PAYAPPLY = "ap_payapply";
	private static final String MATERIALREQBILL = "im_materialreqbill";
	private static final String PURSUPAGRT = "conm_pursupagrt";
	private static final String XPURCONTRACT = "conm_xpurcontract";
	private static final String SOUCOMPARE_OFFLINE = "hl01_sou_compare_inh";
	private static final String PAYBILL = "cas_paybill";
	//还差定标结果，需要二开
	
	//审核不通过的状态
	private static final String AUDITSTA_F = "F";

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
			String formid = jsonData.getString(data_formid);
			String number = jsonData.getString(data_number);
			String state = jsonData.getString(data_state);
			if(HLCommonUtils.strsEmpty(formid,number,state)) {
				result.setSuccess(false);
				result.setMessage("表头字段不能有空值");
				result.setErrorCode(APIConstant.ERRORCODE);
				return result;
			}
			boolean auditPass = "1".equals(state)?true:false;
			switch (formid) {
			case BIDDOCUMENT://标书
				auditBidDocument(formid,number,auditPass,result);
				break;
			case DECISION://定标提交
				auditDecision(formid,number,auditPass,result);
				break;
			case SOUCOMPARE://询比价单据
				auditSouCompare(formid,number,auditPass,result);
				break;
			case CONMPURCONTRACT://采购合同
				auditPurcontract(formid,number,auditPass,result);
				break;
			case PMPURORDERBILL://采购订单
				auditPurorderbill(formid,number,auditPass,result);
				break;
			case PURRECEIVEBILL://采购收货单
				auditPurreceivebill(formid,number,auditPass,result);
				break;
			case PAYAPPLY: //付款申请单
				auditPayapply(formid,number,auditPass,result);
				break;
			case PAYBILL://付款单
				auditPayBill(formid,number,auditPass,result);
				break;
			case MATERIALREQBILL://领货申请
				auditMaterialreqbill(formid,number,auditPass,result);
				break;
			case PURSUPAGRT://采购补充协议审核
				auditPursupagrt(formid,number,auditPass,result);
				break;
			case XPURCONTRACT://合同变更单
				auditXpurcontract(formid,number,auditPass,result);
				break;
			case SOUCOMPARE_OFFLINE://线下比价单
				auditOfflineSouCompare(formid,number,auditPass,result);
				break;
			default:
				break;
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
	/**
	 * 标书审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditBidDocument(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	/**
	 * 定标审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditDecision(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	/**
	 * 询比价审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditSouCompare(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 采购合同审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPurcontract(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}

	/**
	 * 采购申请单审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPurorderbill(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 采购收货审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPurreceivebill(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus =AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}

	
	/**
	 * 付款申请单审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPayapply(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 付款单审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPayBill(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = "J";
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 领料申请单审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditMaterialreqbill(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 采购补充协议审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditPursupagrt(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	/**
	 * 采购合同变更审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditXpurcontract(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	
	//
	/**
	 * 线下比价单审核
	 * @param number 单据编码
	 * @param auditPass 审核是否通过
	 * @param apiResult 反馈
	 */
	private void auditOfflineSouCompare(String formid,String number,boolean auditPass,ApiResult apiResult) throws Exception{
		String unauditbillstatus = AUDITSTA_F;
		auditCommonBillByBillno(formid,number,auditPass,unauditbillstatus,apiResult);
	}
	/**
	 * 审核通用单据带有billNo，billstatus的单据编码
	 * @param formid 表单标识
	 * @param number 单据编码
	 * @param auditPass 是否审核通过
	 * @param newbillstatus 不通过的单据状态
	 * @param apiResult 反馈结果
	 */
	public void auditCommonBillByBillno(String formid,String number,boolean auditPass,String newbillstatus,ApiResult apiResult) throws Exception{
		//查找number在表单中是否存在
		QFilter filter = new QFilter("billno", QCP.equals, number);
		DynamicObject commonObject = BusinessDataServiceHelper.loadSingle(formid,CommonConstant.ID,filter.toArray());
		if(commonObject==null)
		{
			throw new Exception(MessageFormat.format("找不到编码为{0},单据标识为{1}的对应", new Object[] {number,formid})) ;
		}
		commonObject = BusinessDataServiceHelper.loadSingle(commonObject.getPkValue(), formid);
		if(commonObject!=null) {
			if(auditPass) {
				//执行审核
				OperationResult commonObjectAuditResult = OperationServiceHelper.executeOperate(CommonConstant.AUDIT, formid, new DynamicObject[]{commonObject},OperateOption.create());
				Map<String, Object>  commonAuditMap = HLCommonUtils.executeOperateResult(commonObjectAuditResult,formid);
				if (!(Boolean)commonAuditMap.get("result")) {
					apiResult.setSuccess(false);
					apiResult.setMessage(commonAuditMap.get("msg")+":审核失败");
					apiResult.setErrorCode(APIConstant.ERRORCODE);
				}
			}else {
//				String billstatus = commonObject.getString("billstatus");
//				if("C".equalsIgnoreCase(billstatus)) {
//					//已审核的执行反审核
//					OperationResult commonObjectUnAuditResult = OperationServiceHelper.executeOperate(CommonConstant.UNAUDIT, formid, new DynamicObject[]{commonObject},OperateOption.create());
//					Map<String, Object>  commonUnAuditMap = HLCommonUtils.executeOperateResult(commonObjectUnAuditResult,formid);
//					if (!(Boolean)commonUnAuditMap.get("result")) {
//						apiResult.setSuccess(false);
//						apiResult.setMessage(commonUnAuditMap.get("msg")+":反审核失败");
//						apiResult.setErrorCode(APIConstant.ERRORCODE);
//					}
//				}
				//commonObject.set("billstatus", "F");
				OperationResult result = OperationServiceHelper.executeOperate(CommonConstant.NOAPPRIVE, formid, new DynamicObject[]{commonObject}, OperateOption.create());
				if (!result.isSuccess()) {
					apiResult.setSuccess(false);
					apiResult.setMessage("审核不通过单据失败:"+result.getMessage());
					apiResult.setErrorCode(APIConstant.ERRORCODE);
				}
			}
		}
		else {
			apiResult.setSuccess(false);
			apiResult.setMessage("获取不到对应的单据");
			apiResult.setErrorCode(APIConstant.ERRORCODE);
		}
	}
}
