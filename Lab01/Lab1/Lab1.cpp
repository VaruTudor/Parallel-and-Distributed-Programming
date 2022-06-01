/*
  3. Summation with fixed structure of inputs
  We have to keep the values of some integer variables. Some of them are primary variables; they represent input data. The others are secondary variables, and represent aggregations of some other variables. In our case, each secondary variable is a sum of some input variables. The inputs may be primary or secondary variables. However, we assume that the relations do not form cycles.
  At runtime, we get notifications of value changes for the primary variable. Processing a notification must atomically update the primary variable, as well as any secondary variable depending, directly or indirectly, on it. The updating shall not re-compute the sums; instead, you must use the difference between the old value and the new value of the primary variable.
  From time to time, as well as at the end, a consistency check shall be performed. It shall verify that all the secondary variables are indeed the sums of their inputs, as specified.
*/

#include <iostream>
#include <chrono>
#include <fstream>
#include <vector>
#include <map>
#include <stdlib.h>
#include <time.h>
#include <thread>
#include <mutex>
#include <cassert>

using namespace std;

#define debug
#define NODE_MUTEX
//#define ENABLE_GLOBAL_MUTEX

const string FILENAME = "graph2.in";
const int NUMBER_OF_THREADS = 10;

class Node {
protected:
#ifdef NODE_MUTEX
	std::mutex node_mtx;
#endif
public:
	virtual int getIndex() = 0;
	virtual int getValue() = 0;
	virtual void addToValue(int) = 0;
	virtual int computeValue() = 0;
	virtual bool isConsistent() = 0;
};

class Primary : public Node {
protected:
	int value, index;
public:
	Primary(int ind) {
		/*
		* Needed for created a Secondary.
		*/
		index = ind;
		value = 0;
	}
	Primary(int ind, int x) {
		index = ind;
		value = x;
	}
	virtual int getIndex() {
		return index;
	}
	virtual int getValue() {
#ifdef NODE_MUTEX
		node_mtx.lock();
#ifdef debug
		cout << "Locking " << index << '\n';
#endif
#endif
		int auxval = value;
#ifdef NODE_MUTEX
		node_mtx.unlock();
#ifdef debug
		cout << "unlocking " << index << '\n';
#endif
#endif
		return auxval;
	}
	virtual void addToValue(int x) {
#ifdef NODE_MUTEX
		node_mtx.lock();
#ifdef debug
		cout << "Locking " << index << '\n';
#endif
#endif
		value += x;
#ifdef NODE_MUTEX
#ifdef debug
		cout << "Unlocking " << index << '\n';
#endif
		node_mtx.unlock();
#endif
	}
	virtual int computeValue() {
		return value;
	}
	virtual bool isConsistent() {
		return true;
	}
};

class Secondary : public Primary {
private:
	vector <Node*> dependency;
public:
	Secondary(int ind) : Primary(ind) {
	}
	void addDependency(Node* n) {
		dependency.push_back(n);
	}
	virtual int computeValue() {
		value = 0;
		for (auto it : dependency) {
			value += it->computeValue();
		}
		return value;
	}
	virtual bool isConsistent() {
		/*
		* Checks if current value is equal to the sum of all nodes it depends on.
		*/
		int sum = 0;
		for (auto it : dependency) {
			sum += it->getValue();
		}
		return sum == value;
	}
};


class ComputationalGraph {
private:
#ifdef ENABLE_GLOBAL_MUTEX
	mutex global_mutex;
#endif
	map<int, Node*> nodes;
	map<Node*, vector<Node*>> dependants;
	int n, m;
	void dfs(Node* node, int quantity) {
		node->addToValue(quantity);
		for (auto it : dependants[node]) {
			dfs(it, quantity);
		}
	}
public:
	void updatePrimary(Primary* Primary, int newValue) {
#ifdef ENABLE_GLOBAL_MUTEX
		global_mutex.lock();
#ifdef debug
		cout << "Locking graph";
#endif
#endif
		dfs(Primary, newValue - Primary->getValue());
#ifdef ENABLE_GLOBAL_MUTEX
		global_mutex.unlock();
#ifdef debug
		cout << "Unlocking graph";
#endif
#endif
	}
	void loadGraph(string filename) {
		ifstream fin(filename);
		fin >> n; // number of leaf nodes
		for (int i = 1; i <= n; ++i) {
			int x;
			fin >> x;
			Node* node = new Primary(i, x);
			nodes[i] = node;
		}
		fin >> m; // number of dependencies
		for (int i = 1; i <= m; ++i) {
			int x, y; // y depends on x
			fin >> x >> y;
			if (!nodes[x]) {
				nodes[x] = new Secondary(x);
			}
			if (!nodes[y]) {
				nodes[y] = new Secondary(y);
			}
			dynamic_cast<Secondary*>(nodes[y])->addDependency(nodes[x]);
			dependants[nodes[x]].push_back(nodes[y]);
		}
	}
	void computeInitialValue() {
		for (auto it : nodes) {
			it.second->computeValue();
		}
	}

	void checkConsistency() {
#ifdef debug
		cout << "Checking consistency\n";
#endif
		for (auto it : nodes) {
			assert(it.second->isConsistent());
		}
#ifdef debug
		cout << "Consistent\n";
#endif
	}
	void printValues() {
		for (auto it : nodes) {
			cout << "value of " << it.first << " is: " << it.second->getValue() << '\n';
		}
	}
	Primary* getRandomPrimary() {
		return dynamic_cast<Primary*>(nodes[rand() % n + 1]);
	}
};

int getRandomInteger(int _max, int _min) {
	return _min + (rand() % static_cast<int>(_max - _min + 1));
}

int main() {
	auto startTime = std::chrono::high_resolution_clock::now();
	srand(time(NULL));
	vector <thread> threads;
	ComputationalGraph g;
	g.loadGraph(FILENAME);
	g.computeInitialValue();
	for (int t = 0; t < NUMBER_OF_THREADS; ++t) {
		if (t % 5 == 0) {
			// check for consistency
			for (int i = 0; i < threads.size(); ++i) {
				threads[i].join();
			}
			threads.clear();
			g.checkConsistency();
		}
		// update some primary with a random value
		Primary* currentNode = g.getRandomPrimary();
		int newVal = getRandomInteger(-10, 10);
#ifdef debug
		cout << "thread " << t + 1 << " updates node " << currentNode->getIndex()
			<< " with " << newVal << '\n';
#endif
		threads.push_back(thread(&ComputationalGraph::updatePrimary, &g, currentNode, newVal));
	}
	for (int t = 0; t < (int)threads.size(); ++t) {
		threads[t].join();
	}
	threads.clear();
	g.checkConsistency();
#ifdef debug
	g.printValues();
#endif
	auto endTime = std::chrono::high_resolution_clock::now();
	std::chrono::duration<double> elapsed = endTime - startTime;
	cerr << "time: " << elapsed.count() << '\n';
	return 0;
}