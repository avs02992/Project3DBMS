 /*****************************************************************************************
 * @file  TestTupleGenerator.java
 *
 * @author Arjun V. Sivanesan
 */

import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*****************************************************************************************
 * This class tests the TupleGenerator on the Student Registration Database defined in the
 * Kifer, Bernstein and Lewis 2006 database textbook (see figure 3.6).  The primary keys
 * (see figure 3.6) and foreign keys (see example 3.2.2) are as given in the textbook.
 */
public class TestTupleGenerator {
    /*************************************************************************************
     * The main method is the driver for TestGenerator.
     * @param args the command-line arguments
     */

    public static void main(String [] args) {

        //create a new TupleGeneratorImpl instance
        var test = new TupleGeneratorImpl();

        //add relational schemas for Student, Professor, Course, Teaching and Transcript
        test.addRelSchema("Student",
                           "id name address status",
                           "Integer String String String",
                           "id",
                           null);
        
        test.addRelSchema("Professor",
                           "id name deptId",
                           "Integer String String",
                           "id",
                           null);
        
        test.addRelSchema("Course",
                           "crsCode deptId crsName descr",
                           "String String String String",
                           "crsCode",
                           null);
        
        test.addRelSchema("Teaching",
                           "crsCode semester profId",
                           "String String Integer",
                           "crcCode semester",
                           new String[][] {{ "profId", "Professor", "id" },
                                            { "crsCode", "Course", "crsCode" }});
        
        test.addRelSchema("Transcript",
                           "studId crsCode semester grade",
                           "Integer String String String",
                           "studId crsCode semester",
                           new String[][] {{ "studId", "Student", "id"},
                                            { "crsCode", "Course", "crsCode" },
                                            { "crsCode semester", "Teaching", "crsCode semester" }});

        //define table names and corresponding tuple counts
        var tables = new String[]{"Student", "Professor", "Course", "Teaching", "Transcript"};
        var tups = new int[]{10000, 1000, 2000, 50000, 5000};
    
        //generate tuples for the specified tables
        var resultTest = test.generate(tups);
        
        //iterate over each table in the generated result
        for(var i = 0; i < resultTest.length; i++) {

            //print the name of the current table
            out.println(tables[i]);

            //iterate over each attribute value in the current tuple
            for(var j = 0; j < resultTest [i].length; j++) {

                //print the current attribute vaue followed by a comma
                for(var k = 0; k < resultTest [i][j].length; k++) {
                    out.print (resultTest[i][j][k] + ",");
                } //for

                out.println();

            } //for

            out.println();
        } //for

        List<Table> tableList = new ArrayList<>();


    } //main

} //TestTupleGenerator

