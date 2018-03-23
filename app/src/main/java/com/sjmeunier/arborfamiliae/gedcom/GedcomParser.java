package com.sjmeunier.arborfamiliae.gedcom;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.FamilyNote;
import com.sjmeunier.arborfamiliae.database.FamilySource;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.IndividualAlternativeName;
import com.sjmeunier.arborfamiliae.database.IndividualNote;
import com.sjmeunier.arborfamiliae.database.IndividualSource;
import com.sjmeunier.arborfamiliae.database.Note;
import com.sjmeunier.arborfamiliae.database.Place;
import com.sjmeunier.arborfamiliae.database.Source;
import com.sjmeunier.arborfamiliae.database.Tree;
import com.sjmeunier.arborfamiliae.util.FileDetail;
import com.sjmeunier.arborfamiliae.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GedcomParser {

    private AppDatabase database;
    private ContentResolver contentResolver;

    private GedcomRecordEnum currentRecord = GedcomRecordEnum.None;
    private GedcomSubRecordEnum currentSubRecord = GedcomSubRecordEnum.None;
    private GedcomSubSubRecordEnum currentSubSubRecord = GedcomSubSubRecordEnum.None;

    private int treeId;
    private int defaultIndividualId = 0;
    private GedcomHeader gedcomHeader = new GedcomHeader();
    private Individual currentGedcomIndividual;
    private IndividualAlternativeName currentGedcomIndividualAlternativeName;
    private Family currentGedcomFamily;
    private Note currentGedcomNote;
    private Source currentGedcomSource;
    private Place currentPlace = null;
    private Tree tree = null;

    private Map<String, Place> places =  new HashMap<String, Place>();
    private List<Individual> individuals = new ArrayList<Individual>();
    private List<IndividualAlternativeName> individualAlternativeNames = new ArrayList<IndividualAlternativeName>();
    private List<Family> families = new ArrayList<Family>();
    private List<Note> notes = new ArrayList<Note>();
    private List<Source> sources = new ArrayList<Source>();
    private List<IndividualNote> individualNotes = new ArrayList<IndividualNote>();
    private List<IndividualSource> individualSources = new ArrayList<IndividualSource>();
    private List<FamilyChild> familyChildren = new ArrayList<FamilyChild>();
    private List<FamilyNote> familyNotes = new ArrayList<FamilyNote>();
    private List<FamilySource> familySources = new ArrayList<FamilySource>();

    private static final String PARAM_OUT_MSG = "omsg";
    private boolean alreadyFoundName = false;

    public int individualCount = 0;
    public int familyCount = 0;
    public int noteCount = 0;
    public int sourceCount = 0;
    public int individualAlternativeNameCount = 0;

    public long bytesRead = 0;

    private int currentEmbeddedNoteId = 10000000;



    private OnGedcomImportProgressListener onGedcomImportProgressListener;

    public void setOnGedcomImportProgressListener(OnGedcomImportProgressListener onGedcomImportProgressListener) {
        this.onGedcomImportProgressListener = onGedcomImportProgressListener;
    }

    public GedcomParser(AppDatabase database, ContentResolver contentResolver) {
        this.database = database;
        this.contentResolver = contentResolver;
    }

    private int convertStringToInt(String value) {
        value = value.replaceAll("[\\D]", "");
        if (TextUtils.isEmpty(value))
            return -1;
        int val = -1;
        try {
            val = Integer.parseInt(value);
        } catch (Exception e) {
            // Number not in correct format so returning -1
        }
        return val;
    }

    public int parseGedcom(Context context, Uri uri) throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException, ClassCastException {
        FileDetail fileDetail = FileUtils.getFileDetailFromUri(context, uri);

        String fileName = "";
        if (fileDetail.fileName != null) {
            fileName = fileDetail.fileName.substring(fileDetail.fileName.lastIndexOf("/") + 1);
        } else {
            fileName = uri.getLastPathSegment();
        }
        tree = new Tree(fileName, new Date(System.currentTimeMillis()));
        tree.id = (int)database.treeDao().addTree(tree);
        treeId = tree.id;

        Log.d("ARBORFAMILIAE", "tree id :- " + String.valueOf(treeId));
        InputStream inputStream;
        File file = null;
        if (uri.getScheme().equals("file")) {
            file = new File(uri.toString());
            inputStream = new FileInputStream(file);
        } else {
            inputStream = contentResolver.openInputStream(uri);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        String line;
        Log.d("ARBORFAMILIAE", "starting reading");
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            lineCount++;
            bytesRead += line.length();

            while (line.indexOf("  ") > 0)
            {
                line = line.replace("  ", " ");
            }
            String[] lineArray = line.split(" ", 3);
            switch (lineArray[0])
            {
                case "0":
                    ProcessRootLevel(lineArray);
                    break;
                case "1":
                    ProcessLevel1(lineArray);
                    break;
                case "2":
                    ProcessLevel2(lineArray);
                    break;
                case "3":
                    ProcessLevel3(lineArray);
                    break;
                case "4":
                    ProcessLevel4(lineArray);
                    break;
            }
        }
        br.close();

        if (individuals.size() > 0) {
            database.individualDao().addIndividuals(individuals.toArray(new Individual[0]));
            individuals.clear();

            tree.individualCount = individualCount;
        }
        if (individualAlternativeNames.size() > 0) {
            database.individualAlternativeNameDao().addIndividualAlternativeNames(individualAlternativeNames.toArray(new IndividualAlternativeName[0]));
            individualAlternativeNames.clear();
        }
        if (families.size() > 0) {
            database.familyDao().addFamilies(families.toArray(new Family[0]));
            families.clear();
            tree.familyCount = familyCount;
        }
        if (notes.size() > 0) {
            database.noteDao().addNotes(notes.toArray(new Note[0]));
            notes.clear();
            tree.noteCount = noteCount;
        }
        if (sources.size() > 0) {
            database.sourceDao().addSources(sources.toArray(new Source[0]));
            sources.clear();
            tree.sourceCount = sourceCount;
        }
        if (individualNotes.size() > 0) {
            database.individualNoteDao().addIndividualNotes(individualNotes.toArray(new IndividualNote[0]));
            individualNotes.clear();
        }
        if (individualSources.size() > 0) {
            database.individualSourceDao().addIndividualSources(individualSources.toArray(new IndividualSource[0]));
            individualSources.clear();
        }
        if (familyNotes.size() > 0) {
            database.familyNoteDao().addFamilyNotes(familyNotes.toArray(new FamilyNote[0]));
            familyNotes.clear();
        }
        if (familySources.size() > 0) {
            database.familySourceDao().addFamilySources(familySources.toArray(new FamilySource[0]));
            familySources.clear();
        }
        if (familyChildren.size() > 0) {
            database.familyChildDao().addFamilyChildren(familyChildren.toArray(new FamilyChild[0]));
            familySources.clear();
        }

        if (places.size() > 0) {
            Place[] placeArray = new Place[places.size()];
            int i = 0;
            for(Place place : places.values()) {
                placeArray[i] = place;
                i++;
            }
            database.placeDao().addPlaces(placeArray);
            tree.placeCount = places.size();
            places.clear();

        }

        tree.defaultIndividual = defaultIndividualId;
        database.treeDao().updateTree(tree);

        return treeId;
    }

    public void ProcessRootLevel(String[] lineArray) throws NumberFormatException, ArrayIndexOutOfBoundsException, ClassCastException
    {
        if (currentPlace != null) {
            places.put(currentPlace.placeName, currentPlace);
            currentPlace = null;
        }
        alreadyFoundName = false;
        switch (currentRecord)
        {
            case Individual:
                individuals.add(currentGedcomIndividual);
                individualCount++;
                if (individualCount % 1000 == 0) {
                    database.individualDao().addIndividuals(individuals.toArray(new Individual[0]));

                    tree.individualCount = individualCount;
                    database.treeDao().updateTree(tree);
                    individuals.clear();
                }
                break;
            case Family:
                families.add(currentGedcomFamily);
                familyCount++;
                if (familyCount % 1000 == 0) {
                    database.familyDao().addFamilies(families.toArray(new Family[0]));
                    tree.familyCount = familyCount;
                    database.treeDao().updateTree(tree);
                    families.clear();
                }
                break;
            case Note:
                notes.add(currentGedcomNote);
                noteCount++;
                if (noteCount % 1000 == 0) {
                    database.noteDao().addNotes(notes.toArray(new Note[0]));
                    tree.noteCount = noteCount;
                    database.treeDao().updateTree(tree);
                    notes.clear();
                }
                break;
            case Source:
                sources.add(currentGedcomSource);
                sourceCount++;
                if (sourceCount % 1000 == 0) {
                    database.sourceDao().addSources(sources.toArray(new Source[0]));
                    tree.sourceCount = sourceCount;
                    database.treeDao().updateTree(tree);
                    sources.clear();
                }
                break;
        }

        if (currentGedcomIndividualAlternativeName != null) {
            individualAlternativeNames.add(currentGedcomIndividualAlternativeName);
            individualAlternativeNameCount++;
            if (individualAlternativeNameCount % 1000 == 0) {
                database.individualAlternativeNameDao().addIndividualAlternativeNames(individualAlternativeNames.toArray(new IndividualAlternativeName[0]));
                individualAlternativeNames.clear();
            }
            currentGedcomIndividualAlternativeName = null;
        }

        if (lineArray[1].toUpperCase().equals("HEAD"))
        {
            currentRecord = GedcomRecordEnum.Header;
            currentSubRecord = GedcomSubRecordEnum.None;
        } else if (lineArray[1].indexOf("@") >= 0) {
            String val = lineArray[2];
            if (val.length() > 4)
                val = val.substring(0, 4).toUpperCase();
            switch (val)
            {
                case "INDI":
                    int id = convertStringToInt(lineArray[1]);
                    if (id != -1) {
                        currentRecord = GedcomRecordEnum.Individual;
                        currentGedcomIndividual = new Individual(id, treeId);
                        if (defaultIndividualId == 0) {
                            defaultIndividualId = id;
                            tree.defaultIndividual = defaultIndividualId;
                        }
                    }
                    currentSubRecord = GedcomSubRecordEnum.None;

                    break;
                case "FAM":
                    id = convertStringToInt(lineArray[1]);
                    if (id != -1) {
                        currentRecord = GedcomRecordEnum.Family;
                        currentGedcomFamily = new Family(id, treeId);
                        currentSubRecord = GedcomSubRecordEnum.None;
                    }
                    break;
                case "NOTE":
                    id = convertStringToInt(lineArray[1]);
                    if (id != -1) {
                        currentRecord = GedcomRecordEnum.Note;
                        currentGedcomNote = new Note(id, treeId);

                        String[] subLine = lineArray[2].split(" ", 2);
                        if (subLine.length > 1)
                            currentGedcomNote.text = subLine[1];
                        currentSubRecord = GedcomSubRecordEnum.None;
                    }
                    break;
                case "SOUR":
                    id = convertStringToInt(lineArray[1]);
                    if (id != -1) {
                        currentRecord = GedcomRecordEnum.Source;
                        currentGedcomSource = new Source(id, treeId);
                        currentSubRecord = GedcomSubRecordEnum.None;
                    }
                    break;
            }
        }
    }
    public void ProcessLevel1(String[] lineArray) throws NumberFormatException, ArrayIndexOutOfBoundsException, ClassCastException
    {
        switch (currentSubRecord)
        {
            case EmbeddedNote:
                notes.add(currentGedcomNote);
                noteCount++;
                if (noteCount % 1000 == 0) {
                    database.noteDao().addNotes(notes.toArray(new Note[0]));
                    tree.noteCount = noteCount;
                    database.treeDao().updateTree(tree);
                    notes.clear();
                }
                break;
        }


        if (currentRecord == GedcomRecordEnum.Header)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "SOUR":
                    gedcomHeader.Source = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.HeaderSource;
                    break;
                case "DEST":
                    gedcomHeader.Destination = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "DATE":
                    gedcomHeader.Date = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "FILE":
                    gedcomHeader.File = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "CHAR":
                    gedcomHeader.CharacterEncoding = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "GEDC":
                    currentSubRecord = GedcomSubRecordEnum.HeaderGedcom;
                    break;
            }
        }
        else if (currentRecord == GedcomRecordEnum.Individual)
        {
            if (currentSubRecord == GedcomSubRecordEnum.IndividualAlternativeName) {
                if (currentGedcomIndividualAlternativeName != null) {
                    individualAlternativeNames.add(currentGedcomIndividualAlternativeName);
                    individualAlternativeNameCount++;
                    if (individualAlternativeNameCount % 1000 == 0) {
                        database.individualAlternativeNameDao().addIndividualAlternativeNames(individualAlternativeNames.toArray(new IndividualAlternativeName[0]));
                        individualAlternativeNames.clear();
                    }
                    currentGedcomIndividualAlternativeName = null;
                }
            }

            switch (lineArray[1].toUpperCase())
            {
                case "NAME":

                    if (TextUtils.isEmpty(currentGedcomIndividual.givenName) && TextUtils.isEmpty(currentGedcomIndividual.surname)) {
                        currentSubRecord = GedcomSubRecordEnum.IndividualName;
                    } else {
                        currentSubRecord = GedcomSubRecordEnum.IndividualAlternativeName;
                    }

                    if (currentSubRecord == GedcomSubRecordEnum.IndividualName) {
                        if (lineArray.length > 2) {
                            if (lineArray[2].contains("/")) {
                                String[] name = lineArray[2].split("/", 3);
                                currentGedcomIndividual.givenName = name[0].trim();
                                currentGedcomIndividual.surname = name[1].trim();
                            } else {
                                currentGedcomIndividual.givenName = lineArray[2].trim();
                            }
                        }
                    } else {
                        currentGedcomIndividualAlternativeName = new IndividualAlternativeName(currentGedcomIndividual.individualId, treeId);
                        if (lineArray.length > 2) {
                            if (lineArray[2].contains("/")) {
                                String[] name = lineArray[2].split("/", 3);
                                currentGedcomIndividualAlternativeName.givenName = name[0].trim();
                                currentGedcomIndividualAlternativeName.surname = name[1].trim();
                            } else {
                                currentGedcomIndividualAlternativeName.givenName = lineArray[2].trim();
                            }
                        }
                    }
                    break;
                case "SEX":
                    if (lineArray[2].equals("M"))
                        currentGedcomIndividual.gender = GenderEnum.Male;
                    else if (lineArray[2].equals("F"))
                        currentGedcomIndividual.gender = GenderEnum.Female;
                    else
                        currentGedcomIndividual.gender = GenderEnum.Unknown;
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "BIRT":
                    currentSubRecord = GedcomSubRecordEnum.IndividualBirth;
                    break;
                case "DEAT":
                    currentSubRecord = GedcomSubRecordEnum.IndividualDeath;
                    break;
                case "BAPM":
                    currentSubRecord = GedcomSubRecordEnum.IndividualBaptism;
                    break;
                case "BURI":
                    currentSubRecord = GedcomSubRecordEnum.IndividualBurial;
                    break;
                case "FAMC":
                    currentGedcomIndividual.parentFamilyId = convertStringToInt(lineArray[2]);
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "OCCU":
                    currentGedcomIndividual.occupation = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "DSCR":
                    if (lineArray.length > 2)
                        currentGedcomIndividual.description = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "NATI":
                    currentGedcomIndividual.nationality = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "NOTE":
                    int id = -1;

                    if (lineArray.length > 2)
                        id = convertStringToInt(lineArray[2]);
                    if (id != -1) {
                        individualNotes.add(new IndividualNote(treeId, currentGedcomIndividual.individualId, id));
                        currentSubRecord = GedcomSubRecordEnum.None;
                    } else {
                        if (lineArray.length > 2) {
                            currentEmbeddedNoteId++;
                            individualNotes.add(new IndividualNote(treeId, currentGedcomIndividual.individualId, currentEmbeddedNoteId));
                            currentGedcomNote = new Note(currentEmbeddedNoteId, treeId);

                            currentGedcomNote.text = lineArray[2];
                            currentSubRecord = GedcomSubRecordEnum.EmbeddedNote;
                        } else {
                            currentSubRecord = GedcomSubRecordEnum.None;
                        }
                    }

                    break;
                case "SOUR":
                    id = convertStringToInt(lineArray[2]);
                    if (id != -1) {
                        individualSources.add(new IndividualSource(treeId, currentGedcomIndividual.individualId, id));
                    }
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                default:
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
            }
        }
        else if (currentRecord == GedcomRecordEnum.Family)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "HUSB":
                    currentGedcomFamily.husbandId = convertStringToInt(lineArray[2]);
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "WIFE":
                    currentGedcomFamily.wifeId = convertStringToInt(lineArray[2]);
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "CHIL":
                    familyChildren.add(new FamilyChild(treeId, convertStringToInt(lineArray[2]), currentGedcomFamily.familyId));
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "MARR":
                    currentSubRecord = GedcomSubRecordEnum.FamilyMarriage;
                    break;
                case "NOTE":
                    int id = -1;

                    if (lineArray.length > 2)
                        id = convertStringToInt(lineArray[2]);
                    if (id != -1) {
                        familyNotes.add(new FamilyNote(treeId, currentGedcomFamily.familyId, id));
                        currentSubRecord = GedcomSubRecordEnum.None;
                    } else {
                        if (lineArray.length > 2) {
                            currentEmbeddedNoteId++;
                            familyNotes.add(new FamilyNote(treeId, currentGedcomFamily.familyId, currentEmbeddedNoteId));
                            currentGedcomNote = new Note(currentEmbeddedNoteId, treeId);

                            currentGedcomNote.text = lineArray[2];
                            currentSubRecord = GedcomSubRecordEnum.EmbeddedNote;
                        } else {
                            currentSubRecord = GedcomSubRecordEnum.None;
                        }
                    }

                    break;
                case "SOUR":
                    id = convertStringToInt(lineArray[2]);
                    if (id != -1) {
                        familySources.add(new FamilySource(treeId, currentGedcomFamily.familyId, id));
                    }
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                default:
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
            }
        }
        else if (currentRecord == GedcomRecordEnum.Note)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "CONC":
                    if (lineArray.length > 2 && !TextUtils.isEmpty(lineArray[2]))
                        currentGedcomNote.text += lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "CONT":
                    if (lineArray.length > 2 && !TextUtils.isEmpty(lineArray[2]))
                        currentGedcomNote.text += "\r\n" + lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                default:
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
            }
        }
        else if (currentRecord == GedcomRecordEnum.Source)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "TITL":
                    currentGedcomSource.text = lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "CONT":
                    currentGedcomSource.text += lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                case "CONC":
                    currentGedcomSource.text += lineArray[2];
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
                default:
                    currentSubRecord = GedcomSubRecordEnum.None;
                    break;
            }
        }
    }

    public void ProcessLevel2(String[] lineArray) throws NumberFormatException, ArrayIndexOutOfBoundsException, ClassCastException
    {
        if (currentSubRecord == GedcomSubRecordEnum.HeaderSource)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "VERS":
                    gedcomHeader.SourceVersion = lineArray[2];
                    break;
                case "NAME":
                    gedcomHeader.SourceName = lineArray[2];
                    break;
                case "CORP":
                    gedcomHeader.SourceCorporation = lineArray[2];
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.HeaderGedcom)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "VERS":
                    gedcomHeader.GedcomVersion = lineArray[2];
                    break;
                case "FORM":
                    gedcomHeader.GedcomForm = lineArray[2];
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualName)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "GIVN":
                    currentGedcomIndividual.givenName = lineArray[2];
                    break;
                case "SURN":
                    currentGedcomIndividual.surname = lineArray[2];
                    break;
                case "NSFX":
                    currentGedcomIndividual.suffix = lineArray[2];
                    break;
                case "SPFX":
                   currentGedcomIndividual.prefix = lineArray[2];
                    break;
                case "_AKA":
                    currentGedcomIndividual.suffix = lineArray[2];
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualAlternativeName)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "GIVN":
                    currentGedcomIndividualAlternativeName.givenName = lineArray[2];
                    break;
                case "SURN":
                    currentGedcomIndividualAlternativeName.surname = lineArray[2];
                    break;
                case "NSFX":
                    currentGedcomIndividualAlternativeName.suffix = lineArray[2];
                    break;
                case "SPFX":
                    currentGedcomIndividualAlternativeName.prefix = lineArray[2];
                    break;
                case "_AKA":
                    currentGedcomIndividualAlternativeName.suffix = lineArray[2];
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualBirth)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "DATE":
                    currentGedcomIndividual.birthDate = lineArray[2];
                    break;
                case "PLAC":
                    if (currentPlace != null) {
                        places.put(currentPlace.placeName, currentPlace);
                        currentPlace = null;
                    }
                    if (!TextUtils.isEmpty(lineArray[2])) {
                        if (places.containsKey(lineArray[2])) {
                            currentGedcomIndividual.birthPlace = places.get(lineArray[2]).placeId;
                        } else {
                            currentPlace = new Place(treeId, places.size() + 1);
                            currentPlace.placeName = lineArray[2];
                            currentGedcomIndividual.birthPlace = currentPlace.placeId;
                        }
                    } else {
                        currentGedcomIndividual.birthPlace = -1;
                    }
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualDeath)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "DATE":
                    currentGedcomIndividual.diedDate = lineArray[2];
                    break;
                case "PLAC":
                    if (currentPlace != null) {
                        places.put(currentPlace.placeName, currentPlace);
                        currentPlace = null;
                    }
                    if (!TextUtils.isEmpty(lineArray[2])) {
                        if (places.containsKey(lineArray[2])) {
                            currentGedcomIndividual.diedPlace = places.get(lineArray[2]).placeId;
                        } else {
                            currentPlace = new Place(treeId, places.size() + 1);
                            currentPlace.placeName = lineArray[2];
                            currentGedcomIndividual.diedPlace = currentPlace.placeId;
                        }
                    } else {
                        currentGedcomIndividual.diedPlace = -1;
                    }
                    break;
                case "CAUS":
                    currentGedcomIndividual.diedCause = lineArray[2];
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualBaptism)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "DATE":
                    currentGedcomIndividual.baptismDate = lineArray[2];
                    break;
                case "PLAC":
                    if (currentPlace != null) {
                        places.put(currentPlace.placeName, currentPlace);
                        currentPlace = null;
                    }
                    if (!TextUtils.isEmpty(lineArray[2])) {
                        if (places.containsKey(lineArray[2])) {
                            currentGedcomIndividual.baptismPlace = places.get(lineArray[2]).placeId;
                        } else {
                            currentPlace = new Place(treeId, places.size() + 1);
                            currentPlace.placeName = lineArray[2];
                            currentGedcomIndividual.baptismPlace = currentPlace.placeId;
                        }
                    } else {
                        currentGedcomIndividual.baptismPlace = -1;
                    }
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualBurial)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "DATE":
                    currentGedcomIndividual.burialDate = lineArray[2];
                    break;
                case "PLAC":
                    if (currentPlace != null) {
                        places.put(currentPlace.placeName, currentPlace);
                        currentPlace = null;
                    }
                    if (!TextUtils.isEmpty(lineArray[2])) {
                        if (places.containsKey(lineArray[2])) {
                            currentGedcomIndividual.burialPlace = places.get(lineArray[2]).placeId;
                        } else {
                            currentPlace = new Place(treeId, places.size() + 1);
                            currentPlace.placeName = lineArray[2];
                            currentGedcomIndividual.burialPlace = currentPlace.placeId;
                        }
                    } else {
                        currentGedcomIndividual.burialPlace = -1;
                    }
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.FamilyMarriage)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "DATE":
                    currentGedcomFamily.marriageDate = lineArray[2];
                    break;
                case "PLAC":
                    if (currentPlace != null) {
                        places.put(currentPlace.placeName, currentPlace);
                        currentPlace = null;
                    }
                    if (!TextUtils.isEmpty(lineArray[2])) {
                        if (places.containsKey(lineArray[2])) {
                            currentGedcomFamily.marriagePlace = places.get(lineArray[2]).placeId;
                        } else {
                            currentPlace = new Place(treeId, places.size() + 1);
                            currentPlace.placeName = lineArray[2];
                            currentGedcomFamily.marriagePlace = currentPlace.placeId;
                        }
                    } else {
                        currentGedcomFamily.marriagePlace = -1;
                    }
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.EmbeddedNote)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "CONC":
                    if (lineArray.length > 2 && !TextUtils.isEmpty(lineArray[2]))
                        currentGedcomNote.text += lineArray[2];
                    break;
                case "CONT":
                    if (lineArray.length > 2 && !TextUtils.isEmpty(lineArray[2]))
                        currentGedcomNote.text += "\r\n" + lineArray[2];
                    break;
                default:
                    break;
            }
        }
    }

    public void ProcessLevel3(String[] lineArray) throws NumberFormatException, ArrayIndexOutOfBoundsException, ClassCastException
    {
        if (currentSubRecord == GedcomSubRecordEnum.IndividualBirth)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "MAP":
                    currentSubSubRecord = GedcomSubSubRecordEnum.IndividualBirthMap;
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualDeath)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "MAP":
                    currentSubSubRecord = GedcomSubSubRecordEnum.IndividualDeathMap;
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualBaptism)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "MAP":
                    currentSubSubRecord = GedcomSubSubRecordEnum.IndividualBaptismMap;
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.IndividualBurial)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "MAP":
                    currentSubSubRecord = GedcomSubSubRecordEnum.IndividualBurialMap;
                    break;
            }
        }
        else if (currentSubRecord == GedcomSubRecordEnum.FamilyMarriage)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "MAP":
                    currentSubSubRecord = GedcomSubSubRecordEnum.FamilyMarriageMap;
                    break;
            }
        }
    }

    public void ProcessLevel4(String[] lineArray) throws NumberFormatException, ArrayIndexOutOfBoundsException
    {
        if (currentSubSubRecord == GedcomSubSubRecordEnum.IndividualBirthMap)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "LATI":
                    if (currentPlace != null) {
                        currentPlace.latitude = convertLatitudeToFloat(lineArray[2]);
                    }
                    break;
                case "LONG":
                    if (currentPlace != null) {
                        currentPlace.longitude = convertLongitudeToFloat(lineArray[2]);
                    }
                    break;
            }
        }
        else if (currentSubSubRecord == GedcomSubSubRecordEnum.IndividualDeathMap)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "LATI":
                    if (currentPlace != null) {
                        currentPlace.latitude = convertLatitudeToFloat(lineArray[2]);
                    }
                    break;
                case "LONG":
                    if (currentPlace != null) {
                        currentPlace.longitude = convertLongitudeToFloat(lineArray[2]);
                    }
                    break;
            }
        }
        else if (currentSubSubRecord == GedcomSubSubRecordEnum.IndividualBaptismMap)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "LATI":
                    if (currentPlace != null) {
                        currentPlace.latitude = convertLatitudeToFloat(lineArray[2]);
                    }
                    break;
                case "LONG":
                    if (currentPlace != null) {
                        currentPlace.longitude = convertLongitudeToFloat(lineArray[2]);
                    }
                    break;
            }
        }
        else if (currentSubSubRecord == GedcomSubSubRecordEnum.IndividualBurialMap)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "LATI":
                    if (currentPlace != null) {
                        currentPlace.latitude = convertLatitudeToFloat(lineArray[2]);
                    }
                    break;
                case "LONG":
                    if (currentPlace != null) {
                        currentPlace.longitude = convertLongitudeToFloat(lineArray[2]);
                    }
                    break;
            }
        }
        else if (currentSubSubRecord == GedcomSubSubRecordEnum.FamilyMarriageMap)
        {
            switch (lineArray[1].toUpperCase())
            {
                case "LATI":
                    if (currentPlace != null) {
                        currentPlace.latitude = convertLatitudeToFloat(lineArray[2]);
                    }
                    break;
                case "LONG":
                    if (currentPlace != null) {
                        currentPlace.longitude = convertLongitudeToFloat(lineArray[2]);
                    }
                    break;
            }
        }
    }

    private float convertLatitudeToFloat(String latitude) {
        float numericLatitude = -9999;
        if (TextUtils.isEmpty(latitude))
            return numericLatitude;
        numericLatitude = Float.parseFloat(latitude.substring(1));
        if (latitude.substring(0, 1).equals("S"))
            numericLatitude *= -1;

        return numericLatitude;
    }

    private float convertLongitudeToFloat(String longitude) {
        float numericLongitude = -9999;
        if (TextUtils.isEmpty(longitude))
            return numericLongitude;
        numericLongitude = Float.parseFloat(longitude.substring(1));
        if (longitude.substring(0, 1).equals("W"))
            numericLongitude *= -1;

        return numericLongitude;
    }
}
