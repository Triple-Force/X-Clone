package Shared.Database.XMLManager;

import Shared.Database.Database;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

public class PersistenceXmlReader
{
    public static String readFromPersistenceXml(String filePath, String persistenceUnitName, String attributeName)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory.newDocumentBuilder().parse(
                    Database.class.getClassLoader().getResourceAsStream(filePath));

            NodeList units = document.getElementsByTagName("persistence-unit");
            for (int i = 0; i < units.getLength(); i++)
            {
                Element unit = (Element) units.item(i);
                if (!persistenceUnitName.equals(unit.getAttribute("name")))
                    continue;

                NodeList props = unit.getElementsByTagName("property");
                for (int j = 0; j < props.getLength(); j++)
                {
                    Element prop = (Element) props.item(j);
                    if (attributeName.equals(prop.getAttribute("name")))
                        return prop.getAttribute("value");
                }
            }
            throw new RuntimeException("Attribute '" + attributeName + "' not found in '" + filePath + "'");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse " + filePath, e);
        }
    }
}
