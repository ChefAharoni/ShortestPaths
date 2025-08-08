import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ShortestPaths
{
    private long[][] dist; // distance matrix
    private long[][] pathLength; // path lengths matrix
    private char[][] interVert; // intermediate vertices matrix

    private Character[] vertices;
    private int vertNum;
    private int tableWidth;
    private final long INF = (long) Integer.MAX_VALUE + 1;


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
//        StringBuilder sb = new StringBuilder();
        String line;
        int lineNum = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            while((line = br.readLine()) != null)
            {
//                sb.append(line).append(System.lineSeparator());
                if (lineNum == 1) vertNum = checkVertInput(line);
                else checkInput(line, lineNum, vertNum);
                lineNum++;
            }
        } catch (IOException ioe)
        {
            System.err.println("Error: Cannot open file '" + filePath + "'.");
            System.exit(1);
        }

//        return sb.toString();
    }

    private void checkInput(String line, int lineNum, int vert)
    {
        String[] args = line.split(" ");
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
        int asciiVal = (int) charArr[0];
        if (asciiVal > delta || asciiVal < 65)
        {
            System.err.println("Error: Starting vertex '"+ charArr[0] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        int fromInx = asciiVal - 65;

        // Ending vertex
        asciiVal = (int) charArr[1];
        if (asciiVal > delta || asciiVal < 65)
        {
            System.err.println("Error: Ending vertex '"+ charArr[1] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }

        int toInx = asciiVal - 65;
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
                    long prevVal = pathLength[i][j];
                    long newVal = Math.min(pathLength[i][j],
                            pathLength[i][k] + pathLength[k][j]);
                    if (newVal < prevVal)
                    {
                        // save to table
                        pathLength[i][j] = newVal;
                        interVert[i][j] = inxToChar(k);
                    }
                }
            }
        }
    }

    private char inxToChar(int inx)
    {
        return (char) (65 + inx);
    }

    private int charToInx(char ch)
    {
        return (int) ch - 'A';
    }

    private void backtrackSolution()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pathLength.length; i++)
        {
            for  (int j = 0; j < pathLength[i].length; j++)
            {
                sb.append(inxToChar(i)).append(" -> ").append(inxToChar(j))
                    .append(", distance: ").append(pathLength[i][j])
                    .append(", path: ").append(printPath(i, j))
                    .append(System.lineSeparator());
            }
        }

        System.out.println(sb);
    }

    private String getPath(int from, int to, int pathsNum)
    {
        if (from == to)
            return "" + inxToChar(from);

        else if (interVert[from][to] == '-')
            return "" + inxToChar(from) + inxToChar(to);

        else
        {
            int inx = charToInx(interVert[from][to]);
            return inxToChar(from) + getPath(inx, to, pathsNum + 1);
        }

    }

    private String printPath(int from, int to)
    {
        String path = getPath(from, to, 0);
        char[] chars = path.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (char aChar : chars)
        {
            sb.append(aChar)
                    .append(" -> ");
        }

        // Delete the last arrow
        sb.delete(sb.length() - 4, sb.length());

        return sb.toString();
    }

    private void printDistMatrix()
    {
        // To get the length of printing, we need to get the width of the
        // longest number that is printed
        // TODO: get longest width and repeat with that
        tableWidth = 4;

        StringBuilder sb = new StringBuilder();
        System.out.println("Distance matrix:");

        sb.append(" ".repeat(tableWidth + 1));

        for (int i = 0; i < dist.length; i++)
        {
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
        }

        for (int i = 0; i < dist.length; i++)
        {
            sb.append(System.lineSeparator());
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
            for (int j = 0; j < dist[i].length; j++)
            {
                if (dist[i][j] == INF)
                    sb.append("∞").append(" ".repeat(tableWidth));
                else
                    sb.append(dist[i][j]).append(" ".repeat(tableWidth));
            }
        }

        sb.append(System.lineSeparator());
        System.out.println(sb);
    }

    private void printPathLenMatrix()
    {
        StringBuilder sb = new StringBuilder();
        System.out.println("Path length:");
        sb.append(" ".repeat(tableWidth + 1));

        for (int i = 0; i < pathLength.length; i++)
        {
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
        }

        for (int i = 0; i < pathLength.length; i++)
        {
            sb.append(System.lineSeparator());
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
            for (int j = 0; j < pathLength[i].length; j++)
            {
                if (pathLength[i][j] == INF)
                    sb.append("∞").append(" ".repeat(tableWidth));
                else
                    sb.append(pathLength[i][j]).append(" ".repeat(tableWidth));
            }
        }

        sb.append(System.lineSeparator());
        System.out.println(sb);
    }

    private void printInterVertMatrix()
    {
        StringBuilder sb = new StringBuilder();
        System.out.println("Intermediate vertices:");
        sb.append(" ".repeat(tableWidth + 1));

        for (int i = 0; i < interVert.length; i++)
        {
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
        }

        for (int i = 0; i < interVert.length; i++)
        {
            sb.append(System.lineSeparator());
            sb.append((char) ('A' + i)).append(" ".repeat(tableWidth));
            for (int j = 0; j < interVert[i].length; j++)
            {
                if (interVert[i][j] == INF)
                    sb.append("∞").append(" ".repeat(tableWidth));
                else
                    sb.append(interVert[i][j]).append(" ".repeat(tableWidth));
            }
        }

        sb.append(System.lineSeparator());
        System.out.println(sb);
    }


    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java ShortestPaths <filename>");
            System.exit(1);
        }

        ShortestPaths shortestPaths = new ShortestPaths(args[0]);
    }
}
