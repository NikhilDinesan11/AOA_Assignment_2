import java.util.*;
import java.io.*;

public class WeightedSubstring {
    private double[] weights;
    private double penalty;
    
    // Letter frequencies in English (approximate percentages)
    private static final double[] ENGLISH_FREQ = {
        8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094, 6.966, 0.153,
        0.772, 4.025, 2.406, 6.749, 7.507, 1.929, 0.095, 5.987, 6.327, 9.056,
        2.758, 0.978, 2.360, 0.150, 1.974, 0.074
    };
    
    // Result class to store substring information
    static class SubstringResult {
        double score;
        int start1, start2;
        int length;
        String substring1, substring2;
        
        SubstringResult(double score, int start1, int start2, int length, 
                       String substring1, String substring2) {
            this.score = score;
            this.start1 = start1;
            this.start2 = start2;
            this.length = length;
            this.substring1 = substring1;
            this.substring2 = substring2;
        }
        
        @Override
        public String toString() {
            return String.format("Score: %.2f\nPosition in string1: %d\nPosition in string2: %d\n" +
                                "Length: %d\nSubstring1: %s\nSubstring2: %s",
                                score, start1, start2, length, substring1, substring2);
        }
    }
    
    public WeightedSubstring(int scenario, double penalty) {
        this.penalty = penalty;
        this.weights = new double[26];
        
        if (scenario == 1) {
            // Scenario 1: all weights = 1
            Arrays.fill(weights, 1.0);
        } else {
            // Scenario 2: weights proportional to English frequency
            setWeightsProportional();
        }
    }
    
    // Constructor for Scenario 2 with custom weight range
    public WeightedSubstring(double penalty, double minWeight, double maxWeight) {
        this.penalty = penalty;
        this.weights = new double[26];
        setWeightsProportional(minWeight, maxWeight);
    }
    
    private void setWeightsProportional() {
        setWeightsProportional(1.0, 10.0);
    }
    
    private void setWeightsProportional(double minWeight, double maxWeight) {
        double minFreq = Double.MAX_VALUE;
        double maxFreq = Double.MIN_VALUE;
        
        for (double freq : ENGLISH_FREQ) {
            minFreq = Math.min(minFreq, freq);
            maxFreq = Math.max(maxFreq, freq);
        }
        
        for (int i = 0; i < 26; i++) {
            // Normalize frequency to [minWeight, maxWeight]
            weights[i] = minWeight + (ENGLISH_FREQ[i] - minFreq) * 
                        (maxWeight - minWeight) / (maxFreq - minFreq);
        }
    }
    
    // Main DP algorithm
    public SubstringResult findBestSubstring(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        // DP table: dp[i][j] = max score for substring ending at s1[i-1], s2[j-1]
        double[][] dp = new double[m + 1][n + 1];
        int[][] length = new int[m + 1][n + 1]; // Track substring length
        
        // Variables to track best solution
        double maxScore = 0;
        int maxI = 0, maxJ = 0, maxLength = 0;
        
        // Fill DP table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char c1 = s1.charAt(i - 1);
                char c2 = s2.charAt(j - 1);
                
                // Calculate score for current position
                double score;
                if (c1 == c2) {
                    // Match: add weight
                    int charIndex = Character.toUpperCase(c1) - 'A';
                    score = weights[charIndex];
                } else {
                    // Mismatch: subtract penalty
                    score = -penalty;
                }
                
                // Either extend previous substring or start new
                if (dp[i-1][j-1] + score > 0) {
                    dp[i][j] = dp[i-1][j-1] + score;
                    length[i][j] = length[i-1][j-1] + 1;
                } else {
                    dp[i][j] = 0;
                    length[i][j] = 0;
                }
                
                // Update maximum if needed
                if (dp[i][j] > maxScore) {
                    maxScore = dp[i][j];
                    maxI = i;
                    maxJ = j;
                    maxLength = length[i][j];
                }
            }
        }
        
        // Extract the optimal substring
        if (maxLength > 0) {
            int start1 = maxI - maxLength;
            int start2 = maxJ - maxLength;
            String substring1 = s1.substring(start1, maxI);
            String substring2 = s2.substring(start2, maxJ);
            
            return new SubstringResult(maxScore, start1, start2, maxLength, 
                                      substring1, substring2);
        }
        
        return new SubstringResult(0, -1, -1, 0, "", "");
    }
    
    // Generate random string for testing
    public static String generateRandomString(int length, Random rand) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char)('A' + rand.nextInt(26)));
        }
        return sb.toString();
    }
    
    // Run experiments
    public static void runExperiments() {
        System.out.println("=== WEIGHTED APPROXIMATE COMMON SUBSTRING EXPERIMENTS ===\n");
        
        // Test with example from problem
        System.out.println("Example Test:");
        System.out.println("--------------");
        String s1 = "ABCAABCAA";
        String s2 = "ABBCAACCBBBBBB";
        
        WeightedSubstring ws1 = new WeightedSubstring(1, 5.0);
        SubstringResult result = ws1.findBestSubstring(s1, s2);
        System.out.println("Input strings:");
        System.out.println("String 1: " + s1);
        System.out.println("String 2: " + s2);
        System.out.println("\nResult:");
        System.out.println(result);
        
        // Scenario 1: w[i] = 1 for all i, p = 5
        System.out.println("\n\n=== SCENARIO 1: Uniform weights (w=1), penalty=5 ===");
        runScenario(1, 5.0, 1.0, 1.0);
        
        // Scenario 2: Weights proportional to English frequency
        System.out.println("\n\n=== SCENARIO 2: Weights proportional to English frequency ===");
        System.out.println("Testing with 10 different weight ranges:\n");
        
        for (int i = 0; i <= 10; i++) {
            double minWeight = 1.0;
            double maxWeight = 1.0 + i * 0.9; // Range from 1-1 to 1-10
            System.out.printf("\nWeight range [%.1f, %.1f]:\n", minWeight, maxWeight);
            runScenario(2, 5.0, minWeight, maxWeight);
        }
    }
    
    private static void runScenario(int scenario, double penalty, 
                                   double minWeight, double maxWeight) {
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        // Generate test strings of various lengths
        int[] lengths1 = {50, 100, 200, 500, 1000};
        int[] lengths2 = {50, 100, 200, 500, 1000};
        
        System.out.println("String Lengths | Avg Score | Avg Length | Avg Time(ms)");
        System.out.println("--------------------------------------------------------");
        
        for (int len1 : lengths1) {
            for (int len2 : lengths2) {
                if (len1 > 200 && len2 > 200) continue; // Skip very large combinations
                
                double totalScore = 0;
                double totalLength = 0;
                long totalTime = 0;
                int trials = 5;
                
                for (int trial = 0; trial < trials; trial++) {
                    String s1 = generateRandomString(len1, rand);
                    String s2 = generateRandomString(len2, rand);
                    
                    WeightedSubstring ws;
                    if (scenario == 1) {
                        ws = new WeightedSubstring(1, penalty);
                    } else {
                        ws = new WeightedSubstring(penalty, minWeight, maxWeight);
                    }
                    
                    long startTime = System.nanoTime();
                    SubstringResult result = ws.findBestSubstring(s1, s2);
                    long endTime = System.nanoTime();
                    
                    totalScore += result.score;
                    totalLength += result.length;
                    totalTime += (endTime - startTime);
                }
                
                double avgScore = totalScore / trials;
                double avgLength = totalLength / trials;
                double avgTime = totalTime / trials / 1_000_000.0; // Convert to ms
                
                System.out.printf("%4d x %4d    | %9.2f | %10.1f | %11.3f\n",
                                 len1, len2, avgScore, avgLength, avgTime);
            }
        }
    }
    
    // Print weights for debugging
    public void printWeights() {
        System.out.println("Character weights:");
        for (int i = 0; i < 26; i++) {
            System.out.printf("%c: %.3f  ", (char)('A' + i), weights[i]);
            if ((i + 1) % 6 == 0) System.out.println();
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        runExperiments();
    }
}
