package hailiang.utils;

import java.net.URL;
import javax.xml.rpc.ParameterMode;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import kd.bos.orm.ORM;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;


public class WebServiceUtil {
	public static WSContext getWebservice(String endpoint)
    throws Exception
  {
	String appId = "0B1T04PJ49LH";//应用id通过查询分析器t_meta_bizapp查询
    String userName = HailiangParamHelper.getParam("easUserName");
    String password = HailiangParamHelper.getParam("easUserPassword");
    String slnName = "eas";
    String dcName = HailiangParamHelper.getParam("easDncName");
    String longuage = "L2";
    int dbType = 2;
    String authPattern = "";
    int isEncodePwd = 0;
    Object[] o = { userName, password, slnName, dcName, longuage, Integer.valueOf(dbType), authPattern, Integer.valueOf(isEncodePwd) };
    WSContext result = webServiceLogin(endpoint, "login", o);
    return result;
  }
  
  public static WSContext webServiceLogin(String URL, String methodName, Object[] o)
    throws Exception
  {
    Service service = new Service();
    Call call = (Call)service.createCall();
    call.setTargetEndpointAddress(new URL(URL));
    call.setOperationName(methodName);
    call.setReturnClass(WSContext.class);
    //call.setReturnType(XMLType.XSD_STRING);
    
    call.addParameter("userName", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("password", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("slnName", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("dcName", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("longuage", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("dbType", XMLType.XSD_INT, ParameterMode.IN);
    call.addParameter("authPattern", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("isEncodePwd", XMLType.XSD_INT, ParameterMode.IN);
    call.setTimeout(20000);
    
    WSContext result = (WSContext)call.invoke(o);
    
    return result;
  }
  
  public static String webServicemethod(String URL, String methodName, Object[] o)
    throws Exception
  {
    Service service = new Service();
    Call call = (Call)service.createCall();
    call.setTargetEndpointAddress(new URL(URL));
    call.setOperationName(methodName);
    call.setTimeout(30000);
    String result = (String)call.invoke(o);
    return result;
  }
  
  /**
   * 获取应收账款系统参数
   * @param number
   * @return
   */
  public static String getparam(String number) {
	  ORM orm = ORM.create();
	  String oql = "";
	  return null;
	  
  }
  
}
