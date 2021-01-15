package hailiang.sys.base.devapiplugin;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import hailiang.constant.SupplierConstant;
import hailiang.utils.HLCommonUtils;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.cache.TempFileCache;
import kd.bos.cache.tempfile.RedisTempFileCache;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
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

	//黑名单所在应用编码t_meta_bizapp
	private static final String appId = "XV1IC150UR4";
	
	private static final String BILLSTA_AUDIT = "C";
	private static final String BILLSTA_SAVE = "A";
	/**
	 * 以下为json的key值定义
	 */
	private static final String data = "data" ;
	private static final String data_orgid = "orgid" ;
	private static final String data_name= "name" ;
	private static final String data_remark = "remark" ;
	private static final String data_description = "description" ;
	private static final String data_comment = "comment" ;
	private static final String data_number = "number" ;

	private static final String data_attachment = "attachment" ;
	private static final String data_attachment_file = "file" ;
	private static final String data_attachment_filename = "filename" ;

	/**
	 * blackenterprise字段
	 * 
	 */
	private static final String ORG = "org" ;
	private static final String NAME = "name" ;
	private static final String NUMBER = "number" ;
	private static final String REMARK = "remark" ;
	private static final String STATUS = "status" ;
	private static final String DESCRIPTION = "description";
	private static final String CREATETIME = "createtime" ;
	private static final String ENUMBER = "enumber" ;
//	private static final String ENAME = "ename" ;
	private static final String ENTRYENTRY_E = "entryentity_e";
	private static final String BILLSTATUS = "status" ;


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
			String number = jsonData.getString(data_number);
			if(StringUtils.isBlank(number)) {
				result.setSuccess(false);
				result.setMessage("传入的统一社会信用代码不能为空");
				result.setErrorCode(APIConstant.ERRORCODE);
			}
			//查找到黑名单中是否存在
			boolean isexistblankenterprise = isExistBlackEnterprise(number);
			if(isexistblankenterprise)
			{
				return result;
			}
			//查找到对应的供应商
			QFilter supplierFilter = new QFilter(SupplierConstant.societycreditcode, QCP.equals,number);
			QFilter[] supplierFilters = new QFilter[]{supplierFilter};
			DynamicObject standrd = QueryServiceHelper.queryOne(SupplierConstant.bd_supplier, SupplierConstant.id, supplierFilters );
			if(standrd==null) {
				//则创建该供应商，并禁用且拉黑
				DynamicObject suppliser = createSuppliser(jsonData);
				return createBlankEnterprise(suppliser,jsonData,result);
			}
			else {
				DynamicObject suppliser = BusinessDataServiceHelper.loadSingle(standrd.getString(SupplierConstant.id), SupplierConstant.bd_supplier);
				return createBlankEnterprise(suppliser,jsonData,result);
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
	 * 检查社会统一性代码是否在黑名单中
	 * @param societycreditcode
	 * @return
	 */
	private boolean isExistBlackEnterprise(String societycreditcode) {

		//黑名单是否存在的标识
		boolean isExist = false;
		DynamicObject[]  dynamicObjectCollection =  BusinessDataServiceHelper.load(SupplierConstant.srm_blackenterprise,"id,enumber,"+ENTRYENTRY_E, null);
		for(DynamicObject dynamicObject : dynamicObjectCollection) {
			//表头判断
			String exsocietycreditcode =dynamicObject.getString(CommonConstant.NUMBER);
			if(societycreditcode.equals(exsocietycreditcode)) {
				isExist = true;
				break;
			}
			//单据体判断
			DynamicObjectCollection collection = dynamicObject.getDynamicObjectCollection(ENTRYENTRY_E);
			for(DynamicObject glcompany:collection) {
				String enumber = glcompany.getString(ENUMBER);
				if(societycreditcode.equals(enumber)) {
					isExist = true;
					break;
				}
			}
			if(isExist)
				break;
		}
		return isExist;
	}
	
	/**
	 * 创建供应商
	 * @param jsonData
	 * @return
	 */
	private DynamicObject createSuppliser(JSONObject jsonData) throws Exception{
		String name = jsonData.getString(data_name);
		String number = jsonData.getString(data_number);
		DynamicObject supplier = BusinessDataServiceHelper.newDynamicObject(SupplierConstant.bd_supplier);
		supplier.set("name", name);
		supplier.set("societycreditcode", number);
		supplier.set("ctrlstrategy", 5);//控制策略默认 全局共享
		QFilter orgfilter = new QFilter(CommonConstant.NUMBER, QCP.equals, NUMBER_DEFAULT);
		DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ORG, CommonConstant.ID, new QFilter[] {orgfilter});
		adminorg =  BusinessDataServiceHelper.loadSingle(adminorg.getLong(CommonConstant.ID),CommonConstant.BOS_ORG);
		supplier.set("createorg", adminorg);	
		supplier.set("enable", 1);//默认可用状态
		supplier.set(BILLSTATUS, BILLSTA_SAVE);
		OperationResult saveResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, SupplierConstant.bd_supplier, new DynamicObject[]{supplier}, OperateOption.create());
		Map<String, Object> saveMap = HLCommonUtils.executeOperateResult(saveResult,number);
		if ((Boolean)saveMap.get("result")) {
			List<Object> suppliserList =  saveResult.getSuccessPkIds();
			Long suppliserid = (Long) suppliserList.get(0);
			DynamicObject suppliser = BusinessDataServiceHelper.loadSingle(suppliserid, SupplierConstant.bd_supplier);
			suppliser.set(BILLSTATUS, BILLSTA_AUDIT);
			SaveServiceHelper.update(suppliser);
			return suppliser;
		}else {
			throw new Exception("供应商创建失败:"+saveMap.get("msg"));
		}
		
	}

	/**
	 * 禁用供应商并且再拉黑
	 * @param suppliser
	 * @return
	 */
	private ApiResult createBlankEnterprise(DynamicObject suppliser,JSONObject jsonData ,ApiResult result) throws Exception{

		
		String orgid = jsonData.getString(data_orgid);
		String name = jsonData.getString(data_name);
		String comment = jsonData.getString(data_comment);
		String remark = jsonData.getString(data_remark);
		String number = jsonData.getString(data_number);
		String description= jsonData.getString(data_description);

		OperateOption option  = OperateOption.create();
		//禁用掉该供应商,该方法类似主页的操作代码
		OperationResult disableResult = OperationServiceHelper.executeOperate(CommonConstant.DISABLE, SupplierConstant.bd_supplier, new DynamicObject[] {suppliser},option);
		//Map<String, Object> retMap = HLCommonUtils.executeOperateResult(disableResult,name);
		String supplierName = suppliser.getString(SupplierConstant.name);
		//创建一个企业供应商管理黑名单的单子
		DynamicObject blackenterprise = BusinessDataServiceHelper.newDynamicObject(SupplierConstant.srm_blackenterprise);
		QFilter orgfilter = new QFilter(SupplierConstant.number, QCP.equals, orgid);
		DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ADMINORG, CommonConstant.ID, new QFilter[] {orgfilter});

		blackenterprise.set(ORG,adminorg);
		blackenterprise.set(NAME, name);
		blackenterprise.set(NUMBER, number);
		blackenterprise.set(DESCRIPTION, description);
		blackenterprise.set(REMARK, remark);
		blackenterprise.set("isblacklist", true);
		blackenterprise.set("hl01_comment", comment);
		
		//表单状态，默认A已提交
		blackenterprise.set(STATUS, BILLSTA_SAVE);
		blackenterprise.set(CREATETIME, new Date());
		OperationResult saveResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, SupplierConstant.srm_blackenterprise, new DynamicObject[]{blackenterprise}, option);
		Map<String, Object>  saveMap = HLCommonUtils.executeOperateResult(saveResult,supplierName);
		if ((Boolean)saveMap.get("result")) {
			List<Object> blackenterpriseidList =  saveResult.getSuccessPkIds();
			Long blackenterpriseid = (Long) blackenterpriseidList.get(0);
			JSONArray attachmentArray=jsonData.getJSONArray(data_attachment);
			Map<String, Object> attachmentInfo = new HashMap<String, Object>();
			List<Map<String, Object>> attachmentlist = new ArrayList<Map<String, Object>>();
			if(attachmentArray!=null) {
				for (int i = 0; i < attachmentArray.size(); i++) {
					JSONObject jsonObj = attachmentArray.getJSONObject(i);
					//base64
					String file = jsonObj.getString(data_attachment_file);
					String filename = jsonObj.getString(data_attachment_filename);	
					if (file.lastIndexOf(",") > 0) {
						file = file.substring(file.lastIndexOf(",")+1);
					}
					TempFileCache fileCache = new RedisTempFileCache();
					byte[] buffer = DatatypeConverter.parseBase64Binary(file);
					ByteArrayInputStream stream  = new ByteArrayInputStream(buffer); 
					//先保存缓存
					String tempUrl = fileCache.saveAsUrl(filename, stream, 10*1000);
					Map<String, Object> attachmentinfodetl  =new HashMap<String, Object>();
					String uid = "srm-upload-";
					uid+=(new Date().getTime());
					uid+="-";
					attachmentinfodetl.put("uid", uid+i);
					attachmentinfodetl.put("lastModified", new Date().getTime());
					attachmentinfodetl.put("name", filename);
					attachmentinfodetl.put("size", String.valueOf(HLCommonUtils.base64FileSize(file)));
					attachmentinfodetl.put("description", filename);
					attachmentinfodetl.put("url", tempUrl);
					attachmentinfodetl.put("entityNum", String.valueOf(i));
					attachmentlist.add(attachmentinfodetl);
				}
				//这个key值必须是附件面板的标识
				attachmentInfo.put("attachmentpanelap", attachmentlist);
				AttachmentServiceHelper.saveTempAttachments(SupplierConstant.srm_blackenterprise, blackenterpriseid, appId, attachmentInfo);
			}
			
			DynamicObject newblackenterprise = BusinessDataServiceHelper.loadSingle(blackenterpriseid,SupplierConstant.srm_blackenterprise);
			newblackenterprise.set(BILLSTATUS, BILLSTA_AUDIT);
			newblackenterprise.set("auditstatus", BILLSTA_AUDIT);
			SaveServiceHelper.update(newblackenterprise);
			result.setSuccess(true);
		}
		else {
			result.setSuccess(false);
			result.setMessage("保存黑名单失败："+saveMap.get("msg"));
			result.setErrorCode(APIConstant.ERRORCODE);
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
