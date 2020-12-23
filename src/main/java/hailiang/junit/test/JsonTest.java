package hailiang.junit.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kd.bos.entity.api.ApiResult;


public class JsonTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String jsonStr = "{\r\n" + 
				"  \"data\": {\r\n" + 
				"    \"supplier\": \"123\",\r\n" + 
				"    \"date\": \"456\",\r\n" + 
				"    \"remark\": \"789\",   \r\n" + 
				"    \"entry\": [\r\n" + 
				"      {\r\n" + 
				"        \"ename\": \"haha\",\r\n" + 
				"        \"enumber\": \"huhu\"        \r\n" + 
				"      }\r\n" + 
				"    ]\r\n" + 
				"  }\r\n" + 
				"}";
		//记录传入参数日志
		JSONObject rootObj = JSONObject.parseObject(jsonStr);
		//获取传入参数data数据
		JSONObject jsonData = (JSONObject)rootObj.get("data");
		
		String supplier = jsonData.getString("supplier");
		System.out.println(supplier);
		JSONArray jsonArray=jsonData.getJSONArray("entry");
		JSONArray data = new JSONArray();
		//循环处理设备变更
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			System.out.println(jsonObj.getString("ename"));
		}
		ApiResult pApiResult = new ApiResult();
		pApiResult.setMessage("niha");
		doit(pApiResult);
		System.out.println(pApiResult.getSuccess());

	}
	
	public static void doit(ApiResult api) {
		api.setSuccess(false);
		api.setMessage("buhao");
		
	}

}
