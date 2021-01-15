package hailiang.pur.bid.ten.formplugin;

import org.apache.commons.lang3.StringUtils;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.form.cardentry.CardEntry;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.scm.bid.business.bill.IBidPayService;
import kd.scm.bid.business.bill.serviceImpl.BidPayServiceImpl;

/**
 * 新增银行账户和开户银行
 * @author TonyQ
 *
 */
public class MyTenPayEdit extends AbstractFormPlugin{
	
	private IBidPayService bidPayService = new BidPayServiceImpl();
	private static final String HL_BANK = "hl01_bank";
	private static final String HL_BEBANK = "hl01_bebank";
	private static final String systemType = "hl-scm-ten-formplugin";
    private static final String SAVE = "save";
	@Override
	public void beforeDoOperation(BeforeDoOperationEventArgs args) {
		// TODO Auto-generated method stub
		super.beforeDoOperation(args);
		FormOperate operate = (FormOperate) args.getSource();
		String operateKey = operate.getOperateKey();
		if (SAVE.equals(operateKey)) {
			CardEntry cardEntry = (CardEntry) this.getView().getControl("entryentity");
			int[] rows = cardEntry.getSelectRows();
			if (rows.length == 0) {
				this.getView().showTipNotification(ResManager.loadKDString("请选择缴费的项目，在进行提交。", "MyTenPayEdit_1",
						systemType, new Object[0]));
				args.setCancel(true);
				return;
			}
			DynamicObject arg20 = this.getModel().getDataEntity(true);
			DynamicObjectCollection arg21 = arg20.getDynamicObjectCollection("entryentity1");
			for (int i = 0; i < rows.length; ++i) {
				DynamicObject temp = (DynamicObject) arg21.get(rows[i]);
				String bank = temp.getString(HL_BANK);
				if(StringUtils.isEmpty(bank)) {
					this.getView().showTipNotification(ResManager.loadKDString("“银行账号”为必填项。", "MyTenPayEdit_2",
							systemType, new Object[0]));
					args.setCancel(true);
					return;
				}
				DynamicObject bebank = temp.getDynamicObject(HL_BEBANK);
				if(bebank == null) {
					this.getView().showTipNotification(ResManager.loadKDString("“开户银行”为必填项。", "MyTenPayEdit_3",
							systemType, new Object[0]));
					args.setCancel(true);
					return;
				}
			}
			
		}
	}

	
	
	
	@Override
	public void afterDoOperation(AfterDoOperationEventArgs args) {
		// TODO Auto-generated method stub
		super.afterDoOperation(args);
		String operateKey = args.getOperateKey();
		DynamicObject pay = this.getModel().getDataEntity(true);
		CardEntry cardEntry;
		int[] rows;
		DynamicObject temp;
		Long id;
		DynamicObject file;
		if (SAVE.equals(operateKey)) {
			if (args.getOperationResult().isSuccess()) {
				cardEntry = (CardEntry) this.getView().getControl("entryentity");
				rows = cardEntry.getSelectRows();
				MainEntityType entrys1 = EntityMetadataCache.getDataEntityType("bid_pay_list");
				DynamicObjectCollection i = pay.getDynamicObjectCollection("entryentity1");
				for (int isChoose = 0; isChoose < rows.length; ++isChoose) {
					temp = (DynamicObject) i.get(rows[isChoose]);
					id = Long.valueOf(temp.getLong("id"));
					file = this.bidPayService.getPayListById(id);
					file.set("hl01_bank", temp.get("hl01_bank"));
					file.set("hl01_bebank", temp.get("hl01_bebank"));
					BusinessDataServiceHelper.save(entrys1, new DynamicObject[]{file});
					QFilter pkId = new QFilter("bidsection.supplierentry.paylistid", "=", id);
					DynamicObject t = QueryServiceHelper.queryOne("bid_pay", "id", new QFilter[]{pkId});
					if (null != t) {
						MainEntityType payMainType = EntityMetadataCache.getDataEntityType("bid_pay");
						t = BusinessDataServiceHelper.loadSingle(t.get("id"), "bid_pay");
						DynamicObjectCollection ex = t.getDynamicObjectCollection("bidsection");
						int k = 0;

						while (true) {
							if (k >= ex.size()) {
								BusinessDataServiceHelper.save(payMainType, new DynamicObject[]{t});
								break;
							}

							DynamicObjectCollection supllierEntrys = ((DynamicObject) ex.get(k))
									.getDynamicObjectCollection("supplierentry");

							for (int j = 0; j < supllierEntrys.size(); ++j) {
								DynamicObject entry = (DynamicObject) supllierEntrys.get(j);
								long paylistid = entry.getLong("paylistid");
								if (paylistid == id.longValue()) {
									entry.set("hl01_bank", temp.get("hl01_bank"));
									entry.set("hl01_bebank", temp.get("hl01_bebank"));
								}
							}
							++k;
						}
					}
				}
			}
		}
	}
	
	

}
