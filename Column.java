class Column {
    String column_name;
    String column_type;
    int restriction;
    Integer restriction_2 = null;
    String is_null_allowed = "true";

    // column constructor with decimal restriction
    Column(String name, String type, int restriction, int restriction_2, String is_null_allowed) {
        this.column_name = name;
        this.column_type = type;
        this.restriction = restriction;
        this.restriction_2 = restriction_2;
        this.is_null_allowed = is_null_allowed;
    }

    // toString
    public String toString() {
        if (this.restriction_2 == 0)
            return this.column_name + " " + this.column_type + " (" + this.restriction + ")";
        else
            return this.column_name + " " + this.column_type + " (" + this.restriction + "," + this.restriction_2 + ")";
    } //tostring
}