import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShortestPaths
{
    public ShortestPaths(String filePath)
    {
        String fileContents = readFileContents(filePath);
        testMethod(fileContents);
    }

    private String readFileContents(String filePath)
    {
        StringBuilder sb = new StringBuilder();
        String line;
        int lineNum = 1;
        int vert = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            while((line = br.readLine()) != null)
            {
                sb.append(line).append(System.lineSeparator());
                if (lineNum == 1) vert = checkVertInput(line);
                else checkInput(line, lineNum, vert);
                lineNum++;
            }
        } catch (IOException ioe)
        {
            System.err.println("Error: Cannot open file '" + filePath + "'.");
            System.exit(1);
        }

        return sb.toString();
    }


    private void checkInput(String line, int lineNum, int vertices)
    {
        String[] args = line.split(" ");
        int delta = (int) 'A' + vertices - 1;

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

        // Ending vertex
        asciiVal = (int) charArr[1];
        if (asciiVal > delta || asciiVal < 65)
        {
            System.err.println("Error: Ending vertex '"+ charArr[1] +
                    "' on line " + lineNum +  " is not among valid values " +
                    "A-"+ (char) delta + ".");
            System.exit(1);
        }
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

        return vertNum;
    }

    // Debugger
    private void testMethod(String file)
    {
        System.out.println("File contents:\n" + file);
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
