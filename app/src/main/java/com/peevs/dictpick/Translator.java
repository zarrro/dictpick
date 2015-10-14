package com.peevs.dictpick;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zarrro on 15.8.2015 г..
 */
public class Translator {

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_TRANSLATE_MODE = "at";

    public static final String PROTO = "https";
    public static final String HOST = "translate.google.com";
    public static final String QUERY_STRING_TEMPLATE = "/translate_a/single?"
            + "client=a&sl=%s&tl=%s&dt=%s&ie=%s&oe=%s&q=%s";
//    public static final String HTTP_HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1)"
//            + " AppleWebKit/537.36 (HTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    public static final String HTTP_HEADER_USER_AGENT =
        "User-Agent: Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko";

    /**
     * Translates the srcText from srcLang to targetLang using HTTP call to google.bg/translate.
     *
     * @param srcText    - text to be translated
     * @param srcLang    - the language of the srcText
     * @param targetLang - the language in which the srcText to be translated
     * @return - list of translations, by decreasing popularity (the most popular translation is first)
     */
    public static List<String> translate(String srcText, String srcLang, String targetLang) {
        return translate(srcText, srcLang, targetLang, DEFAULT_TRANSLATE_MODE, DEFAULT_CHARSET);
    }

    public static List<String> translate(String srcText, String srcLang, String targetLang,
                                         String mode, String charset) {
        try {
            URL url = new URL(PROTO, HOST, String.format(QUERY_STRING_TEMPLATE, srcLang,
                    targetLang, mode, charset, charset, URLEncoder.encode(srcText, charset)));
            URLConnection gooCon = url.openConnection();
            gooCon.setRequestProperty("User-Agent", HTTP_HEADER_USER_AGENT);

            Reader in = new InputStreamReader(gooCon.getInputStream(), Charset.forName("UTF-8"));

            LinkedList<String> res = new LinkedList<String>();
            if (in != null) {
                boolean w = false;
                try {
                    int val = -1;
                    char c = '\n';
                    StringWriter sw = null;

                    while ((val = in.read()) != -1) {
                        c = (char) val;
                        if (c == '"') {
                            w = !w;
                            if (sw == null) {
                                sw = new StringWriter();
                            } else {
                                String word = sw.toString();
                                // skip src lang and text in the result
                                if (!(word.equals(srcLang) || word.equalsIgnoreCase(srcText))) {
                                    res.add(sw.toString());
                                }
                                sw = null;
                            }
                        } else if (w) {
                            sw.write(c);
                        }
                    }
                } finally {
                    in.close();
                }
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
