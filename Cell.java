import java.util.Date;
import java.util.LinkedList;

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