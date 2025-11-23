import java.util.*;
import java.io.*;

public class LargestZeroSubMatrix {
    
    // Result class to store the solution
    static class MatrixResult {
        int size;  // k for k×k matrix
        int row, col;  // Top-left corner position
        
        MatrixResult(int size, int row, int col) {
            this.size = size;
            this.row = row;
            this.col = col;
        }
        
        @Override
        public String toString() {
            return String.format("Largest zero sub-matrix: %d×%d at position (%d, %d)", 
                                size, size, row, col);
        }
    }
    
    // Main DP algorithm to find largest zero square sub-matrix
    public static MatrixResult findLargestZeroSquare(byte[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return new MatrixResult(0, -1, -1);
        }
        
        int m = matrix.length;
        int n = matrix[0].length;
        
        // DP table: dp[i][j] = size of largest square with bottom-right at (i,j)
        int[][] dp = new int[m][n];
        
        // Track the maximum size and position
        int maxSize = 0;
        int maxRow = -1, maxCol = -1;
        
        // Initialize first row
        for (int j = 0; j < n; j++) {
            dp[0][j] = (matrix[0][j] == 0) ? 1 : 0;
            if (dp[0][j] > maxSize) {
                maxSize = dp[0][j];
                maxRow = 0;
                maxCol = j;
            }
        }
        
        // Initialize first column
        for (int i = 0; i < m; i++) {
            dp[i][0] = (matrix[i][0] == 0) ? 1 : 0;
            if (dp[i][0] > maxSize) {
                maxSize = dp[i][0];
                maxRow = i;
                maxCol = 0;
            }
        }
        
        // Fill the DP table
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                if (matrix[i][j] == 0) {
                    // Can extend the square from three directions
                    dp[i][j] = 1 + Math.min(dp[i-1][j-1], 
                                  Math.min(dp[i-1][j], dp[i][j-1]));
                    
                    if (dp[i][j] > maxSize) {
                        maxSize = dp[i][j];
                        maxRow = i;
                        maxCol = j;
                    }
                } else {
                    dp[i][j] = 0;
                }
            }
        }
        
        // Calculate top-left corner from bottom-right corner
        if (maxSize > 0) {
            maxRow = maxRow - maxSize + 1;
            maxCol = maxCol - maxSize + 1;
        }
        
        return new MatrixResult(maxSize, maxRow, maxCol);
    }
    
    // Generate random boolean matrix
    public static byte[][] generateRandomMatrix(int m, int n, double zeroProbability, Random rand) {
        byte[][] matrix = new byte[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = (rand.nextDouble() < zeroProbability) ? (byte)0 : (byte)1;
            }
        }
        return matrix;
    }
    
    // Print matrix for debugging (only for small matrices)
    public static void printMatrix(byte[][] matrix, MatrixResult result) {
        if (matrix.length > 20 || matrix[0].length > 20) {
            System.out.println("Matrix too large to display");
            return;
        }
        
        System.out.println("Matrix:");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                // Highlight the solution
                boolean inSolution = result != null && 
                    i >= result.row && i < result.row + result.size &&
                    j >= result.col && j < result.col + result.size;
                
                if (inSolution) {
                    System.out.print("[" + matrix[i][j] + "]");
                } else {
                    System.out.print(" " + matrix[i][j] + " ");
                }
            }
            System.out.println();
        }
    }
    
    // Calculate memory usage
    public static long calculateMemoryUsage(int m, int n) {
        // Input matrix: m*n bytes
        // DP table: m*n*4 bytes (int array)
        // Total = m*n*5 bytes
        return (long)m * n * 5;
    }
    
    // Run experiments
    public static void runExperiments() {
        System.out.println("=== LARGEST ZERO SUB-MATRIX EXPERIMENTS ===\n");
        
        // Test with a small example first
        System.out.println("Example Test:");
        System.out.println("--------------");
        byte[][] example = {
            {1, 0, 0, 1, 0},
            {0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0},
            {1, 0, 0, 0, 0},
            {0, 0, 1, 0, 0}
        };
        
        MatrixResult exampleResult = findLargestZeroSquare(example);
        printMatrix(example, exampleResult);
        System.out.println(exampleResult);
        
        // Run experiments with different matrix sizes
        System.out.println("\n\n=== PERFORMANCE EXPERIMENTS ===\n");
        
        int[][] testSizes = {
            {10, 10},
            {10, 100},
            {10, 1000},
            {100, 1000},
            {1000, 1000}
        };
        
        double zeroProbability = 0.7; // 70% zeros
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        System.out.println("Matrix Size    | Max Square | Time (ms) | Memory (MB) | Memory Used (MB)");
        System.out.println("--------------------------------------------------------------------------");
        
        List<Integer> sizes = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        List<Double> memories = new ArrayList<>();
        
        for (int[] size : testSizes) {
            int m = size[0];
            int n = size[1];
            
            // Run multiple trials for more accurate timing
            int trials = (m * n <= 10000) ? 10 : 3; // More trials for smaller matrices
            long totalTime = 0;
            int totalMaxSize = 0;
            
            // Get memory before allocation
            System.gc();
            Runtime runtime = Runtime.getRuntime();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            
            for (int trial = 0; trial < trials; trial++) {
                byte[][] matrix = generateRandomMatrix(m, n, zeroProbability, rand);
                
                long startTime = System.nanoTime();
                MatrixResult result = findLargestZeroSquare(matrix);
                long endTime = System.nanoTime();
                
                totalTime += (endTime - startTime);
                totalMaxSize += result.size;
            }
            
            // Get memory after allocation
            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            double memUsedMB = (memAfter - memBefore) / (1024.0 * 1024.0);
            
            double avgTime = totalTime / trials / 1_000_000.0; // Convert to ms
            double avgMaxSize = (double) totalMaxSize / trials;
            double theoreticalMemoryMB = calculateMemoryUsage(m, n) / (1024.0 * 1024.0);
            
            System.out.printf("%4d × %4d    | %10.1f | %9.3f | %11.2f | %15.2f\n",
                            m, n, avgMaxSize, avgTime, theoreticalMemoryMB, memUsedMB);
            
            // Store for graphing
            sizes.add(m * n);
            times.add(avgTime);
            memories.add(theoreticalMemoryMB);
        }
        
        // Print summary statistics
        System.out.println("\n=== COMPLEXITY ANALYSIS ===");
        System.out.println("Time Complexity: O(m × n) - Each cell is processed once");
        System.out.println("Space Complexity: O(m × n) - DP table storage");
        System.out.println("\nAs matrix size increases, both time and memory scale linearly with total elements (m×n)");
        
        // Create simple ASCII graph for visualization
        System.out.println("\n=== TIME COMPLEXITY GRAPH (ASCII) ===");
        printAsciiGraph(sizes, times, "Matrix Size (m×n)", "Time (ms)");
        
        System.out.println("\n=== MEMORY USAGE GRAPH (ASCII) ===");
        printAsciiGraph(sizes, memories, "Matrix Size (m×n)", "Memory (MB)");
    }
    
    // Simple ASCII graph printer
    private static void printAsciiGraph(List<Integer> x, List<Double> y, 
                                       String xLabel, String yLabel) {
        int width = 60;
        int height = 15;
        
        double maxY = Collections.max(y);
        double minY = 0;
        int maxX = Collections.max(x);
        int minX = 0;
        
        System.out.println(yLabel);
        System.out.println("^");
        
        for (int row = height; row >= 0; row--) {
            double yVal = minY + (maxY - minY) * row / height;
            System.out.printf("%7.1f |", yVal);
            
            for (int col = 0; col < width; col++) {
                int xVal = minX + (maxX - minX) * col / width;
                
                boolean plotPoint = false;
                for (int i = 0; i < x.size(); i++) {
                    int xPos = (int)((x.get(i) - minX) * width / (double)(maxX - minX));
                    int yPos = (int)((y.get(i) - minY) * height / (maxY - minY));
                    
                    if (Math.abs(col - xPos) <= 1 && Math.abs(row - yPos) <= 0) {
                        plotPoint = true;
                        break;
                    }
                }
                
                System.out.print(plotPoint ? "*" : (row == 0 ? "-" : " "));
            }
            System.out.println();
        }
        
        System.out.println("        +" + "-".repeat(width) + "> " + xLabel);
        System.out.printf("        0%s%d\n", " ".repeat(width-5), maxX);
    }
    
    // Additional analysis method to verify correctness
    public static boolean verifyResult(byte[][] matrix, MatrixResult result) {
        if (result.size == 0) return true;
        
        // Check if all elements in the reported square are zeros
        for (int i = result.row; i < result.row + result.size; i++) {
            for (int j = result.col; j < result.col + result.size; j++) {
                if (matrix[i][j] != 0) {
                    return false;
                }
            }
        }
        
        // Check if there's no larger square (brute force for verification)
        for (int size = result.size + 1; size <= Math.min(matrix.length, matrix[0].length); size++) {
            for (int i = 0; i <= matrix.length - size; i++) {
                for (int j = 0; j <= matrix[0].length - size; j++) {
                    boolean allZeros = true;
                    for (int di = 0; di < size && allZeros; di++) {
                        for (int dj = 0; dj < size && allZeros; dj++) {
                            if (matrix[i + di][j + dj] != 0) {
                                allZeros = false;
                            }
                        }
                    }
                    if (allZeros) {
                        return false; // Found a larger square
                    }
                }
            }
        }
        
        return true;
    }
    
    public static void main(String[] args) {
        runExperiments();
        
        // Run verification tests
        System.out.println("\n\n=== VERIFICATION TESTS ===");
        Random rand = new Random();
        boolean allCorrect = true;
        
        for (int test = 0; test < 10; test++) {
            int m = 10 + rand.nextInt(20);
            int n = 10 + rand.nextInt(20);
            byte[][] matrix = generateRandomMatrix(m, n, 0.7, rand);
            MatrixResult result = findLargestZeroSquare(matrix);
            
            boolean correct = verifyResult(matrix, result);
            System.out.printf("Test %d (%dx%d): %s\n", test+1, m, n, 
                            correct ? "PASSED" : "FAILED");
            if (!correct) allCorrect = false;
        }
        
        System.out.println("\nAll tests " + (allCorrect ? "PASSED" : "FAILED"));
    }
}
