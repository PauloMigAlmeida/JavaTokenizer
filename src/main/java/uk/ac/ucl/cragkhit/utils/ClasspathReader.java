package uk.ac.ucl.cragkhit.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;
import java.util.logging.Logger;


public class ClasspathReader {
    // static
    private static ClasspathReader instance;

    // instance
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Reflections reflections;
    private Map<String,Integer> javaClasses;
    private Map<String,Integer> javaPackages;


    private ClasspathReader() {
        // Obtained from: https://stackoverflow.com/a/44817646/832748
        reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(
                        new SubTypesScanner(false /* don't exclude Object.class */),
                        new ResourcesScanner())
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(""))));

        javaClasses = new HashMap<>();
        javaPackages = new HashMap<>();

        this.init();
    }

    private void init(){
        logger.info("Initialising classpath reading... this may take a while");
        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
        logger.info(String.format("Classpath read. Total amount of classes loaded: %d", allClasses.size()));

        logger.info("Breaking values into packages and classes");
        allClasses.forEach(it -> parseString(it.getName()));
        logger.info("Done");
    }

    private void parseString(String text){
        logger.fine("parseString: " + text);

        String packageName = handlePackage(text);

        if(packageName != null)
            handleClasses(text.substring(packageName.length()+1));
        else
            handleClasses(text);
    }

    private String handlePackage(String text){
        int packageDelimited;
        if((packageDelimited = text.lastIndexOf(".")) != -1){
            String fullPackageName = text.substring(0, packageDelimited);
            for (String packageName : fullPackageName.split("\\.")){
                javaPackages.put(packageName, 1);
            }
            System.out.println("handlePackage: " + fullPackageName);
            return fullPackageName;
        }else {
            return null;
        }
    }

    private void handleClasses(String text){
        logger.fine("handleClasses: " + text);
        if(!text.contains("$")){
            // Plain and simple class
            javaClasses.put(text, 1);
        }else{
            // Might have either a subclass or a anonymous class.
            String[] baseClassNames = text.split("\\$");

            StringBuilder classNameSb = new StringBuilder(text.length());
            for (String baseClassName : baseClassNames) {
                if(!NumberUtils.isNumber(baseClassName)){
                    classNameSb.append(baseClassName);
                    classNameSb.append(".");
                }else {
                    break;
                }
            }

            String finalclassName = classNameSb.toString();
            finalclassName = finalclassName.substring(0, finalclassName.lastIndexOf("."));
            logger.fine(finalclassName);

            javaClasses.put(finalclassName,1);

        }
    }


    // accessors
    public static ClasspathReader getInstance() {
        if(instance == null)
            instance = new ClasspathReader();
        return instance;
    }

    public Map<String, Integer> getJavaClasses() {
        return javaClasses;
    }

    public Map<String, Integer> getJavaPackages() {
        return javaPackages;
    }

    public static void main(String[] args){
        ClasspathReader.getInstance();
    }
}
