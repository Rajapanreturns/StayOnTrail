package net.limaru.stayontrail;

import androidx.core.util.Pair;
import net.limaru.stayontrail.graphutils.Graph;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

public class GraphTest {

	Graph graph1;
	Graph graph2;

	Task task1;
	Task task2;
	Task task3;
	Task task4;
	Task task5;
	Task task6;
	Task task7;
	Task task8;
	Task task9;
	Task task10;
	Task task11;
	Task task12;

	@Before
	public void makeGraphs () {
		task1 = (new Task.Builder().id(1).date(63L).priority(SqlHelper.EPriority.LOWEST).build());
		task2 = (new Task.Builder().id(2).date(64L).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build());
		task3 = (new Task.Builder().id(3).date(65L).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build());
		task4 = (new Task.Builder().id(4).date(66L).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(2).build());
		task5 = (new Task.Builder().id(5).date(67L).priority(SqlHelper.EPriority.LOWEST).build());
		task6 = (new Task.Builder().id(6).date(68L).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(5).addPrerequisite(3).build());
		task7 = (new Task.Builder().id(7).date(69L).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(4).addPrerequisite(6).build());
		ArrayList<Task> tasks = new ArrayList<>(Arrays.asList(task1, task2, task3, task4, task5, task6, task7));
		this.graph1 = new Graph(tasks);
		task8 = (new Task.Builder().id(8).date(69L).priority(SqlHelper.EPriority.HIGH).build());
		task9 = (new Task.Builder().id(9).date(69L).priority(SqlHelper.EPriority.NORMAL).addPrerequisite(8).build());
		task10 = (new Task.Builder().id(10).date(69L).priority(SqlHelper.EPriority.HIGHEST).addPrerequisite(8).build());
		task11 = (new Task.Builder().id(11).date(69L).priority(SqlHelper.EPriority.LOW).addPrerequisite(9).addPrerequisite(10).build());
		task12 = (new Task.Builder().id(12).date(69L).priority(SqlHelper.EPriority.NORMAL).addPrerequisite(11).build());
		tasks.addAll(Arrays.asList(task8, task9, task10, task11, task12));
		this.graph2 = new Graph(tasks);
	}

	@Test
	public void testGraph () {
		HashMap<Long, Integer> depth1 = new HashMap<>();
		depth1.put(1L, 3);
		depth1.put(2L, 2);
		depth1.put(3L, 2);
		depth1.put(4L, 1);
		depth1.put(5L, 2);
		depth1.put(6L, 1);
		depth1.put(7L, 0);
		assertEquals (new Pair<>(depth1, 7), graph1.getSubGraphDepthsWeight(7));
		HashMap<Long, Integer> depth2 = new HashMap<>();
		depth2.put(8L, 3);
		depth2.put(9L, 2);
		depth2.put(10L, 2);
		depth2.put(11L, 1);
		depth2.put(12L, 0);
		assertEquals (new Pair<>(depth2, 8+4+16+2+4), graph2.getSubGraphDepthsWeight(12));
	}

	@Test
	public void testTodayTaskSingle () {
		// subgraph:
		//  7     <- depth 0
		// 4 6    <- depth 1
		// 2 3 5  <- depth 2
		//  1     <- depth 3
		// total weight: 7 (confirmed by test above)
		List<Task> result = graph1.suggestTasks(Collections.singletonList(task7), Long.MAX_VALUE);

		// task1 (depth=3) should be first
		assertEquals(7, result.size());
		assertEquals(task1, result.get(0)); // depth 3
		// depth=2
		assertTrue(result.subList(1, 4).containsAll(Arrays.asList(task2, task3, task5)));
		// depth=1
		assertTrue(result.subList(4, 6).containsAll(Arrays.asList(task4, task6)));
		// task7 (root) should be last
		assertEquals(task7, result.get(6));
	}

	@Test
	public void testEmptyTask() {
		List<Task> result = graph1.suggestTasks(Collections.emptyList(), Long.MAX_VALUE);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testMultipleWeights() {
		// subgraph:
		//  12   <- depth 0
		//  11   <- depth 1
		// 10 9  <- depth 2
		//   8   <- depth 3
		// Total weight: 34 (confirmed by test above)
		List<Task> result = graph2.suggestTasks(Collections.singletonList(task12), Long.MAX_VALUE);

		assertEquals(5, result.size());
		assertEquals(task8,  result.get(0)); // depth 3 - deepest
		assertTrue(result.subList(1, 3).containsAll(Arrays.asList(task9, task10))); // depth 2
		assertEquals(task11, result.get(3)); // depth 1
		assertEquals(task12, result.get(4)); // depth 0 - root
	}

	@Test
	public void testDisjointGraphs() {
		// todayTask: [task4, task5]
		// task4's subgraph (depth, weight): {4(0,4), 2(1,4), 1(2,4)}
		// task5's subgraph (depth, weight): {5(0,1)}
		List<Task> result = graph1.suggestTasks(Arrays.asList(task4, task5), Long.MAX_VALUE);

		assertEquals(4, result.size());
		assertEquals(task1, result.get(0));
		assertEquals(task2, result.get(1));
		assertEquals(task4, result.get(2));
		assertEquals(task5, result.get(3));
	}

	@Test
	public void testTaskMerge() {
		List<Task> result = graph1.suggestTasks(Arrays.asList(task2, task3), Long.MAX_VALUE);

		assertEquals(3, result.size());
		assertEquals(task1, result.get(0)); // shared deepest prerequisite always first
		assertTrue(result.subList(1, 3).containsAll(Arrays.asList(task2, task3)));
	}

	@Test
	public void testTaskNonZeroDuration() {
		Task t1 = new Task.Builder().id(1).date(63L).duration(10).priority(SqlHelper.EPriority.LOWEST).build();
		Task t2 = new Task.Builder().id(2).date(64L).duration(10).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build();
		Task t3 = new Task.Builder().id(3).date(65L).duration(10).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build();
		Task t4 = new Task.Builder().id(4).date(66L).duration(10).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(2).build();
		Task t5 = new Task.Builder().id(5).date(67L).duration(10).priority(SqlHelper.EPriority.LOWEST).build();
		Task t6 = new Task.Builder().id(6).date(68L).duration(10).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(5).addPrerequisite(3).build();
		Task t7 = new Task.Builder().id(7).date(69L).duration(10).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(4).addPrerequisite(6).build();
		Graph g = new Graph(new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5, t6, t7)));

		List<Task> result = g.suggestTasks(Collections.singletonList(t7), 150L);

		assertEquals(7, result.size());

		int indexT1 = result.indexOf(t1);
		int indexT5 = result.indexOf(t5);
		int indexT4 = result.indexOf(t4);
		int indexT6 = result.indexOf(t6);
		int indexT7 = result.indexOf(t7);

		assertEquals("t1 (depth=3, no prereqs) must be at index 0", 0, indexT1);
		assertEquals("t7 (depth=0, root) must be last", 6, indexT7);
		assertTrue("t5 should precede t4", indexT5 < indexT4);
		assertTrue("t5 should precede t6", indexT5 < indexT6);
		assertTrue("t5 should precede t7", indexT5 < indexT7);
		assertTrue("t4 should precede t7", indexT4 < indexT7);
		assertTrue("t6 should precede t7", indexT6 < indexT7);
	}

	@Test
	public void testTaskTimeLimitCutList() {
		Task t1 = new Task.Builder().id(1).date(63L).duration(5).priority(SqlHelper.EPriority.LOWEST).build();
		Task t2 = new Task.Builder().id(2).date(64L).duration(5).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build();
		// t3 has a very long duration — it should trigger the break
		Task t3 = new Task.Builder().id(3).date(65L).duration(100).priority(SqlHelper.EPriority.LOWEST).addPrerequisite(1).build();
		Graph g = new Graph(new ArrayList<>(Arrays.asList(t1, t2, t3)));

		List<Task> result = g.suggestTasks(Arrays.asList(t2, t3), 10L);

		assertFalse("t3 (duration=100 > timeLimit=10) must not appear", result.contains(t3));
		// Tasks before t3 in iteration order are still present
		assertFalse("result should be non-empty before the break point", result.isEmpty());
	}
}