import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.Arrays;

//Group A1 Since Day 1
//COP 4710 - Data Modeling
//wSQL Engine Project

public class Project {
    private static final String DELIMITER = "##";
    private static final String LISTDELIMITER = "^^^";
    // all the stuff needed for Project execution
    private static ArrayList<String> groupColumns = new ArrayList<>();
    private static ArrayList<ArrayList<String>> groupingSets = new ArrayList<>();
    private static ArrayList<String> AggFunc = new ArrayList<>();
    private static String groupType;
    private static boolean auto_input = false;
    private static boolean display_debugger_stuff = false;
    private static Scanner scanning = new Scanner(System.in);
    private static String input = "";
    public static int countCounter=0;
    private static ArrayList<String> commands = new ArrayList<>();
    private static boolean quotes = false;
    private static ArrayList<Project> tokens = new ArrayList<>();
    private static ArrayList<String> parse_error = new ArrayList<>();
    private static ArrayList<String> semantic_error = new ArrayList<>();
    private static int index; // global index
    private static String directory_to = "databases/";
    // needed for semantics
    private static ArrayList<String> temp1 = new ArrayList<>();
    private static ArrayList<String> temp2 = new ArrayList<>();
    private static ArrayList<Integer> temp3 = new ArrayList<>();
    private static ArrayList<Integer> temp4 = new ArrayList<>();
    private static ArrayList<String> temp5 = new ArrayList<>();
    private static ArrayList<Column> temp6 = new ArrayList<>();
    private static ArrayList<String> temp7 = new ArrayList<>();
    private static ArrayList<String> temp8 = new ArrayList<>();
    private static ArrayList<String> temp9 = new ArrayList<>();
    private static ArrayList<Column> PotentiallyAListOfColumns = new ArrayList<>();
    private static ArrayList<String> temp11 = new ArrayList<>();
    private static ArrayList<Column> temp12 = new ArrayList<>();
    private static ArrayList<String> temp13 = new ArrayList<>();
    private static ArrayList<String> having_clmn = new ArrayList<>();
    private static ArrayList<Column> having_clmn_type = new ArrayList<>();
    private static ArrayList<String> having_oper = new ArrayList<>();
    private static ArrayList<String> having_val = new ArrayList<>();
    private static ArrayList<String> having_sep = new ArrayList<>();
    private static ArrayList<String> asClmn_name = new ArrayList<>();
    private static boolean in_where;
    private static boolean in_having;
    private static String table_name = "";
    private static String command = "";
    ////////////////////////////////////////////////////////
    // object definitions
    private String value;
    private String type;

    // constructor
    public Project(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public static void main(String[] args)
            throws FileNotFoundException, UnsupportedEncodingException, InterruptedException, ParseException {
        System.out.println("Starting SQL Engine!\n");

        // loop forever
        while (true) {
            // get the input from the user
            capture_input();

            // checks how many commands where given and separates them so they
            // execute sequentially and separately
            split_commands();

            // make sure that there were actual commands parsed
            if (commands.size() != 0) {
                // loop through each command, begin processing
                process();
            }
        }

    }

    // process each command
    private static void process() throws FileNotFoundException, UnsupportedEncodingException, ParseException {
        String temp;

        // while there are commands
        while (commands.size() != 0) {
            if (commands.get(0).equals("") || commands.get(0).equals("0"))
                commands.remove(0);

            // assign the temporary command variable
            temp = clean(commands.get(0));

            // create tokens
            create_tokens(temp);

            // cleanse tokens
            cleanse_tokens();

            // check if user opts to exit
            if (tokens.size() > 1) {
                if (tokens.get(0).value.toUpperCase().equals("EXIT") && tokens.get(1).value.equals(";")) {
                    System.out.println("\nExiting SQL Engine!");
                    System.exit(0);
                }
            }

            // make sure we're in a valid database
            if (Database.database_name == null) {
                if (tokens.size() != 4)
                    parse_error.add("You are not working in an active database; please CREATE or LOAD a database.");
                if (parse_error.size() == 0) {
                    if ((tokens.size() >= 2)) {
                        // make sure the token size is 2 or greater
                        if (!(tokens.get(0).value.equals("CREATE") || tokens.get(0).value.equals("LOAD")
                                || tokens.get(0).value.equals("DROP"))) {
                            parse_error.add(
                                    "You are not working in an active database; please CREATE or LOAD a database.");
                        } else if (!tokens.get(1).value.equals("DATABASE"))
                            parse_error.add(
                                    "You are not working in an active database; please CREATE or LOAD a database.");
                    } else
                        parse_error.add("You are not working in an active database; please CREATE or LOAD a database.");
                }
            }

            // parse if no token error
            // this also does semantics check

            if (!is_parse_error()) {
                parse();

                if (display_debugger_stuff) {
                    System.out.println(temp1);
                    System.out.println(temp2);
                    System.out.println(temp3);
                    System.out.println(temp4);
                    System.out.println(temp5);
                    System.out.println(temp6);
                    System.out.println(temp7);
                    System.out.println(temp8);
                    System.out.println(temp9);
                    System.out.println(PotentiallyAListOfColumns);
                    System.out.println(temp11);
                    System.out.println(temp12);
                    System.out.println(temp13);
                    System.out.println(AggFunc);
                    System.out.println(groupColumns);
                    System.out.println(having_clmn);
                    System.out.println(having_clmn_type);
                    System.out.println(having_oper);
                    System.out.println(having_val);
                    System.out.println(having_sep);
                    System.out.println(asClmn_name);
                }

            }

            // display errors, if any
            display_error();

            // if there are no errors
            // execute the command
            if (!is_parse_error() && !is_semantic_error()) {
                // begin execution
                if (display_debugger_stuff)
                    System.out.println("COMMAND: " + command);
                if (command.equals("LIST TABLES"))
                    execute_list_tables();
                else if (command.equals("CREATE DATABASE"))
                    execute_create_database();
                else if (command.equals("DROP DATABASE"))
                    execute_drop_database();
                else if (command.equals("SAVE"))
                    execute_save();
                else if (command.equals("LOAD"))
                    execute_load_database();
                else if (command.equals("CREATE TABLE"))
                    execute_create_table();
                else if (command.equals("DROP TABLE"))
                    execute_drop_table();
                else if (command.equals("INSERT"))
                    execute_insert();
                else if (command.equals("DELETE"))
                    execute_delete();
                else if (command.equals("UPDATE"))
                    execute_update();
                else if (command.equals("WUPDATE"))
                    execute_update();
                else if (command.equals("SELECT"))
                    execute_select();
                else
                    System.out.println("ERROR! COMMAND NOT FOUND!");
            }

            System.out.println("");

            // remove the item off the arraylist
            commands.remove(0);
        }
    }

    // parse the entire program
    private static void parse() {
        // reset the global index
        index = 0;

        // begin parsing
        command();

        // after we're done parsing, the last token must be ;
        if (tokens.size() == 0 || !tokens.get(index).value.equals(";"))
            parse_error.add("There is an issue with your syntax; look near the semi-colon.");

    }

    // parsing
    private static void command() {
        if (tokens.get(index).value.equals("LIST") && tokens.get(index + 1).value.equals("TABLES")) {
            index += 2;
            command = "LIST TABLES";
            return;
        }
        if (tokens.get(index).value.equals("CREATE")) {
            index++;
            create();
        } else if (tokens.get(index).value.equals("DROP")) {
            index++;
            drop();
        } else if (tokens.get(index).value.equals("LOAD")) {
            index++;
            command = "LOAD";
            load();
        } else if (tokens.get(index).value.equals("SAVE")) {
            index++;
            command = "SAVE";
            save();
        } else if (tokens.get(index).value.equals("COMMIT")) {
            index++;
            commit();
        } else if (tokens.get(index).value.equals("INSERT")) {
            index++;
            command = "INSERT";
            insert();
        } else if (tokens.get(index).value.equals("DELETE")) {
            index++;
            command = "DELETE";
            delete();
        } else if (tokens.get(index).value.equals("UPDATE") || tokens.get(index).value.equals("WUPDATE")) {
            index++;
            command = "UPDATE";
            update();
        } else if (tokens.get(index).value.equals("SELECT") || tokens.get(index).value.equals("WSELECT")) {
            index++;
            if (tokens.get(index - 1).value.equals("SELECT"))
                command = "SELECT";
            else if (tokens.get(index - 1).value.equals("WSELECT"))
                command = "SELECT";
            select();
        } else
            parse_error.add(tokens.get(index).value + " is not a recognized command.");
            
        // SEMANTIC CHECK
        // check for aggregate functions
        // cannont have columns without aggregate functions
        // without a group by statment. 
        if(groupColumns.size() == 0 && groupingSets.size() == 0){
           if(AggFunc.contains("NULL")){
               if(AggFunc.contains("SUM") || AggFunc.contains("AVG") || AggFunc.contains("MIN") ||
                  AggFunc.contains("MAX") || AggFunc.contains("COUNT"))
                  parse_error.add("Missing a GROUP BY statment");
           }
        }

            
    }

    // parsing
    @SuppressWarnings("unchecked")
    private static void insert() {
        if (tokens.get(index).value.equals("INTO"))
            index++;
        else
            parse_error.add(tokens.get(index).value + " is not valid; expecting INTO.");

        table();

        // SEMANTIC CHECK
        // cannot insert into a table if the table does not exist
        if (!does_table_exist(tokens.get(index - 1).value))
            semantic_error.add("The table: " + tokens.get(index - 1).value + " does not exist.");

        // capture and retain the table name for execution
        table_name = tokens.get(index - 1).value;

        if (tokens.get(index).value.equals("(")) {
            index++;

            field_name_1();

            field_name_2();

            if (tokens.get(index).value.equals(")"))
                index++;
            else
                parse_error.add(tokens.get(index).value + " is not valid; expecting ).");

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // we need to make sure all those columns actually exist
            for (String a : temp1) {
                if (!does_column_exist(table_name, a))
                    semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                else
                    // add it to the temp list of columns
                    temp6.add(get_column(table_name, a));
            }

            // SEMANTIC CHECK
            // check for duplicate columns
            check_duplicate_columns(temp1);

        } else {
            // no column names were specified
            // so we need to capture all columns
            if (semantic_error.size() == 0)
                temp6 = (ArrayList<Column>) Database.tables.get(table_name).columns.clone();
        }

        if (tokens.get(index).value.equals("VALUES"))
            index++;
        else
            parse_error.add(tokens.get(index).value + " is not valid; expecting VALUES.");

        if (tokens.get(index).value.equals("(")) {
            index++;

            literal_1();
            literal_2();

            if (tokens.get(index).value.equals(")"))
                index++;
            else
                parse_error.add(tokens.get(index).value + " is not valid; expecting ).");
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting (.");

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure the # of columns being passed
        // matches the # of values being passed
        if (semantic_error.size() == 0 && temp6.size() != temp2.size())
            semantic_error.add("The # of columns does not match the # of values.");

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure the fields being inserted to and
        // the actual values are compatible
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp6.size(); i++) {
                if (!are_we_compatible(get_type(temp2.get(i)), temp6.get(i).column_type)) {
                    semantic_error
                            .add("The value " + temp2.get(i) + " is not compatible with the " + temp6.get(i).column_name
                                    + " column; the column is of type " + temp6.get(i).column_type + ".");
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure that no values will end up getting truncated
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp6.size(); i++) {
                if (is_data_truncated(temp6.get(i), temp2.get(i))) {
                    String error_message = "The value " + temp2.get(i) + " will get truncated when inserted into "
                            + temp6.get(i).column_name + " column; the column is of max length "
                            + temp6.get(i).restriction;

                    if (temp6.get(i).column_type.equals("number"))
                        error_message += "," + temp6.get(i).restriction_2;
                    error_message += ".";
                    semantic_error.add(error_message);
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure that if we're doing a BIT, it has to be of
        // values either 0, 1 or NULL
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp6.size(); i++) {
                if (temp6.get(i).column_type.equals("BIT")) {
                    if (!temp2.get(i).equals("0") && !temp2.get(i).equals("1") && !temp2.get(i).equals("NULL")) {

                        String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                + temp6.get(i).column_name + " column; the column is of type BIT.";
                        semantic_error.add(error_message);
                    }
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure that if we're doing a INT
        // it cannot have a decimal
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp6.size(); i++) {
                if (temp6.get(i).column_type.equals("INT")) {
                    if (!is_int(temp2.get(i)) && !temp2.get(i).equals("NULL")) {

                        String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                + temp6.get(i).column_name + " column; the column is of type INT.";
                        semantic_error.add(error_message);
                    }
                }
            }
        }

        // SEMANTIC CHECK
        // we need to make sure that if we're inserting a new record
        // all NON-NULL columns must be included in in the insert statement
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic/parse errors
            ArrayList<String> must_be_declared = (ArrayList<String>) get_all_non_null_columns(table_name).clone();

            // loop through all provided columns and mandatory columns - report
            // all errors, if any
            for (String a : must_be_declared) {
                if (!does_column_exist_in_list(temp6, a)) {
                    semantic_error.add("The column " + a
                            + " was not included in your INSERT statement; this column does not allow NULL values.");
                }
            }
        }

        // SEMANTIC CHECK
        // we need to make sure that when we're inserting records
        // all NON-NULL columns are being given a NON-NULL value
        if (!is_semantic_error() && !is_parse_error()) {
            for (int i = 0; i < temp6.size(); i++) {
                // only do this check where NULL values are not allowed
                if (temp6.get(i).is_null_allowed.equals("false")) {
                    if (temp2.get(i).equals("NULL")) {
                        // report error to the console
                        semantic_error.add("The value " + temp2.get(i) + " cannot be inserted into "
                                + temp6.get(i).column_name + "; the column does not allow NULL values.");
                    }
                }
            }
        }

    }

    // parsing
    private static void delete() {
        if (tokens.get(index).value.equals("FROM"))
            index++;
        else
            parse_error.add(tokens.get(index).value + " is not valid; expecting FROM.");

        table();

        // SEMANTIC CHECK
        // cannot delete from a table if the table does not exist
        if (!does_table_exist(tokens.get(index - 1).value))
            semantic_error.add("The table: " + tokens.get(index - 1).value + " does not exist.");

        if (tokens.get(index).value.equals("WHERE")) {
            index++;
            in_where = true;
            where();
            in_where = false;

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // we need to make sure all those columns actually exist
            if (!is_semantic_error() && !is_parse_error()) {
                for (String a : temp1) {
                    if (!does_column_exist(table_name, a))
                        semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                    else
                        // add it to the temp list of columns
                        temp6.add(get_column(table_name, a));
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure that if we're doing a BIT, it has to be of
            // values either 0, 1 or NULL
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (temp6.get(i).column_type.equals("BIT")) {
                        if (!temp2.get(i).equals("0") && !temp2.get(i).equals("1") && !temp2.get(i).equals("NULL")) {

                            String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                    + temp6.get(i).column_name + " column; the column is of type BIT.";
                            semantic_error.add(error_message);
                        }
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, we have all the values and columns
            // we need to make sure that if we're doing a INT
            // it cannot have a decimal
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (temp6.get(i).column_type.equals("INT")) {
                        if (!is_int(temp2.get(i)) && !temp2.get(i).equals("NULL")) {

                            String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                    + temp6.get(i).column_name + " column; the column is of type INT.";
                            semantic_error.add(error_message);
                        }
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all the columns and values are compatible
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!are_we_compatible(get_type(temp2.get(i)), temp6.get(i).column_type)) {
                        semantic_error.add(
                                "The value " + temp2.get(i) + " is not compatible with the " + temp6.get(i).column_name
                                        + " column; the column is of type " + temp6.get(i).column_type + ".");
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all relational operators are compatible with
            // the values being passed

            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!is_relational_operator_valid(get_type(temp2.get(i)), temp7.get(i))) {
                        semantic_error.add("The value " + temp2.get(i)
                                + " is not compatible with the relational operator " + temp7.get(i) + ".");
                    }
                }
            }

        }
    }

    // parsing
    private static void update() {

        table();

        if (tokens.get(index).value.equals("SET")) {
            index++;
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting SET.");

        update_set_1();
        update_set_2();

        // SEMANTIC CHECK
        // at this point, all the column names were inserted into temp9
        // we need to make sure all those columns actually exist
        if (!is_semantic_error() && !is_parse_error()) {
            for (String a : temp9) {
                if (!does_column_exist(table_name, a))
                    semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                else
                    // add it to the temp list of columns
                    temp12.add(get_column(table_name, a));
            }
        }

        // SEMANTIC CHECK
        // at this point, all the column names were inserted into temp9
        // all the values were inserted into temp13
        // we need to make sure that if we're doing a BIT, it has to be of
        // values either 0, 1 or NULL
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp12.size(); i++) {
                if (temp12.get(i).column_type.equals("BIT")) {
                    if (!temp13.get(i).equals("0") && !temp13.get(i).equals("1") && !temp13.get(i).equals("NULL")) {

                        String error_message = "The value " + temp13.get(i) + " is not compatible with the "
                                + temp12.get(i).column_name + " column; the column is of type BIT.";
                        semantic_error.add(error_message);
                    }
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure that if we're doing a INT
        // it cannot have a decimal
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp12.size(); i++) {
                if (temp12.get(i).column_type.equals("INT")) {
                    if (!is_int(temp13.get(i)) && !temp13.get(i).equals("NULL")) {

                        String error_message = "The value " + temp13.get(i) + " is not compatible with the "
                                + temp12.get(i).column_name + " column; the column is of type INT.";
                        semantic_error.add(error_message);
                    }
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, all the column names were inserted into temp12
        // all the values were inserted into temp13
        // we need to make sure all the columns and values are compatible
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp12.size(); i++) {
                if (!are_we_compatible(get_type(temp13.get(i)), temp12.get(i).column_type)) {
                    semantic_error.add(
                            "The value " + temp13.get(i) + " is not compatible with the " + temp12.get(i).column_name
                                    + " column; the column is of type " + temp12.get(i).column_type + ".");
                }
            }
        }

        // SEMANTIC CHECK
        // at this point, we have all the values and columns
        // we need to make sure that no values will end up getting truncated
        if (!is_semantic_error() && !is_parse_error()) {
            // only do this check if there are no current semantic errors
            for (int i = 0; i < temp12.size(); i++) {
                if (is_data_truncated(temp12.get(i), temp13.get(i))) {
                    String error_message = "The value " + temp13.get(i) + " will get truncated when inserted into "
                            + temp12.get(i).column_name + " column; the column is of max length "
                            + temp12.get(i).restriction;

                    if (temp12.get(i).column_type.equals("number"))
                        error_message += "," + temp12.get(i).restriction_2;
                    error_message += ".";
                    semantic_error.add(error_message);
                }
            }
        }

        // SEMANTIC CHECK
        // we need to make sure that when we're inserting records
        // all NON-NULL columns are being given a NON-NULL value
        if (!is_semantic_error() && !is_parse_error()) {
            for (int i = 0; i < temp12.size(); i++) {
                // only do this check where NULL values are not allowed
                if (temp12.get(i).is_null_allowed.equals("false")) {
                    if (temp13.get(i).equals("NULL")) {
                        // report error to the console
                        semantic_error.add("The value " + temp13.get(i) + " cannot be inserted into "
                                + temp12.get(i).column_name + "; the column does not allow NULL values.");
                    }
                }
            }
        }

        if (tokens.get(index).value.equals("WHERE")) {
            index++;
            in_where = true;
            where();
            in_where = false;

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // we need to make sure all those columns actually exist
            if (!is_semantic_error() && !is_parse_error()) {
                for (String a : temp1) {
                    if (!does_column_exist(table_name, a))
                        semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                    else
                        // add it to the temp list of columns
                        temp6.add(get_column(table_name, a));
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure that if we're doing a BIT, it has to be of
            // values either 0, 1 or NULL
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (temp6.get(i).column_type.equals("BIT")) {
                        if (!temp2.get(i).equals("0") && !temp2.get(i).equals("1") && !temp2.get(i).equals("NULL")) {

                            String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                    + temp6.get(i).column_name + " column; the column is of type BIT.";
                            semantic_error.add(error_message);
                        }
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, we have all the values and columns
            // we need to make sure that if we're doing a INT
            // it cannot have a decimal
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (temp6.get(i).column_type.equals("INT")) {
                        if (!is_int(temp2.get(i)) && !temp2.get(i).equals("NULL")) {

                            String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                    + temp6.get(i).column_name + " column; the column is of type INT.";
                            semantic_error.add(error_message);
                        }
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all the columns and values are compatible
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!are_we_compatible(get_type(temp2.get(i)), temp6.get(i).column_type)) {
                        semantic_error.add(
                                "The value " + temp2.get(i) + " is not compatible with the " + temp6.get(i).column_name
                                        + " column; the column is of type " + temp6.get(i).column_type + ".");
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all relational operators are compatible with
            // the values being passed

            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!is_relational_operator_valid(get_type(temp2.get(i)), temp7.get(i))) {
                        semantic_error.add("The value " + temp2.get(i)
                                + " is not compatible with the relational operator " + temp7.get(i) + ".");
                    }
                }
            }

        }
    }

    // parsing
    private static void update_set_1() {
        if (tokens.get(index).type.equals("attribute")) {
            index++;

            // SEMANTIC OPERATION
            // add to token list
            temp9.add(tokens.get(index - 1).value);
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting a column name.");

        if (tokens.get(index).value.equals("=")) {
            index++;
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting =.");

        literal_1();
    }

    // parsing
    private static void update_set_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;
            update_set_1();
            update_set_2();
        }
    }

    // parsing
    private static void literal_1() {
        if (tokens.get(index).type.equals("number") || tokens.get(index).value.equals("NULL")
                || tokens.get(index).type.equals("varchar")) {
            index++;

            // SEMANTIC CHECK
            // add these columns to temp2
            if (command.equals("UPDATE")) {
                if (in_where)
                    temp2.add(tokens.get(index - 1).value);
                else
                    temp13.add(tokens.get(index - 1).value);
            } else if (in_having)
                    having_val.add(tokens.get(index - 1).value);
              else
                temp2.add(tokens.get(index - 1).value);

        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting a literal.");
    }

    // parsing
    private static void literal_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;
            literal_1();
            literal_2();
        }
    }

    // parsing
    private static void select() {
        if (tokens.get(index).value.equals("*")) {
            index++;
        } else {
            //call the aggregate functions
            aggregate_1();
            aggregate_2();
        }

        if (tokens.get(index).value.equals("FROM"))
            index++;
        else
            parse_error.add(tokens.get(index).value + " is not valid; expecting FROM.");

        table();

        // SEMANTIC CHECK
        // cannot read from a table if the table does not exist
        if (!does_table_exist(tokens.get(index - 1).value))
            semantic_error.add("The table: " + tokens.get(index - 1).value + " does not exist.");

        table_name = tokens.get(index - 1).value;

        // SEMANTIC OPERATION
        // add all the columns to temp9
        // only if SELECT *
        if (!is_semantic_error()) {
            if (tokens.get(1).value.equals("*")) {
                for (int i = 0; i < Database.tables.get(table_name).columns.size(); i++){
                    temp9.add(Database.tables.get(table_name).columns.get(i).column_name);
                    AggFunc.add("NULL");
                }
            } else {
                // if not SELECT *
                // then the user manually specified the columns
                // now we need to make sure those columns exist
                for (String aTemp9 : temp9) {
                    if (!does_column_exist(table_name, aTemp9)) {
                        semantic_error.add("The column " + aTemp9 + " does not exist.");
                    }
                }
            }
            
            // SEMANTIC CHECK
            // cannot perform SUM or AVG operation on columns
            // that are not of type INT.
            for (int i = 0; i < temp9.size(); i++) {
               if(AggFunc.get(i).equals("SUM") || AggFunc.get(i).equals("AVG")){
                  for (int j = 0; j < Database.tables.get(table_name).columns.size(); j++){
                     if (Database.tables.get(table_name).columns.get(j).column_name.equals(temp9.get(i))){
                        if(!(Database.tables.get(table_name).columns.get(j).column_type.equals("INT") || Database.tables.get(table_name).columns.get(j).column_type.equals("NUMBER"))){
                           semantic_error.add("Cannot perform " + AggFunc.get(i) + " on a column of type "
                              + Database.tables.get(table_name).columns.get(j).column_type + ".");
                        }
                     }
                  }
               }
            }
        }

        if (tokens.get(index).value.equals("WHERE")) {
            index++;
            in_where = true;
            where();
            in_where = false;

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // we need to make sure all those columns actually exist
            if (!is_semantic_error() && !is_parse_error()) {
                for (String a : temp1) {
                    if (!does_column_exist(table_name, a))
                        semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                    else
                        // add it to the temp list of columns
                        temp6.add(get_column(table_name, a));
                }

            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure that if we're doing a BIT, it has to be of
            // values either 0, 1 or NULL
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (temp6.get(i).column_type.equals("BIT")) {
                        if (!temp2.get(i).equals("0") && !temp2.get(i).equals("1") && !temp2.get(i).equals("NULL")) {

                            String error_message = "The value " + temp2.get(i) + " is not compatible with the "
                                    + temp6.get(i).column_name + " column; the column is of type BIT.";
                            semantic_error.add(error_message);
                        }
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all the columns and values are compatible
            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!are_we_compatible(get_type(temp2.get(i)), temp6.get(i).column_type)) {
                        semantic_error.add(
                                "The value " + temp2.get(i) + " is not compatible with the " + temp6.get(i).column_name
                                        + " column; the column is of type " + temp6.get(i).column_type + ".");
                    }
                }
            }

            // SEMANTIC CHECK
            // at this point, all the column names were inserted into temp1
            // all the values were inserted into temp2
            // all the relational operators were inserted into temp7
            // we need to make sure all relational operators are compatible with
            // the values being passed

            if (!is_semantic_error() && !is_parse_error()) {
                // only do this check if there are no current semantic errors
                for (int i = 0; i < temp6.size(); i++) {
                    if (!is_relational_operator_valid(get_type(temp2.get(i)), temp7.get(i))) {
                        semantic_error.add("The value " + temp2.get(i)
                                + " is not compatible with the relational operator " + temp7.get(i) + ".");
                    }
                }
            }

        }
        group_by_functions();
    }
    
    //parsing
    private static void group_by_functions(){
    
        //Check for grouping, else check for multiple grouping
        if (tokens.get(index).value.toUpperCase().equals("GROUP")) {
            index++;

            if (!tokens.get(index).value.toUpperCase().equals("BY")) {
                parse_error.add(tokens.get(index).value + " is not valid; expecting BY.");
            }
            
            index++;
            
            switch(tokens.get(index).value)
            {
               case "GROUPING":
                  index++;
                  if (!tokens.get(index).value.toUpperCase().equals("SETS")) {
                      parse_error.add(tokens.get(index).value + " is not valid; expecting SETS.");
                  }
                  groupType = "GROUPING SETS";
                  index++;
                  grouping_sets();
                  break;
               case "CUBE":
                  groupType = "CUBE";
                  index++;
                  rollup_cube();
                  break;
               case "ROLLUP":
                  groupType = "ROLLUP";
                  index++;
                  rollup_cube();
                  break;
               case "(":
                  groupType = "GROUP BY";
                  index++;
                  group_by();
                  if(tokens.get(index).value.equals("HAVING")){
                     index++;
                     in_having = true;
                     having();
                     in_having = false;
                  }
                  break;
               default:
                  parse_error.add(tokens.get(index).value + " is not valid; expecting (.");
                  break;
            } 
        }
    }
    
    // parsing
    private static void group_by(){
         while (!tokens.get(index).value.equals(")")) {
            String column = tokens.get(index).value;
            if (does_column_exist(table_name, column)) {
               index++;
               groupColumns.add(column);
                       
               if (tokens.get(index).value.equals(",")) {
                   index++;
               } else if(!tokens.get(index).value.equals(")")){
                  parse_error.add("Expecting ) before " + tokens.get(index).value + ".");
                  break;
               }
            } else
                semantic_error.add("The column " + column + " does not exist in the current table.");
         }
         
         index++;
    }
    
    // parsing
    private static void grouping_sets(){
       if (tokens.get(index).value.equals("(")) {
           index++;
           while (!tokens.get(index).value.equals(")")) 
           {
               if (tokens.get(index).value.equals("(")) 
               {
                  ArrayList<String> set = new ArrayList<>();
                  index++;
                  while(!tokens.get(index).value.equals(")"))
                  {
                     String column = tokens.get(index).value;
                     if (does_column_exist(table_name, column)) {
                        index++;
                        set.add(column);
                                
                        if (tokens.get(index).value.equals(",")) {
                            index++;
                        } else if(!tokens.get(index).value.equals(")")){
                           parse_error.add("Expecting ) before " + tokens.get(index).value + ".");
                           break;
                        }
                     } else
                         semantic_error.add("The column " + column + " does not exist in the current table.");
                  }
                  groupingSets.add(set);
                  index++;
                  if (tokens.get(index).value.equals(",")) {
                      index++;
                  } else if(!tokens.get(index).value.equals(")")){
                     parse_error.add("Expecting ) before " + tokens.get(index).value + ".");
                     break;
                  }
               } else
                  parse_error.add("Expecting ( before " + tokens.get(index).value + ".");
            }
            index++;
       } else
           parse_error.add("Expecting ( before " + tokens.get(index).value + ".");
           
       //SEMANTIC CHECK
       //check if there is a column
       //with the SUM aggregate function
       if(!AggFunc.contains("SUM")){
            parse_error.add("Missing aggregate function (SUM).");
       }
    }
    
    // parsing
    private static void rollup_cube() {
       if (tokens.get(index).value.equals("(")) {
          index++;
          while (!tokens.get(index).value.equals(")")) {
             String column = tokens.get(index).value;
             if (does_column_exist(table_name, column)) {
                index++;
                groupColumns.add(column);
                        
                if (tokens.get(index).value.equals(",")) {
                    index++;
                } else if(!tokens.get(index).value.equals(")")){
                   parse_error.add("Expecting ) before " + tokens.get(index).value + ".");
                   break;
                }
             } else
                semantic_error.add("The column " + column + " does not exist in the current table.");
          }
         
          index++;

       } else
           parse_error.add("Expecting ( before " + tokens.get(index).value + ".");
           
       //SEMANTIC CHECK
       //check if there is a column
       //with the SUM aggregate function
       if(!AggFunc.contains("SUM")){
            parse_error.add("Missing aggregate function (SUM).");
       }
    }
    
    // parsing
    private static void having() {
        condition_1();
        condition_2();

        // if we are doing a NULL comparison on the where clause
        // we need to make sure the operator is either =, != or <>
        for (int i = 0; i < having_val.size(); i++) {
            if (having_val.get(i).equals("NULL")) {
                if (!having_oper.get(i).equals("<>") && !having_oper.get(i).equals("!=") && !having_oper.get(i).equals("="))
                    semantic_error.add("The relational operator " + having_oper.get(i) + " cannot be used with NULL values.");
            }
        }
        
         // SEMANTIC CHECK
         // at this point, all the column names were inserted into having_clmn
         // we need to make sure all those columns actually exist
         if (!is_semantic_error() && !is_parse_error()) {
             for (String a : having_clmn) {
                 if (!does_column_exist(table_name, a))
                     semantic_error.add("The column " + a + " does not exist in the table " + table_name + ".");
                 else
                     // add it to the temp list of columns
                     having_clmn_type.add(get_column(table_name, a));
             }
   
         }
   
         // SEMANTIC CHECK
         // at this point, all the column names were inserted into having_clmn
         // all the values were inserted into having_val
         // all the relational operators were inserted into having_oper
         // we need to make sure that if we're doing a BIT, it has to be of
         // values either 0, 1 or NULL
         if (!is_semantic_error() && !is_parse_error()) {
             // only do this check if there are no current semantic errors
             for (int i = 0; i < having_clmn_type.size(); i++) {
                 if (having_clmn_type.get(i).column_type.equals("BIT")) {
                     if (!having_val.get(i).equals("0") && !having_val.get(i).equals("1") && !having_val.get(i).equals("NULL")) {
   
                         String error_message = "The value " + having_val.get(i) + " is not compatible with the "
                                 + having_clmn_type.get(i).column_name + " column; the column is of type BIT.";
                         semantic_error.add(error_message);
                     }
                 }
             }
         }
   
         // SEMANTIC CHECK
         // at this point, all the column names were inserted into having_clmn
         // all the values were inserted into having_val
         // all the relational operators were inserted into having_oper
         // we need to make sure all the columns and values are compatible
         if (!is_semantic_error() && !is_parse_error()) {
             // only do this check if there are no current semantic errors
             for (int i = 0; i < having_clmn_type.size(); i++) {
                 if (!are_we_compatible(get_type(having_val.get(i)), having_clmn_type.get(i).column_type)) {
                     semantic_error.add(
                             "The value " + having_val.get(i) + " is not compatible with the " + having_clmn_type.get(i).column_name
                                     + " column; the column is of type " + having_clmn_type.get(i).column_type + ".");
                 }
             }
         }
   
         // SEMANTIC CHECK
         // at this point, all the column names were inserted into having_clmn
         // all the values were inserted into having_val
         // all the relational operators were inserted into having_oper
         // we need to make sure all relational operators are compatible with
         // the values being passed
   
         if (!is_semantic_error() && !is_parse_error()) {
             // only do this check if there are no current semantic errors
             for (int i = 0; i < having_clmn_type.size(); i++) {
                 if (!is_relational_operator_valid(get_type(having_val.get(i)), having_oper.get(i))) {
                     semantic_error.add("The value " + having_val.get(i)
                             + " is not compatible with the relational operator " + having_oper.get(i) + ".");
                 }
             }
         }
    }
    
    // parsing
    private static void where() {
        condition_1();
        condition_2();

        // if we are doing a NULL comparison on the where clause
        // we need to make sure the operator is either =, != or <>
        for (int i = 0; i < temp2.size(); i++) {
            if (temp2.get(i).equals("NULL")) {
                if (!temp7.get(i).equals("<>") && !temp7.get(i).equals("!=") && !temp7.get(i).equals("="))
                    semantic_error.add("The relational operator " + temp7.get(i) + " cannot be used with NULL values.");
            }
        }
    }

    // parsing
    private static void condition_1() {
        column_1();
        relational_operator();
        literal_1();
    }

    // parsing
    private static void condition_2() {
        if (tokens.get(index).value.equals("AND") || tokens.get(index).value.equals("OR")) {
            index++;

            // SEMANTIC OPERATION
            // add this to temp11
            if (!is_semantic_error() && !is_parse_error()){
               if(in_having)
                  having_sep.add(tokens.get(index - 1).value);
               else
                  temp11.add(tokens.get(index - 1).value);
            }

            condition_1();
            condition_2();
        }
    }

    // parsing
    private static void relational_operator() {
        if (tokens.get(index).value.equals("=") || tokens.get(index).value.equals("<")
                || tokens.get(index).value.equals("<=") || tokens.get(index).value.equals(">")
                || tokens.get(index).value.equals(">=") || tokens.get(index).value.equals("!=")
                || tokens.get(index).value.equals("<>")) {
            index++;

            // this will add the token the list of relational operators
            if(in_having){
               having_oper.add(tokens.get(index - 1).value);
            } else
               temp7.add(tokens.get(index - 1).value);
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting a relational operator.");
    }

    // parsing
    private static void create() {
        if (tokens.get(index).value.equals("DATABASE")) {
            index++;
            command = "CREATE DATABASE";
            database();
        } else if (tokens.get(index).value.equals("TABLE")) {
            index++;
            command = "CREATE TABLE";
            table();

            // SEMANTIC CHECK
            // make sure the table does not already exist
            if (does_table_exist(tokens.get(index - 1).value))
                semantic_error.add("The table: " + tokens.get(index - 1).value + " already exists.");

            // check for open parenthesis
            if (tokens.get(index).value.equals("("))
                index++;
            else
                parse_error.add(tokens.get(index).value + " is not valid; expecting (.");

            // check for field definitions
            field_def_1();

            // check for close parenthesis
            if (tokens.get(index).value.equals(")"))
                index++;
            else
                parse_error.add(tokens.get(index).value + " is not valid; expecting ).");

        } else
            parse_error.add(tokens.get(index).value + " is not a valid item to create.");

        // SEMANTIC CHECK
        // check for duplicate columns
        check_duplicate_columns(temp1);
    }

    // parsing
    private static void drop() {
        if (tokens.get(index).value.equals("DATABASE")) {
            index++;
            command = "DROP DATABASE";
            database();
        } else if (tokens.get(index).value.equals("TABLE")) {
            index++;
            command = "DROP TABLE";
            table();

            // SEMANTIC CHECK
            // cannot drop a table if the table does not exist
            if (!does_table_exist(tokens.get(index - 1).value))
                semantic_error.add("The table: " + tokens.get(index - 1).value + " does not exist.");

        } else
            parse_error.add(tokens.get(index).value + " is not a valid item to drop.");
    }

    // parsing
    private static void load() {
        if (tokens.get(index).value.equals("DATABASE")) {
            index++;
            database();
        } else
            parse_error.add(tokens.get(index).value + " is not a valid item to load.");
    }

    // parsing
    private static void save() {
        // execute save command
        // nothing to do here because this is the end of the command
    }

    // parsing
    private static void commit() {
        // execute commit command
        // nothing to do here because this is the end of the command
        // display message to console
        parse_error.add("This SQL Engine uses auto-commit.");
    }

    // parsing
    private static void database() {
        if (tokens.get(index).type.equals("attribute")) {
            index++;
            Database.temp_database_name = tokens.get(index - 1).value;
        } else {
            parse_error.add(tokens.get(index).value + " is not a database name.");
        }
    }

    // parsing
    private static void table() {
        if (tokens.get(index).type.equals("attribute")) {
            index++;

            table_name = tokens.get(index - 1).value;
        } else {
            parse_error.add(tokens.get(index).value + " is not a table name.");
        }
    }

    // parsing
    private static void column_1() {
        if (tokens.get(index).type.equals("attribute")) {
            index++;
            
            
            // SEMANTIC OPERATION

            if (!command.equals("SELECT") && !command.equals("UPDATE")) {
                // System.out.println("IN HERE!");
                temp1.add(tokens.get(index - 1).value);
            } else {
                // System.out.println("IN HERE!");
                if (in_where) {
                    // System.out.println("IN TRUE!");
                    temp1.add(tokens.get(index - 1).value);
                } else if(in_having) {
                    having_clmn.add(tokens.get(index - 1).value);
                } else {
                    // System.out.println("IN FALSE!");
                    temp9.add(tokens.get(index - 1).value);
                }
            }
        } else {
            parse_error.add(tokens.get(index).value + " is not a column name.");
        }
    }

    // parsing
    private static void column_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;
            column_1();

            column_2();
        }
    }
    
    // parsing
    private static void aggregate_1() {
        if (tokens.get(index).type.equals("attribute")) {
            AggFunc.add("NULL");
            index++;
           
            // SEMANTIC OPERATION

            if (!command.equals("SELECT") && !command.equals("UPDATE")) {
                // System.out.println("IN HERE!");
                temp1.add(tokens.get(index - 1).value);
            } else {
                // System.out.println("IN HERE!");
                if (in_where) {
                    // System.out.println("IN TRUE!");
                    temp1.add(tokens.get(index - 1).value);
                } else if(in_having) {
                    having_clmn.add(tokens.get(index - 1).value);
                } else {
                    // System.out.println("IN FALSE!");
                    temp9.add(tokens.get(index - 1).value);
                }
            }
        } else if(tokens.get(index).type.equals("keyword")) {
            switch(tokens.get(index).value){
               case "SUM":
                  AggFunc.add(tokens.get(index).value);
                  index++;
                  break;
               case "COUNT":
                  AggFunc.add(tokens.get(index).value);
                  index++;
                  countCounter=1;
                  break;
               case "AVERAGE":
                  AggFunc.add("AVG");
                  index++;
                  break;
               case "AVG":
                  AggFunc.add(tokens.get(index).value);
                  index++;
                  break;
               case "MINIMUM":
                  AggFunc.add("MIN");
                  index++;
                  break;
               case "MIN":
                  AggFunc.add(tokens.get(index).value);
                  index++;
                  break;
               case "MAXIMUM":
                  AggFunc.add("MAX");
                  index++;
                  break;
               case "MAX":
                  AggFunc.add(tokens.get(index).value);
                  index++;
                  break;
               default:
                  parse_error.add(tokens.get(index).value + " is an invalid command.");
            }
            
            if (tokens.get(index).value.equals("(")) {
               index++;
                  column_1();
               if (tokens.get(index).value.equals(")")) {
                  index++;
               } 
               else{
                  parse_error.add("Missing ')' after " + tokens.get(index - 1).value);
               } 
            } 
            else {
            parse_error.add("Missing '(' after " + tokens.get(index - 1).value);
            }
        } 
        else 
        {
            parse_error.add(tokens.get(index).value + " is not a column name.");
        }
        
        if(tokens.get(index).value.equals("AS"))
        {
            index++;
            if (tokens.get(index).type.equals("attribute")) {
               asClmn_name.add(tokens.get(index).value);
               index++;
            } else 
               parse_error.add("Expecting an attribute following AS");
        } 
        else {
            asClmn_name.add("NULL");
        }
    }

    // parsing
    private static void aggregate_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;
            aggregate_1();
            aggregate_2();
        }
    }

    // parsing
    private static void field_def_1() {
        field_name_1();

        field_type();

        // user can specify NOT NULL
        if (tokens.get(index).value.equals("NOT")) {
            index++;

            if (tokens.get(index).value.equals("NULL")) {
                index++;
            } else {
                parse_error.add(tokens.get(index).value + " is not valid; expecting NULL.");
            }

            // user did specify NOT NULL
            temp5.add("false");
        } else {
            // user did not specify NOT NULL
            temp5.add("true");
        }

        // this lets users type in multiple field definitions
        field_def_2();
    }

    // parsing
    private static void field_def_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;

            field_def_1();
        }
    }

    // parsing
    private static void field_name_1() {
        column_1();
    }

    // parsing
    private static void field_name_2() {
        if (tokens.get(index).value.equals(",")) {
            index++;
            field_name_1();
            field_name_2();
        }
    }

    // parsing
    private static void field_type() {
        if (tokens.get(index).value.equals("INT")) {
            index++;

            // SEMANTIC CHECK
            // add these field data types to temp2
            temp2.add(tokens.get(index - 1).value);

            // can't have a decimal restriction
            temp4.add(0);

            if (tokens.get(index).value.equals("(")) {
                index++;

                if (tokens.get(index).type.equals("number")) {
                    // SEMANTIC CHECK
                    // make sure the number is an int
                    if (!is_int(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting an integer.");

                    // SEMANTIC CHECK
                    // make sure the number is positive and greater than 0
                    if (!is_valid_restriction(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting a number greater than 0.");

                    index++;

                    // SEMANTIC CHECK
                    // add this field restriction to temp3
                    // but first we need to ensure it's a valid, whole integer
                    // if it's not, set to the default value
                    // doesn't matter, if it's not a valid integer, the query
                    // won't execute
                    int temp = -1;
                    if (is_int(tokens.get(index - 1).value)) {
                        if (is_valid_restriction(tokens.get(index - 1).value)) {
                            temp = Integer.parseInt(tokens.get(index - 1).value);
                        }
                    }
                    if (temp != -1)
                        temp3.add(temp);
                    else
                        temp3.add(get_default_size(temp2.get(temp2.size() - 1)));

                } else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a number.");

                if (tokens.get(index).value.equals(")"))
                    index++;
                else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a ).");
            } else {
                // if we're here, that means that no restriction was passed
                // so it's the default value
                // SEMANTIC CHECK
                // add this field restriction to temp3
                temp3.add(get_default_size("INT"));
            }
        } else if (tokens.get(index).value.equals("NUMBER")) {
            index++;

            // SEMANTIC CHECK
            // add these field data types to temp2
            temp2.add(tokens.get(index - 1).value);

            if (tokens.get(index).value.equals("(")) {
                index++;

                if (tokens.get(index).type.equals("number")) {
                    // SEMANTIC CHECK
                    // make sure the number is an int
                    if (!is_int(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting an integer.");

                    // SEMANTIC CHECK
                    // make sure the number is positive and greater than 0
                    if (!is_valid_restriction(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting a number greater than 0.");

                    index++;

                    // SEMANTIC CHECK
                    // add this field restriction to temp3
                    // but first we need to ensure it's a valid, whole integer
                    // if it's not, set to the default value
                    // doesn't matter, if it's not a valid integer, the query
                    // won't execute
                    int temp = -1;
                    if (is_int(tokens.get(index - 1).value)) {
                        if (is_valid_restriction(tokens.get(index - 1).value)) {
                            temp = Integer.parseInt(tokens.get(index - 1).value);
                        }
                    }
                    if (temp != -1)
                        temp3.add(temp);
                    else
                        temp3.add(get_default_size(temp2.get(temp2.size() - 1)));
                } else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a number.");

                if (tokens.get(index).value.equals(",")) {
                    index++;

                    if (tokens.get(index).type.equals("number")) {
                        // SEMANTIC CHECK
                        // make sure the number is an int
                        if (!is_int(tokens.get(index).value))
                            parse_error.add(tokens.get(index).value + " is not valid; expecting an integer.");

                        // SEMANTIC CHECK
                        // make sure the number is positive and greater than 0
                        if (!is_valid_restriction(tokens.get(index).value))
                            parse_error
                                    .add(tokens.get(index).value + " is not valid; expecting a number greater than 0.");

                        index++;

                        // SEMANTIC CHECK
                        // add this field restriction to temp3
                        // but first we need to ensure it's a valid, whole
                        // integer
                        // if it's not, set to the default value
                        // doesn't matter, if it's not a valid integer, the
                        // query won't execute
                        int temp = -1;
                        if (is_int(tokens.get(index - 1).value)) {
                            if (is_valid_restriction(tokens.get(index - 1).value)) {
                                temp = Integer.parseInt(tokens.get(index - 1).value);
                            }
                        }
                        if (temp != -1)
                            temp4.add(temp);
                        else
                            temp4.add(get_default_size(temp2.get(temp2.size() - 1)));

                    } else
                        parse_error.add(tokens.get(index).value + " is not valid; expecting a number.");
                } else {
                    // if we're here, then no restriction for the decimal was
                    // passed
                    // so default values

                    // SEMANTIC CHECK
                    // add this field restriction to temp4
                    temp4.add(0);
                }

                if (tokens.get(index).value.equals(")"))
                    index++;
                else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a ).");
            } else {
                // if we're here, then no restriction was passed,
                // do default values

                // SEMANTIC CHECK
                // add this field restriction to temps
                temp3.add(get_default_size("NUMBER"));
                temp4.add(0);
            }

            // SEMANTIC CHECK
            // We need to ensure that the number in temp4 is ALWAYS less than or
            // equal to the number in temp3
            int a = temp3.get(temp3.size() - 1);
            int x = temp4.get(temp4.size() - 1);
            if (a < x)
                semantic_error.add("You cannot define a NUMBER column with more decimal values than total values.");

        } else if (tokens.get(index).value.equals("CHAR")) {
            index++;

            // SEMANTIC CHECK
            // add these field data types to temp2
            temp2.add(tokens.get(index - 1).value);

            // no decimal restriction can be passed
            temp4.add(0);

            if (tokens.get(index).value.equals("(")) {
                index++;

                if (tokens.get(index).type.equals("number")) {
                    // SEMANTIC CHECK
                    // make sure the number is an int
                    if (!is_int(tokens.get(index).value))
                        semantic_error.add(tokens.get(index).value + " is not valid; expecting an integer.");

                    // SEMANTIC CHECK
                    // make sure the number is positive and greater than 0
                    if (!is_valid_restriction(tokens.get(index).value))
                        semantic_error
                                .add(tokens.get(index).value + " is not valid; expecting a number greater than 0.");

                    index++;

                    // SEMANTIC CHECK
                    // add this field restriction to temp3
                    // but first we need to ensure it's a valid, whole integer
                    // if it's not, set to the default value
                    // doesn't matter, if it's not a valid integer, the query
                    // won't execute
                    int temp = -1;
                    if (is_int(tokens.get(index - 1).value)) {
                        if (is_valid_restriction(tokens.get(index - 1).value)) {
                            temp = Integer.parseInt(tokens.get(index - 1).value);
                        }
                    }
                    if (temp != -1)
                        temp3.add(temp);
                    else
                        temp3.add(get_default_size(temp2.get(temp2.size() - 1)));
                } else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a number.");

                if (tokens.get(index).value.equals(")"))
                    index++;
                else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a ).");
            } else {
                // if we're here, that means that no restriction was passed
                // so it's the default value

                // SEMANTIC CHECK
                // add this field restriction to temp3
                temp3.add(get_default_size("CHAR"));
            }
        } else if (tokens.get(index).value.equals("VARCHAR")) {
            index++;

            // SEMANTIC CHECK
            // add these field data types to temp2
            temp2.add(tokens.get(index - 1).value);

            // can't have decimal restriction
            temp4.add(0);

            if (tokens.get(index).value.equals("(")) {
                index++;

                if (tokens.get(index).type.equals("number")) {
                    // SEMANTIC CHECK
                    // make sure the number is an int
                    if (!is_int(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting an integer.");

                    // SEMANTIC CHECK
                    // make sure the number is positive and greater than 0
                    if (!is_valid_restriction(tokens.get(index).value))
                        parse_error.add(tokens.get(index).value + " is not valid; expecting a number greater than 0.");

                    index++;

                    // SEMANTIC CHECK
                    // add this field restriction to temp3
                    // but first we need to ensure it's a valid, whole integer
                    // if it's not, set to the default value
                    // doesn't matter, if it's not a valid integer, the query
                    // won't execute
                    int temp = -1;
                    if (is_int(tokens.get(index - 1).value)) {
                        if (is_valid_restriction(tokens.get(index - 1).value)) {
                            temp = Integer.parseInt(tokens.get(index - 1).value);
                        }
                    }
                    if (temp != -1)
                        temp3.add(temp);
                    else
                        temp3.add(get_default_size(temp2.get(temp2.size() - 1)));
                } else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a number.");

                if (tokens.get(index).value.equals(")"))
                    index++;
                else
                    parse_error.add(tokens.get(index).value + " is not valid; expecting a ).");
            } else {
                // if we're here, that means that no restriction was passed
                // so it's the default value
                // SEMANTIC CHECK
                // add this field restriction to temp3
                temp3.add(get_default_size("VARCHAR"));
            }
        } else if (tokens.get(index).value.equals("BIT")) {
            index++;

            // SEMANTIC CHECK
            // add these field data types to temp2
            temp2.add(tokens.get(index - 1).value);

            // cannot have any restrictions
            temp3.add(1);
            temp4.add(0);
        } else
            parse_error.add(tokens.get(index).value + " is not valid; expecting a field type.");
    }

    // cleans string
    private static String clean(String input) {
        // replace all new lines with spaces
        input = input.replace("\n", " ").replace("\r", "");

        // replace all indentations/tabs with spaces
        input = input.replace("\t", " ");

        // replace all 2 or more spaces with 1 space
        input = input.replaceAll("( )+", " ");

        // remove all extra spaces at beginning and end of file
        return input.trim();
    }

    // print SQL input thing
    private static void print_thing() {
        System.out.print(">  ");
    }

    // capture user's input
    private static void capture_input() {
        print_thing();

        if (auto_input) {
            input = "CREATE DATABASE Jack-Database;";
            input = input + "Create table S (SNO varchar(2) NOT NULL, SNAME varchar(50), QUOTA int, CITY varchar(20));INSERT INTO S (SNO, SNAME, QUOTA, CITY) VALUES ('S1', 'Job', 4000, 'Dallas');INSERT INTO S (SNO, SNAME, QUOTA, CITY) VALUES ('S2', 'Baker', 20000, 'Chicago');INSERT INTO S (SNO, SNAME, QUOTA, CITY) VALUES ('S3', 'Kirby', 6000, 'Phoenix');INSERT INTO S (SNO, SNAME, QUOTA, CITY) VALUES ('S4', 'Sims', 3000, 'San Diego');INSERT INTO S (SNO, SNAME, QUOTA, CITY) VALUES ('S5', 'Jones', 30000, 'New York');";
            input = input + "Create table P (PNO varchar(2) NOT NULL, PNAME varchar(50), COST Number, AVLQTY int);insert into P values ('P1', 'Modem', 350.00, 100);Insert into P values ('P2', 'Monitor', 400.00, 45);insert into P values ('P3', 'Printer', 700.00, 15);insert into P values ('P4', 'CPU Cards', 2500.00, 10);insert into P values ('P5', 'Disk Unit', 700.00, 25);insert into P values ('P6', 'Tape Drive', 1200.00, 27);";
            input = input + "Create table SP (SNO varchar(2) NOT NULL, PNO varchar(2) NOT NULL, QTY Number);insert into SP values ('S1', 'P1', 300);insert into SP values ('S1', 'P2', 200);insert into SP values ('S2', 'P1', 300);insert into SP values ('S2', 'P2', 400);insert into SP values ('S3', 'P2', 200);insert into SP values ('S4', 'P2', 200);";
            
            auto_input = false;
        } else
            // get the input from the console
            input += scanning.nextLine();

        // make sure it's not blank
        while (input.trim().length() == 0) {
            print_thing();
            input = scanning.nextLine();
        }

        // cleanup extra bad characters at the end
        while (input.substring(input.length() - 1).equals(" ") || input.substring(input.length() - 1).equals("\n")
                || input.substring(input.length() - 1).equals("\t")
                || input.substring(input.length() - 1).equals("\r")) {
            input = input.substring(0, input.length() - 1);
        }

        // if the last item is not a ; - user will have to keep on typing
        while (!input.substring(input.length() - 1).equals(";")) {
            print_thing();
            input += " " + scanning.nextLine();

            // cleanup extra bad characters at the end
            while (input.substring(input.length() - 1).equals(" ") || input.substring(input.length() - 1).equals("\n")
                    || input.substring(input.length() - 1).equals("\t")
                    || input.substring(input.length() - 1).equals("\r")) {
                input = input.substring(0, input.length() - 1);
            }
        }
        input = clean(input);
    }

    // split into separate commands
    private static void split_commands() {
        commands.clear();
        quotes = false;

        // loop through the input
        for (int i = 0; i < input.length(); i++) {
            // if we're not at the first item of the string
            if (i != 0) {
                // if we see a quote
                if (input.substring(i, i + 1).equals("'") && !input.substring(i - 1, i).equals("\\")) {
                    quotes = !quotes;
                }
            } else {
                // if we see a quote
                if (input.substring(i, i + 1).equals("'")) {
                    quotes = !quotes;
                }
            }

            // if we're outside of a quote, add it to the command list
            if (input.substring(i, i + 1).equals(";") && !quotes) {
                commands.add(input.substring(0, i + 1));
                input = input.substring(i + 1, input.length());
                i = 0;
            }
        }
    }

    // cleans out all tokens
    private static void cleanse_tokens() {
        // loop through all tokens
        for (int i = 0; i < tokens.size(); i++) {
            // check all tokens
            if (tokens.get(i).type.equals("token")) {
                if (!is_token(tokens.get(i).value)) {
                    // this is not a token, we need to analyze it
                    if (tokens.get(i).value.length() == 1)
                        parse_error.add(tokens.get(i).value + " is not a valid token.");
                    else {
                        // at this point, the token being passed is greater than
                        // 1 character long
                        // so now we check the first 2 characters of the token
                        // to see whether or not it's valid
                        if (!is_token(tokens.get(i).value.substring(0, 2))) {
                            // so the first 2 characters are not a valid token
                            // BUT the 1st character can be a valid token
                            // so we need to check
                            if (!is_token(tokens.get(i).value.substring(0, 1))) {
                                // report error - no tokens found anywhere
                                parse_error.add(tokens.get(i).value + " is not a valid token.");
                            } else {
                                // the first character is a valid token
                                // so we need to separate it, and insert it
                                String t = tokens.get(i).value.substring(0, 1);

                                // update the current token
                                tokens.get(i).value = tokens.get(i).value.substring(1, tokens.get(i).value.length());

                                // insert new token
                                tokens.add(i, new Project(t, "token"));
                            }
                        } else {
                            // the first 2 characters are a valid token
                            // so we need to separate it, and insert it
                            String t = tokens.get(i).value.substring(0, 2);

                            // update the current token
                            tokens.get(i).value = tokens.get(i).value.substring(2, tokens.get(i).value.length());

                            // insert new token
                            tokens.add(i, new Project(t, "token"));
                        }
                    }
                }
            }

            // if we see words
            if (tokens.get(i).type.equals("letter")) {
                if (is_keyword(tokens.get(i).value)) {
                    tokens.get(i).type = "keyword";
                } else {
                    tokens.get(i).type = "attribute";
                }
            }
        }
    }

    // create tokens
    private static void create_tokens(String temp) {
        // reset
        tokens.clear();
        quotes = false;
        parse_error.clear();
        semantic_error.clear();
        temp1.clear();
        temp2.clear();
        temp3.clear();
        temp4.clear();
        temp4.clear();
        temp5.clear();
        temp6.clear();
        temp7.clear();
        temp8.clear();
        temp9.clear();
        PotentiallyAListOfColumns.clear();
        temp11.clear();
        temp12.clear();
        temp13.clear();
        AggFunc.clear();
        groupColumns.clear();
        groupingSets.clear();
        having_clmn.clear();
        having_clmn_type.clear();
        having_oper.clear();
        having_val.clear();
        having_sep.clear();
        asClmn_name.clear();
        in_where = false;
        in_having = false;
        groupType = "";
        table_name = "";
        command = "";

        // reset all line parameters
        String current_type = "";
        String current_string = "";
        String previous_type = "";
        String previous_string = "";

        // loop through all characters in the string
        for (int i = 0; i < temp.length() + 1; i++) {
            // set this new character to the current string
            // and current type
            if (i < temp.length()) {

                if (temp.substring(i, i + 1).equals("'")) {
                    // we see a varchar
                    if (i == 0 || !temp.substring(i - 1, i).equals("\\")) {
                        // we see a varchar happening

                        // first of all, we need to add the current token in the
                        // buffer
                        if (!current_type.equals("space"))
                            tokens.add(new Project(current_string, current_type));
                        // reset the buffer
                        previous_string = "";
                        previous_type = "space";

                        int t = 1;
                        String t2 = "'";

                        while (i + t < temp.length()) {
                            // loop through the string until we find the closing
                            // parenthesis thing
                            t2 += temp.substring(i + t, i + t + 1);

                            // if we see another valid closing brace, break out
                            if (temp.substring(i + t, i + t + 1).equals("'")
                                    && !temp.substring(i + t - 1, i + t).equals("\\"))
                                break;

                            t++;
                        }
                        // insert the token to the token list
                        tokens.add(new Project(t2.replace("\\'", "'"), "varchar"));

                        // set the counter to the next immediate place in the
                        // token list
                        i += t + 1;
                    }
                }

                // begin processing token list
                current_string = temp.substring(i, i + 1);
                current_type = character_type(current_string);

                /*********************************
                 *********** FOR FLOATS **********
                 *********************************/
                // if the current string is a . and there is a
                // number on either side of it - process as number
                if (current_string.equals(".") && (previous_type.equals("number")
                        || character_type(temp.substring(i + 1, i + 2)).equals("number")))
                    current_type = "number";

                // if we see a negative number
                if (current_string.equals("-") && (character_type(temp.substring(i + 1, i + 2)).equals("number")
                        || (temp.substring(i + 1, i + 2).equals(".")
                        && character_type(temp.substring(i + 2, i + 3)).equals("number"))))
                    current_type = "number";

            } else {
                current_string = "";
                current_type = "space";
            }

            // if this is the very first run of the program
            if (previous_string.equals("")) {
                previous_string = current_string;
                previous_type = current_type;
            } else {
                // not the first run of the program
                // normal processing occurs here

                // if the current type and the new type are the same
                // add new character to previous character
                if (current_type.equals(previous_type)) {
                    previous_string += current_string;
                } else if (previous_type.equals("letter") && (current_string.equals("_") || current_string.equals("-")
                        || current_type.equals("number"))) {
                    previous_string += current_string;
                } else {

                    // process the previous string
                    // only if it's not a space
                    if (!previous_string.equals(" ")) {
                        if (!previous_type.equals("space"))
                            tokens.add(new Project(simplify(previous_string), previous_type));
                    }

                    // check for potential of floating point number
                    if (previous_type.equals("number") && current_string.equals(".")) {
                        previous_string += current_string;
                    } else {
                        // replace the previous string and previous type
                        // with the new type
                        previous_string = current_string;
                        previous_type = current_type;
                    }
                }
            }
        }

    }

    // checks if string is a keyword
    private static boolean is_keyword(String input) {
        input = input.toUpperCase();
        return input.equals("CREATE") || input.equals("DATABASE") || input.equals("DROP") || input.equals("SAVE")
                || input.equals("COMMIT") || input.equals("LOAD") || input.equals("TABLE") || input.equals("DROP")
                || input.equals("INSERT") || input.equals("INTO") || input.equals("VALUES") || input.equals("DELETE")
                || input.equals("FROM") || input.equals("UPDATE") || input.equals("WUPDATE") || input.equals("SET")
                || input.equals("WHERE") || input.equals("SELECT") || input.equals("WSELECT") || input.equals("INTEGER")
                || input.equals("NUMBER") || input.equals("CHAR") || input.equals("INT") || input.equals("VARCHAR")
                || input.equals("CHARACTER") || input.equals("BIT") || input.equals("NOT") || input.equals("NULL")
                || input.equals("LIST") || input.equals("TABLES") || input.equals("OR") || input.equals("AND")
                || input.equals("COUNT") || input.equals("AVG") || input.equals("AVERAGE") || input.equals("SUM") 
                || input.equals("MIN") || input.equals("MINIMUM") || input.equals("MAX") || input.equals("MAXIMUM")
                || input.equals("CUBE") || input.equals("ROLLUP") || input.equals("HAVING") || input.equals("GROUPING")
                || input.equals("BY") || input.equals("AS");

    }

    // checks if string is a token
    private static boolean is_token(String input) {
        return input.equals("*") || input.equals(";") || input.equals("(") || input.equals(")") || input.equals("'")
                || input.equals("=") || input.equals("<") || input.equals("<=") || input.equals(">")
                || input.equals(">=") || input.equals("!=") || input.equals("<>") || input.equals(":")
                || input.equals("/") || input.equals(",");

    }

    // checks if string is only letters
    private static boolean is_letters(String input) {
        return Pattern.matches("[a-zA-Z]+", input);
    }

    // simplify some tokens to different version
    // ex. INTEGER to INT
    private static String simplify(String temp) {
        if (is_keyword(temp))
            temp = temp.toUpperCase();

        if (temp.equals("INTEGER"))
            return "INT";

        if (temp.equals("CHARACTER"))
            return "CHAR";

        return temp;
    }

    // checks if string is only numbers
    private static boolean is_numeric(String input) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // gets character type
    private static String character_type(String input) {
        if (clean(input).isEmpty())
            return "space";
        if (is_letters(clean(input)))
            return "letter";
        if (is_numeric(clean(input)))
            return "number";
        // if none of the above
        return "token";
    }

    // sees if there is an error
    private static boolean is_parse_error() {
        return parse_error.size() > 0;
    }

    // sees if there is an error
    private static boolean is_semantic_error() {
        return semantic_error.size() > 0;
    }

    // displays the error message if there is one
    private static void display_error() {
        if (is_parse_error()) {
            System.out.println("   " + parse_error.get(0));
        } else {
            if (is_semantic_error())
                System.out.println("   " + semantic_error.get(0));
        }

        if (is_semantic_error() || is_parse_error())
            System.out.println("   Query did not execute.");
    }

    // sees if a string is an integer
    private static boolean is_int(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }

        // return true if it parsed with no issues
        return true;
    }

    // sees if a string is an integer
    private static boolean is_valid_restriction(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }

        int number = Integer.parseInt(input);

        // only return true if the number is a positive, int, greater than 1
        return number >= 1;
    }

    // sees if a string is a bit
    public static boolean is_bit(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }

        // parse the number
        int number = Integer.parseInt(input);

        // if it's between 0 and 1, report true
        return number >= 0 && number <= 1;
    }

    // check to make sure that a table exists
    private static boolean does_table_exist(String table_name) {
        // if the table exists, return true
        if (Database.tables.containsKey(table_name))
            return true;

        // if not, return false
        return false;
    }

    ////////////////////////////////////////////////////////////
    ////// SEMANTIC CHECK METHODS //////
    ///////////////////////////////////////////////////////////

    // checks to see if a column exists
    private static boolean does_column_exist(String it, String check) {
        // first, check to make sure the table exists
        if (!does_table_exist(it))
            return false;

        // loop through all columns in the table passed
        // if we see a column with that name, display it
        for (int i = 0; i < Database.tables.get(it).columns.size(); i++)
            if (Database.tables.get(it).columns.get(i).column_name.equals(check))
                return true;

        // if not, return false
        return false;
    }

    // get column object
    private static Column get_column(String it, String check) {
        // first, check to make sure the table exists
        if (!does_table_exist(it))
            return null;

        // loop through all columns in the table passed
        // if we see a column with that name, display it
        for (int i = 0; i < Database.tables.get(it).columns.size(); i++)
            if (Database.tables.get(it).columns.get(i).column_name.equals(check))
                return Database.tables.get(it).columns.get(i);

        // if not, return null pointer
        return null;
    }

    // get the default size restrictions for all different data types
    private static int get_default_size(String data_type) {
        switch (data_type) {
            case "INT":
                return 5;
            case "NUMBER":
                return 5;
            case "CHAR":
                return 1;
            case "VARCHAR":
                return 10;
            case "BIT":
                return 1;
            default:
                // if none match
                return -1;
        }
    }

    // pass it the contents of the record, get the type
    private static String get_type(String it) {
        if (is_numeric(it))
            return "number";
        else if (it.equals("NULL"))
            return "NULL";
        else if (it.length() > 0) {
            if (it.substring(0, 1).equals("'"))
                return "varchar";
        }
        return "ERROR";
    }

    // pass it the contents of the record, get the type
    // NOTE: if passing a number with a decimal, this function will only return
    // the length of the top numbers before the decimal
    private static int get_length(String it) {
        String it2 = get_type(it);

        switch (it2) {
            case "varchar":
                return it.length() - 2;
            case "NULL":
                return 0;
            case "number":

                int subtract = 0;

                if (it.contains("-"))
                    subtract++;
                if (it.contains(".")) {
                    subtract += it.substring(it.indexOf("."), it.length()).length() + 1;
                }

                return it.length() - subtract;
        }
        // if error
        return -1;
    }

    // see if data would be truncated
    private static boolean is_data_truncated(Column column_to_check, String value) {
        if (!get_type(value).equals("NULL")) {
            if (column_to_check.restriction_2 == null || !column_to_check.column_type.equals("number")) {
                // we don't have to worry about decimal spaces
                int max_length = column_to_check.restriction;
                int actual_length = get_length(value);

                if (actual_length > max_length)
                    return true;
            } else {
                // we do have to worry about decimal spaces
                int front_max_length = column_to_check.restriction - column_to_check.restriction_2;
                int back_max_length = column_to_check.restriction_2;

                // remove the negative sign if included
                value = value.replace("-", "");
                String[] values = value.split("\\.");

                // if the front is greater than the alloted space, truncation
                // error
                if (values[0].length() > front_max_length)
                    return true;

                // if the back is greater than the alloted space, truncation
                // error
                if (values[1].length() > back_max_length)
                    return true;

            }
        }

        return false;
    }

    // see if these fields can play with these fields
    private static boolean are_we_compatible(String type1, String type2) {
        type1 = type1.toUpperCase();
        type2 = type2.toUpperCase();

        ////////////////

        if (type1.equals("VARCHAR") && type2.equals("NULL"))
            return true;

        if (type1.equals("CHAR") && type2.equals("NULL"))
            return true;

        if (type1.equals("INT") && type2.equals("NULL"))
            return true;

        if (type1.equals("NUMBER") && type2.equals("NULL"))
            return true;

        if (type1.equals("BIT") && type2.equals("NULL"))
            return true;

        if (type1.equals("NULL") && type2.equals("VARCHAR"))
            return true;

        if (type1.equals("NULL") && type2.equals("CHAR"))
            return true;

        if (type1.equals("NULL") && type2.equals("INT"))
            return true;

        if (type1.equals("NULL") && type2.equals("NUMBER"))
            return true;

        if (type1.equals("NULL") && type2.equals("BIT"))
            return true;

        ///////////////////////////////////////////////////

        if (type1.equals("VARCHAR") && type2.equals("VARCHAR"))
            return true;

        if (type1.equals("VARCHAR") && type2.equals("CHAR"))
            return true;

        if (type1.equals("CHAR") && type2.equals("VARCHAR"))
            return true;

        ///////////////////////////////////////////////////

        if (type1.equals("INT") && type2.equals("INT"))
            return true;

        if (type1.equals("INT") && type2.equals("NUMBER"))
            return true;

        if (type1.equals("NUMBER") && type2.equals("INT"))
            return true;

        if (type1.equals("NUMBER") && type2.equals("NUMBER"))
            return true;

        ///////////////////////////////////////////////////

        if (type1.equals("BIT") && type2.equals("BIT"))
            return true;

        if (type1.equals("BIT") && type2.equals("INT"))
            return true;

        if (type1.equals("BIT") && type2.equals("NUMBER"))
            return true;

        if (type1.equals("INT") && type2.equals("BIT"))
            return true;

        if (type1.equals("NUMBER") && type2.equals("BIT"))
            return true;

        // else, return false
        return false;
    }

    // can we use this logical operator
    private static boolean is_relational_operator_valid(String type, String relational_operator) {
        type = type.toUpperCase();
        relational_operator = relational_operator.toUpperCase();

        // if we see a VARCHAR, CHAR or BIT operator using any of the following
        // relational operators, this is an error and a problem
        if (type.equals("VARCHAR") || type.equals("CHAR")) {
            if (relational_operator.equals("<") || relational_operator.equals("<=") || relational_operator.equals(">")
                    || relational_operator.equals(">="))
                return false;
        }

        // by default good
        return true;
    }

    // check for duplicate columns
    private static void check_duplicate_columns(ArrayList<String> it) {
        // SEMANTIC CHECK
        // at this point, all the column names were inserted into temp1
        // we need to make sure there are no duplicate column names being
        // insert into
        HashSet<String> temp_set = new HashSet<String>(temp1);

        if (temp_set.size() != it.size()) {
            // there are duplicate columns in the table
            // now we're going to identify them
            @SuppressWarnings("unchecked")
            ArrayList<String> t = (ArrayList<String>) it.clone();
            Object[] array = temp_set.toArray();

            // iterate through, remove all currently existing copies
            for (Object anArray : array) {
                t.remove(anArray);
            }

            // now we loop through and display the duplicates
            // we have to convert back to hash set in case the duplicates
            // are
            // more than 2
            HashSet<String> final_dupes = new HashSet<String>(t);
            for (String a : final_dupes) {
                semantic_error.add("The column " + a + " is specified multiple times.");
            }
        }
    }

    // gets all columns for a table that do not allow nulls
    private static ArrayList<String> get_all_non_null_columns(String table_name) {
        // first, check to make sure the table exists
        if (!does_table_exist(table_name))
            return null;

        // create the array list
        ArrayList<String> all_non_null_columns = new ArrayList<String>();
        // get all columns from the table where NULL is not allowed

        for (int i = 0; i < Database.tables.get(table_name).columns.size(); i++) {
            if (Database.tables.get(table_name).columns.get(i).is_null_allowed.equals("false"))
                all_non_null_columns.add(Database.tables.get(table_name).columns.get(i).column_name);
        }

        return all_non_null_columns;

    }

    // get the index # of a specific column
    private static int get_column_index(String column_name) {
        column_name = column_name.toLowerCase();
        // iterate through all columns in the table
        for (int i = 0; i < Database.tables.get(table_name).columns.size(); i++) {
            if (Database.tables.get(table_name).columns.get(i).column_name.toLowerCase().equals(column_name))
                return i;
        }

        // return -1 if column not found
        return -1;
    }

    // gets all columns for a table that do not allow nulls
    private static boolean does_column_exist_in_list(ArrayList<Column> column_list_1, String column_name) {
        // loop through the list of columns
        // return true if the column name entered exists
        for (Column a : column_list_1) {
            if (column_name.equals(a.column_name))
                return true;
        }

        // if we reach here, assume the column does not exist
        return false;
    }

    private static void execute_create_database() {
        // check to make sure that database doesn't already exist
        File file = new File(Database.temp_database_name);
        if (file.exists() && file.isFile()) {
            System.out.println("**Warning: that database already exists; you might overwrite the data.");
        }

        if (Database.database_name == null) {
            // there is no current database set
            // no worries, just create it
            Database.database_name = Database.temp_database_name;
            Database.temp_database_name = null;
        } else {
            // there is a current database set
            // we need to ask the user to confirm whether
            // or not they are OK with switching

            // quick check - make sure current database name and new database
            // name are not equivalent
            if (Database.database_name.equals(Database.temp_database_name)) {
                System.out.println(
                        "That database already exists. To create a new database with the same name, you need to drop it first.");
            } else {

                System.out.println(
                        "\nCreating a new database will lose all current data if not saved. Are you ok with this? (y/n)");
                String it = scanning.nextLine();
                while (!it.toUpperCase().equals("N") && !it.toUpperCase().equals("NO") && !it.toUpperCase().equals("Y")
                        && !it.toUpperCase().equals("YES")) {
                    System.out.println("Bad user input; enter: (y/n)");
                    it = scanning.nextLine();
                }
                if (it.toUpperCase().equals("Y") || it.toUpperCase().equals("YES")) {
                    // if the user approved
                    // MAKE THE SWITCH
                    Database.database_name = Database.temp_database_name;
                    Database.temp_database_name = null;
                    Database.tables.clear();
                } else
                    Database.temp_database_name = null;
            }
        }
    }

    ////////////////////////////////////////////////////////////
    ////// EXECUTION //////
    ///////////////////////////////////////////////////////////

    private static void execute_drop_database() {
        // drop everything
        if (Database.database_name != null) {
            if (Database.temp_database_name.toLowerCase().equals(Database.database_name.toLowerCase())) {
                Database.database_name = null;
                Database.temp_database_name = null;
                Database.tables.clear();
            }
        }
        // WE ALSO NEED TO DROP THE FILE FROM SECONDARY MEMORY
        try {
            File f = new File(directory_to + Database.temp_database_name.toLowerCase());
            if (f.exists()) {
                f.delete();
                System.out.println("The saved datbase was successfully deleted!");
            } else
                System.out.println("The database was not saved; so no file was deleted.");
        } catch (Exception e) {
        }
    }

    private static void execute_save() throws FileNotFoundException, UnsupportedEncodingException {
        // can only do this command if we're working on an active database
        if (Database.database_name != null) {

            // first we check if the database exists
            File theDir = new File("databases");

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                try {
                    theDir.mkdir();
                } catch (SecurityException se) {
                    // handle it
                }
            }

            // create the file. This also overwrites the file
            saveToDatabase();

        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void saveToDatabase() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(directory_to + Database.database_name.toLowerCase(), "UTF-8");

        // iterate through all tables
        Enumeration<String> e = Database.tables.keys();
        String t;
        while (e.hasMoreElements()) {
            t = e.nextElement();
            writer.println(t); // name of the table
            writer.println(Database.tables.get(t).columns.size()); // number of columns in the table

            for (int i = 0; i < Database.tables.get(t).columns.size(); i++) {
                // iterate through each column and write to file
                writer.println(Database.tables.get(t).columns.get(i).column_name + DELIMITER
                        + Database.tables.get(t).columns.get(i).column_type + DELIMITER
                        + Database.tables.get(t).columns.get(i).restriction + DELIMITER
                        + Database.tables.get(t).columns.get(i).restriction_2 + DELIMITER
                        + Database.tables.get(t).columns.get(i).is_null_allowed);
            }
            for (int i = 0; i < Database.tables.get(t).records.size(); i++) {
                // iterate through each record and write to file
                String recordStringToFile = "";
                recordStringToFile += Database.tables.get(t).records.get(i).record_date;
                // now that we have the date, we will iterate through each cell and append to file
                for (int j = 0; j < Database.tables.get(t).records.get(i).listofCells.size(); j++) {
                    Cell tempCell = Database.tables.get(t).records.get(i).listofCells.get(j);
                    for(CellTuple ct : tempCell.cellTuples){
                        recordStringToFile += DELIMITER + ct.value;
                        recordStringToFile += DELIMITER + ct.date;
                    }
//                    recordStringToFile += DELIMITER + Database.tables.get(t).records.get(i).listofCells.get(j).getFirstValue();
//                    recordStringToFile += DELIMITER + Database.tables.get(t).records.get(i).listofCells.get(j).getFirstDate();
                    recordStringToFile += DELIMITER + LISTDELIMITER;
                }
                // now we write it to file
                writer.println(recordStringToFile);
            }
            writer.println("");
        }

        // close out the file
        writer.close();
    }

    private static void execute_load_database() {
        // can only do this command if we're working on an active database

        // first of all, make sure the database we're loading isn't equivalent
        // to the current name

        // check to see if the file exists
        File file = new File(directory_to + Database.temp_database_name);
        if (!(file.exists() && file.isFile())) {
            System.out.println("Cannot load " + Database.temp_database_name + "; the database does not exist.");
        } else {
            if (Database.database_name != null) {
                System.out.println(
                        "Loading a database will lose all current data if not saved. Are you ok with this? (y/n)");
                String it = scanning.nextLine();
                while (!it.toUpperCase().equals("N") && !it.toUpperCase().equals("NO") && !it.toUpperCase().equals("Y")
                        && !it.toUpperCase().equals("YES")) {
                    System.out.println("Bad user input; enter: (y/n)");
                    it = scanning.nextLine();
                }
                if (it.toUpperCase().equals("Y") || it.toUpperCase().equals("YES")) {
                    // if the user approved
                    // MAKE THE SWITCH
                    Database.tables.clear();

                    // HERE IS WHERE WE DO EVERYTHING!
                    loadFromDatabase();
                    Database.database_name = Database.temp_database_name;
                    Database.temp_database_name = null;

                    System.out.println("Database loaded successfully!");
                } else {
                    Database.temp_database_name = null;
                    System.out.println("Database did not load.");
                }
            } else {
                // there is no current database loaded
                // HERE IS WHERE WE DO EVERYTHING!
                loadFromDatabase();
                Database.database_name = Database.temp_database_name;
                System.out.println("Database loaded successfully!");
            }
        }
        Database.temp_database_name = null;
    }

    private static void execute_create_table() {
        if (Database.database_name != null) {
            // can only do this command if we're working on an active database

            Database.tables.put(table_name, new Table());
            for (int i = 0; i < temp1.size(); i++)
                Database.tables.get(table_name).columns
                        .add(new Column(temp1.get(i), temp2.get(i), temp3.get(i), temp4.get(i), temp5.get(i)));
        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void execute_drop_table() {
        if (Database.database_name != null) {
            // can only do this command if we're working on an active database

            // drop the table
            Database.tables.remove(table_name);
            System.out.println(table_name + " successfully dropped.");
        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void execute_insert() {
        if (Database.database_name != null) {
            // can only do this command if we're working on an active database

            // get the total # of columns
            int c = Database.tables.get(table_name).columns.size();

            // add the blank record record
            Database.tables.get(table_name).records.add(new Record());

            // iterate through all columns to insert
            for (int i = 0; i < c; i++) {
                // so now we're going to insert 1 cell per column in that table
                Database.tables.get(table_name).records.get(Database.tables.get(table_name).records.size() - 1).listofCells
                        .add(new Cell("NULL"));
            }

            // IF that column ends has a value specified in the SQL command
            // overwrite the insert with the value
            for (int i = 0; i < temp2.size(); i++) {
                Database.tables.get(table_name).records.get(Database.tables.get(table_name).records.size() - 1).listofCells
                        .set(get_column_index(temp6.get(i).column_name), new Cell(temp2.get(i)));
            }

        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void execute_delete() {
        if (Database.database_name != null) {
            // can only do this command if we're working on an active database

            // get all the rows for the table
            @SuppressWarnings("unchecked")
            ArrayList<Record> r = (ArrayList<Record>) Database.tables.get(table_name).records.clone();

            Boolean it;
            int counter = 0;
            for (int i = 0; i < r.size(); i++) {
                // loop through every record

                it = execute_where(r.get(i));

                // if it it qualifies, delete the record
                if (it) {
                    Database.tables.get(table_name).records.remove(i);
                    r.remove(i);

                    // go back by one, so we don't skip a record when evaluating
                    i--;
                    counter++;
                }
            }
            // report the # of records deleted
            String d = counter + " record(s) deleted.";
            System.out.println(d);
        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void execute_update() {
        // can only do this command if we're working on an active database
        if (Database.database_name != null) {

            // get all the rows for the table
            @SuppressWarnings("unchecked")
            ArrayList<Record> r = (ArrayList<Record>) Database.tables.get(table_name).records.clone();

            Boolean it;
            int counter = 0, column_index;
            for (int i = 0; i < r.size(); i++) {
                // loop through every record

                it = execute_where(r.get(i));

                // if it it qualifies, update the record
                if (it) {
                    // loop through every column we're updating
                    for (int j = 0; j < temp13.size(); j++) {

                        String newCellValue = temp13.get(j);

                        // get the column index that we're updating
                        column_index = get_column_index(temp12.get(j).column_name);
                        if (tokens.get(0).value.equals("UPDATE")) {

//							Get the current time for that record, so that the updated cell does not lose its date
                            Date tempDate = Database.tables.get(table_name).records.get(i).listofCells.get(column_index).getFirstDate();

                            // now that we have the index we literally update the record
                            Database.tables.get(table_name).records.get(i).listofCells.set(column_index, new Cell(newCellValue, tempDate));
                        } else if (tokens.get(0).value.equals("WUPDATE")) {
                            // if it's a wUpdate command, update the date of the record as well
                            Database.tables.get(table_name).records.get(i).record_date = new Date();

//							Update the designated cell by inserting the new update into the first position of the cell's fields (Lists)
                            Database.tables.get(table_name).records.get(i).listofCells.get(column_index).cellTuples.addFirst(new CellTuple(newCellValue, new Date()));
                        }
                    }
                    // increment the counter
                    counter++;
                }
            }
            // report the # of records updated
            String d = counter + " record(s) updated.";
            System.out.println(d);
        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static void execute_select() throws ParseException {

        if (Database.database_name != null) {
            // can only do this command if we're working on an active database

            // dividing line
            System.out.println("");

            // add all the columns to PotentiallyAListOfColumns
            for (String a : temp9)
                PotentiallyAListOfColumns.add(get_column(table_name, a));

            // display the column names
            for (int i = 0; i < PotentiallyAListOfColumns.size(); i++){
                Column aTemp10 = PotentiallyAListOfColumns.get(i);
                if(asClmn_name.size() > 0){
                   if(asClmn_name.get(i).equals("NULL"))
                       System.out.print(display(aTemp10.column_name, aTemp10) + "  ");
                   else
                       System.out.print(display(asClmn_name.get(i), aTemp10) + "  ");
                } 
                else
                    System.out.print(display(aTemp10.column_name, aTemp10) + "  ");
            }
            // display the dividing line between columns and tuples
            System.out.println("");
            // display dashes
            for (Column aTemp10 : PotentiallyAListOfColumns) {
                System.out.print(display_dashes(aTemp10) + "  ");
            }
            System.out.println("");

            // at this point, all we're missing is the actual rows
            @SuppressWarnings("unchecked")
            ArrayList<Record> r = (ArrayList<Record>) Database.tables.get(table_name).records.clone();
            ArrayList<Record> fr = new ArrayList<Record>();
            ArrayList<Integer> colIndexes = new ArrayList<>();

            // Apply Grouping clauses
            for (int i = 0; i < groupColumns.size(); i++) {
                Column col = get_column(table_name, groupColumns.get(i));
                int c = get_column_index(col.column_name);
                colIndexes.add(c);
            }
            
            //execute group by clause, else return a list of records
            switch(groupType){
               case "GROUP BY":
                  ArrayList<Record> tempList = new ArrayList<Record>();
                  fr.addAll(set_nulls(execute_group_functions(r, colIndexes),colIndexes));
                  for (Record aR : fr) {
                     if(execute_having(aR))
                        tempList.add(aR);
                  }
                  
                  fr = tempList;
                  break;
               case "GROUPING SETS":
                  if(groupingSets.size() != 0) {
                     for (int j = 0; j < groupingSets.size(); j++) {
                         groupColumns = groupingSets.get(j);
                         ArrayList<Integer> colmns = new ArrayList<>();
                         for (int i = 0; i < groupColumns.size(); i++) {
                            Column col = get_column(table_name, groupColumns.get(i));
                            int c = get_column_index(col.column_name);
                            colmns.add(c);
                         }
                         fr.addAll(set_nulls(execute_group_functions(r, colmns),colmns));
                     }
                  } else
                     fr.addAll(execute_group_functions(r, colIndexes));
                  break;
               case "ROLLUP":
                  @SuppressWarnings("unchecked")
                  ArrayList<Integer> colmns = (ArrayList<Integer>) colIndexes.clone();
                  for(int i = groupColumns.size(); i > -1; i--)
                  {
                     if(i != groupColumns.size())
                     {
                        colmns.remove(i);
                     }
                     fr.addAll(set_nulls(execute_group_functions(r, colmns),colmns));
                  }
                  break;
               case "CUBE":
            		for(int i = groupColumns.size(); i > -1; i--)
                  {
               		colmns = new ArrayList<>();
                     fr.addAll(execute_cube_function(colIndexes, colIndexes.size(), i, 0, colmns, 0, r, new ArrayList<Record>()));
                  }
                  break;
               default:
                  fr.addAll(execute_group_functions(r, colIndexes));
                  break;
            }

            // now we're looking at the individual rows
            int c;
            String d;
            
            // loop through each record
            for (Record aR : fr) {
                // we can only display the columns being displayed
                // so we must loop through each column to display
                 printSELECT(aR);

                 // if we're in a wSELECT statement
                 // display the time at the end
                 if (tokens.get(0).value.equals("WSELECT")) {
                     DateFormat dFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
                     Date tempDate = aR.record_date;
                     System.out.print(" updated " + formatDateToString(tempDate));
                     printWSELECT(aR);
                 }

                 // display new line for next record
                 System.out.println("");
            }

        } else
            System.out.println("You are not working in an active database; please CREATE or LOAD a database.");
    }

    private static String formatDateToString(Date tempDate) {
        return new SimpleDateFormat("yyyy MMM dd hh:mm:ss a").format(tempDate);
    }

    private static ArrayList<Record> execute_cube_function(ArrayList<Integer> colIndexes, int n, int r, 
               int index, ArrayList<Integer> colmns, int i, ArrayList<Record> recs, ArrayList<Record> fr)
    {
   		if (index == r)
   		{
   			fr.addAll(set_nulls(execute_group_functions(recs, colmns),colmns));
   		   return fr;
   		}
   
   		// When no more elements are there to put in colmns
   		if (i >= n)
   		   return fr;
   
   		// current is included, put next at next location
         if(colmns.size() > index){
   		   colmns.set(index, colIndexes.get(i));
         } 
         else {
            colmns.add(colIndexes.get(i));
         }
   		execute_cube_function(colIndexes, n, r, index+1, colmns, i+1, recs, fr);
   
   		// current is excluded, replace it with next 
   		execute_cube_function(colIndexes, n, r, index, colmns, i+1, recs, fr);
         return fr;
    }
    
    private static ArrayList<Record> set_nulls(ArrayList<Record> fr, ArrayList<Integer> clmns){
        ArrayList<Integer> cln = new ArrayList<>();
        Column col;
        int c;
        
        //get the columns with aggregate functions
        //they dont need to be null when using a 
        //Group by statment
        for (int i = 0; i < temp9.size(); i++) {
            col = get_column(table_name, temp9.get(i));
            c = get_column_index(col.column_name);
            if(!AggFunc.get(i).equals("NULL")){
               cln.add(c);
            }
        }
        
        cln.addAll(clmns);
        //set the value of the columns that are
        //not inside the group by statment nor
        //aggregate function to null
        for (Record record : fr) {
            Record rec = cloneRecord(record);
            fr.set(fr.indexOf(record), rec);
            for (int i = 0; i < Database.tables.get(table_name).columns.size(); i++){
               if(!cln.contains(i))
                   rec.listofCells.get(i).cellTuples.get(0).value = "NULL";
            }
        }
        
        return fr;
    }
   
    private static void printSELECT(Record aR) {
        int c;
        String d = "";


        for (int i = 0; i < PotentiallyAListOfColumns.size(); i++) {
            Column aTemp10 = PotentiallyAListOfColumns.get(i);
            // get the index number of the column
            c = get_column_index(aTemp10.column_name);

            // get the value to display
            // strip out quotes at beginning and end if displaying a
            // VARCHAR or CHAR
            if (aR.listofCells.get(c).getFirstValue().equals("NULL"))
                d = "";
            else 
            {
                try
                {
                  if(!isNumeric(aR.listofCells.get(c).getFirstValue()))
                  {
                     Integer.parseInt(aR.listofCells.get(c).getFirstValue());
                     d = aR.listofCells.get(c).getFirstValue();
                  }
                  else
                  {
                     Double.parseDouble(aR.listofCells.get(c).getFirstValue());
                     d = aR.listofCells.get(c).getFirstValue();
                  }
                }
                catch(Exception e)
                {
                  d = aR.listofCells.get(c).getFirstValue().substring(1, aR.listofCells.get(c).getFirstValue().length()-1);
                }
            }

            d = display(d, aTemp10) + "  ";

            // display the cell
            System.out.print(d);
//            System.out.flush();
        }
    }
    
    private static ArrayList<Record> execute_group_functions(ArrayList<Record> r, ArrayList<Integer> colIndexes)
    {
      ArrayList<ArrayList<String>> groups = new ArrayList<>();
      ArrayList<Record> finalList = new ArrayList<>();
      
      if (colIndexes.size() > 0) {
          for (Record record : r) { 
              // only include the record if it qualified the WHERE clause
              if (execute_where(record)) {
                 ArrayList<String> currentValues = new ArrayList<>();
                 Record rec = null;
                 
                 for (int i : colIndexes) {
                     currentValues.add(record.listofCells.get(i).getFirstValue());
                 }
   
                 if (groups.contains(currentValues)) {
                     for(int j = 0; j < groups.size(); j++){
                        if(groups.get(j).equals(currentValues)){
                           rec = cloneRecord(finalList.get(j));
                           finalList.set(j, execute_aggregate(rec, record));
                           break;
                        }
                     }
                 } else {
                     finalList.add(record);
                     groups.add(currentValues);
                 }
              }
          }
      } else if(AggFunc.contains("SUM") || AggFunc.contains("AVG") || AggFunc.contains("MIN") ||
            AggFunc.contains("MAX") || AggFunc.contains("COUNT")) {
         int j = 0;
         Record rec = null;
         
         for (Record record : r) {
            // only include the record if it qualified the WHERE clause
            if (execute_where(record)) {
               rec = cloneRecord(record);
               finalList.add(rec);
               break;
            }
            j++;
         }
         
         if(rec != null){
            for(int i = j + 1; i < r.size(); i++){
               if(rec != r.get(i)){
                  if (execute_where(r.get(i))) {
                     rec = execute_aggregate(rec, r.get(i));
                  }
               }
            }
         }
      } else {
          for (Record record : r) {
              // only include the record if it qualified the WHERE clause
              if (execute_where(record)) {
                 finalList.add(record);
              }
          }
      }
      
      return finalList;
    }

    private static void printWSELECT(Record aR) {
//        TODO Work In Progress

//			Enumerates through the list of cells, adding all previous updates to a TreeMap. The TreeMap sorts by date (most recent first), and allows the Tuple to be mapped to column
        List<Cell> recordArgumentCellList = aR.listofCells;
        Record tempRecord = new Record();
        SortedMap<CellTuple, Integer> mapOfCells = new TreeMap<>(new Comparator<CellTuple>() {
            @Override
            public int compare(CellTuple o1, CellTuple o2) {
                if (o1.date.equals(o2.date)) {
                    if (o1.value.equals(o2.value)) {
                        return 0;
                    }
                    return 1;
                }
                return o2.date.compareTo(o1.date);
            }
        });
//        Enumerates through the list of cells, getting WUPDATES.
        for (int i = 0; i < recordArgumentCellList.size(); i++) {
            Cell currentCell = recordArgumentCellList.get(i);
            if (currentCell.cellTuples.size() > 1) {
//					Enumerates through the updates in Cell, adding to the TreeMap. Index starts at 1, because 0 is the Current Value of the Cell
                for (int j = 1; j < currentCell.cellTuples.size(); j++) {
                    mapOfCells.put(new CellTuple(currentCell.cellTuples.get(j).value, currentCell.cellTuples.get(j).date), i);
                }
            }
        }
//        To look nice, we're going to try to display the original Record, if all of the original values have already been wupdated
        if(mapOfCells.size() >= recordArgumentCellList.size()){
            CellTuple[] ctArray = mapOfCells.keySet().toArray(new CellTuple[mapOfCells.size()]);
            Boolean equalFlag = true;

            int n = ctArray.length - 1;

            for(int i = 0; i < recordArgumentCellList.size() - 1; i++){
                if(!(ctArray[n-i-1].date.equals(ctArray[n-i].date))){
                    equalFlag = false;
                }
            }
//            If equalFlag is true, then the last listofCells.size values have the same date, and could be considered the original Record
            if(equalFlag){
                for(int i = 0; i < recordArgumentCellList.size(); i++){
//                    tempRecord.listofCells.add(new Cell().cellTuples.add(ctArray[n-i]));
                    Cell temp = new Cell();
                    temp.cellTuples.add(ctArray[n-i]);
                    tempRecord.listofCells.add(temp);
                    mapOfCells.remove(ctArray[n-i]);
                }
            }
        }

//        Prints the wupdates
        if(mapOfCells.size() > 0 || tempRecord.listofCells.size() > 0) System.out.println();
        for (CellTuple ct : mapOfCells.keySet()) {
            String output;
            String value = ct.value;
            int position = 0;
            if (mapOfCells.containsKey(ct)) {
                position = mapOfCells.get(ct);
            }

            for (int i = 0; i < recordArgumentCellList.size(); i++) {
                if (i == position) {
                    output = formatStringByColumnIndex(value, i);
                } else {
                    output = "";
                }
                output = display(output, PotentiallyAListOfColumns.get(i)) + "  ";
                System.out.print(output);
            }
            System.out.println(" updated " + formatDateToString(ct.date));
        }
        if(tempRecord.listofCells.size()>0){
            String output;
            for (int i = 0; i < recordArgumentCellList.size(); i++) {
                String value = tempRecord.listofCells.get(i).getFirstValue();
                output = formatStringByColumnIndex(value, i);
                output = display(output, PotentiallyAListOfColumns.get(i)) + "  ";
                System.out.print(output);
            }
            System.out.println(" updated " + formatDateToString(tempRecord.listofCells.get(0).getFirstDate()));
        }
        if(mapOfCells.size() > 0 || tempRecord.listofCells.size() > 0){
            for(int i = 0; i < recordArgumentCellList.size(); i++){
                System.out.print(display_dashes(PotentiallyAListOfColumns.get(i)) + "-");
            }
        }
    }

    private static String formatStringByColumnIndex(String value, int i) {
        String output;
        if(value.equals("NULL"))
            output = "";
        else if (PotentiallyAListOfColumns.get(i).column_type.equals("VARCHAR") || PotentiallyAListOfColumns.get(i).column_type.equals("CHAR"))
            output = value.substring(1, value.length()-1);
        else
            output = value;
        return output;
    }
    private static boolean execute_having (Record r) {
        if(having_clmn.size() == 0){
            return true;
        } else {
            // there is a where clause used
            // evaluate the where clause

            int c;
            Boolean previous_condition = null;
            Boolean current_condition = null;
            String temp_column_value, temp_literal_value, temp_operator;
            Column col;
            Float t_c_v, t_l_v;
            for (int i = 0; i < having_clmn.size(); i++) {
                // move over the current and previous results
                if (i != 0) {
                    previous_condition = current_condition;
                    current_condition = null;
                }

                // get the column
                col = get_column(table_name, having_clmn.get(i));

                // get the index number of the column
                c = get_column_index(col.column_name);

                // now we get the variables
                temp_column_value = r.listofCells.get(c).getFirstValue().toLowerCase();
                temp_literal_value = having_val.get(i).toLowerCase();
                temp_operator = having_oper.get(i);

                // at this point, we have all the variables needed for
                // evaluation
                if (having_clmn_type.get(i).column_type.equals("VARCHAR") || having_clmn_type.get(i).column_type.equals("CHAR")) {
                    // we're evaluating a string

                    switch (temp_operator) {
                        case "=":
                            current_condition = temp_column_value.equals(temp_literal_value);
                            break;
                        case "<>":
                            current_condition = !temp_column_value.equals(temp_literal_value);
                            break;
                        case "!=":
                            current_condition = !temp_column_value.equals(temp_literal_value);
                            break;
                    }
                } else {
                    // we're evaluating a number of some kind

                    // get the temp column value
                    if (temp_column_value.toUpperCase().equals("NULL"))
                        t_c_v = null;
                    else
                        t_c_v = Float.parseFloat(temp_column_value);
                    // get the temp literal value
                    if (temp_literal_value.toUpperCase().equals("NULL"))
                        t_l_v = null;
                    else
                        t_l_v = Float.parseFloat(temp_literal_value);

                    // here is where we do the actual evaluation
                    if (t_c_v != null) {
                        switch (temp_operator) {
                            case "=":
                                current_condition = t_c_v.equals(t_l_v);
                                break;
                            case "<>":
                            case "!=":
                                current_condition = !t_c_v.equals(t_l_v);

                                break;
                            case ">":
                                current_condition = t_c_v > t_l_v;
                                break;
                            case ">=":
                                current_condition = t_c_v >= t_l_v;
                                break;
                            case "<":
                                current_condition = t_c_v < t_l_v;
                                break;
                            case "<=":
                                current_condition = t_c_v <= t_l_v;
                                break;
                        }
                    } else {
                        // the row value is null
                        switch (temp_operator) {
                            case "=":
                                current_condition = t_l_v == null;
                                break;
                            case "<>":
                                current_condition = t_l_v != null;
                                break;
                            case "!=":
                                current_condition = t_l_v != null;
                                break;
                            default:
                                // else, default NULL for all other operations
                                current_condition = false;
                                break;
                        }
                    }
                }

                // at this point, we've evaluated the current command
                // now we need to evaluate it against the AND/OR command
                // ONLY DO THIS IF we're not in the 0 index
                if (i != 0) {
                    String condition_separator = having_sep.get(i - 1);

                    // now depending on the AND/OR, we evaluate
                    if (condition_separator.equals("AND")) {
                        current_condition = current_condition && previous_condition;
                    } else if (condition_separator.equals("OR")) {
                        current_condition = current_condition || previous_condition;
                    }

                }
            }
            // return the condition result
            return current_condition;
        }
    }
    // checks if record qualifies
    private static boolean execute_where(Record r) {

        // qualify by default if there is no WHERE clause used
        if (temp1.size() == 0) {
            // there is no where clause
            return true;
        } else {
            // there is a where clause used
            // evaluate the where clause

            int c;
            Boolean previous_condition = null;
            Boolean current_condition = null;
            String temp_column_value, temp_literal_value, temp_operator;
            Column col;
            Float t_c_v, t_l_v;
            for (int i = 0; i < temp1.size(); i++) {
                // move over the current and previous results
                if (i != 0) {
                    previous_condition = current_condition;
                    current_condition = null;
                }

                // get the column
                col = get_column(table_name, temp1.get(i));

                // get the index number of the column
                c = get_column_index(col.column_name);

                // now we get the variables
                temp_column_value = r.listofCells.get(c).getFirstValue().toLowerCase();
                temp_literal_value = temp2.get(i).toLowerCase();
                temp_operator = temp7.get(i);

                // at this point, we have all the variables needed for
                // evaluation
                if (temp6.get(i).column_type.equals("VARCHAR") || temp6.get(i).column_type.equals("CHAR")) {
                    // we're evaluating a string

                    switch (temp_operator) {
                        case "=":
                            current_condition = temp_column_value.equals(temp_literal_value);
                            break;
                        case "<>":
                            current_condition = !temp_column_value.equals(temp_literal_value);
                            break;
                        case "!=":
                            current_condition = !temp_column_value.equals(temp_literal_value);
                            break;
                    }
                } else {
                    // we're evaluating a number of some kind

                    // get the temp column value
                    if (temp_column_value.toUpperCase().equals("NULL"))
                        t_c_v = null;
                    else
                        t_c_v = Float.parseFloat(temp_column_value);
                    // get the temp literal value
                    if (temp_literal_value.toUpperCase().equals("NULL"))
                        t_l_v = null;
                    else
                        t_l_v = Float.parseFloat(temp_literal_value);

                    // here is where we do the actual evaluation
                    if (t_c_v != null) {
                        switch (temp_operator) {
                            case "=":
                                current_condition = t_c_v.equals(t_l_v);
                                break;
                            case "<>":
                            case "!=":
                                current_condition = !t_c_v.equals(t_l_v);

                                break;
                            case ">":
                                current_condition = t_c_v > t_l_v;
                                break;
                            case ">=":
                                current_condition = t_c_v >= t_l_v;
                                break;
                            case "<":
                                current_condition = t_c_v < t_l_v;
                                break;
                            case "<=":
                                current_condition = t_c_v <= t_l_v;
                                break;
                        }
                    } else {
                        // the row value is null
                        switch (temp_operator) {
                            case "=":
                                current_condition = t_l_v == null;
                                break;
                            case "<>":
                                current_condition = t_l_v != null;
                                break;
                            case "!=":
                                current_condition = t_l_v != null;
                                break;
                            default:
                                // else, default NULL for all other operations
                                current_condition = false;
                                break;
                        }
                    }
                }

                // at this point, we've evaluated the current command
                // now we need to evaluate it against the AND/OR command
                // ONLY DO THIS IF we're not in the 0 index
                if (i != 0) {
                    String condition_separator = temp11.get(i - 1);

                    // now depending on the AND/OR, we evaluate
                    if (condition_separator.equals("AND")) {
                        current_condition = current_condition && previous_condition;
                    } else if (condition_separator.equals("OR")) {
                        current_condition = current_condition || previous_condition;
                    }

                }
            }
            // return the condition result
            return current_condition;
        }
    }
    
    private static Record cloneRecord(Record r)
    {
       Record n = new Record(r.record_date, new ArrayList<Cell>());
       if (r.listofCells.size() != 0){
            for (int i = 0; i < r.listofCells.size(); i++) {
               n.listofCells.add(new Cell(r.listofCells.get(i).getFirstValue(), r.listofCells.get(i).getFirstDate()));
            }
        }
        
       return n;
    }

    private static Record execute_aggregate(Record r , Record r2){
       Column col;
       int c;
       for (int i = 0; i < temp9.size(); i++) {
          col = get_column(table_name, temp9.get(i));
          c = get_column_index(col.column_name);
          if(!AggFunc.get(i).equals("NULL")){
             switch(AggFunc.get(i)){
                case "SUM":
                   r.listofCells.get(c).cellTuples.get(0).value = execute_sum_function(
                              r.listofCells.get(c).getFirstValue(), 
                              r2.listofCells.get(c).getFirstValue());
                   break;
                case "COUNT":
                    r.listofCells.get(c).cellTuples.get(0).value = execute_count_function(); 
                   break;
                case "AVG":
                   r.listofCells.set(c, execute_avg_function(
                              r.listofCells.get(c), r2.listofCells.get(c)));
                   break;
                case "MIN":
                    r.listofCells.get(c).cellTuples.get(0).value = execute_min_function(
                            r.listofCells.get(c).getFirstValue(), 
                            r2.listofCells.get(c).getFirstValue());                    
                   break;
                case "MAX":
                    r.listofCells.get(c).cellTuples.get(0).value = execute_max_function(
                            r.listofCells.get(c).getFirstValue(), 
                            r2.listofCells.get(c).getFirstValue());                   
                   break;
             }
          }
       }
       
       return r;
    }
    
    private static String execute_sum_function(String val, String val2){

        if(isInteger(val)){
            return Integer.toString(Integer.parseInt(val) + Integer.parseInt(val2));
        }else{
            return Float.toString(Float.parseFloat(val) + Float.parseFloat(val2));
        }
    }
    
    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }return true;
    }
  
    public static String execute_count_function(){
     	countCounter++;
    	return Integer.toString(countCounter);
    }
    
    
    private static String execute_min_function(String val, String val2){
 
         	String minVal=val;
        	int result=val.compareTo(val2);
        	
        	if(result>=0){
        		minVal=val2;}
         
    		return minVal;   
    }

    private static String execute_max_function(String val, String val2){
    	String maxVal=val;
    	int result=val.compareTo(val2);
    	if(result<=0){
    		maxVal=val2;
    	}
    	
		return maxVal; 	
    }
    
    private static Cell execute_avg_function(Cell val, Cell val2){
       double total = 0;
       int count = 0;
       double avg = 0;
       
       if(val.cellTuples.size() > 1){
          val.cellTuples.add(new CellTuple(val2.getFirstValue(), val2.getFirstDate()));
       }
       else {
          val.cellTuples.add(new CellTuple(val.getFirstValue(), val.getFirstDate()));
          val.cellTuples.add(new CellTuple(val2.getFirstValue(), val2.getFirstDate()));
       }
       
       for(int i = 1; i < val.cellTuples.size(); i++){
          count++;
          if(!isNumeric(val.cellTuples.get(i).value))
          {
            total = total + Integer.parseInt(val.cellTuples.get(i).value);
          } 
          else
          {
            total = total + Double.parseDouble(val.cellTuples.get(i).value);
          }
       }
       avg = total/count;
       
       val.cellTuples.get(0).value = Double.toString(avg);
       
       return val;
    }

    private static void execute_list_tables() {
        // this command will display all tables in the database
        Enumeration<String> e = Database.tables.keys();
        String t;
        @SuppressWarnings("unused")
        int counter = 0;
        ArrayList<String> results = new ArrayList<String>();
        while (e.hasMoreElements()) {
            t = e.nextElement();
            results.add(t);
        }
        System.out.println("");

        if (results.size() == 0)
            // if there were no tables
            System.out.println("There are no tables in your database.");
        else {
            // if there are tables
            System.out.println("tables");
            System.out.println("------");
            for (String a : results) {
                System.out.print(a + "  -> ");
                // loop through every column
                for (int i = 0; i < Database.tables.get(a).columns.size(); i++) {
                    if (i != 0)
                        System.out.print(", ");
                    System.out.print(Database.tables.get(a).columns.get(i).toString());
                    if (Database.tables.get(a).columns.get(i).is_null_allowed.equals("false"))
                        System.out.print(" NOT NULL");
                }
                System.out.println("");
            }
        }
    }

    // display formatting for SELECT command
    private static String display(String it, Column c) {
        String r = it;

        int to_length = 0;
        if (c.restriction_2 == 0)
            to_length = c.restriction;
        else
            to_length = c.restriction + (c.restriction - c.restriction_2);

        if (c.column_type.equals("number"))
            to_length += 2;

        if (c.column_type.toLowerCase().equals("int"))
            to_length += 1;

        // if the name of the column is greater than the length
        // increase the max length
        if (r.length() > to_length)
            to_length += r.length() - to_length;

        // add spaces until it formats nicely
        while (r.length() != to_length)
            r += " ";

        // if the length of the final cell is less than the name of the column,
        // add whitespace
        while (r.length() < c.column_name.length())
            r += " ";

        return r;
    }

    // display formatting for SELECT command
    private static String display_dashes(Column c) {
        String r = "";

        int to_length = 0;
        if (c.restriction_2 == 0)
            to_length = c.restriction;
        else
            to_length = c.restriction + (c.restriction - c.restriction_2);

        if (c.column_type.equals("number"))
            to_length += 2;

        if (c.column_type.toLowerCase().equals("int"))
            to_length += 1;

        // if the name of the column is greater than the length
        // increase the max length
        if (r.length() > to_length)
            to_length += r.length() - to_length;

        // add spaces until it formats nicely
        while (r.length() != to_length)
            r += "-";

        // if the length of the final cell is less than the name of the column,
        // add whitespace
        while (r.length() < c.column_name.length())
            r += "-";

        return r;
    }

    // loads a database file from memory
    private static int loadFromDatabase() {
        File DB = new File(directory_to + Database.temp_database_name);
        int failure = 1;
        Scanner input_ = null;
        Scanner line = null;
        try {
            input_ = new Scanner(DB);
        } catch (FileNotFoundException ignored) {
        }
        ;

        String key = null;
        String s = null;
        // line = new Scanner(s);
        // line.useDelimiter(DELIMITER);
        int numCol = 0;

        // still possibility of another table
        while (input_.hasNextLine()) {
            key = input_.nextLine();
            Database.tables.put(key, new Table());
            numCol = input_.nextInt();
            input_.nextLine();
            ArrayList<Column> tempcolumns = Database.tables.get(key).columns;
            for (int i = 0; i < numCol; i++) {
                s = input_.nextLine();

                line = new Scanner(s);
                line.useDelimiter(DELIMITER);
                tempcolumns.add(new Column(line.next(), line.next(), Integer.parseInt(line.next()),
                        Integer.parseInt(line.next()), line.next()));

                line.close();
            }

            // Create records until a blank line is found
            while (input_.hasNextLine() && !(s = input_.nextLine()).equals("") && !s.equals("\n")) {
                line = new Scanner(s);
                line.useDelimiter(DELIMITER);

                String dateString = line.next();
                DateFormat dFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
                Date date;
                try {
                    date = dFormat.parse(dateString);
                } catch (ParseException ignored) {
                    date = new Date();
                }
                ArrayList<Cell> CellList = new ArrayList<>();
                for (int i = 0; i < numCol; i++) {
//					Each iteration of the for loop will gather the data for each cell (which may consist of a list of updates)
                    Cell inProgressCell = new Cell();
                    while (true) {
                        CellTuple inProgressTuple = new CellTuple();
//						if we're out of tokens, move to the next line
                        if (!line.hasNext()) {
                            break;
                        }
                        String token = line.next();
                        if (token.equals(LISTDELIMITER)) {
                            break;
                        }
                        inProgressTuple.value = token;
                        try {
                            inProgressTuple.date = dFormat.parse(line.next());
                        } catch (ParseException ignored) {
//							this shouldn't ever happen
                        }
                        inProgressCell.cellTuples.add(inProgressTuple);
                    }
                    CellList.add(inProgressCell);
                }

                Record r = new Record(date, CellList);
                Database.tables.get(key).records.add(r);
                line.close();
            }

        }
        input_.close();
        failure = 0;
        return failure;

    }

    // toString
    public String toString() {
        return this.value + " -> " + this.type;
    }
}


