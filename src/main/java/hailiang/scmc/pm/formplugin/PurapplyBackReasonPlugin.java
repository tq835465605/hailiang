package hailiang.scmc.pm.formplugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;

/**
 * 退出理由对话框
 * @author TonyQ
 *
 */
public class PurapplyBackReasonPlugin  extends AbstractFormPlugin {
	//页面确认按钮标识
	private final static String KEY_OK = "btnok";
	//页面取消按钮标识
	private final static String KEY_CANCEL = "btncancel";
	
	private final static String KEY_REASON = "hl01_reason";
	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);
		//页面确认按钮和取消按钮添加监听
		this.addClickListeners(KEY_OK, KEY_CANCEL);
	}

	@Override
	public void click(EventObject evt) {
		super.click(evt);
		//获取被点击的控件对象
		Control source = (Control) evt.getSource();
		if (StringUtils.equals(source.getKey(), KEY_OK)) {
			Map<String, String> hashMap = new HashMap<String, String>();
			//如果被点击控件为确认，则获取页面相关控件值，组装数据传入returnData返回给父页面，最后关闭页面
			hashMap.put(KEY_REASON, String.valueOf(this.getModel().getValue(KEY_REASON)));
			hashMap.put("itemkey", this.getView().getFormShowParameter().getCustomParam("itemkey"));
			this.getView().returnDataToParent(hashMap);
			this.getView().close();
		} else if (StringUtils.equals(source.getKey(), KEY_CANCEL)) {
			//被点击控件为取消则设置返回值为空并关闭页面（在页面关闭回调方法中必须验证返回值不为空，否则会报空指针）
			this.getView().returnDataToParent(null);
			this.getView().close();
		}
	}



}
