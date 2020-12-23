package hailiang.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;

public class HLCommonUtils {
	
	/**
	 * 时间格式datePattern
	 */
	public final static String DATEPATTERN = "yyyy-MM-dd";
	
	/**
	 * 格式化时间str->date
	 * 
	 * @param sdate
	 *            时间文本
	 * @param format
	 *            格式
	 * @return 时间
	 * @throws ParseException
	 *             格式化失败异常
	 * @since version 121024
	 */
	public static Date parseDateTime(String sdate, String format)
			throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.parse(sdate);
	}
	
	/**
	 * 格式化时间date->str
	 * 
	 * @param date
	 *            时间
	 * @param format
	 *            格式
	 * @return 时间文本
	 * @since version 121024
	 */
	public static String formatDateTime(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	/**
	 * 判断字符串组不能有空
	 * @param strs
	 * @return
	 */
	public static boolean strsNotEmpty(Object... strs) {
		if (strs == null) {
			return false;
		}
		for (Object str : strs) {
			if (str == null || str.toString().isEmpty()) {
				//TODO trim()? or not trim()?
				return false;
			}
		}
		return true;
	}
	
	public static boolean strsEmpty(Object... strs) {
		return !strsNotEmpty(strs);
	}
	
	/**
	 * 操作执行结果处理
	 * @param opResult
	 * @param keyword
	 * @return
	 */
	public static Map<String, Object> executeOperateResult(OperationResult opResult,String keyword) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("result", opResult.isSuccess());
		if(!opResult.isSuccess()) {
			//操作失败，记录错误日志		
			StringBuffer errorMsg = new StringBuffer(keyword+":");
			//List<OperateErrorInfo> list = opResult.getAllErrorInfo();
			List<IOperateInfo> lists =	opResult.getAllErrorOrValidateInfo();
			System.out.println(lists.get(0).getMessage());
			for (IOperateInfo operateErrorInfo : lists) {
				errorMsg.append(operateErrorInfo.getMessage());
			}
			retMap.put("msg", errorMsg.toString());
		}
		return retMap;
	}


}
