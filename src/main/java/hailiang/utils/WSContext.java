package hailiang.utils;

import java.io.Serializable;
import javax.xml.namespace.QName;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;


/**
 * EAS登录接口帮助类型
 * @author leach
 *
 */
public class WSContext implements Serializable {
	
	public WSContext(){};
	
	private int dbType;
	  private String dcName;
	  private String password;
	  private String sessionId;
	  private String slnName;
	  private String userName;
	  
	  public WSContext(int dbType, String dcName, String password, String sessionId, String slnName, String userName)
	  {
	    this.dbType = dbType;
	    this.dcName = dcName;
	    this.password = password;
	    this.sessionId = sessionId;
	    this.slnName = slnName;
	    this.userName = userName;
	  }
	  
	  public int getDbType()
	  {
	    return this.dbType;
	  }
	  
	  public void setDbType(int dbType)
	  {
	    this.dbType = dbType;
	  }
	  
	  public String getDcName()
	  {
	    return this.dcName;
	  }
	  
	  public void setDcName(String dcName)
	  {
	    this.dcName = dcName;
	  }
	  
	  public String getPassword()
	  {
	    return this.password;
	  }
	  
	  public void setPassword(String password)
	  {
	    this.password = password;
	  }
	  
	  public String getSessionId()
	  {
	    return this.sessionId;
	  }
	  
	  public void setSessionId(String sessionId)
	  {
	    this.sessionId = sessionId;
	  }
	  
	  public String getSlnName()
	  {
	    return this.slnName;
	  }
	  
	  public void setSlnName(String slnName)
	  {
	    this.slnName = slnName;
	  }
	  
	  public String getUserName()
	  {
	    return this.userName;
	  }
	  
	  public void setUserName(String userName)
	  {
	    this.userName = userName;
	  }
	  
	  private Object __equalsCalc = null;
	  
	  public synchronized boolean equals(Object obj)
	  {
	    if (!(obj instanceof WSContext)) {
	      return false;
	    }
	    WSContext other = (WSContext)obj;
	    if (obj == null) {
	      return false;
	    }
	    if (this == obj) {
	      return true;
	    }
	    if (this.__equalsCalc != null) {
	      return this.__equalsCalc == obj;
	    }
	    this.__equalsCalc = obj;
	    
	    boolean _equals = 
	      (this.dbType == other.getDbType()) && (
	      ((this.dcName == null) && (other.getDcName() == null)) || (
	      (this.dcName != null) && 
	      (this.dcName.equals(other.getDcName())) && (
	      ((this.password == null) && (other.getPassword() == null)) || (
	      (this.password != null) && 
	      (this.password.equals(other.getPassword())) && (
	      ((this.sessionId == null) && (other.getSessionId() == null)) || (
	      (this.sessionId != null) && 
	      (this.sessionId.equals(other.getSessionId())) && (
	      ((this.slnName == null) && (other.getSlnName() == null)) || (
	      (this.slnName != null) && 
	      (this.slnName.equals(other.getSlnName())) && (
	      ((this.userName == null) && (other.getUserName() == null)) || (
	      (this.userName != null) && 
	      (this.userName.equals(other.getUserName()))))))))))));
	    this.__equalsCalc = null;
	    return _equals;
	  }
	  
	  private boolean __hashCodeCalc = false;
	  
	  public synchronized int hashCode()
	  {
	    if (this.__hashCodeCalc) {
	      return 0;
	    }
	    this.__hashCodeCalc = true;
	    int _hashCode = 1;
	    _hashCode += getDbType();
	    if (getDcName() != null) {
	      _hashCode += getDcName().hashCode();
	    }
	    if (getPassword() != null) {
	      _hashCode += getPassword().hashCode();
	    }
	    if (getSessionId() != null) {
	      _hashCode += getSessionId().hashCode();
	    }
	    if (getSlnName() != null) {
	      _hashCode += getSlnName().hashCode();
	    }
	    if (getUserName() != null) {
	      _hashCode += getUserName().hashCode();
	    }
	    this.__hashCodeCalc = false;
	    return _hashCode;
	  }
	  
	  private static TypeDesc typeDesc = new TypeDesc(WSContext.class, true);
	  
	  static
	  {
	    typeDesc.setXmlType(new QName("urn:client", "WSContext"));
	    ElementDesc elemField = new ElementDesc();
	    elemField.setFieldName("dbType");
	    elemField.setXmlName(new QName("", "dbType"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
	    elemField.setNillable(false);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new ElementDesc();
	    elemField.setFieldName("dcName");
	    elemField.setXmlName(new QName("", "dcName"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(true);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new ElementDesc();
	    elemField.setFieldName("password");
	    elemField.setXmlName(new QName("", "password"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(true);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new ElementDesc();
	    elemField.setFieldName("sessionId");
	    elemField.setXmlName(new QName("", "sessionId"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(true);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new ElementDesc();
	    elemField.setFieldName("slnName");
	    elemField.setXmlName(new QName("", "slnName"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(true);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new ElementDesc();
	    elemField.setFieldName("userName");
	    elemField.setXmlName(new QName("", "userName"));
	    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(true);
	    typeDesc.addFieldDesc(elemField);
	  }
	  
	  public static TypeDesc getTypeDesc()
	  {
	    return typeDesc;
	  }
	  
	  public static Serializer getSerializer(String mechType, Class _javaType, QName _xmlType)
	  {
	    return 
	      new BeanSerializer(
	      _javaType, _xmlType, typeDesc);
	  }
	  
	  public static Deserializer getDeserializer(String mechType, Class _javaType, QName _xmlType)
	  {
	    return 
	      new BeanDeserializer(
	      _javaType, _xmlType, typeDesc);
	  }
}
