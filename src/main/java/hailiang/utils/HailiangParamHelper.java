package hailiang.utils;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 自定义参数帮助类型
 * @author leach
 *
 */
public class HailiangParamHelper {

	private static Log logger = LogFactory.getLog(HailiangParamHelper.class);

	private final static String HAILIANGPARAMSET = "hl01_paramset";//参数基础资料标识
	private final static String HL01_VALUE = "hl01_value";//参数值字段

	/**
	 * 获取参数
	 * @param number
	 * @return
	 */
	public static String getParam(String number) {
		String value = null;
		QFilter[] filter = new QFilter[] { new QFilter("number", QCP.equals, number)};
		DynamicObjectCollection coll = QueryServiceHelper.query(HAILIANGPARAMSET,HL01_VALUE,filter);
		if(!coll.isEmpty()) {
			value = coll.get(0).getString(HL01_VALUE);
		}
		return value;
	}

	/**
	 * 设置参数
	 * @param number
	 * @param value
	 */
	public static void setParam(String number,String value) {
		QFilter[] filter = new QFilter[] { new QFilter("number", QCP.equals, number)};
		DynamicObjectCollection coll = QueryServiceHelper.query(HAILIANGPARAMSET,"id,number,HL01_VALUE",filter);
		if(!coll.isEmpty()) {
			Long id = coll.get(0).getLong("id");
			DynamicObject paramInfo = BusinessDataServiceHelper.loadSingle(id, HAILIANGPARAMSET);
			paramInfo.set(HL01_VALUE, value);
			SaveServiceHelper.update(paramInfo);
		}
	}

}
