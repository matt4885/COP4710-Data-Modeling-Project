import java.util.Date;
import java.util.LinkedList;

//Cell class
//This cell class was extended to support a Date() object 
//This date object allows for WUPDATE/WSELECT implemetation
//In addition, this class implements a Cell tuple via way 
//of LinkedList. 
class Cell  {
    LinkedList<CellTuple> cellTuples = new LinkedList<>();

    Cell() {

    }

    Cell(String s) {
        cellTuples.add(new CellTuple(s, new Date()));
    }

    //    A constructor with a date parameter, when loading from the database
    Cell(String s, Date d) {
        cellTuples.add(new CellTuple(s, d));
    }

    Date getFirstDate() {
        return (cellTuples.size() > 0) ? cellTuples.get(0).date : new Date();
    }

    String getFirstValue() {
        return (cellTuples.size() > 0) ? cellTuples.get(0).value : "";
    }
}
