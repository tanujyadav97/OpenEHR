package com.hackslash.medeformlibrary;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.serialize.XMLSerializer;

import se.acode.openehr.parser.ADLParser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.os.Environment;
import android.util.Pair;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Parser {
    public static Map<Integer, ArrayList<Integer>> graph = new HashMap<>();
    public static ArrayList<ArrayList<String>> childData = new ArrayList<>();
    public static ArrayList<String> childNames = new ArrayList<>();
    public static Map<String, Pair<String, String>> fieldDesc = new HashMap<>();

    public static void getData(String filename, String type, String Path) {
        graph.clear();
        childData.clear();
        childNames.clear();
        fieldDesc.clear();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+Path+"/";
        File adlFile = new File(path + filename);

        try {
            ADLParser parser = new ADLParser(adlFile);
            Archetype at = parser.parse();
//			System.out.println(at.toString());
            XMLSerializer xmlser = new XMLSerializer();
            String xmlstring = xmlser.output(at);
//			System.out.println(xmlstring);
            CComplexObject c = at.getDefinition();
//			System.out.println(c.toString());

            xmlParser(xmlstring, type);
        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }

    /**
     * Parsing the xml
     */
    public static void xmlParser(String xmlstring, String type) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlstring)));

            doc.getDocumentElement().normalize();

            ArrayList<Node> nlist = getNamedChild(doc.getElementsByTagName("definition").item(0), "attributes");

            if (type.equals("EVALUATION") || type.equals("OBSERVATION"))
                nlist = getChildWithTagName(nlist, "rm_attribute_name", "data");
            else if (type.equals("ACTION"))
                nlist = getChildWithTagName(nlist, "rm_attribute_name", "description");
            else if (type.equals("INSTRUCTION"))
                nlist = getChildWithTagName(nlist, "rm_attribute_name", "activities");

            /*
              this list contains all useful children
             */
            ArrayList<Node> childnodes = getAncestorsWithTagName(nlist.get(0), "rm_type_name", "ELEMENT");

            /*
              text and description of fields
             */
            getFieldName(doc.getElementsByTagName("ontology").item(0));

            for (int i = 0; i < childnodes.size(); i++) {
                childNames.add(fieldDesc.get(getTagContent(childnodes.get(i), "node_id")).second);
                System.out.println(childNames.get(i));
            }

            System.out.println(childnodes.size());

            System.out.println(graph.toString());

            dataFromObject(childnodes);

            for (int i = 0; i < childData.size(); i++)
                System.out.println(childData.get(i));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Node> getNamedChild(Node node, String name) {
        ArrayList<Node> nlist = new ArrayList<>();
        NodeList child = node.getChildNodes();
        for (int i = 0; i < child.getLength(); i++) {
            if (child.item(i).getNodeType() == Node.ELEMENT_NODE && child.item(i).getNodeName().equals(name))
                nlist.add(child.item(i));
        }
        return nlist;
    }

    public static ArrayList<Node> getNamedChild(Node node, String name1, String name2) {
        ArrayList<Node> nlist = new ArrayList<>();
        NodeList child = node.getChildNodes();
        for (int i = 0; i < child.getLength(); i++) {
            if (child.item(i).getNodeType() == Node.ELEMENT_NODE
                    && (child.item(i).getNodeName().equals(name1) || child.item(i).getNodeName().equals(name2)))
                nlist.add(child.item(i));
        }
        return nlist;
    }

    public static String getTagContent(Node node, String tag) {
        ArrayList<Node> child = getNamedChild(node, tag);
        if (child.size() == 0)
            return "";
        return child.get(0).getTextContent();
    }

    public static void getFieldName(Node node) {
        Node tmpnode = getNamedChild(node, "term_definitions").get(0);
        ArrayList<Node> labels = getNamedChild(tmpnode, "items");

        for (int i = 0; i < labels.size(); i++) {
            String code = labels.get(i).getAttributes().item(0).getNodeValue();
            ArrayList<Node> desclist = getNamedChild(labels.get(i), "items");
            String desc = "", text = "";
            for (int j = 0; j < desclist.size(); j++) {
                if (desclist.get(j).getAttributes().item(0).getNodeValue().equals("description"))
                    desc = desclist.get(j).getTextContent();
                else if (desclist.get(j).getAttributes().item(0).getNodeValue().equals("text"))
                    text = desclist.get(j).getTextContent();
            }
            fieldDesc.put(code, new Pair<String, String>(desc, text));
        }
    }

    public static ArrayList<Node> getChildWithTagName(ArrayList<Node> list, String tag, String value) {
        ArrayList<Node> newlist = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (getTagContent(list.get(i), tag).equals(value))
                newlist.add(list.get(i));
        }
        return newlist;
    }

    public static ArrayList<Node> getAncestorsWithTagName(Node root, String tag, String value) {
        ArrayList<Node> ans = new ArrayList<>();
        ArrayList<Pair<Integer, Node>> temp = new ArrayList<>();
        temp.add(new Pair<Integer, Node>(-1, root));
        int i = 0;
        while (i < temp.size()) {
            int key = temp.get(i).first;
            ArrayList<Node> child = getNamedChild(temp.get(i).second, "children", "attributes");
            for (int j = 0; j < child.size(); j++)
                if (getTagContent(child.get(j), "rm_attribute_name").equals("data")) {
                    Node nod = child.get(j);
                    child.clear();
                    child.add(nod);
                }

            for (int j = 0; j < child.size(); j++)
                if (getTagContent(child.get(j), tag).equals(value)) {
                    ans.add(child.get(j));
                    if (graph.containsKey(key)) {
                        ArrayList<Integer> templist = graph.get(key);
                        templist.add(ans.size() - 1);
                        graph.put(key, templist);
                    } else {
                        ArrayList<Integer> templist = new ArrayList<>();
                        templist.add(ans.size() - 1);
                        graph.put(key, templist);
                    }
                } else if (getTagContent(child.get(j), tag).equals("CLUSTER")
                        && getChildWithTagName(getNamedChild(child.get(j), "attributes"), "rm_attribute_name", "items")
                        .size() > 0) {
                    ans.add(child.get(j));
                    if (graph.containsKey(key)) {
                        ArrayList<Integer> templist = graph.get(key);
                        templist.add(ans.size() - 1);
                        graph.put(key, templist);
                    } else {
                        ArrayList<Integer> templist = new ArrayList<>();
                        templist.add(ans.size() - 1);
                        graph.put(key, templist);
                    }
                    temp.add(new Pair<Integer, Node>(ans.size() - 1, child.get(j)));
                } else
                    temp.add(new Pair<Integer, Node>(key, child.get(j)));
            i++;
        }
        return ans;
    }

    public static void dataFromObject(ArrayList<Node> objs) {
        for (int i = 0; i < objs.size(); i++) {
            if (!getTagContent(objs.get(i), "rm_type_name").equals("ELEMENT")) {
                ArrayList<String> temp = new ArrayList<>();
                childData.add(temp);
                continue;
            }
            ArrayList<Node> childs = getNamedChild(getNamedChild(objs.get(i), "attributes").get(0), "children");
            if (childs.size() == 1) {
                switch (getTagContent(childs.get(0), "rm_type_name")) {
                    case "DV_QUANTITY":
                    case "DvQuantity":
                        childData.add(getQuantityData(childs.get(0)));
                        break;
                    case "DV_TEXT":
                        childData.add(getTextData(childs.get(0)));
                        break;
                    case "DV_COUNT":
                        childData.add(getCountData(childs.get(0)));
                        break;
                    case "DV_ORDINAL":
                    case "DvOrdinal":
                        childData.add(getOrdinalData(childs.get(0)));
                        break;
                    case "DV_DATE_TIME":
                        childData.add(getDateTimeData(childs.get(0)));
                        break;
                    case "DV_BOOLEAN":
                        childData.add(getBooleanData(childs.get(0)));
                        break;
                    case "DV_DURATION":
                        childData.add(getDurationData(childs.get(0)));
                        break;
                    case "DV_PARSABLE":
                        childData.add(getParsableData(childs.get(0)));
                        break;
                    case "DV_CODED_TEXT":
                        childData.add(getCodedTextData(childs.get(0)));
                        break;
                    default:
                        System.out.println("no match");
                }
            } else {
                childData.add(getChoiceData(childs));
            }
        }
    }

    private static ArrayList<String> getChoiceData(ArrayList<Node> childs) {
        ArrayList<String> list = new ArrayList<>();
        list.add("DV_CHOICE");

        for (int i = 0; i < childs.size(); i++) {
            ArrayList<String> temp = new ArrayList<>();

            switch (getTagContent(childs.get(i), "rm_type_name")) {
                case "DV_QUANTITY":
                case "DvQuantity":
                    temp = getQuantityData(childs.get(i));
                    break;
                case "DV_TEXT":
                    temp = getTextData(childs.get(i));
                    break;
                case "DV_COUNT":
                    temp = getCountData(childs.get(i));
                    break;
                case "DV_ORDINAL":
                case "DvOrdinal":
                    temp = getOrdinalData(childs.get(i));
                    break;
                case "DV_DATE_TIME":
                    temp = getDateTimeData(childs.get(i));
                    break;
                case "DV_BOOLEAN":
                    temp = getBooleanData(childs.get(i));
                    break;
                case "DV_DURATION":
                    temp = getDurationData(childs.get(i));
                    break;
                case "DV_PARSABLE":
                    temp = getParsableData(childs.get(i));
                    break;
                case "DV_CODED_TEXT":
                    temp = getCodedTextData(childs.get(0));
                    break;
                default:
                    System.out.println("no match in choice");
            }
            if (temp != null) {
                for (int j = 0; j < temp.size(); j++)
                    list.add(temp.get(j));
            }
        }
        return list;
    }

    private static ArrayList<String> getCodedTextData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        NodeList fields = ((Element) node).getElementsByTagName("code_list");

        list.add("DV_CODED_TEXT");
        for (int i = 0; i < fields.getLength(); i++) {
            list.add(fieldDesc.get(fields.item(i).getTextContent()).second);
        }

        return list;
    }

    private static ArrayList<String> getParsableData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        NodeList fields = ((Element) node).getElementsByTagName("list");

        list.add("DV_PARSABLE");
        for (int i = 0; i < fields.getLength(); i++) {
            list.add(fields.item(i).getTextContent());
        }

        return list;
    }

    private static ArrayList<String> getDurationData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        list.add("DV_DURATION");
        return list;
    }

    private static ArrayList<String> getBooleanData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        list.add("DV_BOOLEAN");
        return list;
    }

    private static ArrayList<String> getDateTimeData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        list.add("DV_DATE_TIME");
        return list;
    }

    private static ArrayList<String> getOrdinalData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        NodeList fields = ((Element) node).getElementsByTagName("code_string");

        list.add("DV_ORDINAL");
        for (int i = 0; i < fields.getLength(); i++) {
            list.add(fieldDesc.get(fields.item(i).getTextContent()).second);
        }

        return list;
    }

    private static ArrayList<String> getCountData(Node node) {
        ArrayList<String> list = new ArrayList<>();

        list.add("DV_COUNT");
        if (((Element) node).getElementsByTagName("range").getLength() > 0) {
            list.add(getTagContent(((Element) node).getElementsByTagName("range").item(0), "lower"));
            list.add(getTagContent(((Element) node).getElementsByTagName("range").item(0), "upper"));
        }
        return list;
    }

    private static ArrayList<String> getTextData(Node node) {
        ArrayList<String> list = new ArrayList<>();
        list.add("DV_TEXT");
        return list;
    }

    private static ArrayList<String> getQuantityData(Node node) {
        Node list_node = getNamedChild(node, "list").get(0);
        ArrayList<String> list = new ArrayList<>();

        list.add("DV_QUANTITY");
        if (getNamedChild(list_node, "magnitude").size() > 0) {
            list.add(getTagContent(getNamedChild(list_node, "magnitude").get(0), "lower"));
            list.add(getTagContent(getNamedChild(list_node, "magnitude").get(0), "upper"));
        } else {
            list.add("");
            list.add("");
        }
        list.add(getTagContent(list_node, "units"));

        return list;
    }
}
