import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShortestPaths
{
    private long[][] dist; // distance matrix
    private long[][] pathLength; // path lengths matrix
    private char[][] interVert; // intermediate vertices matrix

    private int vertNum;
    private final long INF = Integer.MAX_VALUE;
    private final String NL = System.lineSeparator();


    public ShortestPaths(String filePath)
    {
        readFileContents(filePath);
        buildDistanceMatrix();
        buildPathLengthMatrix();
        buildInterVertMatrix();

        floydSolve();
        printDistMatrix();
        printPathLenMatrix();
        printInterVertMatrix();

        backtrackSolution();
    }

    private void readFileContents(String filePath)
    {
        String line;
        int lineNum = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            while((line = br.readLine()) != null)
            {
                if (lineNum == 1) vertNum = checkVertInput(line);
                else checkInput(line, lineNum, vertNum);
                lineNum++;
            }
        } catch (IOException ioe)
        {
            System.err.println("Error: Cannot open file '" + filePath + "'.");
            System.exit(1);
        }
    }

    private void checkInput(String line, int lineNum, int vert)
    {
//        String[] args = line.split(" ");
        String[] args = line.trim().split("\\s+");

        int delta = (int) 'A' + vert - 1;

        if (args.length != 3)
        {
            System.err.println("Error: Invalid edge data '" + line +
                    "' on line " + lineNum + ".");
            System.exit(1);
        }

        int weight = 0;
        try
        {
            weight = Integer.parseInt(args[2]);
        } catch (NumberFormatException e)
        {
            System.err.println("Error: Invalid edge weight '" + args[2] +
                            "' on line " + lineNum + ".");
            System.exit(1);
        }

        if (weight < 1)
        {
            System.err.println("Error: Invalid edge weight '" + weight +
                    "' on line " + lineNum + ".");
            System.exit(1);
        }

        // Starting vertex (if longer than char)
        if (args[0].length() != 1)
        {
            System.err.println("Error: Starting vertex '"+ args[0] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        // Ending vertex (if longer than char)
        if (args[1].length() != 1)
        {
            System.err.println("Error: Ending vertex '"+ args[1] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        // Strip all spaces from the line
        String cleanLine = line.replaceAll("\\s+", "");
        char[] charArr = cleanLine.toCharArray();

        // Check that the vertices are in range (A is 65 in ASCII)
        // Starting vertex
        char startVertex = args[0].charAt(0);
        if (startVertex > delta || startVertex < 'A')
        {
            System.err.println("Error: Starting vertex '"+ charArr[0] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        // Ending vertex
        char endVertex = args[1].charAt(0);
        if (endVertex > delta || endVertex < 'A')
        {
            System.err.println("Error: Ending vertex '"+ charArr[1] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        int fromInx = startVertex - 'A';
        int toInx = endVertex - 'A';
        dist[fromInx][toInx] = weight;
    }

    private int checkVertInput(String firstLine)
    {
        int vertNum = 0;

        try
        {
            vertNum = Integer.parseInt(firstLine);
        } catch (NumberFormatException e)
        {
            // TODO: verify this is the correct error message
            System.err.println("Error: Invalid number of vertices '"
                    + firstLine + "' on line 1.");
            System.exit(1);
        }

        if (vertNum < 1 || vertNum > 26)
        {
            System.err.println("Error: Invalid number of vertices '"
                    + firstLine + "' on line 1.");
            System.exit(1);
        }

        dist = new long[vertNum][vertNum];
        return vertNum;
    }


    private void buildDistanceMatrix()
    {
        for (int i = 0; i < dist.length; i++)
        {
            for (int j = 0; j < dist[i].length; j++)
            {
                if (i != j && dist[i][j] == 0)
                    dist[i][j] = INF;
            }
        }

        // A D 8 --> 'A' - 65 = 0; 'D' - 65 = 3
        // matrix[0][3] = 8
    }

    private void buildPathLengthMatrix()
    {
        pathLength = new long[dist.length][dist.length];

        for (int i = 0; i < dist.length; i++)
            // copy dist matrix to pathLength matrix
            System.arraycopy(dist[i], 0, pathLength[i], 0, dist[i].length);

    }

    private void buildInterVertMatrix()
    {
        interVert = new char[dist.length][dist.length];

        for (int i = 0; i < dist.length; i++)
        {
            for (int j = 0; j < dist[i].length; j++)
            {
                interVert[i][j] = '-';
            }
        }

    }

    private void floydSolve()
    {
        int n = vertNum;

        for (int k = 0; k < n; k++)
        {
            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < n; j++)
                {
                    // Check for infinity before adding to prevent overflow
                    if (pathLength[i][k] != INF && pathLength[k][j] != INF)
                    {
                        long newVal = pathLength[i][k] + pathLength[k][j];
                        if (newVal < pathLength[i][j])
                        {
                            // Save to tables
                            pathLength[i][j] = newVal;
                            interVert[i][j] = inxToChar(k);
                        }
                    }
                }
            }
        }
    }

    private char inxToChar(int inx)
    {
        return (char) ('A' + inx);
    }

    private int charToInx(char ch)
    {
        return (int) ch - 'A';
    }

    private void backtrackSolution()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertNum; i++)
        {
            for (int j = 0; j < vertNum; j++)
            {
                long currentPathLength = pathLength[i][j];
                String distanceString = (currentPathLength == INF) ? "infinity" : String.valueOf(currentPathLength);

                sb.append(inxToChar(i)).append(" -> ").append(inxToChar(j))
                        .append(", distance: ").append(distanceString)
                        .append(", path: ");

                // Only print a path if one exists
                if (currentPathLength != INF)
                {
                    sb.append(getRecursivePath(i, j));
                } else
                {
                    sb.append("none");
                }

                // Append new line unless it's the very last line of output
                if (i < vertNum - 1 || j < vertNum - 1)
                {
                    sb.append(NL);
                }
            }
        }

        System.out.print(sb);
    }

    private String getRecursivePath(int i, int j)
    {
        // Base case: path to the same vertex.
        if (i == j) {
            return String.valueOf(inxToChar(i));
        }

        char intermediateChar = interVert[i][j];

        // Base case: a direct path exists between two different vertices.
        if (intermediateChar == '-') {
            return inxToChar(i) + " -> " + inxToChar(j);
        }

        // Recursive step:
        int interIndex = charToInx(intermediateChar);
        String firstHalf = getRecursivePath(i, interIndex);
        String secondHalf = getRecursivePath(interIndex, j);

        // Combine the two halves. For a path A -> B -> C, firstHalf is "A -> B"
        // and secondHalf is "B -> C". We combine them by taking "A -> B" and
        // appending " -> C" from the second half.
        return firstHalf + secondHalf.substring(1);
    }

    private String render(long v)
    {
        return v == INF ? "∞" : Long.toString(v);
    }

    private int cellWidth(long[][] m)
    {
        int w = 1;
        for (long[] r : m)
            for (long v : r)
                w = Math.max(w, render(v).length());
        return w;
    }
    private int cellWidth(char[][] m) { return 1; }

    private void appendHeader(StringBuilder sb, int n, int cw)
    {
        final String cellFmt = "%" + cw + "s";
        // Add a placeholder for the row header column
        sb.append(' ');
        for (int j = 0; j < n; j++) {
            // Prepend a separator and then the formatted column header
            sb.append(' ');
            sb.append(String.format(cellFmt, (char)('A' + j)));
        }
    }

    private void printDistMatrix()
    {
        final int n  = dist.length;
        final int cw = cellWidth(dist);
        final String cellFmt = "%" + cw + "s";


        System.out.println("Distance matrix:");
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, n, cw);

        for (int i = 0; i < n; i++) {
            sb.append(NL);
            sb.append((char)('A' + i)); // Row header
            for (int j = 0; j < n; j++) {
                sb.append(' '); // Separator
                sb.append(String.format(cellFmt, render(dist[i][j])));
            }
        }
        sb.append(NL).append(NL);
        System.out.print(sb);
    }

    private void printPathLenMatrix()
    {
        final int n  = pathLength.length;
        final int cw = cellWidth(pathLength);
        final String cellFmt = "%" + cw + "s";

        System.out.println("Path lengths:");
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, n, cw);

        for (int i = 0; i < n; i++) {
            sb.append(NL);
            sb.append((char)('A' + i)); // Row header
            for (int j = 0; j < n; j++) {
                sb.append(' '); // Separator
                sb.append(String.format(cellFmt, render(pathLength[i][j])));
            }
        }
        sb.append(NL).append(NL);
        System.out.print(sb);
    }

    private void printInterVertMatrix()
    {
        final int n  = interVert.length;
        final int cw = cellWidth(interVert);
        final String cellFmt = "%" + cw + "s";

        System.out.println("Intermediate vertices:");
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, n, cw);

        for (int i = 0; i < n; i++) {
            sb.append(NL);
            sb.append((char)('A' + i)); // Row header
            for (int j = 0; j < n; j++) {
                sb.append(' '); // Separator
                sb.append(String.format(cellFmt, interVert[i][j]));
            }
        }
        sb.append(NL).append(NL);
        System.out.print(sb);
    }


    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java ShortestPaths <filename>");
            System.exit(1);
        }

        new ShortestPaths(args[0]);
    }
}
