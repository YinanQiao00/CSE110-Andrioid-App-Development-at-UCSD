package edu.ucsd.cse110.zooseeker.component;

import java.util.Comparator;

public class ExhibitComparator implements Comparator<PlanCard> {

    @Override
    public int compare(PlanCard firstExhibit, PlanCard secondExhibit) {
        double firstDistance = firstExhibit.getDistance();
        double secondDistance = secondExhibit.getDistance();
        if(firstDistance == secondDistance) {
            return 0;
        }
        else if (firstDistance > secondDistance) {
            return 1;
        }
        return -1;
    }
}
