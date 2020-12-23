package kd.bos.debug.mservice;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * BusinessDataServiceHelper测试
 *
 * @author zhangzichen
 * @date 2020/06/30 16:36
 */
public class demo {
    public static DynamicObject newDynamicObject(String entityName) {
        //创建单据对象
        DynamicObject bill = BusinessDataServiceHelper.newDynamicObject("");
        //设置单据属性
        bill.set("billno", System.currentTimeMillis());
        bill.set("billstatus", "A");
        bill.set("org", RequestContext.get().getOrgId());
        bill.set("creator", RequestContext.get().getUserId());
        //获取单据体集合
        DynamicObjectCollection entrys = bill.getDynamicObjectCollection("entryentity");
        //获取单据体的Type
        DynamicObjectType type = entrys.getDynamicObjectType();
        //根据Type创建单据体对象
        DynamicObject entry = new DynamicObject(type);
        //设置单据体属性
        entry.set("price", 12);
        entry.set("qty", 24);
        //添加到单据体集合
        entrys.add(entry);
        SaveServiceHelper.saveOperate("",new DynamicObject[]{bill});
        return bill;
    }
}
