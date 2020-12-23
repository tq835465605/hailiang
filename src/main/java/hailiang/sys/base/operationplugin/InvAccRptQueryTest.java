package hailiang.sys.base.operationplugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kd.bos.algo.DataSet;
import kd.bos.algo.Field;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.datatype.BigDecimalType;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.scmc.im.consts.InvAccConst;
import kd.scmc.im.report.common.ReportCommonFilterOrChangeOp;
import kd.scmc.im.report.helper.ReportGeneralOpHelper;
import kd.scmc.sbs.business.reservation.ReserveHelper;

public class InvAccRptQueryTest extends AbstractReportListDataPlugin {
	private String algoKey = this.getClass().getName();

	public DataSet query(ReportQueryParam queryParam, Object arg1) throws Throwable {
		FilterInfo filterInfo = queryParam.getFilter();
		List qFilters = null;
		String orgLogo;
		if ("A".equals(filterInfo.getString("radiogroupfield"))) {
			orgLogo = "multiorghead";
			qFilters = this.buildInvAccPartFilter(queryParam, filterInfo, orgLogo, true);
		} else {
			orgLogo = "multiorgheadbyowner";
			qFilters = this.buildInvAccPartFilter(queryParam, filterInfo, orgLogo, false);
		}

		DataSet invAccDataSet = QueryServiceHelper.queryDataSet(this.algoKey, InvAccConst.getBalTb(),
				String.join(",", this.buildSelectorAcc()), (QFilter[]) qFilters.toArray(new QFilter[qFilters.size()]),
				"material.number, warehouse.number");
		DataSet materialRelatedDataSet = ReportCommonFilterOrChangeOp.getClassifiedMaterialDataSet(filterInfo);
		invAccDataSet = materialRelatedDataSet
				.leftJoin(invAccDataSet).on("material", "material").select(ReportGeneralOpHelper
						.addFileds(this.getDataSetFieldArray(invAccDataSet), new String[]{"group", "materialtype"}))
				.finish();
		invAccDataSet = invAccDataSet.filter("org is not null");
		invAccDataSet = ReportCommonFilterOrChangeOp.filterOwner(filterInfo, invAccDataSet);
		invAccDataSet = ReserveHelper.getAvbbaseqty(invAccDataSet);
		invAccDataSet = invAccDataSet.orderBy(orderByMultiorgQuery(filterInfo));
		invAccDataSet = invAccDataSet.union(this.getSumData(invAccDataSet));
		return invAccDataSet;
	}

	private static String[] orderByMultiorgQuery(FilterInfo filterInfo) {
		return "A".equals(filterInfo.getString("radiogroupfield"))
				? new String[]{"material.number", "warehouse.number", "org", "owner"}
				: new String[]{"material.number", "warehouse.number", "owner", "org"};
	}

	protected String[] getDataSetFieldArray(DataSet dataSet) {
		Set fileds = ReportGeneralOpHelper.getDataSetField(dataSet);
		return (String[]) fileds.toArray(new String[fileds.size()]);
	}

	private DataSet getSumData(DataSet dataSet) {
		Field[] fields = dataSet.getRowMeta().getFields();
		ArrayList sumFieldsList = new ArrayList();
		ArrayList nullFieldsList = new ArrayList();
		ArrayList allFieldsList = new ArrayList(fields.length);
		Field[] groupFields = fields;
		int gpRs = fields.length;

		for (int arg7 = 0; arg7 < gpRs; ++arg7) {
			Field field = groupFields[arg7];
			if (field.getDataType() instanceof BigDecimalType) {
				sumFieldsList.add(field.getAlias());
			} else if (!"summarytype".equals(field.getName()) && !"materialnames".equals(field.getName())) {
				nullFieldsList.add(field.getAlias());
			}

			allFieldsList.add(field.getAlias());
		}

		String[] arg9 = new String[0];
		GroupbyDataSet arg10 = dataSet.groupBy(arg9);
		sumFieldsList.forEach((f) -> {
			arg10.sum((String) f);
		});
		dataSet = arg10.finish();

		String arg12;
		for (Iterator arg11 = nullFieldsList.iterator(); arg11.hasNext(); dataSet = dataSet.addNullField(arg12)) {
			arg12 = (String) arg11.next();
		}

		dataSet = dataSet.addField(
				"\'" + ResManager.loadKDString("合计", "InvAccRptQuery_0", "scmc-im-report", new Object[0]) + "\'",
				"materialnames");
		dataSet = dataSet.addField(String.format("\'%s\'", new Object[]{Integer.valueOf(3)}), "summarytype");
		dataSet = dataSet.select((String[]) allFieldsList.toArray(new String[allFieldsList.size()]));
		return dataSet;
	}

	private List<String> buildSelectorAcc() {
		ArrayList selectorList = new ArrayList();
		Set selectorSet = EntityMetadataCache.getDataEntityType(InvAccConst.getBalTb()).getAllFields().keySet();
		selectorList.addAll(selectorSet);
		selectorList.add("\'0\' as summarytype");
		selectorList.add("id");
		selectorList.add("material.number");
		selectorList.add("material.name as materialnames");
		selectorList.add("warehouse.number");
		return selectorList;
	}

	private List<QFilter> buildInvAccPartFilter(ReportQueryParam queryParam, FilterInfo filterInfo, String orgLogo,
			boolean orgIsMust) {
		ArrayList qFilters = new ArrayList();
		DynamicObjectCollection org = (DynamicObjectCollection) filterInfo.getValue(orgLogo);
		String ownerTypehead;
		if (org != null && (org.size() != 0 || orgIsMust)) {
			ArrayList ownerTypehead1 = new ArrayList();
			Iterator ivnTypeHeads1 = org.iterator();

			while (ivnTypeHeads1.hasNext()) {
				DynamicObject invStatusHeads = (DynamicObject) ivnTypeHeads1.next();
				ownerTypehead1.add((Long) invStatusHeads.getPkValue());
			}

			qFilters.add(new QFilter("org", "in", ownerTypehead1));
		} else {
			ownerTypehead = (String) filterInfo.getValue("orgIdsString");
			List ivnTypeHeads = SerializationUtils.fromJsonStringToList(ownerTypehead, Long.class);
			qFilters.add(new QFilter("org", "in", ivnTypeHeads));
		}

		ownerTypehead = filterInfo.getString("ownertypehead");
		if (ownerTypehead != null && !ownerTypehead.trim().equals("")) {
			QFilter ivnTypeHeads2 = new QFilter("ownertype", "=", ownerTypehead);
			qFilters.add(ivnTypeHeads2);
		}

		DynamicObjectCollection ivnTypeHeads3 = filterInfo.getDynamicObjectCollection("ivntypehead");
		QFilter warehouseFilter1;
		if (ivnTypeHeads3 != null && ivnTypeHeads3.size() > 0) {
			HashSet invStatusHeads1 = new HashSet();
			Iterator warehouseFilter = ivnTypeHeads3.iterator();

			while (warehouseFilter.hasNext()) {
				DynamicObject locationFilter = (DynamicObject) warehouseFilter.next();
				invStatusHeads1.add(locationFilter.getPkValue());
			}

			warehouseFilter1 = new QFilter("invtype", "in", invStatusHeads1);
			qFilters.add(warehouseFilter1);
		}

		DynamicObjectCollection invStatusHeads2 = filterInfo.getDynamicObjectCollection("invstatushead");
		QFilter locationFilter2;
		if (invStatusHeads2 != null && invStatusHeads2.size() > 0) {
			HashSet warehouseFilter2 = new HashSet();
			Iterator locationFilter1 = invStatusHeads2.iterator();

			while (locationFilter1.hasNext()) {
				DynamicObject lotnumberFilter = (DynamicObject) locationFilter1.next();
				warehouseFilter2.add(lotnumberFilter.getPkValue());
			}

			locationFilter2 = new QFilter("invstatus", "in", warehouseFilter2);
			qFilters.add(locationFilter2);
		}

		warehouseFilter1 = ReportCommonFilterOrChangeOp.getDynamicObjectFromToFilter(filterInfo, "warehouse.number",
				"warehousefrom", "warehouseto");
		if (warehouseFilter1 != null) {
			qFilters.add(warehouseFilter1);
		}

		locationFilter2 = ReportCommonFilterOrChangeOp.getDynamicObjectFromToFilter(filterInfo, "location.number",
				"locationfrom", "locationto");
		if (locationFilter2 != null) {
			qFilters.add(locationFilter2);
		}

		QFilter lotnumberFilter1 = ReportCommonFilterOrChangeOp.getTextFromToFilter(filterInfo, "lotnum",
				"lotnumberfrom", "lotnumberto");
		if (lotnumberFilter1 != null) {
			qFilters.add(lotnumberFilter1);
		}

		QFilter projectFilter = ReportCommonFilterOrChangeOp.getDynamicObjectFromToFilter(filterInfo, "project.number",
				"projectfrom", "projectto");
		if (projectFilter != null) {
			qFilters.add(projectFilter);
		}

		qFilters.add(this.addInvAccNotZeroFilter());
		return qFilters;
	}

	private QFilter addInvAccNotZeroFilter() {
		QFilter qtyFilter = new QFilter("baseqty", "<>", Integer.valueOf(0));
		qtyFilter.or(new QFilter("qty", "<>", Integer.valueOf(0)));
		qtyFilter.or(new QFilter("qty2nd", "<>", Integer.valueOf(0)));
		return qtyFilter;
	}
}
