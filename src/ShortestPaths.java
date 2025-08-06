import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShortestPaths
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java ShortestPaths <filename>");
            System.exit(1);
        }

        String fileContents = readFileContents(args[0]);
        testMethod(fileContents);
    }

    private static String readFileContents(String filePath)
    {
        StringBuilder sb = new StringBuilder();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            while((line = br.readLine()) != null)
            {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException ioe)
        {
            System.err.println("Error: Cannot open file '" + filePath + "'.");
            System.exit(1);
        }

        return sb.toString();
    }

    public static void testMethod(String file)
    {
        System.out.println("Testing " + file);
    }
}
