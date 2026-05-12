package net.limaru.stayontrail.graphutils;

import androidx.core.util.Pair;
import net.limaru.stayontrail.Task;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Graph helper for the app.
 * There should only be one graph at any point in time.
 * This graph determines the order in which tasks are shown to the user in the homepage.
 */
public class Graph {

  private final Map<Long, Task> taskMap = new HashMap<>();
  private final Map<Long, long[]> adjList = new HashMap<>();
  private final Map<Long, int[]> depthWeight = new HashMap<>();

  /**
   * Tasks MUST be ID'd before calling this operation.
   * @param tasks Tasks to insert into the Graph
   */
  public Graph (List<Task> tasks) {
    for (Task task : tasks) {
      long id = task.getId();
      taskMap.put(id, task);
      long[] preRequisites = task.getPrerequisites();
      adjList.put(id, preRequisites);
    }
  }

  /**
   * Suggest tasks to the user
   * @param todayTasks Tasks with today's date
   * @param timeLimit Time limit of 8 hours
   * @return Tasks the user should perform, in order
   */
  public List<Task> suggestTasks (List<Task> todayTasks, long timeLimit) {
    // sort by weight, get dependencies
    for (Task task : todayTasks) {
      Pair<Map<Long, Integer>, Integer> depthsWeight = getSubGraphDepthsWeight(task.getId());
      Map<Long, Integer> depths = depthsWeight.first;
      int weight = depthsWeight.second;
      depths.forEach((Long id, Integer depth) -> {
        this.depthWeight.putIfAbsent(id, new int[]{-1, -1});
        int[] depthWeightForId = this.depthWeight.get(id);
        if (depthWeightForId != null) {
          depthWeightForId[0] = Math.max(depthWeightForId[0], depth);  //depth
          depthWeightForId[1] = Math.max(depthWeightForId[1], weight); // weight
          this.depthWeight.put(id, depthWeightForId);
        }
      });
    }
    // Rank by weight, then by depth
    List<Long> sortedIds = depthWeight.keySet().stream().sorted(
        (id1, id2) -> {
          int[] dw1 = depthWeight.getOrDefault(id1, new int[]{-1,-1});
          int[] dw2 = depthWeight.getOrDefault(id2, new int[]{-1,-1});
          int d1 = dw1[0];
          int d2 = dw2[0];
          int w1 = dw1[1];
          int w2 = dw2[1];
          int weightCompare = Integer.compare(w1, w2);
          if (weightCompare != 0) {return weightCompare;}
          else {return Integer.compare(d1, d2);}
        }
    ).collect(Collectors.toList());

    // Get top tasks within <timeLimit>
    ArrayList<Task> result = new ArrayList<>();
    long usedTime = 0;
    for (int i = sortedIds.size() - 1; i >= 0; i--) {
      Task task = taskMap.get(sortedIds.get(i));
      usedTime += task.getDuration();
      if (usedTime > timeLimit)
        break;
      result.add(task);
    }
    return result;
  }

  /**
   * Helper function to get the weight of the subgraph and vertices' depths
   * The weight of the subgraph is the sum of weights (=priority) of all vertices.
   * Depths are calculated using BFS through Kahn's Algorithm (since we don't want to involve disjoint graphs)
   * BFS algorithm modified from CLRS
   * @param id ID of the task (subgraph's head vertex)
   * @return Total weight of subgraph
   */
  public Pair<Map<Long, Integer>, Integer> getSubGraphDepthsWeight (long id) {
    int weights = 0;
    // Use BFS of adjacency list
    List<Long> visited = new LinkedList<>();
    Queue<Long> q = new LinkedList<>();
    visited.add(id);
    q.add(id);
    q.add(null);
    int depth = 0;
    Map<Long, Integer> depths = new HashMap<>();

    // Get depths
    while (!q.isEmpty()) {
      Long currentId = q.poll();
      if (currentId == null) {
        depth++;
        q.add(null);
        if(q.peek() == null) break;  // Two consecutive `nulls` = all nodes visited
        else continue;
      }

      if (this.taskMap.get(currentId) == null) continue;
      weights += this.taskMap.get(currentId).getPriority().weight;
      depths.put(currentId, depth);
      long[] edges = this.adjList.get(currentId);
      if (edges != null)
        for (long x : edges) {
          if (!visited.contains(x)) {
            visited.add(x);
            q.add(x);
          }
        }
    }
    // Sum up all weights
    // Return
    return new Pair<>(depths, weights);
  }

}
