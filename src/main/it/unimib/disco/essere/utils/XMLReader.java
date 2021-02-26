package it.unimib.disco.essere.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;

public class XMLReader {
    private static final Logger logger = LogManager.getLogger(XMLReader.class);
    private DocumentBuilder builder;
    private Document document;
    private XPath xPath;

    public XMLReader() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        xPath = XPathFactory.newInstance().newXPath();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setReader(String url) {
        try {
            /*
             * document = builder.parse(new FileInputStream(Paths .get("C:",
             * "Users", "Ilaria", "Downloads", "qualitas corpus", "metrics",
             * "ant-1.8.2").toString()));
             */
            document = builder.parse(new FileInputStream(url));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean findDuplicates(String expression, String projectName){

        // String expression = "/Metrics/Metric[@id = 'CA']/Values/Value";
        //String m = expression.split("'")[1]
        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
            Set <String> keys= new HashSet<String>();          
            List<String> packages = new ArrayList<>();
            boolean duplicates = false;
            for (int i = 0; i < nodeList.getLength() && !duplicates; i++) {
                if (nodeList.item(i) != null) {
                    String key = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue().toString();
                    
                   // String value = nodeList.item(i).getAttributes().getNamedItem("value").getNodeValue().toString();
                    // List<String> metrics = metricMap.get(key);
                    
                    if (!keys.contains(key)) {                       
                        keys.add(key);
//                        logger.debug("CHIAVE? " + key);
                    } else {
                        duplicates = true;
//                        logger.debug("CHIAVE? " + key);
                    }
                    
                }
            }
            return duplicates;
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
    public void readXML(String expression, Map<String, Map<String,String>> metricMap) {
        // String expression = "/Metrics/Metric[@id = 'CA']/Values/Value";
        String m = expression.split("'")[1];
        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
            Set <String> keys= new HashSet<String>();
            logger.debug("lunghezza: " + nodeList.getLength() + " metrica: " + expression);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i) != null) {
                    String key = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue().toString();
                    //                    logger.debug("CHIAVE? " + key);
                    String value = nodeList.item(i).getAttributes().getNamedItem("value").getNodeValue().toString();
                    // List<String> metrics = metricMap.get(key);
                    Map<String, String> metrics;
                    if (!metricMap.containsKey(key)) {
                        metrics = new HashMap<>();
                        metricMap.put(key, metrics);
                    } else {
                        metrics = metricMap.get(key);
                    }
                    if (value != null && !keys.contains(key)) {
                        keys.add(key);
                        metrics.put(m, value);
                       logger.debug("CHIAVE? " + key+"m: " + metrics);
                    } else {
                        if(value != null && keys.contains(key)){
                            Double a = Double.valueOf(metrics.get(m));
                            Double b = Double.valueOf(value);
//                            if(b < a){
                                metrics.put(m, String.valueOf(b+a));
//                            }
                        }
                        
                        logger.debug(nodeList.item(i).toString());
                    }
                }
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        return metricMap;
    }
}
