package hailiang.sys.base.timertask;

import java.util.ArrayList;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.ExceptionUtils;

/**
 * 同步岗位
 * @author leach
 *
 */
public class SyncJobtitleTask  extends AbstractTask {
	private static Log logger = LogFactory.getLog(SyncJobtitleTask.class);

	private static final String GETJOBTITLEINFOWITHPAGE = "getJobtitleInfoWithPage";//接口名称
	private static final String HL01_JOBTITLE = "hl01_jobtitle";//岗位基础资料

	@Override
	public void execute(RequestContext ctx, Map<String, Object> param) throws KDException {
		String wsdl = HailiangParamHelper.getParam("OA_HRMWEBSERVICE");
		JSONObject jsonParam = new JSONObject();
		int curpage = 1;
		int pagesize = 50;
		jsonParam.put("curpage", curpage);
		jsonParam.put("pagesize", pagesize);
		try {
			String strJobTitle = WebServiceUtil.webServicemethod(wsdl, GETJOBTITLEINFOWITHPAGE, new Object[] {jsonParam.toJSONString()});
			System.out.print(strJobTitle);
			logger.info("岗位信息" + strJobTitle);
			JSONObject rstJson = JSONObject.parseObject(strJobTitle);
			int totalRecord = rstJson.getInteger("totalSize");
			JSONArray jobTitleJsonArr = rstJson.getJSONArray("dataList");
			for (int j = 0; j < jobTitleJsonArr.size(); j++) {
				JSONObject jobTitleJsonObj = jobTitleJsonArr.getJSONObject(j);
				saveJobtile(jobTitleJsonObj);
			}
			//根据总页数做循环获取
			int totalPage = (totalRecord + pagesize - 1) / pagesize;
			for (int i = curpage + 1; i <= totalPage; i++) {
				jsonParam.put("curpage", i);
				strJobTitle = WebServiceUtil.webServicemethod(wsdl, GETJOBTITLEINFOWITHPAGE, new Object[] {jsonParam.toJSONString()});
				System.out.print(strJobTitle);
				logger.info("岗位信息" + strJobTitle);
				rstJson = JSONObject.parseObject(strJobTitle);
				jobTitleJsonArr = rstJson.getJSONArray("dataList");
				for (int j = 0; j < jobTitleJsonArr.size(); j++) {
					JSONObject jobTitleJsonObj = jobTitleJsonArr.getJSONObject(j);
					saveJobtile(jobTitleJsonObj);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			String exception = ExceptionUtils.getExceptionStackTraceMessage(e);
			logger.error("请求岗位信息异常"+exception);
		}

	}
	
	private void saveJobtile(JSONObject jobTitleJsonObj) {
		String jobtitleid = jobTitleJsonObj.getString("id");
//		String shortname = jobTitleJsonObj.getString("shortname");
		String fullname = jobTitleJsonObj.getString("jobtitlename");
		
		DynamicObject jobtitle = BusinessDataServiceHelper.newDynamicObject(HL01_JOBTITLE);
		//查找在岗位表中是否存在
		QFilter filter = new QFilter("number", QCP.equals, jobtitleid);
		DynamicObject jobtitleQueryObj = QueryServiceHelper.queryOne(HL01_JOBTITLE,"id", filter.toArray());				
		if (jobtitleQueryObj != null) {
			//加载OA岗位表数据
			jobtitle = BusinessDataServiceHelper.loadSingle(jobtitleQueryObj.getLong("id"), HL01_JOBTITLE);
		}

		jobtitle.set("number", jobtitleid);
//		jobtitle.set("hl01_shortname", shortname);
		jobtitle.set("hl01_fullname", fullname);
		jobtitle.set("enable", "1");
		jobtitle.set("status", "A");
		SaveServiceHelper.save(new DynamicObject[]{jobtitle});
	}

}
