import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Tehillim_Matcher {
    private static Document doc;

    public static void main(String[] args){

        //A document link is set up to the xml file containing the Prakim of Tehillim
        //Adapted from Gemini
        try{
            File inputFile = new File("tehillim.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e){
            System.out.println("We're sorry, an issue occured. Please try again later.");
            e.printStackTrace();
        }

        //Interfaces with the user and sends them to the appropriate function
        Scanner scan = new Scanner(System.in);
        String response ="";
        System.out.println("Welcome to the Tehillim Matcher!\nTo find someone to daven for, enter \"F\".\nTo enter someone new to daven for, enter \"N\".");
        response = scan.nextLine();
        while (!response.equals("F") && !response.equals("N") && !response.equals("f") && !response.equals("n")){
            System.out.println("I'm sorry, I didn't understand that.\nTo find someone to daven for, enter \"F\".\nTo enter someone new to daven for, enter \"N\".");
            response = scan.nextLine();
        }
        response = response.toUpperCase();
        switch (response){
            case "F":
                try {
                    getPerson();
                } catch (SQLException e){}
                break;
            case "N":
                try {
                    signUp();
                } catch (SQLException e){}
                break;
        }

        //Asks the user if they want to "play again", or quit.
        System.out.println("\n\nIf you would like to take another action, enter \"C\" to continue. Otherwise, press \"Q\" to quit.");
        response = scan.nextLine();
        while (!response.equals("C") && !response.equals("Q") && !response.equals("c") && !response.equals("q")){
            System.out.println("I'm sorry, I didn't understand that.\nIf you would like to take another action, enter \"C\" to continue. Otherwise, press \"Q\" to quit.");
            response = scan.nextLine();
        }
        response = response.toUpperCase();
        switch (response){
            case "C":
                main(null);
                break;
            case "Q":
                scan.close();
                break;
        }
        return;
    }    

    // Assigns the user a random person that needs tehillim from the database
    public static void getPerson() throws SQLException
    {
        //Connects to the sql server
        String sqlUrl = "jdbc:sqlserver://localhost:1433;databaseName=Tmatcher;user=sa;password=flyerstitchkohack;encrypt=true;trustServerCertificate=true;";
        Connection connection = DriverManager.getConnection(sqlUrl);
        if (connection == null){
            System.out.println("We're sorry, an issue occured. Please try again later.");
            return;
        }
        
        //Gets a count of how many people are in the database at the moment
        Statement statement = connection.createStatement();
        ResultSet idres = statement.executeQuery("SELECT COUNT(PersonId) FROM People");
        idres.next();
        String idstr = idres.getString(1);
        int ids = Integer.parseInt(idstr);
        
        //Finds a random person and gives the data of that person
        int personId = (int) (Math.random() * ids) + 1;
        ResultSet personInfo = statement.executeQuery("SELECT HebName, EntryDate, Age, Bio, Shul, UpTo FROM People WHERE PersonId = " + personId);
        personInfo.next();
        String hebName = personInfo.getString(1);
        String entryDate = personInfo.getString(2);
        String age = personInfo.getString(3);
        String bio = personInfo.getString(4);
        String shul = personInfo.getString(5);
        int upTo = Integer.parseInt(personInfo.getString(6));
        System.out.println("\n\nYou have been matched with " + hebName + ".\nFirst entered on " + entryDate + "\nAge: " + age + "\n" + bio + "\nWe are currently up to Tehillim " + upTo);

        //Asks what language option is wanted for the tehillim, and sets booleans to indicate the response
        Scanner scan = new Scanner(System.in);
        String response ="";
        boolean eng = false;
        boolean heb = false;
        boolean trans = false;
        System.out.println("\nFor English only, enter \"E\". For Hebrew only, enter \"H\". For Hebrew Transliteration only, enter \"T\".\nFor English and Hebrew, enter \"EH\". For Hebrew and Hebrew Transliteration, enter \"HT\". For English and Hebrew Transliteration, enter \"ET\".\nFor all three, enter \"A\".");
        response = scan.nextLine();
        response = response.toUpperCase();
        while (!response.equals("E") && !response.equals("H") && !response.equals("T") && !response.equals("EH") && !response.equals("HT") && !response.equals("ET") && !response.equals("A")){
            System.out.println("I'm sorry, I didn't understand that.\nFor English only, enter \"E\". For Hebrew only, enter \"H\". For Hebrew Transliteration only, enter \"T\".\nFor English and Hebrew, enter \"EH\". For Hebrew and Hebrew Transliteration, enter \"HT\". For English and Hebrew Transliteration, enter \"ET\".\nFor all three, enter \"A\".");
            response = scan.nextLine();
            response = response.toUpperCase();
        }
        switch (response){
            case "EH":
                eng = true;
                heb = true;
                break;
            case "ET":
                eng = true;
                trans = true;
                break;
            case "HT":
                trans = true;
                heb = true;
                break;
            case "A":
                eng = true;
                heb = true;
                trans = true;
                break;
            case "E":
                eng = true;
                break;
            case "H":
                heb = true;
                break;
            case "T":
                trans = true;
                break;
        }

        //Prints out The proper Perek of Tehillim to say, based on what languages are requested
        //Adapted from Gemini
        if (eng) {
            String perekEng = getString(upTo, "en");
            System.out.println("\nEnglish Perek " + upTo + ":\n" + perekEng);
        }
        if (heb) {
            String perekHeb = getString(upTo, "he");
            System.out.println("\nHebrew Perek "+ upTo +":\n" + perekHeb);
        }
        if (trans) {
            String perekTrans = getString(upTo, "trans");
            System.out.println("\nHebrew Transliteration Perek "+ upTo +":\n" + perekTrans);
        }
        
        //Increments the UpTo field for the person, so that the next user will get the next Perek of Tehillim
        if (upTo == 150){
            upTo = 0;
        }
        statement.executeQuery("UPDATE People SET UpTo = " + (upTo + 1) + " WHERE PersonID = " + personId);
        
    }

    //Creates a list of all String Elements in the tehillim.xml file and selects the proper language option from the proper tehillim number
    //Adapted from Gemini
    public static String getString(int id, String landCode)
    {
        NodeList nodes = doc.getElementsByTagName("string");
        Element element = (Element) nodes.item(id);
        return element.getElementsByTagName(landCode).item(0).getTextContent();
    }

    //Signs up a new person that needs tehillim said and places it into the database
    public static void signUp() throws SQLException
    {
        //Connects to the sql server
        String sqlUrl = "jdbc:sqlserver://localhost:1433;databaseName=Tmatcher;user=sa;password=flyerstitchkohack;encrypt=true;trustServerCertificate=true;";
        Connection connection = DriverManager.getConnection(sqlUrl);
        if (connection == null){
            System.out.println("We're sorry, an issue occured. Please try again later.");
            return;
        }

        //Recieves the proper information for the database entry, and checks that they are of the right type
        Scanner scan = new Scanner(System.in);
        boolean error = false;
        System.out.println("\n\nWhat is the name of the person that needs Tehillim?");
        String hebName = scan.nextLine();
        if (hebName.isBlank()){
            error = true;
        }
        try{
            Integer.parseInt(hebName);
            Double.parseDouble(hebName);
            error = true;
        } catch (NumberFormatException e){
        }
        while (error){
            error = false;
            System.out.println("I'm sorry, I didn't understand that.\nWhat is the name of the person that needs Tehillim?");
            hebName = scan.nextLine();
            if (hebName.isBlank()){
                error = true;
            }
            try{
                Integer.parseInt(hebName);
                Double.parseDouble(hebName);
                error = true;
            } catch (NumberFormatException e){
            }
        }
        error = false;
        int age = 0;
        System.out.println("How old is the person?");
        try{
            age = scan.nextInt();
            scan.nextLine();
        } catch (InputMismatchException e){
            error = true;
            scan.nextLine();
        }
        if (age <= 0) {
            error = true;
        }
        while (error){
            error = false;
            System.out.println("I'm sorry, I didn't understand that.\nHow old is the person?");
            try{
                age = scan.nextInt();
                scan.nextLine();
            } catch (InputMismatchException e){
                error = true;
                scan.nextLine();
            }
            if (age <= 0){
                error = true;
            }
        }
        error = false;
        System.out.println("Please give a short description about the person and their affliction");
        String bio = scan.nextLine();
        if (bio.isBlank()){
            error = true;
        }
        try{
            Integer.parseInt(bio);
            Double.parseDouble(bio);
            error = true;
        } catch (NumberFormatException e){
        }
        while (error){
            error = false;
            System.out.println("I'm sorry, I didn't understand that.\nPlease give a short description about the person and their affliction");
            bio = scan.nextLine();
            if (bio.isBlank()){
                error = true;
            }
            try{
                Integer.parseInt(bio);
                Double.parseDouble(bio);
                error = true;
            } catch (NumberFormatException e){
            }
        }
        error = false;
        System.out.println("What shul does the person go to?");
        String shul = scan.nextLine();
        if (shul.isBlank()){
            error = true;
        }
        try{
            Integer.parseInt(shul);
            Double.parseDouble(shul);
            error = true;
        } catch (NumberFormatException e){
        }
        while (error){
            error = false;
            System.out.println("I'm sorry, I didn't understand that.\nWhat shul does the person go to?");
            shul = scan.nextLine();
            if (shul.isBlank()){
                error = true;
            }
            try{
                Integer.parseInt(shul);
                Double.parseDouble(shul);
                error = true;
            } catch (NumberFormatException e){
            }
        }
        
        //Enters the information into the database
        Statement statement = connection.createStatement();
        statement.executeQuery("INSERT INTO People(HebName, Age, Bio, Shul, UpTo) VALUES('" + hebName + "', " + age + ", '" + bio + "', '" + shul + "', 1)");
    }

}