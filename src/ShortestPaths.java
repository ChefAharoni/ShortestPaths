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

        String filePath = args[0];
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

        String fileContents = sb.toString();
        testMethod(fileContents);
    }

    public static void testMethod(String file)
    {
        System.out.println("Testing " + file);
    }
}
