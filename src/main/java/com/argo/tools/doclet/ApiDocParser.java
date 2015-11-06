package com.argo.tools.doclet;

import com.argo.annotation.ApiDoc;
import com.argo.annotation.ApiMethodDoc;
import com.argo.annotation.ApiParameterDoc;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by yamingd on 10/16/15.
 */
public class ApiDocParser {

    private ApiDocletProcessor generator;

    public ApiDocParser(ApiDocletProcessor generator) {
        this.generator = generator;
    }

    /**
     *
     * @throws IOException
     */
    public List<Api> parse(TypeElement typeElement) throws IOException {

        RequestMapping requestMapping = typeElement.getAnnotation(RequestMapping.class);
        if (requestMapping == null){
            generator.log("Controller missing @RequestMapping, " + typeElement);
            return null;
        }

        ApiDoc apiDoc = typeElement.getAnnotation(ApiDoc.class);
        if (apiDoc == null){
            generator.log("Controller missing @ApiDoc, " + typeElement);
            return null;
        }

        //printElement(typeElement);

        List<Api> apiList = Lists.newArrayList();

        String controllerName = typeElement.getQualifiedName().toString();

        for (Element ele : typeElement.getEnclosedElements()){
            if (ele.getKind().equals(ElementKind.METHOD) && ele.getModifiers().contains(Modifier.PUBLIC)){

                Api api = new Api();
                api.apiDoc = apiDoc;
                api.packageMapping = requestMapping;
                api.controllerName = controllerName;

                ExecutableElement method = (ExecutableElement)ele;
                this.parseMethod(api, method);
                if (api.apiMethodDoc != null) {
                    apiList.add(api);
                }
            }
        }

        return apiList;
    }

    private void parseMethod(Api api, ExecutableElement executableElement) throws IOException {

        Element actionElement = generator.getProcessingEnvironment().getElementUtils().getTypeElement(ApiMethodDoc.class.getName() );
        TypeMirror apiMethodDocType = actionElement.asType();

        for(AnnotationMirror am : executableElement.getAnnotationMirrors()) {
            if(am.getAnnotationType().equals(apiMethodDocType)) {
                api.apiMethodDoc = Maps.newHashMap();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ee : am.getElementValues().entrySet()){
                    api.apiMethodDoc.put(ee.getKey().getSimpleName().toString(), ee.getValue());
                }
                break;
            }
        }

        //generator.log("apiMethodDoc: " + api.apiMethodDoc);

        if (null == api.apiMethodDoc){
            generator.log("Method miss @ApiMethodDoc. " + executableElement);
            return;
        }

        api.methodName = executableElement.getSimpleName().toString();
        api.methodMapping = executableElement.getAnnotation(RequestMapping.class);
        api.parameters = Lists.newArrayList();

        for (VariableElement var : executableElement.getParameters()) {
            api.parameters.add(var);
        }
    }

    public void printElement(TypeElement typeElement){
        List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();
        generator.log("class: " + typeElement + ", annotations: " + annotationMirrors);
        for (Element ele : typeElement.getEnclosedElements()){
            if (ele.getKind().equals(ElementKind.METHOD)){
                ExecutableElement method = (ExecutableElement)ele;
                printMethod(method);
            }
        }
    }

    private void printMethod(ExecutableElement executableElement) {
        StringBuilder s = new StringBuilder(256);
        s.append("method: " + executableElement).append("\n\t");
        s.append("annotations: " + executableElement.getAnnotationMirrors()).append("\n\t");
        s.append("return: " + executableElement.getReturnType()).append("\n\t");
        for (VariableElement var : executableElement.getParameters()) {
            s.append("parameter: " + var + ", " + var.getAnnotation(ApiParameterDoc.class)).append("\n\t");
        }

        generator.log(s.toString());
    }
}
