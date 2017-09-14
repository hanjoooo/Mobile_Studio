package com.example.khanj.trust

import android.app.Activity
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.FrameLayout
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import java.util.*

/**
 * Created by khanj on 2017-09-14.
 */
abstract class FirebaseListAdapter<T>(mRef:Query,mModelClass: Class<T>,mLayout: Int,activity: Activity) : BaseAdapter() {
    private val mRef=mRef
    private val mModelClass= mModelClass
    private val mLayout= mLayout
    private var mInflater:LayoutInflater = activity.layoutInflater
    private var mModels:ArrayList<T> = ArrayList<T>()
    private var mKeys:ArrayList<String> = ArrayList<String>()
    private var mListener: ChildEventListener= this.mRef.addChildEventListener(object : ChildEventListener{
        override fun onChildAdded(p0: DataSnapshot, previousChildName: String) {
            val model = p0.getValue(this@FirebaseListAdapter.mModelClass)
            val key = p0.getKey()
            if (previousChildName == " ") {
                mModels.add(0, model)
                mKeys.add(0, key)
            } else {
                val previousIndex = mKeys.indexOf(previousChildName)
                val nextIndex = previousIndex + 1
                if (nextIndex == mModels.size) {
                    mModels.add(model)
                    mKeys.add(key)
                } else {
                    mModels.add(nextIndex, model)
                    mKeys.add(nextIndex, key)
                }
            }

            notifyDataSetChanged()
        }
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val key = dataSnapshot.getKey()
            val index = mKeys.indexOf(key)
            mKeys.removeAt(index)
            mModels.removeAt(index)

            notifyDataSetChanged()
        }
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {
            val key = dataSnapshot.getKey()
            val newModel = dataSnapshot.getValue(this@FirebaseListAdapter.mModelClass)
            val index = mKeys.indexOf(key)

            mModels.set(index, newModel)

            notifyDataSetChanged()
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val key = dataSnapshot.getKey()
            val newModel = dataSnapshot.getValue(this@FirebaseListAdapter.mModelClass)
            val index = mKeys.indexOf(key)
            mModels.removeAt(index)
            mKeys.removeAt(index)
            if (previousChildName == null) {
                mModels.add(0, newModel)
                mKeys.add(0, key)
            } else {
                val previousIndex = mKeys.indexOf(previousChildName)
                val nextIndex = previousIndex + 1
                if (nextIndex == mModels.size) {
                    mModels.add(newModel)
                    mKeys.add(key)
                } else {
                    mModels.add(nextIndex, newModel)
                    mKeys.add(nextIndex, key)
                }
            }
            notifyDataSetChanged()
        }
        override fun onCancelled(p0: DatabaseError?) {
        }


    })
    fun cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener)
        mModels.clear()
        mKeys.clear()
    }
    override fun getCount():Int{
        return mModels.size
    }

    override fun getItem(i:Int) : T  {
        return mModels.get(i)
    }




}