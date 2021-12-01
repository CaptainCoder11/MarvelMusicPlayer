package com.example.musicplayer.Fragment

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.musicplayer.Extensions.*
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.R
import com.example.musicplayer.Activity.SongdetailActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.musicplayer.adapter.SwipeListener
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams


class BottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var imgexp:ImageView
    lateinit var relsng:RelativeLayout
    lateinit var expndedll:RelativeLayout
    lateinit var name:TextView
    lateinit var stitle:TextView
    lateinit var artists:TextView
    lateinit var imgtab:ImageView
    lateinit var previous:ImageView
    lateinit var next:ImageView
    lateinit var tabplps:ImageView
    lateinit var bottomSheet:View
    lateinit var play_pause_view:ImageView
    lateinit var seekbar : IndicatorSeekBar

    fun setplspstab(){
        if(musicSrv.isPlaying){
            tabplps.setImageResource(R.drawable.ic_pause_blue_24dp)
        }
        else
        {
            tabplps.setImageResource(R.drawable.ic_play_arrow_blue_24dp)
        }
    }

    private fun setupBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet)
                ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        bottomSheet.layoutParams.height = relsng.measuredHeight
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { setupBottomSheet(it) }
        return dialog
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.bottom_sheet,container,false)
        imgexp = view.findViewById(R.id.imgexp)
        relsng = view.findViewById(R.id.relsng)
        play_pause_view = view.findViewById(R.id.play_pause_view)
        expndedll = view.findViewById(R.id.expndedll)
        name = view.findViewById(R.id.name)
        stitle = view.findViewById(R.id.stitle)
        artists = view.findViewById(R.id.artists)
        imgtab = view.findViewById(R.id.imgtab)
        previous = view.findViewById(R.id.previous)
        next = view.findViewById(R.id.next)
        tabplps = view.findViewById(R.id.tabplps)
        seekbar = view.findViewById(R.id.seekbar)
        var bottomSheet = view.findViewById(R.id.dragView) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.peekHeight = relsng.getMeasuredHeight()
        behavior.isHideable = false
        var state = 0
        ins = this
        play_pause_view.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
        play_pause_view.setOnClickListener({
            if (MainActivity.getInstance()!!.isPlaying)
            {
                MainActivity.getInstance()!!.pause()
            }
            else
            {
                MainActivity.getInstance()!!.start()
            }
            togplpsbt()
        })
        context?.let {
            relsng.setOnTouchListener(object : SwipeListener(it) {

                override fun onSwipeRight() {
                    it.playPrev()
                }

                override fun onSwipeLeft() {
                    it.playNext()

                }
                override fun onClick() {
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@BottomSheetFragment.activity)
                    var intent= Intent(it, SongdetailActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent,options.toBundle())
                }

            })
            expndedll.setOnTouchListener(object : SwipeListener(it) {
                override fun onSwipeRight() {
                    it.playPrev()
                }

                override fun onSwipeLeft() {
                    it.playNext()
                }
                override fun onClick() {
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@BottomSheetFragment.activity)
                    var intent= Intent(it, SongdetailActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent,options.toBundle())
                }
            })
        }
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(view: View, i: Int) {
                when (i) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        state = BottomSheetBehavior.STATE_COLLAPSED
                        relsng.show()
                        relsng.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slideup))
                        bottomSheet.layoutParams.height = relsng.measuredHeight
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> if (state !== BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        relsng.startAnimation(AnimationUtils.loadAnimation(context,
                            R.anim.slidedown
                        ))
                        relsng.hide()
                        bottomSheet.layoutParams.height = expndedll.measuredHeight
                    }
                }
            }

            override fun onSlide(view: View, v: Float) {

            }
        })
        return view
    }
    companion object {
        var ins: BottomSheetFragment? = null
        fun getInstance(): BottomSheetFragment? {
            return ins
        }
        const val TAG = "ModalBottomSheet"
    }
    fun togplpsbt(){
        if (MainActivity.getInstance()!!.isPlaying) {
            play_pause_view!!.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
        } else {
            play_pause_view!!.setImageResource(R.drawable.ic_play_circle_filled_black_24dp)
        }
    }
    fun upprogress() {
        val handler = Handler()
        var mediaPos = MainActivity.getInstance()!!.currentPosition
        var mediaMax = MainActivity.getInstance()!!.duration
        seekbar!!.setMax(mediaMax.toFloat()) // Set the Maximum range of the
        seekbar!!.setProgress(mediaPos.toFloat())// set current progress to song's
        val moveSeekBarThread = object : Runnable {
            override fun run() {

                if (MainActivity.getInstance()!!.isPlaying) {
                    val mediaPos_new = MainActivity.getInstance()!!.currentPosition
                    val mediaMax_new =  MainActivity.getInstance()!!.duration
                    seekbar!!.setMax(mediaMax_new.toFloat())
                    seekbar!!.setProgress(mediaPos_new.toFloat())

                    handler.postDelayed(this, 100) //Looping the thread after 0.1 second

                }


            }
        }
        handler.postDelayed(moveSeekBarThread, 100)

        seekbar!!.setOnSeekChangeListener(object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                if (MainActivity.getInstance()!!.isPlaying) {
                    MainActivity.getInstance()!!.seekTo(seekBar.progress)
                    handler.postDelayed(moveSeekBarThread, 100)
                }
            }
        })
    }
}