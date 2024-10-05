import com.oocourse.spec3.main.Person;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DisjointSetUnion {
    private HashMap<Integer, Integer> parent;
    private HashMap<Integer, Integer> rank;
    private MyNetwork myNetwork;

    DisjointSetUnion(MyNetwork myNetwork1) {
        parent = new HashMap<>();
        rank = new HashMap<>();
        myNetwork = myNetwork1;
    }

    // 添加新元素
    public void add(int id) {
        if (!parent.containsKey(id)) {
            parent.put(id, id);
            rank.put(id, 0);
        }
    }

    // 查找元素的根节点（路径压缩）
    int find(int id) {
        if (parent.get(id) != id) {
            parent.put(id, find(parent.get(id)));
        }
        return parent.get(id);
    }

    // 合并两个集合（按秩合并）
    public int merge(int id1, int id2, int blockSum) {
        int root1 = find(id1);
        int root2 = find(id2);
        int nextBlockSum = blockSum;
        if (root1 != root2) {
            if (rank.get(root1) < rank.get(root2)) {
                parent.put(root1, root2);
            } else if (rank.get(root1) > rank.get(root2)) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank.get(root1) + 1);
            }
            nextBlockSum--;
        }
        return nextBlockSum;
    }

    public int deleteRelation(int id1, int id2, int blockSum) {
        Set<Integer> visitedId1 = new HashSet<>();
        dfs(id1, visitedId1, id1); // 只遍历与id1相连的部分，不包括id2
        int nextBlockSum = blockSum;
        //nextBlockSum++;
        // 如果id2不在visited中，说明它们已经是分开的
        if (!visitedId1.contains(id2)) {
            parent.put(id2, id2);
            nextBlockSum++;
            Set<Integer> visitedId2 = new HashSet<>();
            dfs(id2, visitedId2, id2);
            if (visitedId2.isEmpty()) {
                rank.put(id2,0);
            } else {
                rank.put(id2, 1);
                for (int node : visitedId2) {
                    parent.put(node, id2);
                    rank.put(node, 0);
                }
            }
        }
        // 只更新id1的信息，保留其他节点的原有父节点，确保并查集的结构正确性
        parent.put(id1, id1);
        if (visitedId1.isEmpty()) {
            rank.put(id1,0);
        } else {
            rank.put(id1, 1);
            for (int node : visitedId1) {
                parent.put(node, id1);
                rank.put(node, 0);
            }
        }
        return nextBlockSum;
    }

    private void dfs(int node, Set<Integer> visited, int avoid) {
        visited.add(node);
        if (myNetwork.getPeople().get(node) != null) {
            MyPerson person = (MyPerson) myNetwork.getPeople().get(node);
            if (person.getAcquaintance().values() != null) {
                for (Person acquaintance : person.getAcquaintance().values()) {
                    int neighborId = acquaintance.getId();
                    if (neighborId != avoid) {  // 不访问避免的节点
                        if (!visited.contains(neighborId)) {
                            dfs(neighborId, visited, avoid);
                        }
                    }
                }
            }
        }
    }

    int shortPath(int source,int target,HashMap<Integer, Person> people) {
        Queue<Integer> queueSource = new LinkedList<>();
        Queue<Integer> queueTarget = new LinkedList<>();
        Map<Integer, Integer> visitedSource = new HashMap<>();
        queueSource.offer(source);
        visitedSource.put(source, 0);
        queueTarget.offer(target);
        Map<Integer, Integer> visitedTarget = new HashMap<>();
        visitedTarget.put(target, 0);
        while (!queueSource.isEmpty() && !queueTarget.isEmpty()) {
            int distance;
            if (queueSource.size() <= queueTarget.size()) {
                distance = visitLevel(queueSource, visitedSource, visitedTarget, people);
            } else {
                distance = visitLevel(queueTarget, visitedTarget, visitedSource, people);
            }
            if (distance != -1) {
                return distance;
            }
        }
        return -1;
    }

    int visitLevel(Queue<Integer> queue, Map<Integer, Integer> visited,
                   Map<Integer, Integer> otherVisited, HashMap<Integer, Person> people) {
        int currentSize = queue.size();
        for (int i = 0; i < currentSize; i++) {
            int currentNode = queue.poll();
            int currentDistance = visited.get(currentNode);
            MyPerson person = (MyPerson) people.get(currentNode);
            for (Person acquaintance : person.getAcquaintance().values()) {
                Integer neighbor = acquaintance.getId();
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, currentDistance + 1);
                    queue.offer(neighbor);
                    if (otherVisited.containsKey(neighbor)) {
                        return currentDistance + otherVisited.get(neighbor);
                    }
                }
            }
        }
        return -1;
    }

    public boolean isConnected(int id1, int id2) {
        return find(id1) == find(id2);
    }
}

