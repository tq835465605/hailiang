package hailiang.sys.base.operationplugin;

import com.alibaba.dubbo.common.utils.StringUtils;

import hailiang.constant.CommonConstant;
import hailiang.constant.SupplierConstant;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.entity.plugin.args.BeginOperationTransactionArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;

/**
 * 新增供应商检测，校验是否黑名单中的供应商/关联供应商，若是则不允许新增
 * @author TonyQ
 *
 */
public class CheckSupplierIsBlackPlugin extends AbstractOperationServicePlugIn{

	private static Log logger = LogFactory.getLog(CheckSupplierIsBlackPlugin.class);
	private static final String ENTRYENTRY_E = "entryentity_e";
	private static final String ENUMBER = "enumber";

	@Override
	public void beginOperationTransaction(BeginOperationTransactionArgs e) {
		// TODO Auto-generated method stub
		super.beginOperationTransaction(e);
		System.out.println("beginOperationTransaction");
	}

	@Override
	public void endOperationTransaction(EndOperationTransactionArgs e) {
		// TODO Auto-generated method stub
		super.endOperationTransaction(e);
		System.out.println("endOperationTransaction");
	}

	@Override
	public void onPreparePropertys(PreparePropertysEventArgs e) {
		// TODO Auto-generated method stub
		super.onPreparePropertys(e);
		System.out.println("onPreparePropertys");
	}

	//保存之前
	@Override
	public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
		// TODO Auto-generated method stub
		super.beforeExecuteOperationTransaction(e);
		DynamicObject[] dynameobjs = e.getDataEntities();
		for(DynamicObject obj : dynameobjs){
			try {
				String societycreditcode = obj.getString(SupplierConstant.societycreditcode);
				if(StringUtils.isBlank(societycreditcode)) {
					e.setCancelMessage("统一社会信用代码不能为空");
					e.setCancel(true);
					return;
				}
				//黑名单是否存在的标识
				boolean isExist = false;
				DynamicObject[]  dynamicObjectCollection =  BusinessDataServiceHelper.load(SupplierConstant.srm_blackenterprise,"id,enumber,"+ENTRYENTRY_E, null);
				for(DynamicObject dynamicObject : dynamicObjectCollection) {
					//表头判断
					String exsocietycreditcode =dynamicObject.getString(CommonConstant.NUMBER);
					if(societycreditcode.equals(exsocietycreditcode)) {
						isExist = true;
						break;
					}
					//单据体判断
					DynamicObjectCollection collection = dynamicObject.getDynamicObjectCollection(ENTRYENTRY_E);
					for(DynamicObject glcompany:collection) {
						String enumber = glcompany.getString(ENUMBER);
						if(societycreditcode.equals(enumber)) {
							isExist = true;
							break;
						}
					}
					if(isExist)
						break;
				}
				if(isExist) {
					e.setCancelMessage("该供应商已在黑名单中存在");
					e.setCancel(true);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error(e1.getMessage());
			}
		}
	}
}
