package hailiang.pur.bid.operationplugin;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import hailiang.constant.CommonConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.entity.plugin.args.InitOperationArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.HttpClientUtils;

/**
 * 招标立项提交给oa审核
 * @author TonyQ
 *
 */
public class ProjectSubmitToOAOp extends AbstractOperationServicePlugIn{

	private static Log logger = LogFactory.getLog(ProjectSubmitToOAOp.class);
	private static final String BID_PROJECT = "bid_project";
	private static final String workflowId = "";//流程编码，相当于单据标识

	private static final String KEY_TITLE = "hl01_title";
	private static final String KEY_BILLNO = "billno";
	private static final String KEY_PURTYPE = "purtype";
	private static final String KEY_PROCESSID = "hl01_pmprocessid";
	private static final String KEY_NAME = "name";
	private static final String KEY_REQUESTER = "hl01_requester";
	private static final String KEY_AMOUNT = "hl01_amount";
	private static final String KEY_REQUESTCOMPANY = "hl01_requestcompany";
	private static final String KEY_REQUESTDEPT = "hl01_requestdept";
	private static final String KEY_REQUESTUSER = "hl01_requestuser";

	private static final String  bussbussinessField = "CQDH";
	private static final boolean createNew =  true;
	private static final boolean backToCreate = false;

	private static final String URL = "http://zz.ehailiang.com:8080/oa/workFlowManager/createWorkFlow";


	@Override
	public void afterExecuteOperationTransaction(AfterOperationArgs e) {
		// TODO Auto-generated method stub
		super.afterExecuteOperationTransaction(e);
	}

	@Override
	public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
		// TODO Auto-generated method stub
		super.beforeExecuteOperationTransaction(e);
		DynamicObject[] dynameobjs = e.getDataEntities();
		for(DynamicObject obj : dynameobjs){
			try {
				DynamicObject bidproject=BusinessDataServiceHelper.loadSingle(obj.get(CommonConstant.ID),BID_PROJECT);
				String title = bidproject.getString(KEY_TITLE);
				String bussbussinessId = bidproject.getString(KEY_BILLNO);
				String CGXQBH = bidproject.getString(KEY_PROCESSID);
				String ZBXM = bidproject.getString(KEY_NAME);
				String ZBBH = bidproject.getString(KEY_BILLNO);
				//申请人,工号
				DynamicObject requester =  bidproject.getDynamicObject(KEY_REQUESTER);
				String SQR = requester.getString(CommonConstant.NUMBER);
				//预算金额
				String YSJE = bidproject.getBigDecimal(KEY_AMOUNT).toString();
				//采购类型
				DynamicObject purtype = bidproject.getDynamicObject(KEY_PURTYPE);
				String CGLX = purtype.getString(CommonConstant.NUMBER);
				DynamicObject requestcompany =  bidproject.getDynamicObject(KEY_REQUESTCOMPANY);
				String CGXQFB = requestcompany.getString(CommonConstant.NUMBER);
				DynamicObject requestdept = bidproject.getDynamicObject(KEY_REQUESTDEPT);
				String CGXQBM = requestdept.getString(CommonConstant.NUMBER);
				DynamicObject requestuser = bidproject.getDynamicObject(KEY_REQUESTUSER);
				String CGXQDJR = requestuser.getString(CommonConstant.NUMBER);

				Map<String, Object> body = new HashMap<String, Object>();
				body.put("workflowId", workflowId);
				body.put("title", title);
				body.put("bussbussinessId", bussbussinessId);
				body.put("bussbussinessField", bussbussinessField);
				body.put("createNew", createNew);
				body.put("backToCreate", backToCreate);
				JSONObject fieldjson = new JSONObject();
				fieldjson.put("CGXQBH", CGXQBH);
				fieldjson.put("ZBXM", ZBXM);
				fieldjson.put("ZBBH", ZBBH);
				fieldjson.put("SQR", SQR);
				fieldjson.put("YSJE", YSJE);
				fieldjson.put("CGLX", CGLX);
				fieldjson.put("CGXQFB", CGXQFB);
				fieldjson.put("CGXQBM", CGXQBM);
				fieldjson.put("CGXQDJR", CGXQDJR);
				body.put("fieldValues", fieldjson);
				//body.put("fieldValues", workflowId);
				String result = HttpClientUtils.post(URL, null, body);
				JSONObject result_json = JSONObject.parseObject(result);
				if(result_json==null || !result_json.getBooleanValue("operateResult")) {
					e.setCancel(true);
					e.setCancelMessage("标书审核流程创建失败:"+result_json.getString("operateMsg"));
				}

			}catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error(e1.getMessage());
				e.setCancel(true);
				e.setCancelMessage("标书审核流程创建失败:"+e1.getMessage());
			}
		}
		//e.setCancel(true);
	}

	@Override
	public void beginOperationTransaction(BeginOperationTransactionArgs e) {
		// TODO Auto-generated method stub
		super.beginOperationTransaction(e);
	}

	@Override
	public void endOperationTransaction(EndOperationTransactionArgs e) {
		// TODO Auto-generated method stub
		super.endOperationTransaction(e);
	}

	@Override
	public void initialize(InitOperationArgs e) {
		// TODO Auto-generated method stub
		super.initialize(e);
	}

}
