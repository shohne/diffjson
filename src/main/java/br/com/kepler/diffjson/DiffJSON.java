
package br.com.kepler.diffjson;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.awt.Point;

import org.apache.commons.io.FileUtils;

import org.json.*;


public class DiffJSON {

    public static final String GAP = "-";

    public static void  main(String args[]) throws Exception {
        String nomeArquivoJsonRegraVersaoA = args[0];
        String nomeArquivoJsonRegraVersaoB = args[1];
        String nomeArquivoCampoComparacao = args[2];
        

        String conteudoArquivoJsonRegraVersaoA = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoJsonRegraVersaoA));
        String conteudoArquivoJsonRegraVersaoB = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoJsonRegraVersaoB));
        String conteudoArquivoCampoComparacao = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoCampoComparacao));

        String vLinha[] = conteudoArquivoCampoComparacao.split("\\r?\\n");
        List<String> listaChaveAComparar = new ArrayList<String>();
        for (int i=0; i<vLinha.length; i++) listaChaveAComparar.add(vLinha[i]);

        JSONObject objA = new JSONObject(conteudoArquivoJsonRegraVersaoA);
        JSONObject objB = new JSONObject(conteudoArquivoJsonRegraVersaoB);

        List<String> arrayDiff = new ArrayList<String>();

        JSONObject jsonDiffResult = new JSONObject();
        diffObjs(listaChaveAComparar, "", objA, objB, arrayDiff, jsonDiffResult);

        System.out.print("\n" + jsonDiffResult.toString(4));
    }

    public static boolean diffObjs(List<String> listaChaveAComparar, String prefix, Object objA, Object objB, List<String> arrayDiff, JSONObject jsonDiff) throws Exception {
        if (objA.getClass() != objB.getClass()) {
            
            if (!prefixoNaListaChaveAComparar(listaChaveAComparar,  prefix)) return true;

            arrayDiff.add(prefix + " : " + objA + " | " + objB);
            jsonDiff.put("A", objA);
            jsonDiff.put("B", objB);
            return false;
        }
        else if (objA instanceof JSONObject && objB instanceof JSONObject) {
            Set<String> keySetAB = new HashSet<String>();
            JSONObject jsonObjA = (JSONObject) objA;            
            JSONObject jsonObjB = (JSONObject) objB;            
            keySetAB.addAll(jsonObjA.keySet());
            keySetAB.addAll(jsonObjB.keySet());
            boolean result = true;
            for (String key : keySetAB) {
                String newPrefix = prefix + "/" + key;

                if (!prefixoNaListaChaveAComparar(listaChaveAComparar,  newPrefix)) continue;

                Object valueA = (jsonObjA.has(key) ? jsonObjA.get(key) : null);
                Object valueB = (jsonObjB.has(key) ? jsonObjB.get(key) : null);
                if (valueA != null && valueB == null) {
                        arrayDiff.add(newPrefix + " : " + valueA + " | NULL");
                        JSONObject j = new JSONObject();
                        j.put("A", valueA);
                        j.put("B", JSONObject.NULL);
                        jsonDiff.put(key, j);
                        result = false;
                }
                else if (valueA == null && valueB != null) {
                        arrayDiff.add(newPrefix + " : NULL " + " | " + valueB);
                        JSONObject j = new JSONObject();
                        j.put("A", JSONObject.NULL);
                        j.put("B", valueB);
                        jsonDiff.put(key, j);
                        result = false;
                }
                else if (valueA.getClass() != valueB.getClass()) {
                        arrayDiff.add(newPrefix + " : " + valueA.getClass() + " | " + valueB.getClass());
                        JSONObject j = new JSONObject();
                        j.put("A", valueA);
                        j.put("B", valueB);
                        jsonDiff.put(key, j);
                        result = false;
                }
                else if (isSingleValueObject(valueA)) {
                    if (!valueA.equals(valueB)) {
                        arrayDiff.add(newPrefix + " : " + valueA + " | " + valueB);
                        JSONObject j = new JSONObject();
                        j.put("A", valueA);
                        j.put("B", valueB);
                        jsonDiff.put(key, j);
                        result = false;
                    }
                }
                else if (valueA instanceof JSONObject) {
                    JSONObject jsonDiffContext = new JSONObject();
                    result &= diffObjs(listaChaveAComparar, newPrefix, valueA, valueB, arrayDiff, jsonDiffContext);

                    if ( !(new JSONObject()).similar(jsonDiffContext) ) jsonDiff.put(key, jsonDiffContext);
                }
                else if (valueA instanceof JSONArray) {
                    JSONArray jsonDiffArrayContext = new JSONArray();
                    result &= diffArrays(listaChaveAComparar, newPrefix, (JSONArray) valueA, (JSONArray) valueB, arrayDiff, jsonDiffArrayContext);
                    if ( !(new JSONArray()).similar(jsonDiffArrayContext) ) jsonDiff.put(key, jsonDiffArrayContext);
                }
            }
            return result;
        } else if (objA.equals(objB)) {
            return true;
        }

        if (!prefixoNaListaChaveAComparar(listaChaveAComparar,  prefix)) return true;

        arrayDiff.add(prefix + " : " + objA + " | " + objB);
        jsonDiff.put("A", objA);
        jsonDiff.put("B", objB);
        return false;
    }

    public static boolean diffArrays(List<String> listaChaveAComparar, String prefix, JSONArray arrayA, JSONArray arrayB, List<String> arrayDiff, JSONArray jsonDiffArray) throws Exception {
        if (!prefixoNaListaChaveAComparar(listaChaveAComparar,  prefix)) return true;

        EditDistanceResult result = computeEditDistance(listaChaveAComparar, prefix, arrayA, arrayB, arrayDiff, jsonDiffArray);
        if (result.getDistance() > 0) {
            return false;
        }

        return true;
    }

    public static EditDistanceResult computeEditDistance(List<String> listaChaveAComparar, String prefix, JSONArray ss, JSONArray zz, List<String> arrayDiff, JSONArray jsonDiffArray) throws Exception {
        List<Object> arrayA = new ArrayList<Object>();
        List<Object> arrayB = new ArrayList<Object>();

        arrayA.add("");
        arrayB.add("");
        JSONArray jsonDiffArrayLocal = new JSONArray();
        jsonDiffArrayLocal.put("");

        for (int i=0; i<ss.length(); i++) arrayA.add(ss.get(i));
        for (int i=0; i<zz.length(); i++) arrayB.add(zz.get(i));


        final int n = arrayA.size();
        final int m = arrayB.size();
        final int[][] d = new int[m + 1][n + 1];
        final Map<Point, Point> parentMap = new HashMap<>();

        for (int i = 1; i <= m; ++i) {
            d[i][0] = i;
        }

        for (int j = 1; j <= n; ++j) {
            d[0][j] = j;
        }

        for (int j = 1; j <= n; ++j) {
            for (int i = 1; i <= m; ++i) {

                Object objA = arrayA.get(j - 1);
                Object objB = arrayB.get(i - 1);

                int delta = diffObjs(listaChaveAComparar, prefix, objA, objB, new ArrayList<String>(), new JSONObject()) ? 0 : 1;

                int tentativeDistance = d[i - 1][j] + 1;
                EditOperation editOperation = EditOperation.INSERT;

                if (tentativeDistance > d[i][j - 1] + 1) {
                    tentativeDistance = d[i][j - 1] + 1;
                    editOperation = EditOperation.DELETE;
                }

                if (tentativeDistance > d[i - 1][j - 1] + delta) {
                    tentativeDistance = d[i - 1][j - 1] + delta;
                    editOperation = EditOperation.SUBSTITUTE;
                }

                d[i][j] = tentativeDistance;

                switch (editOperation) {
                    case SUBSTITUTE:
                        parentMap.put(new Point(i, j), new Point(i - 1, j - 1));
                        break;

                    case INSERT:
                        parentMap.put(new Point(i, j), new Point(i - 1, j));
                        break;

                    case DELETE:
                        parentMap.put(new Point(i, j), new Point(i, j - 1));
                        break;
                }
            }
        }

        List<Object> topLineBuilder      = new ArrayList<Object>();
        List<Object> bottomLineBuilder   = new ArrayList<Object>();
        List<Object> editSequenceBuilder = new ArrayList<Object>();
        Point current = new Point(m, n);

        while (true) {
            Point predecessor = parentMap.get(current);

            if (predecessor == null) {
                break;
            }

            if (current.x != predecessor.x && current.y != predecessor.y) {
                Object objA = arrayA.get(predecessor.y);
                Object objB = arrayB.get(predecessor.x);

                topLineBuilder.add(objA);
                bottomLineBuilder.add(objB);

                JSONObject jsonDiffObject = new JSONObject();
                boolean objAEqualObjB = diffObjs(listaChaveAComparar, prefix, objA, objB, arrayDiff, jsonDiffObject);
                editSequenceBuilder.add(!objAEqualObjB ? EditOperation.SUBSTITUTE : EditOperation.NONE);
                JSONObject j = new JSONObject();
                j.put("operation", !objAEqualObjB ? EditOperation.SUBSTITUTE.toString() : EditOperation.NONE.toString());
                if (!objAEqualObjB) j.put("diff", jsonDiffObject);
                jsonDiffArrayLocal.put(j);

            } else if (current.x != predecessor.x) {
                topLineBuilder.add(GAP);
                bottomLineBuilder.add(arrayB.get(predecessor.x));
                editSequenceBuilder.add(EditOperation.INSERT);
                arrayDiff.add(prefix + "[]");
                JSONObject j = new JSONObject();
                j.put("operation", EditOperation.INSERT.toString());
                j.put("a", JSONObject.NULL);
                j.put("b", arrayB.get(predecessor.x));
                jsonDiffArrayLocal.put(j);

            } else {
                topLineBuilder.add(arrayA.get(predecessor.y)); 
                bottomLineBuilder.add(GAP);
                editSequenceBuilder.add(EditOperation.DELETE);
                arrayDiff.add(prefix + "[]");
                JSONObject j = new JSONObject();
                j.put("operation", EditOperation.DELETE.toString());
                j.put("a", arrayA.get(predecessor.y));
                j.put("b", JSONObject.NULL);
                jsonDiffArrayLocal.put(j);
            }

            current = predecessor;
        }

        topLineBuilder     .remove(topLineBuilder.size() - 1);
        bottomLineBuilder  .remove(bottomLineBuilder.size() - 1);
        editSequenceBuilder.remove(editSequenceBuilder.size() - 1);

        topLineBuilder      = reverseList(topLineBuilder);
        bottomLineBuilder   = reverseList(bottomLineBuilder);
        editSequenceBuilder = reverseList(editSequenceBuilder);

        for (int i=jsonDiffArrayLocal.length()-2; i >= 1; i--) {
            jsonDiffArray.put(jsonDiffArrayLocal.get(i));
        }
         
        return new EditDistanceResult(d[m][n], editSequenceBuilder, topLineBuilder, bottomLineBuilder);
    }

    public static List<Object> reverseList(List<Object> list) {
        if (list == null) return null;

        List<Object> result = new ArrayList<Object>();
        for (int i=list.size()-1; i >= 0; i--) {
            result.add(list.get(i));
        }

        return result;
    }

    public static boolean prefixoNaListaChaveAComparar(List<String> listaChaveAComparar, String prefix) {
//        System.out.print("\n" + prefix);
        for (int i=0; i<listaChaveAComparar.size(); i++) {
//            System.out.print("\n     " + listaChaveAComparar.get(i));
            if (prefix.startsWith(listaChaveAComparar.get(i)) || listaChaveAComparar.get(i).startsWith(prefix)) {
 //               System.out.print("\n       true");
                return true;
            }
        }        
//        System.out.print("\n       false");
        return false;
    }

    public static boolean isSingleValueObject(Object object) {
        if (
            object instanceof JSONString || 
            object instanceof Byte || 
            object instanceof Character || 
            object instanceof Short || 
            object instanceof Integer || 
            object instanceof Long || 
            object instanceof Boolean || 
            object instanceof Float || 
            object instanceof Double || 
            object instanceof String || 
            object instanceof BigInteger || 
            object instanceof BigDecimal || 
            object instanceof Enum) {
            return true;
        }
        return false;
    }

}