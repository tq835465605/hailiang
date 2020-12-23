package hailiang.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import scala.xml.Elem;

import java.io.File;
import java.util.ArrayList;

/**
 * XML工具类
 */
public class XmlUtils {
    private static SAXReader reader;
    static {
        reader = new SAXReader();
    }
    public XmlUtils() {
    }

    /***
     * 将xml字符串转换为mxl
     * @param strXml
     * @return
     */
    public static Document stringToXml(String strXml) {
    	
    	Document document = null;
		try {
			document = DocumentHelper.parseText(strXml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return document;
    }

    /**
     * 根据XML对象文件获取根节点的元素
     * @param document XML文件对象
     * @return rootElement 根节点元素
     */
    public static Element getRootElementByDocument(Document document){
        Element rootElement = document.getRootElement();
        return rootElement;
    }

    /**
     * 根据XML对象文件和元素名称在根结点下寻找Element
     * @param elementName 结点名称
     * @param document  XML文件对象
     * @return 根据名称找到的Element
     */
    public static Element getElementByNameUnderRoot(String elementName,Document document){
        Element rootElement = getRootElementByDocument(document);
        return rootElement.element(elementName);
    }

    /**
     *
     * 在根结点下新增元素和赋值通过XML对象
     * @param newElementName 新元素名称
     * @param newElementText 新元素值
     * @param document XML对象
     */
    public static void addNewElementUnderRoot(String newElementName,String newElementText,Document document){
        Element rootElement = getRootElementByDocument(document);
        Element newElement = rootElement.addElement(newElementName);
        newElement.setText(newElementText);
    }

    /**
     * 根据元素名称删除根元素下的结点
     * @param elementName 元素名称
     * @param document XML文件对象
     * @return 成功与否 true为成功，false为失败
     */
    public static boolean deleteElementByNameUnderRoot(String elementName,Document document){
        Element rootElement = getRootElementByDocument(document);
        return (rootElement.remove(rootElement.element(elementName)));
    }
    /**
     * 根据元素名称和值更新元素值
     * @param elementName 元素名称
     * @param elementText 元素值
     * @param document XML文件对象
     */
    public static void setElementTextByNameUnderRoot(String elementName,String elementText,Document document){
        Element rootElement = getRootElementByDocument(document);
        rootElement.element(elementName).setText(elementText);
    }

    /**
     * 复制元素
     * @param element 元素
     * @return 复制的元素
     */
    public static Element cloneElement(Element element){
        return (Element) element.clone();
    }

    /**
     * 添加元素到父元素下
     * @param son 元素
     * @param father 父元素
     */
    public static void addElementTo(Element son,Element father){
        father.add(son);
    }

    /**
     * 获取父节点下所有子节点
     * @param fatherElement 父节点
     * @return 子节点集合
     */
    public static ArrayList<Element> getElementsInFatherElement(Element fatherElement){
       return (ArrayList<Element>)fatherElement.elements();
    }
    
    /**
     * 根据id设置获取元素并设置元素值
     * @param id
     * @param value
     * @param element
     */
    public static void setElementById(String id, String value, Element element) {
    	ArrayList<Element> items= getElementsInFatherElement(element);
		for (Element i : items) {
			if(i.attribute("id").getValue().equals(id)) {
				i.setText(value);
				break;
			}
		}
    }
    


}
