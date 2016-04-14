import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Record {
    Date record_date;
    List<Cell> listofCells = new ArrayList<>();

    // record constructor
    Record() {
        this.record_date = (new Date());
    } //Record Constructor

    // constructor for loading database records
    Record(Date date, ArrayList<Cell> tuples) {
        record_date = date;
        listofCells = tuples;
    } //Record constructor

    // toString
    public String toString() {
        if (this.listofCells.size() == 0)
            return "";
        else {
            // enumerate through all items of the record, display them
            String out = "";
            for (int i = 0; i < this.listofCells.size(); i++) {
                if (i != 0)
                    out += ", ";
                out += this.listofCells.get(i);
            }
            return out;
        } //else
    }
}