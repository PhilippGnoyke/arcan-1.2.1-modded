package it.unimib.disco.essere.test;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.classfile.RuntimeInvisibleAnnotations;
import org.apache.bcel.classfile.RuntimeVisibleAnnotations;
import org.apache.bcel.classfile.AnnotationDefault;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class AnnotationTest {
    private static final Logger logger = LogManager.getLogger(AnnotationTest.class);
    private static SystemBuilder sys = null;

    @Before
    public void setupGraph() {

        sys = new SystemBuilderByUrl();
        // sys = new
        // SystemBuilderByUrl("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/ToySystem/target/classes/it/unimib/disco/essere/toysystem");
        sys.readClass("C:/Users/Ilaria/Downloads/qualitas corpus/picocontainer-2.10.2/org/picocontainer");
    }

    @Test
    public void findAnnotation() {
        for (JavaClass clazz : sys.getClasses()) {
            // List<String> dependences = new ArrayList<>();
            ConstantPool cp = clazz.getConstantPool();
            ConstantPoolGen cpg = new ConstantPoolGen(cp);

            for (AnnotationEntry entry : clazz.getAnnotationEntries()) {
                logger.debug("class Entry: " + entry.getAnnotationType() + " found in class: " + clazz.getClassName());
            }
            for (Method m : clazz.getMethods()) {
                /*
                 * for (Attribute a : m.getAttributes()) { if (a instanceof
                 * Annotations) { Annotations ann = (Annotations) a; if (ann
                 * instanceof RuntimeVisibleAnnotations) {
                 * RuntimeVisibleAnnotations visibleAnn =
                 * (RuntimeVisibleAnnotations) ann; logger.debug("type: visible"
                 * ); logger.debug("numAnnotation: " +
                 * visibleAnn.getNumAnnotations()); for (AnnotationEntry entry :
                 * visibleAnn.getAnnotationEntries()) { logger.debug("entry: " +
                 * entry.getAnnotationType()); } } if (ann instanceof
                 * RuntimeInvisibleAnnotations) { RuntimeInvisibleAnnotations
                 * invisibleAnn = (RuntimeInvisibleAnnotations) ann;
                 * logger.debug("type: invisible"); logger.debug("name: " +
                 * invisibleAnn.getName()); logger.debug("entries: " +
                 * invisibleAnn.getAnnotationEntries().toString()); }
                 * logger.debug("annotation: " + a.toString() + " found in: " +
                 * clazz.getClassName()); } if (a instanceof AnnotationDefault)
                 * { AnnotationDefault ann = (AnnotationDefault) a;
                 * logger.debug("default value" + ann.getDefaultValue() +
                 * " found in: " + clazz.getClassName()); } }
                 */

                for (AnnotationEntry entry : m.getAnnotationEntries()) {
                    logger.debug("method Entry: " + entry.getAnnotationType() + "found in: " + clazz.getClassName());
                }

                for (ParameterAnnotationEntry entry2 : m.getParameterAnnotationEntries()) {
                    logger.debug("parameter Entry: " + entry2.toString() + "found in: " + clazz.getClassName());
                }
            }
            for (Field f : clazz.getFields()) {
                for (AnnotationEntry entry : f.getAnnotationEntries()) {
                    logger.debug("field Entry: " + entry.getAnnotationType() + "found in: " + clazz.getClassName());

                }
            }

            for (Attribute a : clazz.getAttributes()) {
                String className = clazz.getClassName();
                if (a instanceof Annotations) {
                    Annotations ann = (Annotations) a;
                    if (ann instanceof RuntimeVisibleAnnotations) {
                        RuntimeVisibleAnnotations visibleAnn = (RuntimeVisibleAnnotations) ann;
                        logger.debug("type: visible");
                        logger.debug("numAnnotation: " + visibleAnn.getNumAnnotations());

                        for (AnnotationEntry entry : visibleAnn.getAnnotationEntries()) {
                            logger.debug("entry: " + entry.getAnnotationType());
                        }
                    }
                    if (ann instanceof RuntimeInvisibleAnnotations) {
                        RuntimeInvisibleAnnotations invisibleAnn = (RuntimeInvisibleAnnotations) ann;
                        logger.debug("type: invisible");
                        logger.debug("name: " + invisibleAnn.getName());
                        logger.debug("entries: " + invisibleAnn.getAnnotationEntries().toString());
                    }
                    logger.debug("annotation: " + a.toString() + " found in: " + className);
                }
                if (a instanceof AnnotationDefault) {
                    AnnotationDefault ann = (AnnotationDefault) a;
                    logger.debug("default value" + ann.getDefaultValue() + " found in: " + className);
                }

            }

        }
    }
}
