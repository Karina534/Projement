package org.project.projemento.domain.enums;

public enum BoardColumnType {
    BACKLOG("Backlog", 0),
    TODO("To Do", 1),
    IN_PROGRESS("In Progress", 2),
    REVIEW("Review", 3),
    DONE("Done", 4);

    private final String title;
    private final int position;

    BoardColumnType(String title, int position) {
        this.title = title;
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }
}
