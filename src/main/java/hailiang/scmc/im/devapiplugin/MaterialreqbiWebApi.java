package hailiang.scmc.im.devapiplugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import hailiang.constant.PmpurapplyBillConstant;
import hailiang.utils.HLCommonUtils;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
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
 * 领料申请单，还差附件和测试
 * @author TonyQ
 *
 */
public class MaterialreqbiWebApi implements IBillWebApiPlugin{

	private static Log logger = LogFactory.getLog(MaterialreqbiWebApi.class);
	//行政组织
	private static final String NUMBER_DEFAULT = "sub222" ;
	//默认业务类型，普通领料
	private static final String BIZTYPE_DEFAULT = "320";
	
	//默认币别
	private static final String CURRENCY_DEFAULT = "CNY";
	//默认单据状态
	private static final String BILLSTATUS_DEFAULT = "A";
	
	/**扩展的领料申请单*/
	public static final String im_materialreqbi_ext = "im_materialreqbill";
	/**
	 * 以下为json的key值定义
	 */
	private static final String data = "data" ;
	private static final String data_processNumber = "processNumber" ;
	private static final String data_processId = "processId" ;
	private static final String data_biztime = "biztime" ;
	private static final String data_requser = "requser" ;
	private static final String data_bizdept = "bizdept" ;
	private static final String data_comment = "comment" ;


	private static final String data_entry = "entry" ;
	private static final String data_entry_material = "material" ;
	private static final String data_entry_qty = "qty" ;
	private static final String data_entry_entrycomment = "entrycomment" ;
	
	private static final String data_attachment = "attachment" ;
	private static final String data_attachment_file = "file" ;
	private static final String data_attachment_filename = "filename" ;
	
	/**
	 * 以下hl01_im_materialreqbi_ext需要保存的字段
	 * 
	 */
	private static final String ORG = "org" ;
	private static final String BIZTYPE = "biztype" ;
	private static final String BIZTIME = "biztime" ;
	private static final String BILLSTATUS = "billstatus" ;
	private static final String BIZORG = "bizorg" ;
	private static final String SUPPLYOWNERTYPE = "supplyownertype" ;
	private static final String APPLYDEPT = "applydept";
	private static final String SUPPLYOWNER = "supplyowner";
	private static final String APPLYUSER = "applyuser" ;
    private static final String SETTLECURRENCY = "settlecurrency";
    private static final String COMMENT = "comment";

	private static final String PROCESSNUMBER = "hl01_processnumber";
	private static final String PROCESSID = "hl01_processid";
	
	
	
	private static final String BILLENTRY = "billentry";
	private static final String MATERIAL = "material";
	private static final String MATERIALNAME = "materialname";

	
	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// TODO Auto-generated method stub
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
			String processNumber = jsonData.getString(data_processNumber);
			String processId = jsonData.getString(data_processId);
			String requser = jsonData.getString(data_requser);
			String bizdept = jsonData.getString(data_bizdept);
			String biztime = jsonData.getString(data_biztime);
			String comment = jsonData.getString(data_comment);
	
			DynamicObject materialreqbill = BusinessDataServiceHelper.newDynamicObject(im_materialreqbi_ext);
			//相当于where条件
			QFilter orgfilter = new QFilter(CommonConstant.NUMBER, QCP.equals, NUMBER_DEFAULT);
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ORG, CommonConstant.ID, new QFilter[] {orgfilter});
			materialreqbill.set(ORG,adminorg);
			QFilter billtypefilter = new QFilter(CommonConstant.NUMBER, QCP.equals, BIZTYPE_DEFAULT);
			DynamicObject adminBiztype = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_BIZTYPE, CommonConstant.ID, new QFilter[] {billtypefilter});
			materialreqbill.set(BIZTYPE,adminBiztype);
			materialreqbill.set(BIZTIME, HLCommonUtils.parseDateTime(biztime, HLCommonUtils.DATEPATTERN));
			materialreqbill.set(BILLSTATUS, BILLSTATUS_DEFAULT);
			materialreqbill.set(BIZORG, adminorg);
			materialreqbill.set(SUPPLYOWNERTYPE,"bos_org");
			materialreqbill.set(SUPPLYOWNER, adminorg);
			//假如部门编码缺少dep则补上
			if(!bizdept.startsWith(CommonConstant.DEP)) {
				bizdept = CommonConstant.DEP+bizdept;
			}
			QFilter deptfilter = new QFilter(CommonConstant.NUMBER, QCP.equals,bizdept);
			DynamicObject adminDept = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ADMINORG, CommonConstant.ID, new QFilter[] {deptfilter});
			if(adminDept==null) {
				result.setSuccess(false);
				result.setMessage("获取不到对应的申请人部门，请检查bizdept是否正确！");
				result.setErrorCode(APIConstant.ERRORCODE);
				return result;
			}
			materialreqbill.set(APPLYDEPT, adminDept);
			QFilter userfilter = new QFilter(CommonConstant.NUMBER, QCP.equals, requser);
			DynamicObject adminUser = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_USER, CommonConstant.ID, new QFilter[] {userfilter});
			materialreqbill.set(APPLYUSER, adminUser);
			QFilter currencyfilter = new QFilter(CommonConstant.NUMBER, QCP.equals, CURRENCY_DEFAULT);
			DynamicObject adminCurrency = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_CURRENCY, CommonConstant.ID, new QFilter[] {currencyfilter});
			materialreqbill.set(SETTLECURRENCY, adminCurrency);
			materialreqbill.set(COMMENT, comment);
			
			materialreqbill.set(PROCESSNUMBER, processNumber);
			materialreqbill.set(PROCESSID, processId);
		
			//获取物料明细
			JSONArray entryArray=jsonData.getJSONArray(data_entry);
			DynamicObjectCollection billCollection = materialreqbill.getDynamicObjectCollection(BILLENTRY);
			if(entryArray!=null) {
				//循环处理关联的物料明细
				for (int i = 0; i < entryArray.size(); i++) {
					JSONObject jsonObj = entryArray.getJSONObject(i);
					String material = jsonObj.getString(data_entry_material);
					BigDecimal qty = jsonObj.getBigDecimal(data_entry_qty);
					String entrycomment = jsonObj.getString(data_entry_entrycomment);
					
					DynamicObject billentry = billCollection.addNew();
					//根据物料编码查询物料数据包
					QFilter materialfilter = new QFilter(CommonConstant.NUMBER, QCP.equals, material);
					//先根据物料编码获取物料id，然后去采集里赋值
					DynamicObject adminMaterial = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_MATERIAL, "id,baseunit", new QFilter[] {materialfilter});
					DynamicObject adminMaterialLinventoryInfo = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_MATERIALLINVENTORYINFO,CommonConstant.ID, new QFilter[] {new QFilter(PmpurapplyBillConstant.masterid,QCP.equals, adminMaterial.getString(CommonConstant.ID))});
					//DynamicObject adminBaseUnit = BusinessDataServiceHelper.loadSingle("bd_measureunits","id", new QFilter[] {new QFilter("id",QCP.equals, adminMaterial.get("baseunit"))});
					
					billentry.set(MATERIAL, adminMaterialLinventoryInfo);
					billentry.set(MATERIALNAME, adminMaterial.getString(CommonConstant.NAME));
					//billentry.set(MODEL, adminMaterial.getString("modelnum"));
					//获取单位计量
					DynamicObject baseunit = adminMaterial.getDynamicObject("baseunit");
					billentry.set("unit", baseunit);
					billentry.set("qty", qty);
					billentry.set("baseunit", baseunit);
					billentry.set("baseqty", qty);
					billentry.set("entrycomment", entrycomment);
				}
			}
			
			/*JSONArray attachmentArray=jsonData.getJSONArray(data_attachment);
			//
			if(attachmentArray!=null) {
				for (int i = 0; i < attachmentArray.size(); i++) {
					JSONObject jsonObj = attachmentArray.getJSONObject(i);
					//base64
					String file = jsonObj.getString(data_attachment_file);
					String filename = jsonObj.getString(data_attachment_filename);	
					RedisTempFileCache fileCache = new RedisTempFileCache();
					byte[] buffer  = Base64.decodeFast(file);
					//byte[] buffer = new BASE64Decoder().decodeBuffer(file);
					String tempUrl = fileCache.saveAsFullUrl(filename, buffer, 10*1000);
					System.out.println(tempUrl);
					//AttachmentServiceHelper.saveTempAttachments(formId, pkId, appId, attachmentInfo)()
					//String url = AttachmentServiceHelper.upload(formId, pkId, attachKey, attachments);saveTempToFileService(tempUrl, "/JJVO8XV9MVB", PmpurapplyBillConstant.pm_purapplybill_ext, String.valueOf(purapplybillID), filename);
					//System.out.println(url);
				}
			}*/
			OperateOption option  = OperateOption.create();
			OperationResult materialreqbillSaveResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, im_materialreqbi_ext, new DynamicObject[]{materialreqbill}, option);
			Map<String, Object>  materialreqbillSaveMap = HLCommonUtils.executeOperateResult(materialreqbillSaveResult, processNumber);
			if ((Boolean)materialreqbillSaveMap.get("result")) {
				List<Object> materialreqbillList =  materialreqbillSaveResult.getSuccessPkIds();
				Long materialreqbillID = (Long) materialreqbillList.get(0);
				DynamicObject newMaterialreqbill = BusinessDataServiceHelper.loadSingle(materialreqbillID,im_materialreqbi_ext);
				OperationResult materialreqbillSubmitResult = OperationServiceHelper.executeOperate(CommonConstant.SUBMIT, im_materialreqbi_ext, new DynamicObject[]{newMaterialreqbill}, option);
				Map<String, Object>  materialreqbillSubmitMap = HLCommonUtils.executeOperateResult(materialreqbillSubmitResult,processNumber);	
				if((Boolean)materialreqbillSubmitMap.get("result"))
				{
					List<Object> submaterialreqbillList =  materialreqbillSubmitResult.getSuccessPkIds();
					Long submaterialreqbillID = (Long) submaterialreqbillList.get(0);
					DynamicObject subMaterialreqbill = BusinessDataServiceHelper.loadSingle(submaterialreqbillID,im_materialreqbi_ext);
					OperationResult materialreqbillAuditResult = OperationServiceHelper.executeOperate(CommonConstant.AUDIT, im_materialreqbi_ext, new DynamicObject[]{subMaterialreqbill}, option);
					Map<String, Object>  materialreqbillAuditMap = HLCommonUtils.executeOperateResult(materialreqbillAuditResult,processNumber);	
					if(!(Boolean)materialreqbillAuditMap.get("result")) {
						result.setSuccess(false);	
						result.setMessage("领料申请单审核失败:"+materialreqbillAuditMap.get("msg"));
						result.setErrorCode(APIConstant.ERRORCODE);
					}
				}
				else {
					result.setSuccess(false);	
					result.setMessage("领料申请单提交失败:"+materialreqbillSubmitMap.get("msg"));
					result.setErrorCode(APIConstant.ERRORCODE);
				}
				
			}
			else {
				result.setSuccess(false);	
				result.setMessage("领料申请单保存失败:"+materialreqbillSaveMap.get("msg"));
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
