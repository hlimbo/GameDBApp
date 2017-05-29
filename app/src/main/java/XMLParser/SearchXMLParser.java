package XMLParser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harvey on 5/29/2017.
 */

public class SearchXMLParser
{
    //namespace string
    private static final String ns = null;

    //returns a list of games retrieved from xml file.
    public List parse(String xmlString) throws XmlPullParserException, IOException
    {
        //convert xmlString output to InputStream object
        InputStream in = new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8")));

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readXML(parser);
        } finally {
            in.close();
        }

    }

    //TODO(HARVEY): handle xml parsing up to 4 levels of depth to access game name.
    //<search_results>
    //      <row class="games_row">
    //          <field class="games_name">
    //                  <a>
    //                      <atext> NAME OF GAME HERE....

    //I have to go into 4 levels of depth to access the name of the game... this will take some time.

    private List readXML(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        List entries = new ArrayList<String>();
        parser.require(XmlPullParser.START_TAG, ns, "search_results");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            //could introduce bugs since atext is used for names other than game name.
            //e.g. platform name <atext>GB</atext>
            if (name.equals("atext"))
            {
                entries.add(this.readTextValue(parser));
            }
            else
            {
                skip(parser);
            }
        }

        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if(parser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }

        int depth = 1;
        while(depth != 0)
        {
            switch(parser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readTextValue(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String result = "";
        if(parser.next() == XmlPullParser.TEXT)
        {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }
}
