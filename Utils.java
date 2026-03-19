package com.example.upgradedapp2;

import java.util.*;

public class Utils {
    private static final Set<String> STOP = new HashSet<>(Arrays.asList(
            "the","is","a","an","and","or","of","to","in","for","on","with","this","that","it","as","by","be","are","was","at","from","will","we","you","your","our"
    ));

    public static String extractKeywords(String text){
        if(text == null) return "";
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] parts = text.split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for(String p : parts){
            if(p.length() < 3) continue;
            if(STOP.contains(p)) continue;
            freq.put(p, freq.getOrDefault(p,0)+1);
        }
        List<Map.Entry<String,Integer>> list = new ArrayList<>(freq.entrySet());
        list.sort((a,b)->b.getValue()-a.getValue());
        int top = Math.min(5, list.size());
        List<String> out = new ArrayList<>();
        for(int i=0;i<top;i++) out.add(list.get(i).getKey());
        return String.join(", ", out);
    }
}
