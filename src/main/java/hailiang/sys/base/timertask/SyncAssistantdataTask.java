package hailiang.sys.base.timertask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import hailiang.constant.CommonConstant;
import hailiang.utils.HLCommonUtils;
import hailiang.utils.HailiangParamHelper;
import hailiang.utils.JDBCUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 同步oa的基础资料视图
 * 配置：去开发平台设置hl01_paramset这个表
 * 其中，FZJD = TTYY,CGLX,HTXZ,HTLX //如果需要新增则，后面自动对齐格式
 * 然后在hl01_paramset表中设置相应的代码与json格式
 * 
 * @author TonyQ
 *
 */
public class SyncAssistantdataTask  extends AbstractTask{
	
	private static Log logger = LogFactory.getLog(SyncAssistantdataTask.class);

	private static final String Orcale_IP = "Orcale_IP";
	private static final String Orcale_Database = "Orcale_Database";
	private static final String Orcale_User = "Orcale_User";
	private static final String Orcale_Password = "Orcale_Password";
	private static final String CQ_YZ = "CQ_YZ";
	private static final String CQ_XZK = "CQ_XZK";
	private static final String CQ_JDXZ = "CQ_JDXZ";
	private static final String SPJD_WFID = "SPJD_WFID";

	private static final String ASSISTANTDATAGROUP = "bos_assistantdatagroup";//基础资料分类
	private static final String ASSISTANTDATADETAIL = "bos_assistantdata_detail";//基础资料明细
	private static final String ASSISTANT_NUMBER = "cqyz";
	private static final String BIZCLOUD = "bos_devportal_bizcloud";
	private static final String VIEWSCHEMA = "bos_org_viewschema";
	private static final String PUR = "PUR";
	@Override
	public void execute(RequestContext arg0, Map<String, Object> arg1)  {
		Connection connection = null;
		try {// TODO Auto-generated method stub
			String ip = HailiangParamHelper.getParam(Orcale_IP);
			String database = HailiangParamHelper.getParam(Orcale_Database);
			String user = HailiangParamHelper.getParam(Orcale_User);
			String pwd = HailiangParamHelper.getParam(Orcale_Password);
			connection = JDBCUtils.getOrcaleConn(ip, database, user, pwd);
			//同步印章名称
			String st_cqyz = HailiangParamHelper.getParam(CQ_YZ);
			String sql = "select ID,YZMC from "+st_cqyz;
			ResultSet resultSet=JDBCUtils.select(connection,sql,null);
			DynamicObject assistantdatagroup = newAssistantdataGroup(ASSISTANT_NUMBER,"印章名称");
			assistantdatagroup = BusinessDataServiceHelper.loadSingle(assistantdatagroup.getLong(CommonConstant.ID),ASSISTANTDATAGROUP);
			while(resultSet.next()) {
				String id = resultSet.getString("ID");
				String yzmc = resultSet.getString("YZMC");
				newOrUpdateAssistantdataDetail(id,yzmc,assistantdatagroup);
			}
			JDBCUtils.closeResultSet(resultSet);
			//同步审批节点
			String st_cqjdxz = HailiangParamHelper.getParam(CQ_JDXZ);
			String st_wfid = HailiangParamHelper.getParam(SPJD_WFID);
			sql = "select ID,NODENAME from "+st_cqjdxz + " where WFID in ("+st_wfid+")";
			ResultSet spjdResultSet=JDBCUtils.select(connection,sql,null);
			assistantdatagroup = newAssistantdataGroup("jdxz","审批节点");
			assistantdatagroup = BusinessDataServiceHelper.loadSingle(assistantdatagroup.getLong(CommonConstant.ID),ASSISTANTDATAGROUP);
			while(spjdResultSet.next()) {
				String id = spjdResultSet.getString("ID");
				String nodename = spjdResultSet.getString("NODENAME");
				newOrUpdateAssistantdataDetail(id,nodename,assistantdatagroup);
			}
			JDBCUtils.closeResultSet(spjdResultSet);
			
			//同步其他OA选择框
			String st_cqxzk = HailiangParamHelper.getParam(CQ_XZK);//表
			String st_fzjd = HailiangParamHelper.getParam("FZJD");//辅助资料分组
			if(StringUtils.isBlank(st_fzjd)) {
				throw new Exception("辅助资料分组不能为空");
			}
			String[] fzjds = st_fzjd.split(",");
			for(String fzjd:fzjds) {
				String fzjd_param = HailiangParamHelper.getParam(fzjd);
				JSONObject fzjd_json = JSONObject.parseObject(fzjd_param);
				String fzjd_name = fzjd_json.getString("name");
				String fzjd_wfid = fzjd_json.getString("wfid");
		        assistantdatagroup = newAssistantdataGroup(fzjd,fzjd_name);
		        assistantdatagroup = BusinessDataServiceHelper.loadSingle(assistantdatagroup.getLong(CommonConstant.ID),ASSISTANTDATAGROUP);
				sql = "select SELECTVALUE,SELECTNAME from "+st_cqxzk + " where FIELDNAME = '"+fzjd+"' and WFID in ("+fzjd_wfid+")";
				ResultSet fzjdResultSet=JDBCUtils.select(connection,sql,null);
				while(fzjdResultSet.next()) {
					String id = fzjdResultSet.getString("SELECTVALUE");
					String nodename = fzjdResultSet.getString("SELECTNAME");
					newOrUpdateAssistantdataDetail(id,nodename,assistantdatagroup);
				}
				JDBCUtils.closeResultSet(fzjdResultSet);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
			logger.error("请求辅助异常："+exception);
		}
		finally {	
			JDBCUtils.closeConnection(connection);
		}
	}
	
	/**
	 * 查询在辅助资料里有没有分组
	 * @param number
	 * @param formid
	 * @return
	 */
	private DynamicObject isExsitAssistantGroup(String number,String name,String formid) {
		QFilter assistantfilter = new QFilter("number", QCP.equals, number);
		assistantfilter.and(new QFilter("name", QCP.equals, name));
		DynamicObject bizcloud=QueryServiceHelper.queryOne(formid, CommonConstant.ID, assistantfilter.toArray());
		if(bizcloud!=null) {
			return bizcloud;
		}else {
			return null;
		}
	}
	
	/**
	 * 查询在辅助资料里有没有明细
	 * @param number
	 * @param formid
	 * @return
	 */
	private DynamicObject isExsitAssistantDetail(String number,DynamicObject assistantdatagroup,String formid) {
		QFilter assistantfilter = new QFilter("number", QCP.equals, number);
		assistantfilter.and(new QFilter("group.name", QCP.equals, assistantdatagroup.getString(CommonConstant.NAME)));
		DynamicObject bizcloud=QueryServiceHelper.queryOne(formid, CommonConstant.ID, assistantfilter.toArray());
		if(bizcloud!=null) {
			return bizcloud;
		}else {
			return null;
		}
	}
	
	
	/**
	 * 建立辅助资料分组
	 * @return
	 */
	private DynamicObject newAssistantdataGroup(String number,String name)
	{
		DynamicObject assistantdatagroup=isExsitAssistantGroup(number,name,ASSISTANTDATAGROUP);
		if(assistantdatagroup==null) {
			assistantdatagroup = BusinessDataServiceHelper.newDynamicObject(ASSISTANTDATAGROUP);
			QFilter filter = new QFilter("number", QCP.equals, PUR);
			DynamicObject bizcloud=BusinessDataServiceHelper.loadSingle(BIZCLOUD, CommonConstant.ID, filter.toArray());
			assistantdatagroup.set("fbizcloudid", bizcloud);
			assistantdatagroup.set("number", number);
			assistantdatagroup.set("name", name);
			QFilter orgfilter = new QFilter("number", QCP.equals, "sub222");
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ORG, CommonConstant.ID, new QFilter[] {orgfilter});
			assistantdatagroup.set("createorg", adminorg);
			QFilter viewfilter = new QFilter("number", QCP.equals, "16");
			DynamicObject viewschema = BusinessDataServiceHelper.loadSingle(VIEWSCHEMA, CommonConstant.ID, new QFilter[] {viewfilter});
			assistantdatagroup.set("ctrlview", viewschema);
			SaveServiceHelper.save(new DynamicObject[] {assistantdatagroup});
		}
		return assistantdatagroup;
	}
	
	private void newOrUpdateAssistantdataDetail(String number,String name,DynamicObject assistantdatagroup)
	{
		DynamicObject assistantdatagdetail=isExsitAssistantDetail(number,assistantdatagroup,ASSISTANTDATADETAIL);
		if(assistantdatagdetail ==null) {		
			assistantdatagdetail = BusinessDataServiceHelper.newDynamicObject(ASSISTANTDATADETAIL);	
			assistantdatagdetail.set("number", number);
			assistantdatagdetail.set("name", name);
			assistantdatagdetail.set("group", assistantdatagroup);
			QFilter orgfilter = new QFilter("number", QCP.equals, "sub222");
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ORG, CommonConstant.ID, new QFilter[] {orgfilter});
			assistantdatagdetail.set("createorg", adminorg);
			QFilter viewfilter = new QFilter("number", QCP.equals, "16");
			DynamicObject viewschema = BusinessDataServiceHelper.loadSingle(VIEWSCHEMA, CommonConstant.ID, new QFilter[] {viewfilter});
			assistantdatagdetail.set("ctrlview", viewschema);	
			assistantdatagdetail.set("createtime", new Date());
			assistantdatagdetail.set("enable","1");
			assistantdatagdetail.set("status","C");
			assistantdatagdetail.set("level",1);
			OperationResult saveResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, ASSISTANTDATADETAIL, new DynamicObject[]{assistantdatagdetail}, OperateOption.create());
			boolean issuccess = saveResult.isSuccess();
			Map<String, Object>  saveMap = HLCommonUtils.executeOperateResult(saveResult, "");
			System.out.println(issuccess);	
			if(!issuccess) {
				System.out.println(saveMap.get("msg"));	
				logger.error("请求辅助异常："+saveMap.get("msg"));
			}
				
		}
		else {
			assistantdatagdetail = BusinessDataServiceHelper.loadSingle(assistantdatagdetail.getLong(CommonConstant.ID),ASSISTANTDATADETAIL);
			OperationResult deleteResult = OperationServiceHelper.executeOperate("delete", ASSISTANTDATADETAIL, new DynamicObject[]{assistantdatagdetail}, OperateOption.create());
			boolean issuccess = deleteResult.isSuccess();
			Map<String, Object>  deleteMap = HLCommonUtils.executeOperateResult(deleteResult, "");
			System.out.println(issuccess);	
			if(!issuccess) {
				System.out.println(deleteMap.get("msg"));	
				logger.error("请求辅助异常："+deleteMap.get("msg"));
				return;
			}
			assistantdatagdetail = BusinessDataServiceHelper.newDynamicObject(ASSISTANTDATADETAIL);	
			assistantdatagdetail.set("number", number);
			assistantdatagdetail.set("name", name);
			assistantdatagdetail.set("group", assistantdatagroup);
			QFilter orgfilter = new QFilter("number", QCP.equals, "sub222");
			DynamicObject adminorg = BusinessDataServiceHelper.loadSingle(CommonConstant.BOS_ORG, CommonConstant.ID, new QFilter[] {orgfilter});
			assistantdatagdetail.set("createorg", adminorg);
			QFilter viewfilter = new QFilter("number", QCP.equals, "16");
			DynamicObject viewschema = BusinessDataServiceHelper.loadSingle(VIEWSCHEMA, CommonConstant.ID, new QFilter[] {viewfilter});
			assistantdatagdetail.set("ctrlview", viewschema);	
			assistantdatagdetail.set("createtime", new Date());
			assistantdatagdetail.set("enable","1");
			assistantdatagdetail.set("status","C");
			assistantdatagdetail.set("level",1);
			OperationResult saveResult = OperationServiceHelper.executeOperate(CommonConstant.SAVE, ASSISTANTDATADETAIL, new DynamicObject[]{assistantdatagdetail}, OperateOption.create());
			issuccess = saveResult.isSuccess();
			Map<String, Object>  saveMap = HLCommonUtils.executeOperateResult(saveResult, "");
			System.out.println(issuccess);	
			if(!issuccess) {
				System.out.println(saveMap.get("msg"));	
				logger.error("请求辅助异常："+saveMap.get("msg"));
			}
//			assistantdatagdetail.set("number", number);
//			assistantdatagdetail.set("name", name);
//			assistantdatagdetail.set("group", assistantdatagroup);
//			assistantdatagdetail.set("enable","1");
//			assistantdatagdetail.set("status","C");
//			assistantdatagdetail.set("level",1);		
//			SaveServiceHelper.update(new DynamicObject[] {assistantdatagdetail});		
		}
	}

}
