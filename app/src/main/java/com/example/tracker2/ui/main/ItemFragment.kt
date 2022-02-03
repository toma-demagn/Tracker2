package com.example.tracker2.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracker2.R
import com.example.tracker2.ui.main.placeholder.PlaceholderContent

/**
 * A fragment representing a list of Items.
 */
class ItemFragment() : Fragment() {

    private var columnCount = 1

    lateinit var view: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_item_list, container, false) as RecyclerView
        // Set the adapter
        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            theList = initList2(0)
            theAdapter = MyItemRecyclerViewAdapter(theList)
            adapter = theAdapter

        }
        return view

    }

    fun initList2(n: Int): ArrayList<Pair<Int, String>> {
        var liste = ArrayList<Pair<Int, String>>()
        for (i in 1..n) {
            liste.add(Pair(i, "ListIem" + i))
        }
        return liste
    }

    companion object {
        var theList = ArrayList<Pair<Int, String>>()
        var theAdapter: MyItemRecyclerViewAdapter = MyItemRecyclerViewAdapter(theList)

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }


}