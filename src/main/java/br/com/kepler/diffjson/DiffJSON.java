
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
        String nomeArquivoConfigacao = args[2];
        
        String conteudoArquivoJsonRegraVersaoA = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoJsonRegraVersaoA));
        String conteudoArquivoJsonRegraVersaoB = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoJsonRegraVersaoB));
        String conteudoArquivoConfiguracao = org.apache.commons.io.FileUtils.readFileToString(new File(nomeArquivoConfigacao));

        JSONObject jsonDiffResult = new JSONObject();
        diffObjs(conteudoArquivoConfiguracao, conteudoArquivoJsonRegraVersaoA, conteudoArquivoJsonRegraVersaoB, jsonDiffResult);
        System.out.print("\n" + jsonDiffResult.toString(4));
    }

    public static boolean diffObjs(String conteudoArquivoConfiguracao,  String sObjectA, String sObjectB, JSONObject jsonDiff) throws Exception {
        ConfiguracaoComparacao configuracaoComparacao = ConfiguracaoComparacao.buildFromString(conteudoArquivoConfiguracao);
        JSONObject objA = (JSONObject) orderLists(configuracaoComparacao, "", new JSONObject(sObjectA));
        JSONObject objB = (JSONObject) orderLists(configuracaoComparacao, "", new JSONObject(sObjectB));
        return diffObjs(configuracaoComparacao, "", objA, objB, jsonDiff);
    }

    public static boolean diffObjs(ConfiguracaoComparacao configuracaoComparacao, String prefix, Object objA, Object objB, JSONObject jsonDiff) throws Exception {
        if (objA.getClass() != objB.getClass()) {
            
            if (!prefixoNaListaChaveAComparar(configuracaoComparacao.comparacao,  prefix)) return true;

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

                if (!prefixoNaListaChaveAComparar(configuracaoComparacao.comparacao,  newPrefix)) continue;

                Object valueA = (jsonObjA.has(key) ? jsonObjA.get(key) : null);
                Object valueB = (jsonObjB.has(key) ? jsonObjB.get(key) : null);
                if (valueA != null && valueB == null) {
                        JSONArray ja = new JSONArray();
                        ja.put(valueA);
                        ja.put(JSONObject.NULL);
                        jsonDiff.put(key, ja);
                        result = false;
                }
                else if (valueA == null && valueB != null) {
                        JSONArray ja = new JSONArray();
                        ja.put(JSONObject.NULL);
                        ja.put(valueB);
                        jsonDiff.put(key, ja);
                        result = false;
                }
                else if (valueA.getClass() != valueB.getClass()) {
                        JSONArray ja = new JSONArray();
                        ja.put(valueA);
                        ja.put(valueB);
                        jsonDiff.put(key, ja);
                        result = false;
                }
                else if (isSingleValueObject(valueA)) {
                    if (!valueA.equals(valueB)) {
                        JSONArray ja = new JSONArray();
                        ja.put(valueA);
                        ja.put(valueB);
                        jsonDiff.put(key, ja);
                        result = false;
                    }
                }
                else if (valueA instanceof JSONObject) {
                    JSONObject jsonDiffContext = new JSONObject();
                    result &= diffObjs(configuracaoComparacao, newPrefix, valueA, valueB, jsonDiffContext);

                    if ( !(new JSONObject()).similar(jsonDiffContext) ) jsonDiff.put(key, jsonDiffContext);
                }
                else if (valueA instanceof JSONArray) {
                    JSONArray jsonDiffArrayContext = new JSONArray();
                    result &= diffArrays(configuracaoComparacao, newPrefix, (JSONArray) valueA, (JSONArray) valueB, jsonDiffArrayContext);
                    if ( !(new JSONArray()).similar(jsonDiffArrayContext) && !contemSoOperacaoNone(jsonDiffArrayContext) ) jsonDiff.put(key, jsonDiffArrayContext);
                }
            }
            return result;
        } else if (objA.equals(objB)) {
            return true;
        }

        if (!prefixoNaListaChaveAComparar(configuracaoComparacao.comparacao,  prefix)) return true;

        jsonDiff.put("A", objA);
        jsonDiff.put("B", objB);
        return false;
    }

    public static boolean diffArrays(ConfiguracaoComparacao configuracaoComparacao, String prefix, JSONArray arrayA, JSONArray arrayB, JSONArray jsonDiffArray) throws Exception {
        if (!prefixoNaListaChaveAComparar(configuracaoComparacao.comparacao,  prefix)) return true;

        EditDistanceResult result = computeEditDistance(configuracaoComparacao, prefix, arrayA, arrayB, jsonDiffArray);
        if (result.getDistance() > 0) {
            return false;
        }

        return true;
    }

    public static EditDistanceResult computeEditDistance(ConfiguracaoComparacao configuracaoComparacao, String prefix, JSONArray ss, JSONArray zz, /* List<String> arrayDiff, */ JSONArray jsonDiffArray) throws Exception {
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
        final double[][] d = new double[m + 1][n + 1];
        final Map<Point, Point> parentMap = new HashMap<>();

        for (int i = 1; i <= m; ++i) {
            d[i][0] = (double)i;
        }

        for (int j = 1; j <= n; ++j) {
            d[0][j] = (double)j;
        }

        for (int j = 1; j <= n; ++j) {
            for (int i = 1; i <= m; ++i) {

                Object objA = arrayA.get(j - 1);
                Object objB = arrayB.get(i - 1);

                double delta = diffObjs(configuracaoComparacao, prefix, objA, objB, new JSONObject()) ? 0.0 : 1.0;

                double tentativeDistance = d[i - 1][j] + 1;
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
                boolean objAEqualObjB = diffObjs(configuracaoComparacao, prefix, objA, objB, jsonDiffObject);
                editSequenceBuilder.add(!objAEqualObjB ? EditOperation.SUBSTITUTE : EditOperation.NONE);
                if (!objAEqualObjB) {
                    String identificacaoElementoListaParaPrexifo = getIdentificacaoElementoListaParaPrexifo(configuracaoComparacao, prefix);
                    if (identificacaoElementoListaParaPrexifo != null) {
                        JSONArray ja = new JSONArray();
                        ja.put( ((JSONObject)objA).has(identificacaoElementoListaParaPrexifo) ? ((JSONObject)objA).get(identificacaoElementoListaParaPrexifo) : JSONObject.NULL);
                        ja.put( ((JSONObject)objB).has(identificacaoElementoListaParaPrexifo) ? ((JSONObject)objB).get(identificacaoElementoListaParaPrexifo) :JSONObject.NULL);
                        jsonDiffObject.put(identificacaoElementoListaParaPrexifo, ja);
                    } else {
                        JSONArray ja = new JSONArray();
                        ja.put(new Integer(predecessor.y));
                        ja.put(new Integer(predecessor.x));
                        jsonDiffObject.put("posicaoLista", ja);
                    }
                }
                jsonDiffArrayLocal.put(jsonDiffObject);
            } else if (current.x != predecessor.x) {
                Object objB = arrayB.get(predecessor.x);
                topLineBuilder.add(GAP);
                bottomLineBuilder.add(arrayB.get(predecessor.x));
                editSequenceBuilder.add(EditOperation.INSERT);
                JSONObject jsonDiffObject = new JSONObject();
                String identificacaoElementoListaParaPrexifo = getIdentificacaoElementoListaParaPrexifo(configuracaoComparacao, prefix);
                if (identificacaoElementoListaParaPrexifo != null) {
                    JSONArray ja = new JSONArray();
                    ja.put(JSONObject.NULL);
                    ja.put( ((JSONObject)objB).has(identificacaoElementoListaParaPrexifo) ? ((JSONObject)objB).get(identificacaoElementoListaParaPrexifo) : JSONObject.NULL);
                    jsonDiffObject.put(identificacaoElementoListaParaPrexifo, ja);
                } else {
                    JSONArray ja = new JSONArray();
                    ja.put(JSONObject.NULL);
                    ja.put(new Integer(predecessor.x));
                    jsonDiffObject.put(identificacaoElementoListaParaPrexifo, ja);
                }
                JSONArray ja = new JSONArray();
                ja.put(JSONObject.NULL);
                ja.put(arrayB.get(predecessor.x));
                jsonDiffArrayLocal.put(jsonDiffObject);

            } else {
                Object objA = arrayA.get(predecessor.y);
                topLineBuilder.add(arrayA.get(predecessor.y)); 
                bottomLineBuilder.add(GAP);
                editSequenceBuilder.add(EditOperation.DELETE);
                JSONObject jsonDiffObject = new JSONObject();
                String identificacaoElementoListaParaPrexifo = getIdentificacaoElementoListaParaPrexifo(configuracaoComparacao, prefix);
                if (identificacaoElementoListaParaPrexifo != null) {
                    JSONArray ja = new JSONArray();
                    ja.put( ((JSONObject)objA).has(identificacaoElementoListaParaPrexifo) ? ((JSONObject)objA).get(identificacaoElementoListaParaPrexifo) : JSONObject.NULL);
                    ja.put(JSONObject.NULL);
                    jsonDiffObject.put(identificacaoElementoListaParaPrexifo, ja);
                } else {
                    JSONArray ja = new JSONArray();
                    ja.put(new Integer(predecessor.y));
                    ja.put(JSONObject.NULL);
                    jsonDiffObject.put(identificacaoElementoListaParaPrexifo, ja);
                }

                JSONArray ja = new JSONArray();
                ja.put(JSONObject.NULL);
                ja.put(arrayA.get(predecessor.y));
                jsonDiffArrayLocal.put(jsonDiffObject);
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
            if (!( (new JSONObject()).similar(jsonDiffArrayLocal.get(i)) ) ) jsonDiffArray.put(jsonDiffArrayLocal.get(i));
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

    public static String getIdentificacaoElementoListaParaPrexifo(ConfiguracaoComparacao configuracaoComparacao, String prefix) {
        for (int i=0; i<configuracaoComparacao.identificacaoElementoLista.size(); i++) {
            if (prefix.equals(configuracaoComparacao.identificacaoElementoLista.get(i).get(0))) {
                String r = configuracaoComparacao.identificacaoElementoLista.get(i).get(1);
                if (r != null && r.length() > 1 && r.startsWith("/")) return r.substring(1);
                return r;
            }
        }
        return null;
    }

    public static boolean prefixoNaListaChaveAComparar(List<String> listaChaveAComparar, String prefix) {
        for (int i=0; i<listaChaveAComparar.size(); i++) {
            if (prefix.startsWith(listaChaveAComparar.get(i)) || listaChaveAComparar.get(i).startsWith(prefix)) {
                return true;
            }
        }        
        return false;
    }

    public static boolean contemSoOperacaoNone(JSONArray jsonArray) {
        if (jsonArray == null) return true;
        if (jsonArray.length() == 0) return true;
        for (int i=0; i<jsonArray.length(); i++) {
            Object objAtIndex = jsonArray.get(i);
            if (!(objAtIndex instanceof JSONObject)) return false;
            JSONObject jsonObjectAtIndex = (JSONObject) objAtIndex;
            if (!jsonObjectAtIndex.has("operation")) return false;
            Object operationValue = jsonObjectAtIndex.get("operation");
            if (operationValue == null) return false;
            if (!(operationValue instanceof String)) return false;
            String sOperationValue = (String) operationValue;
            if (sOperationValue == null) return false;
            if (!sOperationValue.equals(EditOperation.NONE.toString())) return false;
        }
        return true;
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

    public static Object orderLists(ConfiguracaoComparacao configuracaoComparacao, String prefix, Object obj) {
        if (isSingleValueObject(obj)) return obj;

        if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            JSONArray result = new JSONArray();
            List<String> listaCampoOrdenacao = getListaCampoOrdenacaoParaPrexifo(configuracaoComparacao, prefix);
            if (listaCampoOrdenacao != null) {
                HashMap<Object, Integer>  mapListaCampoOrdenacaoXPosicaoLista= new HashMap<Object,Integer>();
                for (int i=0; i<jsonArray.length(); i++) {
                    List<Object> valorCampoOrdenacao = getValorCampoOrdenacao((JSONObject)jsonArray.get(i), listaCampoOrdenacao);
                    valorCampoOrdenacao.add(new Integer(i));
                    mapListaCampoOrdenacaoXPosicaoLista.put(valorCampoOrdenacao, new Integer(i));
                }

                List<Object> listaValorCampoOrdenacao =  new ArrayList<Object>(mapListaCampoOrdenacaoXPosicaoLista.keySet());
                Collections.sort(
                    listaValorCampoOrdenacao, 
                    new DiffJSONComparator()
                );
                for (int i=0; i<listaValorCampoOrdenacao.size(); i++) {
                    result.put(orderLists(configuracaoComparacao, prefix, jsonArray.get(mapListaCampoOrdenacaoXPosicaoLista.get(listaValorCampoOrdenacao.get(i)))));
                }
                return result;
            }
            return obj;
        }
        
        JSONObject jsonObj = (JSONObject)obj;
        JSONObject result = new JSONObject();
        for (String key : jsonObj.keySet()) {
            result.put(key, orderLists(configuracaoComparacao, prefix + "/" + key, jsonObj.get(key)));
        }
        return result;
    }

    public static List<String> getListaCampoOrdenacaoParaPrexifo(ConfiguracaoComparacao configuracaoComparacao, String prefix) {
        for (int i=0; i<configuracaoComparacao.ordenacao.size(); i++) {
            if (prefix.equals(configuracaoComparacao.ordenacao.get(i).get(0))) {
                return configuracaoComparacao.ordenacao.get(i).subList(1, configuracaoComparacao.ordenacao.get(i).size());
            }
        }
        return null;
    }

   public static List<Object> getValorCampoOrdenacao(JSONObject jsonObj, List<String> listaCampoOrdenacao) {
       List<Object> result = new ArrayList<Object>();        
       for (int i=0; i<listaCampoOrdenacao.size(); i++) {
            String sPathCampo = listaCampoOrdenacao.get(i);
            if (sPathCampo != null && sPathCampo.startsWith("/")) sPathCampo = sPathCampo.substring(1);
            String pathCampo[] = sPathCampo.split("/");
            Object js = jsonObj;
            int j;
            for (j=0; j<pathCampo.length; j++) {
                String campo = pathCampo[j];
                if (js == null || (js instanceof JSONObject) == false || ((JSONObject)js).has(campo)==false )  {
                    js = null;
                    break;
                }
                js = ((JSONObject)js).get(campo);
            }
            result.add(js);
       }
       return result;
    }

}