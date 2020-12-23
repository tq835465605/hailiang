package hailiang.sys.base.devapiplugin;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import hailiang.constant.SupplierConstant;
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
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 供应商拉黑接口API插件
 * @author leach
 *
 */
public class SupplierBlackentWebApi implements IBillWebApiPlugin {

	private static Log logger = LogFactory.getLog(SupplierBlackentWebApi.class);
	//黑名单默认的采购方（行政组织）
	private static final String NUMBER_DEFAULT = "sub222" ;

	/**
	 * 以下为json的key值定义
	 */
	private static final String data = "data" ;
	private static final String data_supplier = "supplier" ;
	private static final String data_date = "date" ;
	private static final String data_remark = "remark" ;
	private static final String data_entry = "entry" ;
	private static final String data_entry_ename = "ename" ;
	private static final String data_entry_enumber = "enumber" ;
	
	/**
	 * blackenterprise字段
	 * 
	 */
	private static final String ORG = "org" ;
	private static final String NAME = "name" ;
	private static final String NUMBER = "number" ;
	private static final String REMARK = "remark" ;
	private static final String STATUS = "status" ;
	private static final String CREATETIME = "createtime" ;
	private static final String ENUMBER = "enumber" ;
	private static final String ENAME = "ename" ;
	private static final String ENTRYENTRY_E = "entryentity_e";


	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// TODO Auto-generated method stub
		//1.禁用供应商
		//2、创建		
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
			//获取供应商id,时间和remark，？是否全部必填
			String supplierid = jsonData.getString(data_supplier);
			String date = jsonData.getString(data_date);
			String remark = jsonData.getString(data_remark);
			if(StringUtils.isBlank(supplierid)) {
				result.setSuccess(false);
				result.setMessage("传入的供应商supplier不能为空");
				result.setErrorCode(APIConstant.ERRORCODE);
			}
			//查找到对应的供应商
			QFilter supplierFilter = new QFilter(SupplierConstant.id, QCP.equals,supplierid);
			QFilter[] supplierFilters = new QFilter[]{supplierFilter};
			DynamicObject standrd = QueryServiceHelper.queryOne(SupplierConstant.bd_supplier, SupplierConstant.id, supplierFilters );
			DynamicObject suppliser = BusinessDataServiceHelper.loadSingle(standrd.getString(SupplierConstant.id), SupplierConstant.bd_supplier);
			if(suppliser==null) {
				result.setSuccess(false);
				result.setMessage("获取不到对应的供应商，请检查supplier是否正确！");
				result.setErrorCode(APIConstant.ERRORCODE);
				return result;
			}
			OperateOption option  = OperateOption.create();
			//禁用掉该供应商,该方法类似主页的操作代码
			OperationResult operationresult = OperationServiceHelper.executeOperate(CommonConstant.DISABLE, SupplierConstant.bd_supplier, new DynamicObject[] {suppliser},option);
			//如果已经禁用掉的会直接失败

			String supplierName = suppliser.getString(SupplierConstant.name);
			Map<String, Object> retMap = HLCommonUtils.executeOperateResult(operationresult,supplierid+supplierName);

			//创建一个企业供应商管理黑名单的单子
			DynamicObject blackenterprise = BusinessDataServiceHelper.newDynamicObject(SupplierConstant.srm_blackenterprise);
			QFilter orgfilter = new QFilter(SupplierConstant.number, QCP.equals, NUMBER_DEFAULT);
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ADMINORG, CommonConstant.ID, new QFilter[] {orgfilter});

			String number = suppliser.getString(SupplierConstant.societycreditcode);
			System.out.println(number);
			//设置采购方
			blackenterprise.set(ORG,adminorg);
			blackenterprise.set(NAME, supplierName);
			blackenterprise.set(NUMBER, number);
			blackenterprise.set(REMARK, remark);
			//表单状态，默认A已提交
			blackenterprise.set(STATUS, "A");
			blackenterprise.set(CREATETIME, new Date());

			JSONArray jsonArray=jsonData.getJSONArray(data_entry);
			if(jsonArray!=null) {
				//循环处理关联的供应商
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					String ename = jsonObj.getString(data_entry_ename);
					String enumber = jsonObj.getString(data_entry_enumber);
					DynamicObject entry = blackenterprise.getDynamicObjectCollection(ENTRYENTRY_E).addNew();
					entry.set(ENUMBER,enumber);
					entry.set(ENAME,ename);
				}
			}
			OperationResult blackenterpriseSubmitResult = OperationServiceHelper.executeOperate(CommonConstant.SUBMIT, SupplierConstant.srm_blackenterprise, new DynamicObject[]{blackenterprise}, option);
			Map<String, Object>  blackenterpriseSubmitMap = HLCommonUtils.executeOperateResult(blackenterpriseSubmitResult,number+supplierName);
			if ((Boolean)blackenterpriseSubmitMap.get("result")) {
				List<Object> blackenterpriseidList =  blackenterpriseSubmitResult.getSuccessPkIds();
				Long blackenterpriseid = (Long) blackenterpriseidList.get(0);
				DynamicObject newBlackenterprise = BusinessDataServiceHelper.loadSingle(blackenterpriseid,SupplierConstant.srm_blackenterprise);
				OperationResult blackenterpriseAuditResult = OperationServiceHelper.executeOperate(CommonConstant.AUDIT, SupplierConstant.srm_blackenterprise, new DynamicObject[]{newBlackenterprise}, option);
				HLCommonUtils.executeOperateResult(blackenterpriseAuditResult,number+supplierName);			
			}
			else {
				result.setSuccess(false);	
				result.setMessage("企业黑名单提交失败"+blackenterpriseSubmitMap.get("msg"));
				result.setErrorCode(APIConstant.ERRORCODE);
			}

		} catch (Exception e) {
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
	 * 操作执行结果处理
	 * @param opResult
	 * @param supplier
	 * @return
	 *//*
	private Map<String, Object> executeOperateResult(OperationResult opResult,String supplier) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("result", opResult.isSuccess());
		if(!opResult.isSuccess()) {
			//操作失败，记录错误日志		
			StringBuffer errorMsg = new StringBuffer(supplier);
			List<OperateErrorInfo> list = opResult.getAllErrorInfo();
			for (OperateErrorInfo operateErrorInfo : list) {
				errorMsg.append(operateErrorInfo.getMessage());
			}
			retMap.put("msg", errorMsg.toString());
			logger.error(errorMsg.toString());
		}
		return retMap;
	}*/

}
