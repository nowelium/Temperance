package temperance.util;

public class LevenshteinDistance {
    protected static int min(int a, int b){
        if(a <= b){
            return a;
        }
        return b;
    }
    
    protected static int minimum(int a, int b, int c) {
        return min(min(a, b), c);
    }
    
    public static int levenshteinDistance(CharSequence seq1, CharSequence seq2){
        final int seq1Length = seq1.length();
        final int seq2Length = seq2.length();
        
        final int[][] distance = new int[seq1Length + 1][seq2Length + 1];
        for (int i = 0; i <= seq1Length; i++){
            distance[i][0] = i;
        }
        for (int i = 0; i <= seq2Length; i++){
            distance[0][i] = i;
        }

        for (int i = 1; i <= seq1Length; i++){
            for (int j = 1; j <= seq2Length; j++){
                int cost;
                if(seq1.charAt(i - 1) == seq2.charAt(j - 1)){
                    cost = 0;
                } else {
                    cost = 1;
                }
                
                int deletion = distance[i - 1][j] + 1;
                int insertion = distance[i][j - 1] + 1;
                int substitution = distance[i - 1][j - 1] + cost;
                distance[i][j] = minimum(deletion, insertion, substitution);
            }
        }
        return distance[seq1Length][seq2Length];
    }
}
