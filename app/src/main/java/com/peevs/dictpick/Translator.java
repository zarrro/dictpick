package com.peevs.dictpick;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by zarrro on 15.8.2015 г..
 */
public class Translator {

    private static final int tkk = 402953;

    public static final String TAG = Translator.class.getSimpleName();

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_TRANSLATE_MODE = "at";

    public static final String PROTO = "https";
    public static final String HOST = "translate.google.com";
    public static final String QUERY_STRING_TEMPLATE = "/translate_a/single?"
            + "client=t&sl=%s&tl=%s&dt=%s&ie=%s&oe=%s";
    public static final String QUERY_STRING_TTS_TEMPLATE =
            "/translate_tts?&client=t&tl=%s&ie=UTF-8&tk=699068|821817&q=%s";
    public static final String HTTP_HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1)"
            + " AppleWebKit/537.36 (HTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    private static String[] clientVals = new String[]{"a", "t", "s"};
    private static String[] tkParamVals = new String[] {""};

    /**
     * Translates the srcText from foreignLang to nativeLang using HTTP call to google.bg/translate.
     *
     * @param srcText    - text to be translated
     * @param srcLang    - the language of the srcText
     * @param targetLang - the language in which the srcText to be translated
     * @return - list of translations, by decreasing popularity (the most popular translation is first)
     */
    public static List<String> translate(String srcText, String srcLang, String targetLang)
            throws IOException {
        return translate(srcText, srcLang, targetLang, DEFAULT_TRANSLATE_MODE, DEFAULT_CHARSET);
    }

    public static List<String> translate(String srcText, String srcLang, String targetLang,
                                         String mode, String charset) throws IOException {

        StringBuilder httpQueryString = new StringBuilder(String.format(QUERY_STRING_TEMPLATE,
                srcLang, targetLang, mode, charset, charset));
        httpQueryString.append(kc());
        httpQueryString.append(tkk(srcText));
        httpQueryString.append("&q=" + URLEncoder.encode(srcText, charset));

        String queryStr = httpQueryString.toString();
        Log.i(TAG, queryStr);

        URL url = new URL(PROTO, HOST, queryStr);
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
    }

    public static void textToSpeach(String text, Language lang, OutputStream outputStream)
            throws IOException {
        if (text == null || text.isEmpty())
            throw new IllegalArgumentException("text == null || text.isEmpty()");
        if (outputStream == null)
            throw new IllegalArgumentException("outputStream == null");
        String langStr = null;
        switch (lang) {
            case EN:
                langStr = "en-us";
                break;
            default:
                Log.i(TAG, "Default translation language is used");
                langStr = "en-us";
        }

        InputStream in = null;
        try {
            URL url = new URL(PROTO, HOST, String.format(QUERY_STRING_TTS_TEMPLATE, langStr,
                    URLEncoder.encode(text, "UTF-8")));
            URLConnection gooCon = url.openConnection();
            gooCon.setRequestProperty("User-Agent", HTTP_HEADER_USER_AGENT);
            gooCon.setRequestProperty("x-client-data", "CKK2yQEIqbbJAQjEtskBCOmIygEI/ZXKAQi8mMoB");
            in = gooCon.getInputStream();

            int i = -1;
            while ((i = in.read()) != -1) {
                outputStream.write(i);
            }
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                Log.wtf(TAG, e);
            }
        }
    }

    // tk value calculation (based on js reverse engineering)
    private static long rl(long a, String b) {
        long res = a;
        for (int c = 0; c < b.length() - 2; c += 3) {
            char dChar = b.charAt(c + 2);
            long dInt = dChar >= 'a' ? (int) dChar - 87 : Long.valueOf(String.valueOf(dChar));
            char char2 = b.charAt(c + 1);
            long dInt2 = char2 == '+' ? res >>> dInt : res << dInt;
            res = b.charAt(c) == '+' ? res + dInt2 & 4294967295l : res ^ dInt2;
        }
        return res;
    }

    private static String tkk(String a) {
        String c = "&tk=";
        ArrayList<Long> d = new ArrayList<>();
        int e = 0;
        int f = 0;
        for (; f < a.length(); f++) {
            long g = Character.codePointAt(a, f);
            if (128 > g) {
                d.add(g);
            } else if (2048 > g) {
                d.add(g >> 6 | 192);
            } else if (55296 == (g & 64512) && f + 1 < a.length() &&
                    56320 == (Character.codePointAt(a, f + 1) & 64512)) {
                g = 65536 + ((g & 1023) << 10) + (Character.codePointAt(a, ++f) & 1023);
                d.add(g >> 18 | 240);
                d.add(g >> 12 & 63 | 128);
            } else {
                d.add(g >> 12 | 224);
                d.add(g >> 6 & 63 | 128);
                d.add(g & 63 | 128);
            }
        }
        long ret = tkk;
        for (long val : d) {
            ret += val;
            ret = rl(ret, "+-a^+6");
        }
        ret = rl(ret, "+-3^+b+-f");
        if (0 > ret) {
            ret = (ret & 2147483647l) + 2147483648l;
        }
        ret %= 1E6;
        return c + (ret + "." + (ret ^ tkk));
    }

    private static String kc() {
        return "&kc=" + (Math.round(Math.random() * 10) % 3 + 1);
    }
}
