package com.example.khanj.trust


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.BooleanResult
import kotlin.jvm.internal.Ref

/**
 * A simple [Fragment] subclass.
 */
class NavigationDrawerFragment : Fragment() {

    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mDrawerLayout: DrawerLayout? = null

    private var mUserLearnedDrawer: Boolean = false
    private var mFromSavedInstanceState: Boolean = false
    private var containerView: View? = null
    private val isDrawerOpened = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mUserLearnedDrawer = readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false").toBoolean()
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true
        }
    }


     override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_navigation_drawer, container, false)
    }

    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
        containerView = getActivity().findViewById(fragmentId)
        mDrawerLayout = drawerLayout
        mDrawerToggle = object : ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer.toString() + "")
                }
                getActivity().invalidateOptionsMenu()
            }

            @Override
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                getActivity().invalidateOptionsMenu()
            }

        }
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout!!.openDrawer(containerView)
        }
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
        mDrawerLayout!!.post(object : Runnable {
            @Override
            override fun run() {
                mDrawerToggle!!.syncState()
            }
        })
    }

    companion object {

        val PREF_FILE_NAME = "testpref"
        val KEY_USER_LEARNED_DRAWER = "user_learned_drawer"

        fun saveToPreferences(context: Context, preferenceName: String, preferenceValue: String) {
            val sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(preferenceName, preferenceValue)
            editor.apply()
        }

        fun readFromPreferences(context: Context, preferenceName: String, defaultValue: String): String {
            val sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(preferenceName, defaultValue)
        }
    }
}// Required empty public constructor
