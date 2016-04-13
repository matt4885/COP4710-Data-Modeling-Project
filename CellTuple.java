import java.util.Date;

class CellTuple{
    String value;
    Date date;

    CellTuple() {

    }

    CellTuple(String s) {
        value = s;
    }

    CellTuple(String s, Date d) {
        value = s;
        date = d;
    }
}