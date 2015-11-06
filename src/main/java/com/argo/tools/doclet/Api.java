package com.argo.tools.doclet;

import com.argo.annotation.ApiDoc;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Map;

/**
 * Created by yamingd on 10/16/15.
 */
public class Api {

    public String controllerName;
    public RequestMapping packageMapping;
    public ApiDoc apiDoc;

    public String methodName;
    public Map<String, AnnotationValue> apiMethodDoc;
    public RequestMapping methodMapping;

    public List<VariableElement> parameters;

    /**
     *
     * @return String
     */
    public String getHttpMethod(){
        String s = "";
        for (int i = 0; i < methodMapping.method().length; i++) {
            s += methodMapping.method()[i].name() + ", ";
        }
        s = s.substring(0, s.length() - 2);
        return s;
    }

    /**
     *
     * @return String
     */
    public String getProduces(){
        String s = "";
        for (int i = 0; i < methodMapping.produces().length; i++) {
            s += methodMapping.produces()[i] + ", ";
        }
        s = s.substring(0, s.length() - 2);
        return s;
    }

    public String nullOr(Object v){
        return v == null ? "" : v.toString();
    }

    /**
     *
     * @return String
     */
    public String getVersion(){
        String v = "";
        if (null == apiMethodDoc){
            v = apiDoc.version();
        }else{
            v = nullOr(apiMethodDoc.get("version"));
        }

        if (null == v || v.length() == 0){
            v = apiDoc.version();
        }

        if (null == v || v.length() == 0){
            v = "1.0.0";
        }

        return v;
    }

    public String decodeUnicode(final String dataStr) {
        if (null == dataStr || dataStr.length() == 0){
            return "";
        }
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start);
            String charStr = "";
            if (end >= 0) {
                charStr = dataStr.substring(end + 2, end + 2 + 4);
                char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
                buffer.append(new Character(letter).toString());
                end = end + 2 + 4;
            }else{
                buffer.append(dataStr.substring(start));
            }
            start = end;
        }
        return buffer.toString();
    }

    public String getMethodTitle(){
        if (null == apiMethodDoc){
            return "NULL";
        }
        AnnotationValue s = apiMethodDoc.get("value");
        if (null == s){
            return "";
        }
        String str = s.toString();
        if (str.length() > 2){
            str = str.substring(1, str.length() - 1);
        }
        str = decodeUnicode(str);
        return str;
    }

    public String getMethodDescription(){
        if (null == apiMethodDoc){
            return "NULL";
        }
        AnnotationValue s = apiMethodDoc.get("description");
        if (null == s){
            return "";
        }
        String str = s.toString();
        if (str.length() > 2){
            str = str.substring(1, str.length() - 1);
        }
        str = decodeUnicode(str);
        return str;
    }

    public String getMethodReturnClass(){
        if (null == apiMethodDoc){
            return "NULL";
        }
        AnnotationValue s = apiMethodDoc.get("returnClass");
        if (null == s){
            return "";
        }
        String str = s.toString();
        str = str.replace(".class", "");
        return str;
    }

    /**
     *
     * @return String
     */
    public String getFullUrl(){
        if (methodMapping.value().length > 0) {
            return String.format("%s%s", packageMapping.value()[0], methodMapping.value()[0]);
        }else{
            return packageMapping.value()[0];
        }
    }

    public String getApiUrl(){
        String fileName = getFullUrl();
        fileName = fileName.replace("/", "_");
        fileName = fileName.replace("{", "_");
        fileName = fileName.replace("}", "_");

        fileName = String.format("apidoc/view/%s%s", fileName, getHttpMethod());
        return fileName;
    }

    public String getApiFileName(){
        return String.format("%s.md", getApiUrl());
    }
}
