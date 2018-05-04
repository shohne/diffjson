package br.com.kepler.diffjson;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class EditDistanceResult {

    private double distance;
    private Object editSequence;
    private Object topAlignmentRow;
    private Object bottomAlignmentRow;

    EditDistanceResult(double distance, Object editSequence, Object topAlignmentRow, Object bottomAlignmentRow) {
        this.distance           = distance;
        this.editSequence       = editSequence;
        this.topAlignmentRow    = topAlignmentRow;
        this.bottomAlignmentRow = bottomAlignmentRow;
    }

    public double getDistance() {
        return distance;
    }

    public Object getEditSequence() {
        return editSequence;
    }

    public Object getTopAlignmentRow() {
        return topAlignmentRow;
    }

    public Object getBottomAlignmentRow() {
        return bottomAlignmentRow;
    }
}