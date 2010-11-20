package se.vgregion.push.types;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

public class XmlUtil {

    private static final Builder PARSER = new Builder();
    
    public static List<Element> stringToXml(String xml) {
        try {
            Document document = PARSER.build(new StringReader("<doc>" + xml + "</doc>"));
            
            Elements elms = document.getRootElement().getChildElements();
            
            List<Element> elmsList = new ArrayList<Element>();
            for(int i = 0; i<elms.size(); i++) {
                elmsList.add((Element) elms.get(i).copy());
            }
            return elmsList;
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
