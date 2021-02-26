package it.unimib.disco.essere.main.graphmanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class InstructionIdentifier {
    private static final Logger logger = LogManager.getLogger(InstructionIdentifier.class);
    
    public static List<String> identify(List<String> dependences, ConstantPoolGen cpg, Instruction instruction) {
        String referencedClassName = "";
        
        if (instruction instanceof InvokeInstruction) {
            InvokeInstruction ii = (InvokeInstruction) instruction;
            ReferenceType referenceType = ii.getReferenceType(cpg);
            if (referenceType instanceof ObjectType) {
                referencedClassName = referenceType.getSignature();
            }
        }
        
        if (instruction instanceof LDC) {
            LDC ldci = (LDC) instruction;
            if (ldci.getValue(cpg) instanceof ObjectType) {
                referencedClassName = ldci.getValue(cpg).toString();
                
            }
        }
        
        if(instruction instanceof INSTANCEOF){
            INSTANCEOF ioi = (INSTANCEOF) instruction;
            if(ioi.getLoadClassType(cpg) instanceof ObjectType){
                referencedClassName = ioi.getLoadClassType(cpg).toString();              
            }
        }
        
    /*    if(instruction instanceof ATHROW){
            ATHROW ai = (ATHROW)instruction;
            for(Class<?> c : ai.getExceptions()){
                referencedClassName = c.getName();
                if(!"".equals(referencedClassName)){
                    dependences.add(referencedClassName);
                }                           
            }
        }*/
        
        if(!"".equals(referencedClassName)){
            dependences.add(referencedClassName);
        }       
        
        return dependences;
    }
    
    public static List<String> findAnnotations(JavaClass clazz){
        List<String> annotations = new ArrayList<>();
        for (AnnotationEntry entry : clazz.getAnnotationEntries()) {
            annotations.add(entry.getAnnotationType());
        }

        for (Method m : clazz.getMethods()) {
            for (AnnotationEntry entry : m.getAnnotationEntries()) {
                annotations.add(entry.getAnnotationType());
            }

            for (ParameterAnnotationEntry entry2 : m.getParameterAnnotationEntries()) {
                for (AnnotationEntry entry : entry2.getAnnotationEntries()) {
                    annotations.add(entry.getAnnotationType());
                }
            }
        }
        for (Field f : clazz.getFields()) {
            for (AnnotationEntry entry : f.getAnnotationEntries()) {
                annotations.add(entry.getAnnotationType());
            }
        }

        return annotations;
    }
    
    public static void findAnnotations(final JavaClass clazz,final List<String>dependecies){
        for (AnnotationEntry entry : clazz.getAnnotationEntries()) {
        	dependecies.add(entry.getAnnotationType());
        }

//        for (Method m : clazz.getMethods()) {
//            for (AnnotationEntry entry : m.getAnnotationEntries()) {
//            	dependecies.add(entry.getAnnotationType());
//            }
//
//            for (ParameterAnnotationEntry entry2 : m.getParameterAnnotationEntries()) {
//                for (AnnotationEntry entry : entry2.getAnnotationEntries()) {
//                	dependecies.add(entry.getAnnotationType());
//                }
//            }
//        }
        for (Field f : clazz.getFields()) {
            for (AnnotationEntry entry : f.getAnnotationEntries()) {
            	dependecies.add(entry.getAnnotationType());
            }
        }

    }
}
