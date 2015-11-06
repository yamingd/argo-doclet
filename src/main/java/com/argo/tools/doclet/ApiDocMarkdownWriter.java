package com.argo.tools.doclet;

import com.argo.annotation.ApiParameterDoc;
import com.google.common.base.Charsets;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

/**
 * Created by yamingd on 10/15/15.
 */
public class ApiDocMarkdownWriter {

    private ApiDocletProcessor processor;
    private Filer filer;

    public ApiDocMarkdownWriter(ApiDocletProcessor processor) {
        this.processor = processor;
        this.filer = processor.getProcessingEnvironment().getFiler();
    }

    public void write(List<Api> apiList) throws IOException {
        writeIndex(apiList);
        for (int i = 0; i < apiList.size(); i++) {
            writeApi(apiList.get(i));
        }
    }

    /**
     * 输出 API 列表
     * @param apiList
     * @throws IOException
     */
    public void writeIndex(List<Api> apiList) throws IOException {
        String fileName = "apidoc/index.md";
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName, new Element[0]);
        OutputStream output = fileObject.openOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, Charsets.UTF_8));
        writer.write("# API 列表 (" + apiList.size() + ")");
        writer.newLine();
        writer.newLine();
        writer.newLine();

        String lastGroup = "";

        for (int i = 0; i < apiList.size(); i++) {
            //writer.write("1. **POST**    [用户会话 - 登录](/apidoc/view/0001)");
            Api api = apiList.get(i);

            if (lastGroup.length() == 0 || !lastGroup.equals(api.apiDoc.value())) {
                writer.write("## ");
                writer.write(api.apiDoc.value());
                writer.newLine();
                writer.newLine();
                lastGroup = api.apiDoc.value();
            }

            writer.write(String.valueOf(i + 1));
            writer.write(".");
            writer.write("  ");
            writer.write("**");
            writer.write(api.getHttpMethod());
            writer.write("**");
            writer.write("  ");
            writer.write("[");
            writer.write(api.apiDoc.value());
            writer.write(" - ");
            writer.write(api.getMethodTitle());
            writer.write("]");
            writer.write("(");
            writer.write("/");
            writer.write(api.getApiUrl());
            writer.write(")");
            writer.write("  ");
            writer.write(api.getFullUrl());
            writer.newLine();
            writer.newLine();
            writer.newLine();
        }

        writer.flush();
        output.flush();
        output.close();
    }

    public TypeElement getExtracted(VariableElement ve) {
        TypeMirror typeMirror = ve.asType();
        Element element = processor.getProcessingEnvironment().getTypeUtils().asElement(typeMirror);

        // instanceof implies null-ckeck
        return (element instanceof TypeElement)
                ? (TypeElement)element : null;
    }

    /**
     * 输出 API 描述
     * @param api
     */
    public void writeApi(Api api) throws IOException {

        String fileName = api.getApiFileName();
        //processor.log("apidoc: " + fileName);

        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName, new Element[0]);
        OutputStream output = fileObject.openOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, Charsets.UTF_8));

        //1. 标题
        writer.write("# ");
        writer.write(api.apiDoc.value());
        writer.write(" - ");
        writer.write(api.getMethodTitle());
        writer.newLine();
        //2. 描述
        writer.write(api.getMethodDescription());
        writer.newLine();
        //3. 访问
        writer.write("## HTTP 访问");
        writer.newLine();

        writer.write("BASE: ");
        writer.write(api.packageMapping.value()[0]);
        writer.newLine();
        writer.newLine();

        writer.write("PATH: ");
        if (api.methodMapping.value().length > 0) {
            writer.write(api.methodMapping.value()[0]);
        }else{
            writer.write("");
        }
        writer.newLine();
        writer.newLine();

        writer.write("METHOD: ");
        writer.write(api.getHttpMethod());
        writer.newLine();
        writer.newLine();

        writer.write("HEADER: ");
        writer.write(api.getProduces());
        writer.newLine();
        writer.newLine();

        writer.write("VERSION: ");
        writer.write(api.getVersion());
        writer.newLine();
        writer.newLine();
        writer.newLine();

        //4. 参数
        writer.write("## 参数列表");
        writer.newLine();
        writer.newLine();

        for (VariableElement variableElement : api.parameters) {

            ApiParameterDoc apiParameterDoc = variableElement.getAnnotation(ApiParameterDoc.class);
            if (null == apiParameterDoc){
                continue;
            }

            writer.newLine();

            TypeElement type = getExtracted(variableElement);
            if (type != null && type.getSimpleName().toString().endsWith("Form")){
                writeFormType(writer, type);
                continue;
            }

            PathVariable pathVariable = variableElement.getAnnotation(PathVariable.class);
            RequestParam requestParam = variableElement.getAnnotation(RequestParam.class);
            RequestHeader requestHeader = variableElement.getAnnotation(RequestHeader.class);
            CookieValue cookieValue = variableElement.getAnnotation(CookieValue.class);

            String name = variableElement.getSimpleName().toString();
            boolean required = false;
            String source = "自动";
            String defaultVal = "";


            if (null != pathVariable){
                if (null != pathVariable.value() && pathVariable.value().length() > 0){
                    name = pathVariable.value();
                }

                required = true;
                source = "Path";
                defaultVal = "N/A";
            }

            if (null != requestParam){
                if (null != requestParam.value() && requestParam.value().length() > 0){
                    name = requestParam.value();
                }

                required = requestParam.required();
                source = "QueryString";
                defaultVal = requestParam.defaultValue();
            }

            if (null != requestHeader){
                if (null != requestHeader.value() && requestHeader.value().length() > 0){
                    name = requestHeader.value();
                }

                required = requestHeader.required();
                source = "Header";
                defaultVal = requestHeader.defaultValue();
            }

            if (null != cookieValue){
                if (null != cookieValue.value() && cookieValue.value().length() > 0){
                    name = cookieValue.value();
                }

                required = cookieValue.required();
                source = "Cookie";
                defaultVal = cookieValue.defaultValue();
            }

            writer.write("### 参数 - ");
            writer.write(name);
            writer.newLine();
            writer.newLine();
            if (null != apiParameterDoc) {
                writer.write(apiParameterDoc.value());
                writer.newLine();
                writer.newLine();
            }

            writer.write("* 必须: ");
            writer.write(required ? "YES" : "NO");
            writer.newLine();

            writer.write("* 来源: ");
            writer.write(source);
            writer.newLine();

            writer.write("* 类型: ");
            if (null == type) {
                writer.write("N/A");
            }else{
                writer.write(type.getSimpleName().toString());
            }
            writer.newLine();

            writer.write("* 默认: ");
            writer.write(defaultVal.length() > 0 ? defaultVal : "N/A");
            writer.newLine();
            writer.newLine();
        }
        writer.newLine();
        writer.newLine();

        //5. 返回
        String typeName = api.getMethodReturnClass();
        writer.write("## 输出数据结构");
        writer.newLine();
        writer.newLine();

        writer.write(String.format("[%s](/apidoc/proto/%s \"%s实体\")", typeName, typeName, typeName));
        writer.newLine();

        //6.
        writer.flush();
        output.flush();
        output.close();

    }

    private void writeFormType(BufferedWriter writer, TypeElement formType) throws IOException {
        for (Element element : formType.getEnclosedElements()) {
            ElementKind elementKind = element.getKind();

            if (elementKind.equals(ElementKind.FIELD)) {
                VariableElement variableElement = (VariableElement) element;
                String fieldName = variableElement.getSimpleName().toString();

                Set<Modifier> modifiers = variableElement.getModifiers();
                if (modifiers.contains(Modifier.STATIC)) {
                    continue; // completely ignore any static fields
                }

                TypeElement fieldType = getExtracted(variableElement);
                ApiParameterDoc apiParameterDoc = variableElement.getAnnotation(ApiParameterDoc.class);
                NotNull notNull = variableElement.getAnnotation(NotNull.class);
                NotEmpty notEmpty = variableElement.getAnnotation(NotEmpty.class);
                Length length = variableElement.getAnnotation(Length.class);

                writer.write("### 参数 - ");
                writer.write(fieldName);
                writer.newLine();
                writer.newLine();
                if (null != apiParameterDoc) {
                    String str = apiParameterDoc.value();
                    while (str.startsWith("#")){
                        str = str.substring(1);
                    }
                    writer.write(str);
                    writer.newLine();
                    writer.newLine();
                }

                writer.write("* 必须: ");
                if (notEmpty != null){
                    writer.write("YES");
                } else if (notNull != null){
                    writer.write("YES");
                }else if (length != null){
                    if (length.min() > 0) {
                        writer.write("YES");
                    }else{
                        writer.write("NO");
                    }

                    writer.write(String.format(", 长度限制为从%s到%s", length.min(), length.max()));

                }else{
                    writer.write("NO");
                }
                writer.newLine();

                writer.write("* 来源: ");
                writer.write("Client");
                writer.newLine();

                writer.write("* 类型: ");
                if (null == fieldType) {
                    writer.write("N/A");
                }else{
                    writer.write(fieldType.getSimpleName().toString());
                }
                writer.newLine();

                writer.write("* 默认: ");
                writer.write("N/A");
                writer.newLine();
                writer.newLine();

            }
        }

    }

}
