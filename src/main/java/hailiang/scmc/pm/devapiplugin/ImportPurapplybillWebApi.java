package hailiang.scmc.pm.devapiplugin;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.constant.CommonConstant;
import hailiang.constant.PmpurapplyBillConstant;
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
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 采购申请接入接口api插件，还差附件
 * @author leach
 *{"data":{"title":"","processNumber":"","processId":"","bizuser":"","dept":"","biztime":"","acceptor":"","costdept":"","company":"","zdsupplier":"","comment":"","entry":[{"seq":"","material":"","materialdesc":"","applyqty":"","budgetprice":"","budgetamount":"","hopedate":"","entryremark":""}],"attachment":[{"file":"","filename":""}]}}
 */

public class ImportPurapplybillWebApi implements IBillWebApiPlugin {

	private static Log logger = LogFactory.getLog(ImportPurapplybillWebApi.class);
	//行政组织
	private static final String NUMBER_DEFAULT = "sub222" ;
	//默认单据类型
	private static final String BILLTYPE_DEFAULT = "pm_PurApplyBill_STD_BT_S";
	//默认币别
	private static final String CURRENCY_DEFAULT = "CNY";
	//默认单据状态
	private static final String BILLSTATUS_DEFAULT = "A";
	
	private static final String appId = "/JJVO8XV9MVB";//采购管理的应用编码
	/**
	 * 以下为json的key值定义
	 */
	private static final String data = "data" ;
	private static final String data_title = "title" ;
	private static final String data_processNumber = "processNumber" ;
	private static final String data_processId = "processId" ;
	private static final String data_bizuser = "bizuser" ;
	private static final String data_dept = "dept" ;
	private static final String data_biztime = "biztime" ;
	private static final String data_acceptor = "acceptor" ;
	private static final String data_costdept = "costdept" ;
	private static final String data_company = "company" ;
	private static final String data_storage = "storage";
	private static final String data_zdsupplier = "zdsupplier" ;
	private static final String data_comment = "comment" ;
	private static final String data_entry = "entry" ;
	private static final String data_entry_seq = "seq" ;
	private static final String data_entry_material = "material" ;
	private static final String data_entry_materialdesc = "materialdesc" ;
	private static final String data_entry_applyqty = "applyqty" ;
	private static final String data_entry_budgetprice = "budgetprice" ;
	private static final String data_entry_budgetamount = "budgetamount" ;
	private static final String data_entry_hopedate = "hopedate" ;
	private static final String data_entry_entryremark = "entryremark" ;
	private static final String data_attachment = "attachment" ;
	private static final String data_attachment_file = "file" ;
	private static final String data_attachment_filename = "filename" ;
	/**
	 * 以下hl01_pm_purapplybill_ext需要保存的字段
	 * 
	 */
	private static final String ORG = "org" ;
	private static final String BIZUSER = "bizuser" ;
	private static final String BILLTYPE = "billtype" ;
	private static final String BIZTIME = "biztime" ;
	private static final String DEPT = "dept";
	private static final String CURRENCY = "currency" ;
	private static final String BILLSTATUS = "billstatus" ;
	private static final String TITLE = "hl01_title" ;
	private static final String PROCESSNUMBER = "hl01_processnumber";
	private static final String PROCESSID = "hl01_processid";
	private static final String ACCEPTOR = "hl01_acceptor" ;
	private static final String ZDSUPPLIER = "hl01_zdsupplier";
	private static final String STORAGE = "hl01_storage";
	
	
	private static final String COMMENT = "hl01_comment";
	private static final String COSTDEPT = "hl01_costdept";
	private static final String COMPANY = "hl01_company";
	
	private static final String BILLENTRY = "billentry";
	private static final String MATERIAL = "material";
	private static final String MATERIALNAME = "materialname";
	private static final String MATERIAL_DESC = "hl01_materialdesc";
	private static final String MODEL = "model";

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
			//获取采购申请单表头
			String title = jsonData.getString(data_title);
			String processNumber = jsonData.getString(data_processNumber);
			String processId = jsonData.getString(data_processId);
			String bizuser = jsonData.getString(data_bizuser);
			String dept = jsonData.getString(data_dept);
			String biztime = jsonData.getString(data_biztime);
			String acceptor = jsonData.getString(data_acceptor);
			String costdept = jsonData.getString(data_costdept);
			String company = jsonData.getString(data_company);
			String zdsupplier = jsonData.getString(data_zdsupplier);
			String comment = jsonData.getString(data_comment);
			String storage = jsonData.getString(data_storage);
			DynamicObject purapplybill = BusinessDataServiceHelper.newDynamicObject(PmpurapplyBillConstant.pm_purapplybill_ext);
			//相当于where条件
			QFilter orgfilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals, NUMBER_DEFAULT);
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ADMINORG, CommonConstant.ID, new QFilter[] {orgfilter});
			purapplybill.set(ORG,adminorg);
			QFilter billtypefilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals, BILLTYPE_DEFAULT);
			DynamicObject adminBilltype = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_BILLTYPE, CommonConstant.ID, new QFilter[] {billtypefilter});
			purapplybill.set(BILLTYPE,adminBilltype);
			purapplybill.set(BIZTIME, HLCommonUtils.parseDateTime(biztime, HLCommonUtils.DATEPATTERN));
			QFilter currencyfilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals, CURRENCY_DEFAULT);
			DynamicObject adminCurrency = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_CURRENCY, CommonConstant.ID, new QFilter[] {currencyfilter});
			purapplybill.set(CURRENCY, adminCurrency);
			purapplybill.set(BILLSTATUS, BILLSTATUS_DEFAULT);
			purapplybill.set(BIZUSER, bizuser);
			//假如部门编码缺少dep则补上
			if(!dept.startsWith(CommonConstant.DEP)) {
				dept = CommonConstant.DEP+dept;
			}
			QFilter deptfilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals,dept);
			DynamicObject adminDept = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ADMINORG, CommonConstant.ID, new QFilter[] {deptfilter});
			if(adminDept==null) {
				result.setSuccess(false);
				result.setMessage("获取不到对应的申请人部门，请检查dept是否正确！");
				result.setErrorCode(APIConstant.ERRORCODE);
				return result;
			}
			purapplybill.set(DEPT, adminDept);
			purapplybill.set(TITLE, title);
			purapplybill.set(PROCESSNUMBER, processNumber);
			purapplybill.set(PROCESSID, processId);
			QFilter userfilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals, acceptor);
			DynamicObject adminUser = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_USER, CommonConstant.ID, new QFilter[] {userfilter});
			purapplybill.set(ACCEPTOR, adminUser);
			purapplybill.set(ZDSUPPLIER, "0".equals(zdsupplier)?false:true);
			purapplybill.set(STORAGE,  "0".equals(storage)?false:true);
			purapplybill.set(COMMENT, comment);
			//待完善
			//purapplybill.set(COSTDEPT, ?);
			//purapplybill.set(COMPANY, ?);
			//获取物料明细
			JSONArray entryArray=jsonData.getJSONArray(data_entry);
			DynamicObjectCollection billCollection = purapplybill.getDynamicObjectCollection(BILLENTRY);
			BigDecimal total = new BigDecimal(0);
			if(entryArray!=null) {
				//循环处理关联的物料明细
				for (int i = 0; i < entryArray.size(); i++) {
					JSONObject jsonObj = entryArray.getJSONObject(i);
					//int seq = jsonObj.getIntValue(data_entry_seq);
					String material = jsonObj.getString(data_entry_material);
					String materialdesc = jsonObj.getString(data_entry_materialdesc);
					BigDecimal applyqty = jsonObj.getBigDecimal(data_entry_applyqty);
					BigDecimal budgetprice = jsonObj.getBigDecimal(data_entry_budgetprice);
					BigDecimal budgetamount = jsonObj.getBigDecimal(data_entry_budgetamount);
					total =total.add(budgetamount);
					String hopedate = jsonObj.getString(data_entry_hopedate);
					String entryremark = jsonObj.getString(data_entry_entryremark);
					DynamicObject billentry = billCollection.addNew();
					//根据物料编码查询物料数据包
					QFilter materialfilter = new QFilter(PmpurapplyBillConstant.number, QCP.equals, material);
					//先根据物料编码获取物料id，然后去采集里赋值
					DynamicObject adminMaterial = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_MATERIAL, "id,baseunit", new QFilter[] {materialfilter});
					DynamicObject adminMaterialPurchaseInfo = BusinessDataServiceHelper.loadSingle(CommonConstant.BD_MATERIALPURCHASEINFO,CommonConstant.ID, new QFilter[] {new QFilter(PmpurapplyBillConstant.masterid,QCP.equals, adminMaterial.getString(CommonConstant.ID))});
					//DynamicObject adminBaseUnit = BusinessDataServiceHelper.loadSingle("bd_measureunits","id", new QFilter[] {new QFilter("id",QCP.equals, adminMaterial.get("baseunit"))});
					
					billentry.set(MATERIAL, adminMaterialPurchaseInfo);
					billentry.set(MATERIALNAME, adminMaterial.getString(CommonConstant.NAME));
					billentry.set(MATERIAL_DESC, materialdesc);
					//billentry.set(MODEL, adminMaterial.getString("modelnum"));
					//获取单位计量
					DynamicObject baseunit = adminMaterial.getDynamicObject("baseunit");
					billentry.set("unit", baseunit);
					billentry.set("applyqty", applyqty);
					billentry.set("qty", applyqty);
					billentry.set("price", budgetprice);
					billentry.set("amount", budgetamount);
					billentry.set("entryreqorg", adminorg);
					billentry.set("entryrecorg", adminorg);
					billentry.set("reqdate", HLCommonUtils.parseDateTime(hopedate, HLCommonUtils.DATEPATTERN));
					billentry.set("entrycomment", entryremark);
				}
			}
			purapplybill.set("hl01_amounttotal", total);
			
			OperateOption option  = OperateOption.create();
			OperationResult purapplybillSubmitResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, PmpurapplyBillConstant.pm_purapplybill_ext, new DynamicObject[]{purapplybill}, option);
			Map<String, Object>  purapplybillSubmitMap = HLCommonUtils.executeOperateResult(purapplybillSubmitResult, title);
			if ((Boolean)purapplybillSubmitMap.get("result")) {
				List<Object> purapplybillList =  purapplybillSubmitResult.getSuccessPkIds();
				Long purapplybillID = (Long) purapplybillList.get(0);
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
						String uid = "pm-upload-";
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
					attachmentInfo.put("attachmentpanel", attachmentlist);
					AttachmentServiceHelper.saveTempAttachments(PmpurapplyBillConstant.pm_purapplybill_ext, purapplybillID, appId, attachmentInfo);
				}
				DynamicObject newPurapplybill = BusinessDataServiceHelper.loadSingle(purapplybillID,PmpurapplyBillConstant.pm_purapplybill_ext);
				newPurapplybill.set(BILLSTATUS, "C");
				SaveServiceHelper.update(newPurapplybill);
				//OperationResult purapplybillAuditResult = OperationServiceHelper.executeOperate(CommonConstant.AUDIT, PmpurapplyBillConstant.pm_purapplybill_ext, new DynamicObject[]{newPurapplybill}, option);
				//HLCommonUtils.executeOperateResult(purapplybillAuditResult,title);	
				
			}
			else {
				result.setSuccess(false);	
				result.setMessage("采购申请单提交失败:"+purapplybillSubmitMap.get("msg"));
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
