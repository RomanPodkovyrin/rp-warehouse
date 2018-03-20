package rp.warehouse.pc.data.robot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rp.warehouse.pc.data.Task;
import rp.warehouse.pc.input.Job;
import rp.warehouse.pc.input.Jobs;
import rp.warehouse.pc.management.providers.main.WarehouseInfoListener;

/**
 * Used to keep record of the points earned
 * @author roman
 *
 */
public class RewardCounter {
    
    private static final Map<String, Job> jobReference = new HashMap<>();
    private static final Map<String, Integer> uncompletedJobReference = new HashMap<>();
    private static final Map<String, Boolean> cancelledJobReference = new HashMap<>();
    private static final Map<String, Boolean> completedJobReference = new HashMap<>();
    private static final List<WarehouseInfoListener> listeners = new ArrayList<>();

    private static float pointsEarned = 0.0f;
    
    public static void setJobs(Jobs jobs) {
        ArrayList<Job> jobList =jobs.getJobs();
        for (Job job : jobList) {
           jobReference.put(job.getName(), job);
        }
    }
    
    private static int getJobsDone() {
        return completedJobReference.size();
    }
    
    private static int getnumberJobsCancelled() {
        return cancelledJobReference.size();
    }
    
    public static float getPointsEarned() {
        return pointsEarned;
    }
    
    public synchronized static void addCancelledJob(Task task) {
        cancelledJobReference.put(task.getJobID(), true);
    }
    
    public static boolean checkIfCancelled(Task task) {
        
        return cancelledJobReference.containsKey(task.getJobID());
    }

    public synchronized static void addReward(float reward) {
        pointsEarned += reward;
        for (WarehouseInfoListener listener : listeners) {
            listener.rewardChanged(getPointsEarned());
        }
    }

    public synchronized static void addCompletedJob(Task task) {
        String jobId= task.getJobID();
        if (!checkIfCancelled(task) && jobReference.containsKey(jobId)) {
            if(uncompletedJobReference.containsKey(jobId)) {
                if(jobReference.get(jobId).numOfTasks() > uncompletedJobReference.get(jobId)) {
                    uncompletedJobReference.put(jobId, uncompletedJobReference.get(jobId)+1);
                } 
                if (jobReference.get(jobId).numOfTasks() == uncompletedJobReference.get(jobId)) {
                    uncompletedJobReference.remove(jobId);
                    completedJobReference.put(jobId, true);
                    Job rewardToClaim = jobReference.get(jobId);
                    ArrayList<Task> tasks = rewardToClaim.getItems();
                    for (Task taskElement: tasks) {
                        addReward(taskElement.getItem().getReward());
                    }
                }
            }else {
                uncompletedJobReference.put(jobId, 1);
            }
            
        }

        for (WarehouseInfoListener listener : listeners) {
            listener.jobCountChanged(getJobsDone());
            listener.cancelledJobsChanged(getnumberJobsCancelled());
        }

    }

    public static void addListener(WarehouseInfoListener listener) {
        listeners.add(listener);
    }

    @Override
    public String toString() {
        return "";
    }
}
