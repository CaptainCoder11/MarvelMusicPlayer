package com.example.musicplayer.Extensions

import java.util.*

class Routers {
    var time = 0
    lateinit var index: IntArray
    lateinit var lTime: IntArray
    lateinit var visited: BooleanArray
    var result: MutableList<List<Int>>? = null

    fun Connection(n: Int, mconnection: List<List<Int>>): List<List<Int>> {
        val map = newAdjacent(mconnection)
        result = ArrayList()
        time = 0
        index = IntArray(n)
        lTime = IntArray(n)
        visited = BooleanArray(n)
        Arrays.fill(index, -1)
        Arrays.fill(lTime, -1)
        Arrays.fill(visited, false)
        DFS(0, -1, map)
        return result as ArrayList<List<Int>>
    }

    fun DFS(cnode: Int, pnode: Int, map: Map<Int, MutableSet<Int>>) {
        visited[cnode] = true
        lTime[cnode] = time.inc()
        index[cnode] = time.inc()
        for (neighbour in map[cnode]!!) {
            if (neighbour == pnode) continue
            if (!visited[neighbour]) {
                DFS(neighbour, cnode, map)
            }
            lTime[cnode] = Math.min(lTime[cnode], lTime[neighbour])
            if (lTime[neighbour] > index[cnode]) {
                result!!.add(Arrays.asList(cnode, neighbour))
            }
        }
    }

    fun newAdjacent(mcon: List<List<Int>>): Map<Int, MutableSet<Int>> {
        val map: MutableMap<Int, MutableSet<Int>> = HashMap()
        for (i in mcon.indices) {
            map[i] = HashSet()
        }
        for (i in mcon.indices) {
            val i = mcon[i][0]
            val j = mcon[i][1]
            map[i]!!.add(j)
            map[j]!!.add(i)
        }
        return map
    }
}