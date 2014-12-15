/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsect;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author christina Christodoulakis
 * @email christina at cs.toronto.edu
 * messy code.
 * reads from the clinical trials xml, and extracts conditions studied and officials involved.
 * removes ascii characters (http://stackoverflow.com/questions/8519669/replace-non-ascii-character-from-string)
 * writes to csv file "ConditionInvestigator.csv" delimited with "|"
 */
 
public class ParseCT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String columnName = "Condition" + " | " + "OfficialName" + "\n";;
                //+ " | "+"OfficialRole \n"; // ...
        String dataOutput = "";

        // entering try catch
        try {
            // set up document read/build
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            // read the file into the program
            Document doc = docBuilder.parse(new File("src/resources/ClinicalTrials.xml"));

            // normalize text representation
            doc.getDocumentElement().normalize();
            System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());

            //Get count of values per tags
            NodeList listOfClinicalStudies = doc.getElementsByTagName("clinical_study");
            // perform count of documents retrieved
            int clinical_study = listOfClinicalStudies.getLength();
            System.out.println("Total number of clinical studies : " + clinical_study);
            //loop through studies and fill nodes
            for (int s = 0; s < listOfClinicalStudies.getLength(); s++) {
                //Set up ClinicalStudyNode to gather data into elements
                Node study = listOfClinicalStudies.item(s);
                if (study.getNodeType() == Node.ELEMENT_NODE) {
                    Element studyElement = (Element) study;
                    //load to respective elements
                    NodeList ConditionList = studyElement.getElementsByTagName("condition");
                    NodeList OfficialList = studyElement.getElementsByTagName("overall_official");
                    Node conditionNode = (Node) ConditionList.item(0);
                    if (conditionNode != null) {
                        String condition = conditionNode.getTextContent();
                        for (int o = 0; o < OfficialList.getLength(); o++) {
                            Element official = (Element) OfficialList.item(o);
                            String officialName = official.getElementsByTagName("last_name").item(0).getTextContent();
                            //String officialRole = official.getElementsByTagName("role").item(0).getTextContent();
                            // load values to columns set up
                            dataOutput = dataOutput + condition.replaceAll("[^\\x00-\\x7F]", "") + " | " + officialName.replaceAll("[^\\x00-\\x7F]", "") + "\n";;
                                    //+ " | " + officialRole + "\n";
                        }
                    }
                }
            }
            System.out.println("This is for Debugging measures: "
                    + columnName + dataOutput);
            // Instantiate file location...
            DataOutputStream outBound = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream("../ConditionInvestigator.csv")));
            // write data into columns set up
            outBound.writeBytes(columnName + dataOutput);
            //close the writer
            outBound.close();
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }// end of program
}
