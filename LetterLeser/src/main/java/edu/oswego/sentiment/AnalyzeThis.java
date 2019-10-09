/**
 * @author Phoenix Boisnier
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//TODO Determine the final path locations of the python file and its output and which file is using that info.

/**
 * This class holds methods that execute python code.
 */
public class AnalyzeThis {

    /**
     * This returns the results from the sentiment analysis of the list of emails.
     * @param emails The list of emails to be analyzed.
     * @return A double[][][] array of emails' sentences' doubles for neg, neu, pos, and compound for the list of emails.
     */
    public static double[][][] sentimize(String[] emails){
        ArrayList<ArrayList<ArrayList<Double>>> retVal = new ArrayList<>();

        //This section feeds the python code the enciphered .txt file and demands the sentiment results.
        String filePath = AnalyzeThis.encipher(emails);
        File input = new File("output.txt");

        try {
            //TODO Replace the command string with the final pathname for the sentiment analyzer, SentimentPerEmail.py.
            Process p = Runtime.getRuntime().exec("python C:/Users/pbois/PycharmProjects/Sentiment/SentimentPerEmail.py "
                    + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()), 8);
            String vals = in.readLine();
            //TODO This will need to be changed to reflect the path the python file spits out, unless python puts it here.
            while(!vals.equals("output.txt")){

            }
            Scanner scone = new Scanner(input);
            while(scone.hasNext()){
                Scanner cone = new Scanner(scone.nextLine());
                ArrayList<ArrayList<Double>> scoresPerSentence = new ArrayList<>();
                while(cone.hasNext()){
                    ArrayList<Double> scores = new ArrayList<>();
                    scores.add(Double.parseDouble(cone.next()));
                    scores.add(Double.parseDouble(cone.next()));
                    scores.add(Double.parseDouble(cone.next()));
                    scores.add(Double.parseDouble(cone.next()));
                    scoresPerSentence.add(scores);
                }
                retVal.add(scoresPerSentence);
            }
            p.destroy();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Oh no.");
        }

        //Get your fresh, hot results here.

        return AnalyzeThis.processResults(retVal);
    }

    /**
     * This returns the results from the sentiment analysis of the list of emails.
     * @param emails The list of emails to be analyzed.
     * @return An array of emails' corresponding sentiment score objects.
     */
    public static SentimentScore[] singleScoreSentimize(String[] emails){
        SentimentScore[] scores = new SentimentScore[emails.length];

        //This section feeds the python code the enciphered .txt file and demands the sentiment results.
        String filePath = AnalyzeThis.encipher(emails);

        try {
            //TODO Replace the command string with the final pathname for the whole sentiment analyzer, SentimentByEmail.py.
            Process p = Runtime.getRuntime().exec("python C:/Users/pbois/PycharmProjects/Sentiment/SentimentByEmail.py "
                    + filePath);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()), 8);
            String vals = in.readLine();
            //TODO This will need to be changed to reflect the path the python file spits out, unless python puts it here.
            File input = new File(vals);
            Scanner scone = new Scanner(input);
            for(int q = 0; q < emails.length; q++){
                String line = scone.nextLine();
                Scanner scune = new Scanner(line);
                SentimentScore s = new SentimentScore(Double.parseDouble(scune.next()),
                        Double.parseDouble(scune.next()),
                        Double.parseDouble(scune.next()),
                        Double.parseDouble(scune.next()));
                scores[q] = s;
            }
            p.destroy();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("Oh no.");
        }
        //Get your fresh, hot results here.
        return scores;

    }

    /**
     * This enciphers and writes the input string array to a text file.
     * @param emails The strings to be enciphered.
     * @return The path name of the file.
     */
    private static String encipher(String[] emails){
        //This section creates the file and enciphers it.

        File pyIn = new File("pythonInput.txt");
        try{
            pyIn.createNewFile();
            FileWriter write = new FileWriter(pyIn);
            for (String email : emails) {
                //What we really do first is fix the string.
                String fixedEmail = fix(email);
                //First we apply a caesar shift of 10 to our email.
                String[] param1 = {fixedEmail, "10"};
                String ciph1 = Cipherer.encipher(0, param1);
                //Then we apply a vignere cipher with keyword systemic to the caesar shifted text.
                String[] param2 = {ciph1, "systemic"};
                String ciph2 = Cipherer.encipher(2, param2);
                write.write(ciph2 + "\n");
                write.flush();
            }
            write.close();
        }
        catch(IOException e) {
            System.out.println("Oh no!");
        }

        return pyIn.getPath();
    }

    /**
     * Used to turn the return value of sentimize into an array.
     * @param results These are the array list results to be processed.
     * @return The double[][][] array.
     */
    private static double[][][] processResults(ArrayList<ArrayList<ArrayList<Double>>> results){
        double[][][] retVal = new double[results.size()][][];
        for(int q = 0; q < results.size(); q++){
            retVal[q] = new double[results.get(q).size()][4];
            for(int w = 0; w < results.get(q).size(); w++){
                for(int e = 0; e < 4; e++){
                    retVal[q][w][e] = results.get(q).get(w).get(e);
                }
            }
        }
        return retVal;
    }

    /**
     * Feed me the results please.
     * @param results Results from a processResults(sentimize(emails)) call.
     * @return The average negative score.
     */
    public static double averageNeg(double[][][] results){
        double total = 0.0;
        double sentences = 0.0;
        for(double[][] email : results){
            for(double[] scores : email){
                total += scores[0];
                sentences += 1;
            }
        }
        return total / sentences;
    }

    /**
     * Feed me the results please.
     * @param results Results from a processResults(sentimize(emails)) call.
     * @return The average neutral score.
     */
    public static double averageNeu(double[][][] results){
        double total = 0.0;
        double sentences = 0.0;
        for(double[][] email : results){
            for(double[] scores : email){
                total += scores[1];
                sentences += 1;
            }
        }
        return total / sentences;
    }

    /**
     * Feed me the results please.
     * @param results Results from a processResults(sentimize(emails)) call.
     * @return The average positive score.
     */
    public static double averagePos(double[][][] results){
        double total = 0.0;
        double sentences = 0.0;
        for(double[][] email : results){
            for(double[] scores : email){
                total += scores[2];
                sentences += 1;
            }
        }
        return total / sentences;
    }

    /**
     * Feed me the results please.
     * @param results Results from a processResults(sentimize(emails)) call.
     * @return The average compound score.
     */
    public static double averageCompound(double[][][] results){
        double total = 0.0;
        double sentences = 0.0;
        for(double[][] email : results){
            for(double[] scores : email){
                total += scores[3];
                sentences += 1;
            }
        }
        return total / sentences;
    }

    /**
     * This method restricts strings to char values 0 to 255.
     * @param s The string that needs to be fixed.
     * @return The fixed string.
     */
    private static String fix(String s) {
        String retVal = "";
        char[] letters = s.toCharArray();
        for(int i = 0; i < letters.length; i++){
            if(letters[i] <=255){
                if(!(i==129||i==141||i==143||i==144||i==157||i==193||i==205||i==207||i==208||i==221)){
                    retVal += letters[i];
                }
            }
        }
        return retVal;
    }

    /**
     * This method counts the number of sentences in an email. Probably easier to use this than the arrayList thing.
     * @param email The text to be counted for sentences.
     * @return The sentence count as an integer.
     */
    public static int sentenceCount(String email) {
        int retVal = -1;
        try {
            //TODO change this to the final resting point for SentenceCounter.py
            Process p =  Runtime.getRuntime().exec("python C:/Users/pbois/PycharmProjects/Sentiment/SentenceCounter.py "+email);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()), 8);
            retVal = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Feed me the results please.
     * @param results Results from a processResults(sentimize(emails)) call.
     * @return The list of compound scores.
     */
    public static SentimentScore[] getScoresObjects(double[][][] results){
        SentimentScore[] retVal = new SentimentScore[results.length];
        for(int q = 0; q < retVal.length; q++){
            double negAvg = 0.0;
            double neuAvg = 0.0;
            double posAvg = 0.0;
            double comAvg = 0.0;
            for(int w = 0; w < results[q].length; w++){
                negAvg += results[q][w][0];
                neuAvg += results[q][w][1];
                posAvg += results[q][w][2];
                comAvg += results[q][w][3];
            }
            negAvg /= results[q].length;
            neuAvg /= results[q].length;
            posAvg /= results[q].length;
            comAvg /= results[q].length;
            SentimentScore s = new SentimentScore(negAvg, neuAvg, posAvg, comAvg);
            retVal[q] = s;
        }
        return retVal;
    }

    /**
     * This method evaluates the sentiment score into three categories based on it's compound score.
     * The ranges for what is considered positive, negative, and neutral is based on evaluation testing.
     * @param s The sentiment score to be evaluated.
     * @return 0, 1, or 2 for either negative, neutral, or positive, respectively.
     */
    public static int evaluateSentiment(SentimentScore s){
        if(s.getCompound() <= -0.05) return 0;
        if(s.getCompound() > -0.05) return 1;
        if(s.getCompound() >= 5.0/12.0) return 2;
        return -1;
    }

}