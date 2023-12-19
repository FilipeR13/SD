import java.util.Comparator;

public class ProgramRequestComparator implements Comparator<ProgramRequest> {
    @Override
    public int compare(ProgramRequest o1, ProgramRequest o2) {

        // the lower the priority, the quicker it is to be executed and if the priority is the same, the one that occupies less memory is executed first
        // everytime a new program request is added to the queue, it is sorted by priority
        // the priority is calculated by the formula: priority = memory + file.length
        // everytime a program is executed, the priority of every program request in the queue is increased by the memory of the program that was executed
        
        if (o1.getPriority() < o2.getPriority()) {
            return -1;
        } else if (o1.getPriority() > o2.getPriority()) {
            return 1;
        } else {
            if (o1.getMemory() < o2.getMemory()) {
                return -1;
            } else if (o1.getMemory() > o2.getMemory()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
