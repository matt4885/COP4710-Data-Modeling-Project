# define as java make file
.SUFFIXES: .java .class

# compile
.java.class:
	javac -g $*.java

# include sources
CLASSES = \
	Project.java \
	Cell.java \
	CellTuple.java \
	Column.java \
	Database.java \
	Record.java \
	Table.java

default: classes

# rename outputs
classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class