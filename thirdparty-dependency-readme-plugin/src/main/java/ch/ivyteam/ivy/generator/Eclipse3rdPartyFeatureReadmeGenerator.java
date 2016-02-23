package ch.ivyteam.ivy.generator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.ivyteam.xml.XmlParserUtil;
import ch.ivyteam.xml.XmlUtil;

/**
 * This class writes an html table to the standard output stream that contains information about all
 * eclipse features that are available at the current directory location. The generated information can 
 * be used to be included in the ReadMe of ivy 
 * @author rwei
 * @since 03.01.2011
 */
public class Eclipse3rdPartyFeatureReadmeGenerator
{
  private final StringBuilder html = new StringBuilder();
  
  /**
   * Analyses the eclipse features installed at the current directory location.
   * Set the current directory location to the installation directory of an ivy Designer. 
   * @param designerDir 
   * @return html
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  public String generate(File designerDir) throws ParserConfigurationException, SAXException, IOException
  {
    printHtml("<!-- The following feature table was generated by the "+Eclipse3rdPartyFeatureReadmeGenerator.class.getCanonicalName()+" //-->");
    printHtml("<table class=\"table table-hover\">");
    printHtml("  <thead>");
    printHtml("    <tr>");
    printHtml("      <th>Feature</th>");
    printHtml("      <th>Id</th>");
    printHtml("      <th>Description</th>");
    printHtml("      <th>Version</th>");
    printHtml("      <th>Provider</th>");
    printHtml("    </tr>");
    printHtml("  </thead>");
    printHtml("  <tbody>");
    
    printFeatures(designerDir);
    
    printHtml("  </tbody>");
    printHtml("</table>");
    
    return html.toString();
  }

  private void printHtml(String line)
  {
    if (line != null)
    {
      html.append(line).append("\n");
    }
  }

  private void printFeatures(File designerDir) throws ParserConfigurationException, SAXException, IOException
  {
    File featuresDir = new File(designerDir, "features");
    if (featuresDir.exists() && featuresDir.isDirectory())
    {
      for (File featureDir : featuresDir.listFiles((FileFilter)DirectoryFileFilter.INSTANCE))
      {
        File feature = new File(featureDir, "feature.xml");
        File featureProps = new File(featureDir, "feature.properties");
        if (feature.exists())
        {
          printHtml(generateFeatureInfo(feature, featureProps)); 
        }
      }    
    }
    else
    {
      printHtml("<tr><td colspan=\"5\" style= \"color:red; font-weight:bold;\">The directory '"+featuresDir.getAbsolutePath()+"' does not exist");
    }
  }

  /**
   * Generates the feature info of the given feature
   * @param feature the directory of the feature
   * @param featureProps the feature property file
   * @return html
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  private static String generateFeatureInfo(File feature, File featureProps) throws ParserConfigurationException, SAXException, IOException
  {
    Document doc = XmlParserUtil.parseXmlDocument(feature, false);
    Element rootElement = (Element)doc.getElementsByTagName("feature").item(0);
    Properties properties = loadProperties(featureProps);
    if (resolveProperty(properties, rootElement.getAttribute("provider-name")).toUpperCase().contains("IVYTEAM"))
    {
      return null;
    }
      
    StringBuilder info = new StringBuilder();
    info.append("    <tr>\n");
    info.append("      <td>");
    info.append(resolveProperty(properties, rootElement.getAttribute("label")));
    info.append("</td>\n");
    info.append("      <td>");
    info.append(resolveProperty(properties, rootElement.getAttribute("id")));
    info.append("</td>\n");
    info.append("      <td>");
    NodeList descriptions = doc.getElementsByTagName("description");
    String description = "";
    if (descriptions != null && descriptions.getLength()>0)
    {
      description = ((Element)descriptions.item(0)).getTextContent();
    }
    info.append(resolveProperty(properties, description));
    info.append("</td>\n");
    info.append("      <td>");
    String version = rootElement.getAttribute("version");
    Matcher matcher = Pattern.compile("([0-9]*(\\.[0-9]*)?(\\.[0-9]*)?)").matcher(version);
    if (matcher.find())
    {
      version = matcher.group(1);
    }    
    info.append(resolveProperty(properties, version));
    info.append("</td>\n");
    info.append("      <td>");
    info.append(resolveProperty(properties, rootElement.getAttribute("provider-name")));
    info.append("</td>\n");
    info.append("    </tr>");
    return info.toString();
  }

  /**
   * Gets the value of the property with the given attribute name
   * @param properties the properties
   * @param attribute the attribute name
   * @return property value
   */
  private static String resolveProperty(Properties properties, String attribute)
  {
    attribute = attribute.trim();
    if (properties != null && attribute.startsWith("%"))
    {
      return XmlUtil.escapeHtmlAndConvertNewline(properties.getProperty(attribute.substring(1)));
    }
    return XmlUtil.escapeHtmlAndConvertNewline(attribute);
  }

  /**
   * Load the properties from the given properties file 
   * @param featureProps properties file
   * @return feature properties
   * @throws IOException 
   */
  private static Properties loadProperties(File featureProps) throws IOException
  {
    Properties properties = new Properties();
    if (featureProps.exists())
    {
      InputStream is = new FileInputStream(featureProps);
      try
      {
        properties.load(is);
        return properties;
      }
      finally
      {
        IOUtils.closeQuietly(is);
      }
    }
    return null;
  }
}
