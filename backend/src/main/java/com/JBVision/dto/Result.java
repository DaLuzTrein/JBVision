package com.JBVision.dto;

import java.util.List;

public class Result {

    private Box box;
    private List<Subject> subjects;

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }
}