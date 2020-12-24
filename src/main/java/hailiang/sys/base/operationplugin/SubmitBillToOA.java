package hailiang.sys.base.operationplugin;

import java.util.UUID;

import com.alibaba.dubbo.common.utils.StringUtils;

import hailiang.constant.CommonConstant;
import hailiang.constant.SupplierConstant;
import hailiang.utils.UUID19;
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
import kd.bos.servicehelper.user.UserServiceHelper;

public class SubmitBillToOA extends AbstractOperationServicePlugIn{

	private static Log logger = LogFactory.getLog(SubmitBillToOA.class);
	private static final String ENTRYENTRY_E = "entryentity_e";
	private static final String ENUMBER = "enumber";

	//开启数据库事务
	@Override
	public void beginOperationTransaction(BeginOperationTransactionArgs e) {
		// TODO Auto-generated method stub
		super.beginOperationTransaction(e);
		System.out.println("beginOperationTransaction");
	}

	//执行事务完毕后
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
		//获取当前用户的工号
		long userid = UserServiceHelper.getCurrentUserId();
		DynamicObject userobj = BusinessDataServiceHelper.loadSingle(userid, CommonConstant.BOS_USER);
		String number = userobj.getString(CommonConstant.NUMBER);
		String workflowId  = UUID19.uuid();
		for(DynamicObject obj : dynameobjs){
			try {
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error(e1.getMessage());
			}
		}
	}

}
