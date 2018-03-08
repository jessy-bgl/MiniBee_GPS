package com.minibee.gps.minibee_gps;


import android.content.Context;

import java.io.File;
import java.io.IOException;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;


/**
 *
 * @author Beydi Sanogo
 */
public class Itineraire {

    int nombreDePas = 0;
    private static String xmlFile;
    private final DocumentBuilderFactory factory;
    private Context context;
    private static String path_file;

    public Itineraire(Context context, String path)  {

        this.factory = DocumentBuilderFactory.newInstance();
        this.xmlFile = "file.xml";
        this.context = context;
        this.path_file = path;

        try {

	    /*
	     * Etape 2 : création d'un parseur
	     */
            final DocumentBuilder builder = factory.newDocumentBuilder();

	    /*
	     * Etape 3 : création d'un Document
	     */
            final Document document= builder.newDocument();

	    /*
	     * Etape 4 : création de l'Element racine
	     */
            final Element racine = document.createElement("Positions");
            document.appendChild(racine);

	    /*
	     * Etape 5 : création d'une personne
	     */
            final Comment commentaire = document.createComment("Ce fichier contient les coordonnées ");
            racine.appendChild(commentaire);


	    /*
	     * Etape 8 : affichage
	     */
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);
            final StreamResult sortie = new StreamResult(new File(path_file + xmlFile));
            //final StreamResult result = new StreamResult(System.out);

            //prologue
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

            //formatage
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            //sortie
            transformer.transform(source, sortie);
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }

    }


    public void addPosition(float _latitude, float _longitude, float _altitude ) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document document = docBuilder.parse(new File(path_file + xmlFile));

        Node racine = document.getElementsByTagName("Positions").item(0);

        final Element position = document.createElement("position");
        Attr attr = document.createAttribute("id");
        attr.setValue(""+nombreDePas+"");
        position.setAttributeNode(attr);
        nombreDePas++;

        final Element latitude = document.createElement("latitude");
        latitude.appendChild(document.createTextNode(""+_latitude+""));

        final Element longitude = document.createElement("longitude");
        longitude.appendChild(document.createTextNode(""+_longitude+""));

        final Element altitude = document.createElement("altitude");
        altitude.appendChild(document.createTextNode(""+_altitude+""));

        position.appendChild(latitude);
        position.appendChild(longitude);
        position.appendChild(altitude);

        racine.appendChild(position);

        Source source = new DOMSource(document);
        //File file = new File(xmlFile);
        Result result = new StreamResult(path_file + xmlFile);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
    }

    public  void addItineraire(List<Position> positions) {

        try {
	    /*
	     * Etape 2 : création d'un parseur
	     */
            final DocumentBuilder builder = factory.newDocumentBuilder();

	    /*
	     * Etape 3 : création d'un Document
	     */
            final Document document= builder.newDocument();

	    /*
	     * Etape 4 : création de l'Element racine
	     */
            final Element racine = document.createElement("Positions");
            document.appendChild(racine);

	    /*
	     * Etape 5 : création d'une personne
	     */
            final Comment commentaire = document.createComment("Ce fichier contient les coordonnées ");
            racine.appendChild(commentaire);


            for(int i=0; i<positions.size(); i++){
                final Element position = document.createElement("position");
                Attr attr = document.createAttribute("id");
                attr.setValue(""+nombreDePas+"");
                position.setAttributeNode(attr);

                final Element latitude = document.createElement("latitude");
                latitude.appendChild(document.createTextNode(""+positions.get(i).getLatitude()+""));

                final Element longitude = document.createElement("longitude");
                longitude.appendChild(document.createTextNode(""+positions.get(i).getLongitude()+""));

                final Element altitude = document.createElement("altitude");
                altitude.appendChild(document.createTextNode(""+positions.get(i).getAltitude()+""));

                position.appendChild(latitude);
                position.appendChild(longitude);
                position.appendChild(altitude);

                racine.appendChild(position);
                nombreDePas++;
            }

	    /*
	     * Etape 8 : affichage
	     */
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(document);
            final StreamResult sortie = new StreamResult(new File(path_file + xmlFile));
            //final StreamResult result = new StreamResult(System.out);

            //prologue
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

            //formatage
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            //sortie
            transformer.transform(source, sortie);
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    public static List<Position> getItineraire(){
        List<Position> positions = null;
        float _latitude, _longitude, _altitude;
        try {
            File fXmlFile = new File(path_file + xmlFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("position");

            //System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                //System.out.println(nNode.getNodeName()+" actuel ");

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    //System.out.println("Numeros de  position : " + eElement.getAttribute("id"));
                    _latitude = Float.parseFloat( eElement.getElementsByTagName("latitude").item(0).getTextContent() );
                    _longitude = Float.parseFloat( eElement.getElementsByTagName("longitude").item(0).getTextContent() );
                    _altitude = Float.parseFloat( eElement.getElementsByTagName("altitude").item(0).getTextContent() );
                    //System.out.println("latitude : " + _latitude);
                    //System.out.println("longitude : " + _longitude);
                    //System.out.println("altitude : " + _altitude);
                    // positions.add(new Position())
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return positions;
    }
}