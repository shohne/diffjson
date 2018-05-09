
package br.com.kepler.diffjson;

import java.util.*;
import org.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.Exception;

public class DiffJSONComparator implements Comparator<Object>  {

    public int compare(Object o1, Object o2)  {
        try {
            if (o1 == null && o2 == null) return  0;
            if (o1 == null && o2 != null) return  1;
            if (o1 != null && o2 == null) return -1;

            List<Object> listaValor1 = (List<Object>) o1;
            List<Object> listaValor2 = (List<Object>) o2;
            
            for (int i=0; i<listaValor1.size(); i++) {
                Object oo1 = listaValor1.get(i);
                Object oo2 = listaValor2.get(i);

                if (oo1 == null && oo2 == null) continue;
                if (oo1 == null && oo2 != null) return  1;
                if (oo1 != null && oo2 == null) return -1;


                if (oo1.getClass() != oo2.getClass()) throw new Exception("Nao eh possivel comparar " + oo1 + " and " + oo2 + " pois possum tipos diferentes");

                if (oo1 instanceof JSONString) {
                    String s1 = ( (JSONString) oo1).toJSONString();
                    String s2 = ( (JSONString) oo2).toJSONString();
                    int r = s1.compareTo(s2); 
                    if (r != 0) return r;
                } 
                else if (oo1 instanceof Byte) {
                    Byte b1 = (Byte) oo1;
                    Byte b2 = (Byte) oo2;
                    int r = b1.compareTo(b2); 
                    if (r != 0) return r;
                } 
                else if (oo1 instanceof Character) {
                    Character c1 = (Character) oo1;
                    Character c2 = (Character) oo2;
                    int r = c1.compareTo(c2); 
                    if (r != 0) return r;
                } 
                else if (oo1 instanceof Short) {
                    Short s1 = (Short) oo1;
                    Short s2 = (Short) oo2;
                    int r = s1.compareTo(s2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof Integer) {
                    Integer i1 = (Integer) oo1;
                    Integer i2 = (Integer) oo2;
                    int r = i1.compareTo(i2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof Long) {
                    Long l1 = (Long) oo1;
                    Long l2 = (Long) oo2;
                    int r = l1.compareTo(l2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof Boolean) {
                    Boolean b1 = (Boolean) oo1;
                    Boolean b2 = (Boolean) oo2;
                    int r = b1.compareTo(b2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof Float) {
                    Float f1 = (Float) oo1;
                    Float f2 = (Float) oo2;
                    int r = f1.compareTo(f2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof Double) {
                    Double d1 = (Double) oo1;
                    Double d2 = (Double) oo2;
                    int r = d1.compareTo(d2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof String) {
                    String s1 = (String) oo1;
                    String s2 = (String) oo2;
                    int r = s1.compareTo(s2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof BigInteger) {
                    BigInteger b1 = (BigInteger) oo1;
                    BigInteger b2 = (BigInteger) oo2;
                    int r = b1.compareTo(b2); 
                    if (r != 0) return r;
                }
                else if (oo1 instanceof BigDecimal) {
                    BigDecimal b1 = (BigDecimal) oo1;
                    BigDecimal b2 = (BigDecimal) oo2;
                    int r = b1.compareTo(b2); 
                    if (r != 0) return r;
                }
            }
        } catch (Exception e) {
                System.out.print("Erro " + e);
        }
        return 0;
    }

}
