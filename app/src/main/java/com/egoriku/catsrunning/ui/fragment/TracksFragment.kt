package com.egoriku.catsrunning.ui.fragment

import android.animation.Animator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import com.egoriku.catsrunning.R
import com.egoriku.catsrunning.activities.FitActivity
import com.egoriku.catsrunning.data.TracksDataManager
import com.egoriku.catsrunning.data.UIListener
import com.egoriku.catsrunning.data.commons.TracksModel
import com.egoriku.catsrunning.helpers.Events
import com.egoriku.catsrunning.helpers.TypeFit
import com.egoriku.catsrunning.models.Constants
import com.egoriku.catsrunning.ui.activity.TrackMapActivity
import com.egoriku.catsrunning.ui.activity.TracksActivity
import com.egoriku.catsrunning.ui.adapter.TracksAdapter
import com.egoriku.catsrunning.utils.FirebaseUtils
import com.egoriku.core_lib.extensions.*
import com.egoriku.core_lib.listeners.SimpleAnimatorListener
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_tracks.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity

class TracksFragment : Fragment(), UIListener {

    private lateinit var adapter: TracksAdapter
    private val tracksDataManager = TracksDataManager.instance
    private var anim: TranslateAnimation = TranslateAnimation(0f, 0f, 40f, 0f)
    private val firebaseUtils by lazy { FirebaseUtils.getInstance() }

    private lateinit var clickEventSubscriber: Disposable
    private lateinit var likedClickSubscriber: Disposable

    init {
        anim.apply {
            duration = 200
            interpolator = LinearOutSlowInInterpolator()
            fillAfter = true
        }
    }

    companion object {
        fun instance(): TracksFragment {
            return TracksFragment()
        }
    }

    override fun handleSuccess(data: List<TracksModel>) {
        d("handleSuccess")
        progressbar.hide()
        tracks_recyclerview.show()
        adapter.setItems(data)
        no_tracks.hide()
        no_tracks_text.hide()

        if (data.isEmpty()) {
            no_tracks.show()
            no_tracks.setImageDrawable(drawableCompat(activity, R.drawable.ic_vec_cats_no_track))
            no_tracks_text.show()
            tracks_recyclerview.hide(gone = false)
        }
    }

    override fun handleError() {
        no_tracks.show()
        no_tracks.setImageDrawable(drawableCompat(activity, R.drawable.ic_vec_cats_no_track))
        no_tracks_text.show()
        tracks_recyclerview.hide(gone = false)
    }

    override fun onStart() {
        super.onStart()
        (activity as TracksActivity).onFragmentStart(R.string.navigation_drawer_main_activity_new)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_tracks)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tracks_recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        progressbar.show()
        initAdapter()
        tracksDataManager.apply {
            addListener(this@TracksFragment)
            loadTracks(TypeFit.WALKING)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_walking -> consumeEvent(TypeFit.WALKING, it.itemId)
                R.id.action_running -> consumeEvent(TypeFit.RUNNING, it.itemId)
                R.id.action_cycling -> consumeEvent(TypeFit.CYCLING, it.itemId)
                else -> false
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline private fun consumeEvent(typeFit: Int, itemId: Int): Boolean {
        if (bottomNavigationView.selectedItemId != itemId) {
            tracks_recyclerview.animation = anim
            anim.start()
        }
        tracksDataManager.loadTracks(typeFit)
        return true
    }

    private fun initAdapter() {
        if (tracks_recyclerview.adapter == null) {
            adapter = TracksAdapter()
            tracks_recyclerview.adapter = adapter

            clickEventSubscriber = adapter.clickItem
                    .subscribe({ (tracksModel, event) ->
                        when (event) {
                            Events.CLICK -> {
                                if (tracksModel.time == 0L) {
                                    startActivity<FitActivity>(Constants.Extras.KEY_TYPE_FIT to tracksModel.typeFit)
                                } else {
                                    startActivity<TrackMapActivity>(Constants.Extras.EXTRA_TRACK_ON_MAPS to tracksModel)
                                }
                            }
                            Events.LONG_CLICK -> {
                                alert(R.string.fitness_data_fragment_alert_title) {
                                    positiveButton(R.string.fitness_data_fragment_alert_positive_btn) { firebaseUtils.removeTrack(tracksModel, context) }
                                    negativeButton(R.string.fitness_data_fragment_alert_negative_btn) {}
                                }.show()
                            }
                        }
                    })

            likedClickSubscriber = adapter.likedClick
                    .subscribe({ (view, event, tracksModel, position) ->
                        when (event) {
                            Events.LIKED_CLICK -> {
                                tracksModel.isFavorite = !tracksModel.isFavorite
                                firebaseUtils.updateFavorite(tracksModel, context)

                                view.addAnimatorListener(object : SimpleAnimatorListener() {

                                    override fun onAnimationEnd(p0: Animator?) {
                                        tracks_recyclerview.post({ adapter.notifyItemChanged(position) })
                                    }

                                    override fun onAnimationCancel(p0: Animator?) = if (tracksModel.isFavorite) view.progress = 1.0f else view.progress = 0.0f
                                })

                                if (tracksModel.isFavorite) view.playAnimation() else view.progress = 0.0f
                            }
                        }
                    })
        }
    }

    override fun onStop() {
        super.onStop()
        tracksDataManager.removeListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickEventSubscriber.dispose()
        likedClickSubscriber.dispose()
    }
}