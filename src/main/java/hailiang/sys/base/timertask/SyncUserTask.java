package hailiang.sys.base.timertask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.IUserService;
import kd.bos.permission.model.UserParam;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 人员同步定时任务
 * @author leach
 *
 */
public class SyncUserTask extends AbstractTask {

	private static Log logger = LogFactory.getLog(SyncOrgTask.class);

	private static final String BOS_ADMINORG = "bos_adminorg";//行政组织
	private static final String[] ENABLESTATE = {"1","2","3"};//可用人员状态
	private static final String BOS_USER = "bos_user";//用户表单标识
	private static final String USER_SYNC_DATE = "USER_SYNC_DATE";//参数设置-人员最好更新时间
	private static final String HL01_JOBTITLE = "hl01_jobtitle";//岗位基础资料

	@Override
	public void execute(RequestContext ctx, Map<String, Object> param) throws KDException {
		// TODO Auto-generated method stub
		String wsdl = HailiangParamHelper.getParam("OA_HRMWEBSERVICE");
		String userMethod ="getHrmUserInfoXML";
		String ipaddress = HailiangParamHelper.getParam("OA_ADDRESSS");
		String syncDate = HailiangParamHelper.getParam(USER_SYNC_DATE);
		
		try {
			//查询所有公司
			QFilter filter = new QFilter("orgpattern.id",QCP.equals,CommonConstant.ORGPATTERN_COMPANY);
			DynamicObject[] companyOrgs = BusinessDataServiceHelper.load(BOS_ADMINORG, "id,number", filter.toArray());
			for (int i = 0; i < companyOrgs.length; i++) {
				//根据公司编码调用接口查询人员				
				String number = companyOrgs[i].getString("number");	
				number = number.replaceAll(CommonConstant.SUB, "");
				String lastChangeDate = null;
				if(StringUtils.isNotEmpty(syncDate)) {
					lastChangeDate = syncDate;
				}
				String strUsers = WebServiceUtil.webServicemethod(wsdl, userMethod, new Object[] {ipaddress,null,number,null,null,lastChangeDate});
				System.out.print(strUsers);	
				logger.info("[人员信息]"+strUsers);
				//人员信息转换为XML
				Document usersXmlDoc = XmlUtils.stringToXml(strUsers);
				Element root = XmlUtils.getRootElementByDocument(usersXmlDoc);
				ArrayList<Element>  userBeans = XmlUtils.getElementsInFatherElement(root);
				//遍历人员进行同步
				for (int j = 0; j < userBeans.size(); j++) {
					Element userBean = userBeans.get(j);
					String workcode = userBean.element("workcode").getText();
					String status = userBean.element("status").getText();//Status：状态:（正式-1、临时-2、试用延期-3、解聘-4、离职-5、退休-6、无效-7、删除-10）
					try {
						Boolean enable = Arrays.asList(ENABLESTATE).contains(status);
						if(!enable) {
							//在人员信息里面没有则不需要接入
							filter = new QFilter("number", QCP.equals, workcode);
							DynamicObject findUser = QueryServiceHelper.queryOne(BOS_USER,"id", filter.toArray());				
							if (findUser == null) {
								continue;
							}
						}
						String lastname = userBean.element("lastname").getText();
						String certificatenum = userBean.element("certificatenum").getText();
						String birthday = userBean.element("birthday").getText();
						String loginid = userBean.element("loginid").getText();
						String mobile = userBean.element("mobile").getText();
						String email = userBean.element("email").getText();
						String departmentid = userBean.element("departmentid").getText();
						departmentid = CommonConstant.DEP+departmentid;
						String jobtitleid = userBean.element("jobtitle").getText();
						String sex = userBean.element("sex").getText();					

						//构建用户API参数
						List<UserParam> paramList = new ArrayList<>();
						UserParam user = new UserParam();
						//查询用户是否存在
						filter = new QFilter("number", QCP.equals, workcode);
						DynamicObject findUser = QueryServiceHelper.queryOne(BOS_USER,"id,isforbidden", filter.toArray());		
						if (findUser != null) {
							user.setId(findUser.getLong("id"));
						}
						// user.setCustomId(123456780L);
						Map<String, Object> dataMap = new HashMap<>();
						dataMap.put("number", workcode);
						dataMap.put("name", lastname);
						dataMap.put("username", loginid);
						dataMap.put("usertype", "1");
						dataMap.put("phone", mobile);
						dataMap.put("email", email);
						dataMap.put("idcard", certificatenum);
						dataMap.put("birthday", birthday);
						dataMap.put("gender", sex.equals("男")?"1":"2");

						// 职位分录
						List<Map<String, Object>> posList = new ArrayList<>();
						Map<String, Object> entryentity = new HashMap<>();
						// 设置部门编码
						Map<String, Object> dptNumMap = new HashMap<>();
						dptNumMap.put("number", departmentid);
						entryentity.put("dpt", dptNumMap);

						//查询岗位
						filter = new QFilter("number", QCP.equals, jobtitleid);
						DynamicObject jobtitleQueryObj = QueryServiceHelper.queryOne(HL01_JOBTITLE,"id,hl01_fullname", filter.toArray());				
						if (jobtitleQueryObj != null) {
							//加载OA岗位表数据
							entryentity.put("position", jobtitleQueryObj.getString("hl01_fullname"));
						}
						entryentity.put("isincharge", false);
						entryentity.put("ispartjob", false);
						entryentity.put("seq", 1);
						posList.add(entryentity);

						dataMap.put("entryentity", posList);
						user.setDataMap(dataMap);
						paramList.add(user);

						// 业务调用可以参照接口名，以下举例说明微服务调用方式
						IUserService userService = (IUserService) ServiceFactory.getService(IUserService.class);
						userService.addOrUpdate(paramList);

						// 判断执行结果
						for (UserParam result : paramList) {
							if (!result.isSuccess()) {
								String msg = "["+number+"]人员同步失败:"+result.getMsg();
								System.out.print(msg);
								logger.error(msg);
							}
						}

						//人员启用和禁用
						filter = new QFilter("number", QCP.equals, workcode);
						//DynamicObject findUser = QueryServiceHelper.queryOne(BOS_USER,"id,isforbidden", filter.toArray());
						if(findUser != null) {
							Boolean isforbidden = findUser.getBoolean("isforbidden");
							Long userId = findUser.getLong("id");
							paramList = new ArrayList<>();
							user = new UserParam();
							user.setId(userId);
							paramList.add(user);

							if(!isforbidden && enable) {
								//启用
								userService.enable(paramList);   
								// 判断执行结果
								for (UserParam result : paramList) {
									if (!result.isSuccess()) {
										String msg = "["+number+"]人员启用同步失败:"+result.getMsg();
										System.out.print(msg);
										logger.error(msg);
									}
								}
							}else if(isforbidden && !enable) {
								//禁用
								userService.disable(paramList);
								// 判断执行结果
								for (UserParam result : paramList) {
									if (!result.isSuccess()) {
										String msg = "["+number+"]人员禁用同步失败:"+result.getMsg();
										System.out.print(msg);
										logger.error(msg);
									}
								}
							}
						}
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
						logger.error(number+"同步人员信息异常"+exception);
					}
				}
			}
			
			//记录本次更新时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			HailiangParamHelper.setParam(USER_SYNC_DATE, format.format(new Date()));  

		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
			logger.error("请求人员信息异常"+exception);
		}
	}

}
