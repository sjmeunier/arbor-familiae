package com.sjmeunier.arborfamiliae;

import android.text.TextUtils;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class AncestryUtil {
    public static String calculateRelationship(int generations, boolean isMale)
    {
        if (generations == 0)
            return "";

        String relationship = "";
        if (isMale)
        {
            if (generations == 1)
                relationship = "Father";
            else if (generations == 2)
                relationship = "Grandfather";
            else if (generations == 3)
                relationship = "Great-grandfather";
            else
                relationship = "Great(" + String.valueOf(generations - 2) + "-grandfather";
        }
        else
        {
            if (generations == 1)
                relationship = "Mother";
            else if (generations == 2)
                relationship = "Grandmother";
            else if (generations == 3)
                relationship = "Great-grandmother";
            else
                relationship = "Great(" + String.valueOf(generations - 2) + "-grandmother";
        }
        return relationship;
    }

    public static int convertStringToInt(String value) {
        return Integer.parseInt(value.replaceAll("[\\D]", ""));
    }

    public static String processDate(String date, boolean onlyYear)
    {
        if (TextUtils.isEmpty(date))
        {
            date = "?";
        }
        else
        {
            if (onlyYear)
            {
                String[] dateArr = date.split(" ");
                if (dateArr.length > 1)
                {
                    date = "";
                    if (dateArr[0] == "ABT")
                        date = "c";
                    else if (dateArr[0] == "AFT")
                        date = ">";
                    else if (dateArr[0] == "BEF")
                        date = "<";
                    date += dateArr[dateArr.length - 1];

                    int year = 0;
                    year = convertStringToInt(dateArr[dateArr.length - 1]);
                    return String.valueOf(year);
                }
            }
            else
            {
                if (date.contains("ABT"))
                    date = date.replace("ABT", "c");
                else if (date.contains("AFT"))
                    date = date.replace("AFT", ">");
                else if (date.contains("BEF"))
                    date = date.replace("BEF", "<");

                date = date.replace("JAN", "Jan").replace("FEB", "Feb").replace("MAR", "Mar").replace("APR", "Apr").replace("MAY", "May").replace("JUN", "Jun")
                        .replace("JUL", "Jul").replace("AUG", "Aug").replace("SEP", "Sep").replace("OCT", "Oct").replace("NOV", "Nov").replace("DEC", "Dec");
            }
        }

        return date;
    }

    public static String generateBirthDeathDate(Individual individual, boolean onlyYear)
    {
        String born = AncestryUtil.processDate(individual.birthDate, onlyYear);
        String died = AncestryUtil.processDate(individual.diedDate, onlyYear);
        if (born != "?" || died != "?")
        {
            if (born == "?")
                return "(d." + died + ")";
            else if (died == "?")
                return "(b." + born + ")";
            else
                return "(b." + born + ", d." + died + ")";
        }
        return "";
    }

    public static String generateName(Individual individual, NameFormat nameFormat)
    {
        if (individual == null)
            return "";

        String name = "";
        if (nameFormat == NameFormat.SurnameFirstnameSuffix || nameFormat == NameFormat.SurnameFirstname) {
            name = individual.prefix + " " + individual.surname;
            if (!TextUtils.isEmpty(individual.givenName))
                name += ", " + individual.givenName;
        } else if (nameFormat == NameFormat.SURNAMEFirstnameSuffix || nameFormat == NameFormat.SURNAMEFirstname) {
            name = individual.prefix + " " + individual.surname.toUpperCase();
            if (!TextUtils.isEmpty(individual.givenName))
                name += ", " + individual.givenName;

        } else if (nameFormat == NameFormat.FirstnameSurnameSuffix || nameFormat == NameFormat.FirstnameSurname) {
            name = individual.givenName;
            if (!TextUtils.isEmpty(individual.surname))
                name += " " + individual.prefix + " " + individual.surname;
        } else  {
            name = individual.givenName;
            if (!TextUtils.isEmpty(individual.surname))
                name += " " + individual.prefix + " " +  individual.surname.toUpperCase();
        }
        if (nameFormat == NameFormat.FirstnameSurnameSuffix || nameFormat == NameFormat.SurnameFirstnameSuffix || nameFormat == NameFormat.FirstnameSURNAMESuffix || nameFormat == NameFormat.SURNAMEFirstnameSuffix) {
            if (!TextUtils.isEmpty(individual.suffix))
                name += " (" + individual.suffix + ")";
        }

        return name.trim();
    }

    public static String generateShortName(Individual individual, boolean surnameFirst)
    {
        if (individual == null)
            return "";

        String name = "";
        if (surnameFirst) {
            name = individual.surname;
            if (!TextUtils.isEmpty(individual.givenName))
                name += ", " + individual.givenName;
        } else {
            name = individual.givenName;
            if (!TextUtils.isEmpty(individual.surname))
                name += " " + individual.surname;
        }

        return name;
    }


    public static String generateBoldNameWithDates(Individual individual, NameFormat nameFormat)
    {
        if (individual == null)
            return "";

        String name = "<b>" + generateName(individual, nameFormat);

        name += "</b> " + generateBirthDeathDate(individual, true);
        name = name.trim();
        return name;
    }

    public static String getMarriageLine(Individual spouse, Family family, NameFormat nameFormat, Map<Integer, Place> places)
    {
        String result = "x <b>" + generateName(spouse, nameFormat);
        result += "</b> " + generateBirthDeathDate(spouse, true);
        String marriageDate = getMarriageDateAndPlace(family, places);
        if (!TextUtils.isEmpty(marriageDate))
            result += " m. " + marriageDate;
        return result;
    }

    public static String getChildLine(Individual child, NameFormat nameFormat)
    {
        String result = "&nbsp;&nbsp;- <b>" + generateName(child, nameFormat);
        result += "</b> " + generateBirthDeathDate(child, true);
        return result;
    }

    public static String getMarriageDateAndPlace(Family family, Map<Integer, Place> places)
    {
        String result = AncestryUtil.processDate(family.marriageDate, false);
        if (result == null || result.equals("?"))
            result = "";
        if (family.marriagePlace > -1) {
            if (!result.equals(""))
                result += ", ";
            result += places.get(family.marriagePlace).placeName;
        }
        return result;
    }


    public static String getDeathDateAndPlace(Individual individual, Map<Integer, Place> places)
    {
        String result = AncestryUtil.processDate(individual.diedDate, false);
        if (result == null || result.equals("?"))
            result = "";
        if (individual.diedPlace > -1) {
            if (!result.equals(""))
                result += ", ";
            result += places.get(individual.diedPlace).placeName;
        }
        return result;
    }

    public static String getBaptismDateAndPlace(Individual individual, Map<Integer, Place> places)
    {
        String result = AncestryUtil.processDate(individual.baptismDate, false);
        if (result == null || result.equals("?"))
            result = "";
        if (individual.baptismPlace > -1) {
            if (!result.equals(""))
                result += ", ";
            result += places.get(individual.baptismPlace).placeName;
        }
        return result;
    }

    public static String getBurialDateAndPlace(Individual individual, Map<Integer, Place> places)
    {
        String result = AncestryUtil.processDate(individual.burialDate, false);
        if (result == null || result.equals("?"))
            result = "";
        if (individual.burialPlace > -1) {
            if (!result.equals(""))
                result += ", ";
            result += places.get(individual.burialPlace).placeName;
        }
        return result;
    }

    public static String getBirthDateAndPlace(Individual individual, Map<Integer, Place> places)
    {
        String result = AncestryUtil.processDate(individual.birthDate, false);
        if (result == null || result.equals("?"))
            result = "";
        if (individual.birthPlace > -1) {
            if (!result.equals(""))
                result += ", ";
            result += places.get(individual.birthPlace).placeName;
        }
        return result;
    }

    public static int getGenerationNumberFromAhnenNumber(int ahnenNumber) {
        return (int)Math.floor(Math.log(ahnenNumber) / Math.log(2));
    }

    public static int getChildAhnenNumber(int number) {
        if (number % 2 == 1)
            number--;
        return number / 2;
    }

    public static int getHighestAhnenNumberForGeneration(int generation) {
        return (int)Math.pow(2, generation + 1) - 1;
    }
}
