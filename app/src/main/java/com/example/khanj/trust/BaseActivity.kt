package com.example.khanj.trust

import android.app.ProgressDialog
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AppCompatActivity

/*
 * Created by khanj on 2017-09-21.
 */
open class BaseActivity : AppCompatActivity() {
    @VisibleForTesting
    var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage(getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    public override fun onStop() {
        super.onStop()
        hideProgressDialog()
    }

}
