package br.com.kepler.diffjson;

import java.util.*;

public class ConfiguracaoComparacao {

    public static String CHAVE_ORDENACAO = "[ordenacao]";
    public static String CHAVE_COMPARACAO = "[comparacao]";
    public static String CHAVE_IDENTIFICAO_ELEMENTO_LISTA = "[identificacaoElementoLista]";

    public static int SECAO_NENHUMA = -1;
    public static int SECAO_ORDENACAO = 10;
    public static int SECAO_COMPARACAO = 20;
    public static int SECAO_INDENTIFICACAO_ELEMENTO_LISTA = 30;
    
    public ArrayList<String> comparacao;
    public ArrayList<ArrayList<String>> ordenacao;
    public ArrayList<ArrayList<String>> identificacaoElementoLista;

    public ConfiguracaoComparacao() {
        this.comparacao = new ArrayList<String>();
        this.ordenacao = new ArrayList<ArrayList<String>>();
        this.identificacaoElementoLista = new ArrayList<ArrayList<String>>();
    }

    public static ConfiguracaoComparacao buildFromString(String str) {
        if (str == null || str.length() == 0) return null;

        ConfiguracaoComparacao resultado = new ConfiguracaoComparacao();

        String vLinha[] = str.split("\\r?\\n");
        int secaoAtual = SECAO_NENHUMA;


        for (int i=0; i<vLinha.length; i++) {
            if (vLinha[i] == null) continue;
            String s = null;
            s = vLinha[i].trim();
            if (s.length() == 0) continue;
            if (s.substring(0,1).equals("#")) continue;
            if (s.equals(CHAVE_ORDENACAO)) {
                secaoAtual = SECAO_ORDENACAO;
            } 
            else if (s.equals(CHAVE_COMPARACAO)) {
                secaoAtual = SECAO_COMPARACAO;
            }
            else if (s.equals(CHAVE_IDENTIFICAO_ELEMENTO_LISTA)) {
                secaoAtual = SECAO_INDENTIFICACAO_ELEMENTO_LISTA;
            }
            else if (secaoAtual == SECAO_ORDENACAO) {
                String sOrdenacao[] = s.split("\\s+");
                ArrayList<String> ordenacao = new ArrayList<String>();
                for (int j=0; j<sOrdenacao.length; j++) ordenacao.add(sOrdenacao[j]);
                resultado.ordenacao.add(ordenacao);
            }
            else if (secaoAtual == SECAO_COMPARACAO) {
                resultado.comparacao.add(s);
            }
            else if (secaoAtual == SECAO_INDENTIFICACAO_ELEMENTO_LISTA) {
                String sIdentificacaoElementoLista[] = s.split("\\s+");
                ArrayList<String> identificacaoElementoLista = new ArrayList<String>();
                for (int j=0; j<sIdentificacaoElementoLista.length; j++) identificacaoElementoLista.add(sIdentificacaoElementoLista[j]);
                resultado.identificacaoElementoLista.add(identificacaoElementoLista);
            }
            else {
                System.out.print("\nNenhuma secao ativa. Ignora linha " + s);
            }
        }
        return resultado;
    }

    public String toString() {
        return 
            "\n{ " +
            "\n   comparacao: [" + 
            "\n     " + this.comparacao.toString() + 
            "\n   ], " + 
            "\n   ordenacao: [" + 
            "\n      " + this.ordenacao.toString() + 
            "\n   ]," + 
            "\n   identificaoElementoLista: [" + 
            "\n      " + this.identificacaoElementoLista + 
            "\n   ]" +
            "\n}";
    }
}
