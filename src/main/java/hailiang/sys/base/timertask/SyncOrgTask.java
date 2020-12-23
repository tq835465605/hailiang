package hailiang.sys.base.timertask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import hailiang.constant.CommonConstant;
import hailiang.utils.HailiangParamHelper;
import hailiang.utils.WebServiceUtil;
import hailiang.utils.XmlUtils;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.org.api.IOrgService;
import kd.bos.org.model.OrgParam;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.util.ExceptionUtils;

/**
 * 组织同步定时任务
 * @author leach
 *
 */
public class SyncOrgTask  extends AbstractTask{

	private static Log logger = LogFactory.getLog(SyncOrgTask.class);

	private static final String HL01_PERORG = "hl01_perorg";//单据分录标识
	private static final String PERORGSELECTER = "number,name,hl01_shortname,hl01_shortname,hl01_supsubcompanyid,hl01_website,hl01_showorder,hl01_code,hl01_canceled,hl01_lastChangdate,hl01_orgpattern,hl01_company";
	private static final String BOS_ADMINORG = "bos_adminorg";//行政组织

	@Override
	public void execute(RequestContext ctx, Map<String, Object> param) throws KDException {
		// 同步分部(公司)信息
		String wsdl = HailiangParamHelper.getParam("OA_HRMWEBSERVICE");
		String companyMethod ="getHrmSubcompanyInfoXML";
		String ipaddress = HailiangParamHelper.getParam("OA_ADDRESSS");
		try {
			String strCompanys = WebServiceUtil.webServicemethod(wsdl, companyMethod, new Object[] {ipaddress});
			System.out.print(strCompanys);	
			logger.info("[组织信息]"+strCompanys);

			Document companysXmlDoc = XmlUtils.stringToXml(strCompanys);
			Element root = XmlUtils.getRootElementByDocument(companysXmlDoc);
			ArrayList<Element>  subCompanyBeans = XmlUtils.getElementsInFatherElement(root);
			//先将所有数据保存到预制表
			for (int i = 0; i < subCompanyBeans.size(); i++) {
				Element subCompanyBean = subCompanyBeans.get(i);
				String subcompanyid = subCompanyBean.element("subcompanyid").getText();
				subcompanyid =CommonConstant.SUB +subcompanyid;
				String shortname = subCompanyBean.element("shortname").getText();
				String fullname = subCompanyBean.element("fullname").getText();
				String supsubcompanyid = subCompanyBean.element("supsubcompanyid").getText();
				String website = subCompanyBean.element("website").getText();
				String showorder = subCompanyBean.element("showorder").getText();
				String code = subCompanyBean.element("code").getText();
				String canceled = subCompanyBean.element("canceled").getText();
				String lastChangdate = subCompanyBean.element("lastChangdate").getText();

				DynamicObject perorg = BusinessDataServiceHelper.newDynamicObject(HL01_PERORG);
				//查找在预制表中是否存在
				QFilter filter = new QFilter("number", QCP.equals, subcompanyid);
				filter = filter.and("hl01_orgpattern",QCP.equals,"1");
				DynamicObject perorgobjectId = QueryServiceHelper.queryOne(HL01_PERORG,"id", filter.toArray());				
				if (perorgobjectId != null) {
					//加载预制表数据
					perorg = BusinessDataServiceHelper.loadSingle(perorgobjectId.getLong("id"), HL01_PERORG);
				}
				perorg.set("number", subcompanyid);
				perorg.set("name", fullname);
				perorg.set("hl01_shortname", shortname);
				perorg.set("hl01_supsubcompanyid", supsubcompanyid);
				perorg.set("hl01_website", website);
				perorg.set("hl01_showorder", showorder);
				perorg.set("hl01_code", code);
				perorg.set("hl01_canceled", canceled);
				perorg.set("hl01_lastChangdate", lastChangdate);
				perorg.set("enable", "1");
				perorg.set("status", "A");
				perorg.set("hl01_orgpattern", "1");//形态公司 

				//保存组织
				SaveServiceHelper.save(new DynamicObject[]{perorg});
				
			}

			//从根节点遍历查询预制表里面的公司
			QFilter filter = new QFilter("number", QCP.equals, HailiangParamHelper.getParam("ORG_ROOT_NUMBER"));
			filter = filter.and("hl01_orgpattern",QCP.equals,"1");
			DynamicObject perorg = BusinessDataServiceHelper.loadSingle(HL01_PERORG, PERORGSELECTER, filter.toArray());
			addOrUpdateOrg(perorg);
			
			//查询所有根部门进行递归同步
			filter = new QFilter("hl01_supsubcompanyid", QCP.equals, "0");
			filter = filter.and("hl01_orgpattern",QCP.equals,"4");
			DynamicObject[] rootDepartments = BusinessDataServiceHelper.load(HL01_PERORG, PERORGSELECTER, filter.toArray());
			for (int i = 0; i < rootDepartments.length; i++) {
				addOrUpdateOrg(rootDepartments[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
			String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
			logger.error("请求组织信息异常"+exception);
		}

	}
	
//	private void updateNumber() {
//		DynamicObjectCollection orgColl = QueryServiceHelper.query(BOS_ADMINORG,"id,number,name,orgpattern,simplename", null);
//		for (int i = 0; i < orgColl.size(); i++) {
//			DynamicObject org = orgColl.get(i);
//			Long orgpattern = org.getLong("orgpattern");
//			String number = org.getString("number");
//			if(orgpattern == 1L) {
//				number = "sub" +number;
//			}else if (orgpattern == 4L) {
//				number = "dep" +number;
//			}
//			List<OrgParam> paramList = new ArrayList<>();
//			OrgParam param = new OrgParam();
//			//查询上级组织id
//			param.setId(org.getLong("id"));
//			param.setName(org.getString("name"));
//			param.setNumber(number);
//			param.setOrgPatternId(orgpattern);//组织形态1公司4部门
//			param.setSimpleName(org.getString("simplename"));
//			param.setDuty(OrgViewType.Admin);
//			Map<String, Object> proMap = new HashMap<>();
//			param.setPropertyMap(proMap);
//			paramList.add(param);
//			IOrgService orgService = ServiceFactory.getService(IOrgService.class);
//			orgService.addOrUpdate(paramList);
//		}
//	}

	/**
	 * 同步组织
	 * @param perorg
	 */
	private void addOrUpdateOrg(DynamicObject perorg) {
		if(perorg != null) {
			String number = perorg.getString("number");
			try {
				Long orgpattern = perorg.getLong("hl01_orgpattern");
				String canceled = perorg.getString("hl01_canceled");
				if(orgpattern == 1L) {
					//同步组织下的所有部门到预制表
					syncDeptToPer(number);
				}
				
				QFilter filter = new QFilter("number", QCP.equals, perorg.getString("number"));
				filter = filter.and("orgpattern", QCP.equals, orgpattern);
				DynamicObject orgObj = QueryServiceHelper.queryOne(BOS_ADMINORG,"id", filter.toArray());	

				List<OrgParam> paramList = new ArrayList<>();
				OrgParam param = new OrgParam();
				//fid 不为0执行更新；为0执行新增
				if (orgObj != null) {
					param.setId(orgObj.getLong("id"));
				}else if(canceled.equals("1")){
					//已取消且从未接入过，则不用接入
					return ;
				}
				//查询上级组织id
				String parentNumber = perorg.getString("hl01_supsubcompanyid");
				Long parentId = 0L; 
				if(!parentNumber.equals("0")) {
					filter = new QFilter("number", QCP.equals, parentNumber);
					filter = filter.and("orgpattern", QCP.equals, orgpattern);
					DynamicObject parentOrgObj = QueryServiceHelper.queryOne(BOS_ADMINORG,"id", filter.toArray());	
					if (parentOrgObj != null) {
						parentId = parentOrgObj.getLong("id");
					}else {
						String msg = "没有找到上级组织信息"+parentNumber;
						System.out.print(msg);
						logger.error(msg);
						return;
					}
				}
				if(parentId == 0L && orgpattern == CommonConstant.ORGPATTERN_DEPT) {
					//顶级部门的上级组织是所属分部字段
					parentNumber = perorg.getString("hl01_company");
					filter = new QFilter("number", QCP.equals, parentNumber);
					filter = filter.and("orgpattern", QCP.equals, CommonConstant.ORGPATTERN_COMPANY);
					DynamicObject parentOrgObj = QueryServiceHelper.queryOne(BOS_ADMINORG,"id", filter.toArray());	
					if (parentOrgObj != null) {
						parentId = parentOrgObj.getLong("id");
					}else {
						String msg = "没有找到上级组织信息"+parentNumber;
						System.out.print(msg);
						logger.error(msg);
						return;
					}
				}
				param.setParentId(parentId);
				//查询是否重名
				String name = perorg.getString("name");
//				filter = new QFilter("number", QCP.not_equals, number);
//				filter = filter.and(new QFilter("name", QCP.equals, name));
//				filter = filter.and("orgpattern", QCP.equals, orgpattern);
//				DynamicObjectCollection orgNameUsed = QueryServiceHelper.query(BOS_ADMINORG,"id", filter.toArray());
//				if(orgNameUsed != null && orgNameUsed.size() > 0) {
//					name += orgNameUsed.size();//重名则在名称后面加序号
//				}

				param.setName(name);
				param.setNumber(number);
				param.setOrgPatternId(orgpattern);//组织形态1公司4部门
				param.setSimpleName(perorg.getString("hl01_shortname"));
				param.setDuty(OrgViewType.Admin);
				Map<String, Object> proMap = new HashMap<>();
				param.setPropertyMap(proMap);
				paramList.add(param);
				IOrgService orgService = ServiceFactory.getService(IOrgService.class);
				//保存或更新
				orgService.addOrUpdate(paramList);
				for (OrgParam result : paramList) {
					System.out.println(number+":"+result.isSuccess()+"========"+result.getName().toString()+result.getMsg());
					if (!result.isSuccess()) {
						String msg = result.getName()+"["+result.getNumber()+"]组织同步异常:"+result.getMsg();
						System.out.print(msg);
						logger.error(msg);
						return;
					}
				}
				
				//封存或解封
				String algoKey = getClass().getName() + ".query_freeze";
				String sql = "select B.FORGID,B.FISFREEZE from  T_ORG_ORG A inner join  T_ORG_Structure B  on A.FID =B.FORGID where A.FNUMBER =? and A.FOrgPatternID =?";
				Object[] params = { number, orgpattern};
				try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("sys"), sql, params)) {
					if (ds.hasNext()) {
						Row row = ds.next();
						Long orgid = row.getLong("FORGID");
						String isfreeze = row.getString("FISFREEZE");
						if(isfreeze.equals("1") && (StringUtils.isEmpty(canceled) || canceled.equals("0") )) {
							//解封
							List<OrgParam> jfList = new ArrayList<>();
							OrgParam jfParam = new OrgParam();
							jfParam.setId(orgid);
							jfList.add(jfParam);
							orgService.unFreeze(jfList);
							for (OrgParam result : jfList) {
								if (!result.isSuccess()) {
									String msg = "["+number+"]解封失败:"+result.getMsg();
									System.out.print(msg);
									logger.error(msg);
								}
							}
						}else if(isfreeze.equals("0") && canceled.equals("1")) {
							//组织封存list
							List<OrgParam> fcList = new ArrayList<>();
							OrgParam fcParam = new OrgParam();
							fcParam.setId(orgid);
							fcList.add(fcParam);
							orgService.freeze(fcList);
							for (OrgParam result : fcList) {
								if (!result.isSuccess()) {
									String msg = "["+number+"]封存失败:"+result.getMsg();
									System.out.print(msg);
									logger.error(msg);
								}
							}
						}
					}
				}

				//递归下级组织进行同步
				filter = new QFilter("hl01_supsubcompanyid", QCP.equals, number);
				filter = filter.and("hl01_orgpattern",QCP.equals,orgpattern);
				DynamicObject[] childOrgs = BusinessDataServiceHelper.load(HL01_PERORG, PERORGSELECTER, filter.toArray());
				if(childOrgs != null) {
					for (int i = 0; i < childOrgs.length; i++) {
						addOrUpdateOrg(childOrgs[i]);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
				logger.error("组织["+number+"]同步异常:"+ExceptionUtils.getExceptionStackTraceMessage(e));
			}
		}
	}
	


	/**
	 * 同步部门到预制表
	 * @param subcompanyid
	 * @throws Exception 
	 */
	private void syncDeptToPer(String subcompanyid) {
		//同步部门到预制表
		String wsdl = HailiangParamHelper.getParam("OA_HRMWEBSERVICE");
		String ipaddress = HailiangParamHelper.getParam("OA_ADDRESSS");
		String departmentMethod ="getHrmDepartmentInfoXML";
		try {
			String strDepartments = WebServiceUtil.webServicemethod(wsdl, departmentMethod, new Object[] {ipaddress,subcompanyid});
			System.out.print(strDepartments);	
			logger.info("[部门信息]"+strDepartments);

			Document deparmetXmlDoc = XmlUtils.stringToXml(strDepartments);
			Element departRoot = XmlUtils.getRootElementByDocument(deparmetXmlDoc);
			ArrayList<Element>  departmentBeans = XmlUtils.getElementsInFatherElement(departRoot);
			for (int j = 0; j < departmentBeans.size(); j++) {
				Element departmentBean = departmentBeans.get(j);
				String departmentid = departmentBean.element("departmentid").getText();
				departmentid = CommonConstant.DEP+departmentid;
				String dptShortname = departmentBean.element("shortname").getText();
				String dptFullname = departmentBean.element("fullname").getText();
				String supdepartmentid = departmentBean.element("supdepartmentid").getText();				
				String dptCanceled = departmentBean.element("canceled").getText();

				DynamicObject perorg = BusinessDataServiceHelper.newDynamicObject(HL01_PERORG);
				//查找在预制表中是否存在
				//String fomartNumber =subcompanyid +"."+ departmentid; //公司id+部门id，OA系统分部和部门是两张表，防止id有重复的情况
				QFilter filter = new QFilter("number", QCP.equals, departmentid);
				filter = filter.and("hl01_orgpattern",QCP.equals,"4");
				DynamicObject perorgobjectId = QueryServiceHelper.queryOne(HL01_PERORG,"id", filter.toArray());				
				if (perorgobjectId != null) {
					//加载预制表数据
					perorg = BusinessDataServiceHelper.loadSingle(perorgobjectId.getLong("id"), HL01_PERORG);
				}
				perorg.set("number", departmentid);
				perorg.set("name", dptFullname);
				perorg.set("hl01_shortname", dptShortname);
				perorg.set("hl01_supsubcompanyid", supdepartmentid);
				perorg.set("hl01_canceled", dptCanceled);
				perorg.set("enable", "1");
				perorg.set("status", "A");
				perorg.set("hl01_orgpattern", "4");//形态部门
				perorg.set("hl01_company", subcompanyid);//部门所属公司					

				//保存部门
				SaveServiceHelper.save(new DynamicObject[]{perorg});
			}
		}catch (Exception e) {
			e.printStackTrace();
			String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
			logger.error(subcompanyid +"请求部门信息异常"+exception);
		}
	}

}
