package org.colm.code;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class StringUtil {

    private StringUtil() {}

    public static String getUtf8String(String originalString) throws UnsupportedEncodingException {
        if (originalString == null) {
            return null;
        }
        if (originalString == "") {
            return "";
        }
        if (originalString.equals(new String(originalString.getBytes(), StandardCharsets.UTF_8))) {
            return originalString;
        }
        Set<String> charsetNameSet = Charset.availableCharsets().keySet();
        for (String chasetName : charsetNameSet) {
            if (originalString.equals(new String(originalString.getBytes(StandardCharsets.UTF_8), chasetName))) {
                return new String(originalString.getBytes(chasetName), StandardCharsets.UTF_8);
            }
        }
        throw new RuntimeException("没有探测到字符集");
    }

    public static String valueOf(Object object) {
        if (object == null) {
            return null;
        }
        return String.valueOf(object);
    }

}
