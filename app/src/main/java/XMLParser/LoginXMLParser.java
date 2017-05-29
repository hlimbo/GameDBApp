package XMLParser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harvey on 5/28/2017.
 */

public class LoginXMLParser {
    //no namespaces
    private static final String ns = null;

    //returns a map of key-value pairs in the xml file
    //e.g. map["status"] returns the value "failure" if email password entered in is invalid.
    public Map parse(String xmlString) throws XmlPullParserException, IOException
    {
        //convert xmlString output to InputStream object
        InputStream in = new ByteArrayInputStream(xmlString.getBytes(Charset.forName("UTF-8")));

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLoginStatus(parser);
        } finally {
            in.close();
        }

    }

    private Map readLoginStatus(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map entries = new HashMap<String, String>();
        parser.require(XmlPullParser.START_TAG, ns, "login_status");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("status_code"))
            {
                entries.put("status_code",this.readTextValue(parser));
            }
            else if(name.equals("status"))
            {
                entries.put("status",this.readTextValue(parser));
            }
            else if(name.equals("message"))
            {
                entries.put("message",this.readTextValue(parser));
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