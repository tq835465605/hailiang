package hailiang.scmc.im.devapiplugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hailiang.constant.APIConstant;
import hailiang.utils.HLCommonUtils;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.api.ApiResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;


/**
 * 即时库存查询
 * @author TonyQ
 *
 */
public class InvaccQueryWebApi implements IBillWebApiPlugin{

	private static Log logger = LogFactory.getLog(InvaccQueryWebApi.class);

	private static final String data = "data" ;
	private static final String data_materials = "materials" ;
	private static final String data_materials_id = "id" ;
	private static final String im_invacc = "im_invacc";

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
			JSONArray materials = jsonData.getJSONArray(data_materials);
			JSONObject resultdata = new JSONObject();
			
			if(materials!=null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < materials.size(); i++) {			
					JSONObject jsonObj = materials.getJSONObject(i);
					String material = jsonObj.getString(data_materials_id);
					if(HLCommonUtils.strsEmpty(material)) {
						continue;
					}
					//先过滤是否存在这个物料
					JSONObject data = new JSONObject();
					data.put("id", material);
					QFilter qFilter = new QFilter("material.number", QCP.equals, material);
					boolean isExists= QueryServiceHelper.exists(im_invacc, qFilter.toArray());
					if(!isExists) {	
						data.put("msg", "没有对应的物料");
						continue;
					}
					//过滤不可用的物料
					qFilter.and(new QFilter("invstatus.number", QCP.equals, "110"));
					//查询key
					String algokey = this.getClass().getName()+".queryInvacc";
					DataSet invAccDataSet = QueryServiceHelper.queryDataSet(algokey, im_invacc,
							String.join(",", this.buildSelectorAcc()), qFilter.toArray(),"material.number, warehouse.number");
					Throwable IteratorThrowable = null;
					BigDecimal qty= null;
					try {
						Iterator<Row> rows = invAccDataSet.iterator();
						while(rows.hasNext()) {
							Row row = (Row)rows.next();
							qty = row.getBigDecimal("qty");
						}
					} catch (Throwable var) {
						IteratorThrowable = var;
						throw var;
					} finally {
						if (invAccDataSet != null) {
							if (IteratorThrowable != null) {
								try {
									invAccDataSet.close();
								} catch (Throwable var) {
									IteratorThrowable.addSuppressed(var);
								}
							} else {
								invAccDataSet.close();
							}
						}

					}
					if(qty!=null) {
						data.put("qty", qty);	
					}
					else {
						data.put("msg", "查询异常");	
					}
					array.add(data);
				}
				resultdata.put("materials", array);
			}	
			result.setData(resultdata);
		}
		catch (Throwable e) {
			//异常情况设置接口返回失败
			result.setSuccess(false);
			result.setMessage(e.getMessage());
			result.setErrorCode(APIConstant.ERRORCODE);
			//记录错误日志信息
			logger.error("查询即时库存异常",e);
		}	
		return result;
	}

	private List<String> buildSelectorAcc() {
		List<String> selectorList = new ArrayList<String>();
		Set<String> selectorSet = EntityMetadataCache.getDataEntityType(im_invacc).getAllFields().keySet();
		selectorList.addAll(selectorSet);
		selectorList.add("id");
		selectorList.add("material.number");
		selectorList.add("material.name as materialnames");
		selectorList.add("warehouse.number");
		return selectorList;
	}
	
	


}
