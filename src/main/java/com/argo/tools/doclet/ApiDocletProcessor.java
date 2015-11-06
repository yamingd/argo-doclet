package com.argo.tools.doclet;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Controller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by yamingd on 9/11/15.
 */
@SupportedOptions({"debug", "verify"})
@SupportedAnnotationTypes({
        "org.springframework.stereotype.Controller"
})
public class ApiDocletProcessor extends AbstractProcessor {

    private ApiDocMarkdownWriter mdGenerator;
    private ApiDocParser apiDocParser;
    private List<Api> apiList;

    private Elements elements;
    private Messager messager;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public void log(String msg) {
        if(this.processingEnv.getOptions().containsKey("debug")) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public void error(String msg, Element element, AnnotationMirror annotation) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    public void fatalError(String msg) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.mdGenerator = new ApiDocMarkdownWriter(this);
        this.apiDocParser = new ApiDocParser(this);
    }

    public ProcessingEnvironment getProcessingEnvironment(){
        return this.processingEnv;
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return this.processImpl(annotations, roundEnv);
        } catch (Exception ex) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            this.fatalError(writer.toString());
            return true;
        }
    }

    private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        if(roundEnv.processingOver()) {
            this.mdGenerator.write(this.apiList);
        } else {
            apiList = Lists.newArrayList();
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Controller.class);
            Iterator<? extends Element> iterator = elements.iterator();
            while (iterator.hasNext()){
                Element element = iterator.next();
                List<Api> apis = this.apiDocParser.parse((TypeElement) element);
                if (null != apis) {
                    apiList.addAll(apis);
                }
            }
        }
        return true;
    }

}

