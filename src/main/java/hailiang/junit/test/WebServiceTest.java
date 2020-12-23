package hailiang.junit.test;

import java.util.ArrayList;
import java.util.List;

import hailiang.utils.WebServiceUtil;

public class WebServiceTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String wsdl = "http://172.17.15.117/services/HrmService";
//		String companyMethod ="getHrmSubcompanyInfoXML";
//		String ipaddress = "172.17.15.117";
//		String strCompanys = WebServiceUtil.webServicemethod(wsdl, companyMethod, new Object[] {ipaddress});
//		System.out.print(strCompanys);	
		String imm = ",";
		List<String> liStrings = new ArrayList<String>();
		liStrings.add("1");
		liStrings.add("2");
		liStrings.add("3");
		System.out.println(String.join(imm, liStrings));
	}

}
