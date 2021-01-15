package hailiang.pur.bid.formplugin.directsource;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.TextEdit;
import kd.bos.form.operate.FormOperate;

/**
 * 定向发起方为采购部门时，特殊情况说明为必填
 * @author TonyQ
 *
 */
public class DirectSourceControlBillPlugin extends AbstractBillPlugIn {
	
	private static final String DIRECTFROM = "hl01_combofield";
	private static final String SPECIALDISPT = "hl01_combofield";
	private static final String directfrom_value = "2";

	@Override
	public void propertyChanged(PropertyChangedArgs e) {
		// TODO Auto-generated method stub
		super.propertyChanged(e);
		String propertyName = e.getProperty().getName();
		if (StringUtils.equals(DIRECTFROM, propertyName)) {
			if(this.getModel().getValue(SPECIALDISPT)!=null) {
				String fleld = (String) this.getModel().getValue(SPECIALDISPT);
				TextEdit text= this.getControl(SPECIALDISPT);
				if(StringUtils.equals(directfrom_value, fleld)) {
					text.setMustInput(true);
				}
				else {	
					text.setMustInput(false);
				}
				this.getView().updateView();
			}
		}
	}

	/***
	 * 事件监听，监听保存时候的事件
	 */
	@Override
	public void registerListener(EventObject e) {
		// TODO Auto-generated method stub
		super.registerListener(e);
		this.addItemClickListeners("bar_save");
		this.addItemClickListeners("bar_submit");

	}
	/**
	 * 控件点击事件触发，通过Key判断触发来源执行不同操作。
	 */
	@Override
	public void itemClick(ItemClickEvent evt) {
		String key = evt.getItemKey();
		// 保存操作
		if ("bar_save".equals(key) || "bar_submit".equals(key)) {
			//do save
			String fleld = (String) this.getModel().getValue(DIRECTFROM);
			if(StringUtils.isNotBlank(fleld)) {
				if(StringUtils.equals(directfrom_value, fleld)) {
					String ifnull = (String) this.getModel().getValue(SPECIALDISPT);
					if(StringUtils.isBlank(ifnull)) {
						this.getView().showErrorNotification("\"特殊情况说明\"必填");
					}
				}
			}
		} 
	}

	@Override
	public void beforeDoOperation(BeforeDoOperationEventArgs args) {
		// TODO Auto-generated method stub
		super.beforeDoOperation(args);
		boolean ismustinput = false;
		String fleld = (String) this.getModel().getValue(DIRECTFROM);
		if(StringUtils.isNotBlank(fleld)) {
			if(StringUtils.equals(directfrom_value, fleld)) {
				String ifnull = (String) this.getModel().getValue(SPECIALDISPT);
				if(StringUtils.isBlank(ifnull)) {
					//this.getView().showErrorNotification("特殊情况说明必填");
					//this.getView().showErrMessage("特殊情况说明必填1", "必填哈");
					ismustinput =true;
				}
			}
		}
		if (args.getSource() instanceof FormOperate) {
			FormOperate formOp = (FormOperate) args.getSource();
			String operateKey = formOp.getOperateKey();
			if(StringUtils.equals("save", operateKey) || StringUtils.equals("submit", operateKey)) {
				 if(ismustinput) {
					 this.getView().showErrorNotification("\"特殊情况说明\"必填");
					// this.getView().showTipNotification(ResManager.loadKDString("\"特殊情况说明\"必填", "hl01_bid_directsource", "my-formplugin"));
				     args.setCancel(true);
				 }
			}
		}
		
		
	}
}
