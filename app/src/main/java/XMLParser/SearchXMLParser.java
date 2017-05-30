package XMLParser;

import android.util.Log;
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
    public ArrayList<String> parse(String xmlString) throws XmlPullParserException, IOException
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

    private ArrayList<String> readXML(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        ArrayList<String> entries = new ArrayList<String>();
       parser.require(XmlPullParser.START_TAG, ns, "search_results");
        while (parser.getEventType() != XmlPullParser.END_DOCUMENT)
        {
            switch (parser.getEventType())
            {
                case XmlPullParser.START_TAG:
                    Log.d("XML", "header tag: " + parser.getName());
                    if (parser.getName().equals("row"))
                    {
                        Log.d("XML1",parser.getName());
                        if (parser.getAttributeCount() > 0 && parser.getAttributeValue(ns, "class").equals("games_row"))
                        {
                            Log.d("XML2",parser.getAttributeValue(ns,"class"));
                            this.handleFields(parser,entries);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    Log.d("XML", "footer tag: " + parser.getName());
                    break;
                case XmlPullParser.TEXT:
                    Log.d("XML", "text: " + parser.getText());
                    break;
            }

            parser.next();

        }

        return entries;
    }

    private void handleFields(XmlPullParser parser,ArrayList<String> entries) throws XmlPullParserException, IOException
    {
        Integer relativeDepth = parser.getDepth();
        Integer currentDepth = relativeDepth;
        //throws an XmlPullParserException if the header tag type does not match <row>
        parser.require(XmlPullParser.START_TAG,ns,"row");
        Log.d("fields", parser.getName());

        Log.d("REL_DEPTH",relativeDepth.toString());
        parser.nextTag();
        currentDepth = parser.getDepth();
        while(currentDepth != relativeDepth)
        {
            if(parser.getName() == null || parser.getName().equals("fields") && parser.getEventType() == XmlPullParser.END_TAG)
            {
                break;
            }

            Log.d("CurrentDepth",currentDepth.toString());
            Log.d("fields2", parser.getName());

            String classname = parser.getAttributeValue(ns,"class");

            if(classname != null)
            {
                if (classname.equals("games_year"))
                {
                    parser.next();//fields text
                    Log.d("YEAR", parser.getText());
                }
                else if (classname.equals("games_price"))
                {
                    parser.next();//fields text
                    Log.d("PRICE", parser.getText());
                }
                else if (classname.equals("games_name"))
                {
                    Log.d("GAME", "games_name header");
                    String gameName = this.retrieveGameName(parser);
                    if(gameName != null)
                    {
                        Log.d("GAME-NAME", gameName);
                        entries.add(gameName);
                    }
                    else
                        Log.d("GAMERROR", "NULL");
                }
            }

            parser.next();//fields footer
            currentDepth = parser.getDepth();
        }


    }

    private String retrieveGameName(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        Integer relativeDepth = parser.getDepth();
        Integer currentDepth = relativeDepth;

        String gameName = "";
        parser.require(XmlPullParser.START_TAG,ns,"field");
        parser.next();//a header
        parser.next();//href header
        parser.next();//href text
        parser.next();//href footer
        parser.next();//atext header
        parser.next();//atext text

        gameName = parser.getText();

        parser.next();//atext footer
        parser.next();//a footer

        return gameName;
    }

    //returns true if any whitespace was skipped, false otherwise ~ BROKEN METHOD
    private boolean skipWhitespace(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        boolean whitespaceSkipped = false;
        if(parser.getEventType() == XmlPullParser.TEXT)
        {
            while(parser.isWhitespace())
            {
                parser.next();
                whitespaceSkipped = true;
            }
        }

        return whitespaceSkipped;
    }

    private boolean isStartTag(XmlPullParser parser,String tagname) throws XmlPullParserException, IOException
    {
        return parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals(tagname);
    }

    private boolean isEndTag(XmlPullParser parser,String tagname) throws XmlPullParserException, IOException
    {
        return parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(tagname);
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
