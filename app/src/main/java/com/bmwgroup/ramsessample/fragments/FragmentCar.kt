//  -------------------------------------------------------------------------
//  Copyright (C) 2022 BMW AG
//  -------------------------------------------------------------------------
//  This Source Code Form is subject to the terms of the Mozilla Public
//  License, v. 2.0. If a copy of the MPL was not distributed with this
//  file, You can obtain one at https://mozilla.org/MPL/2.0/.
//  -------------------------------------------------------------------------
package com.bmwgroup.ramsessample.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.bmwgroup.ramsessample.fragments.databinding.FragmentCarBinding

class FragmentCar : Fragment(), SurfaceHolder.Callback {
    private var _binding: FragmentCarBinding? = null
    /** Only valid between onCreateView() and onDestroyView()! */
    private val binding: FragmentCarBinding get() = _binding!!
    private lateinit var mSceneThread: VehicleSceneThread
    companion object {
        const val MAX_CLICK_DURATION_MS = 200
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mSceneThread = VehicleSceneThread("VehicleSceneThread", requireContext())
        // As the activity is not guaranteed to be created in the Fragments onCreate callback call RamsesThreads initRamsesThreadAndLoadScene here
        mSceneThread.initRamsesThreadAndLoadScene(requireActivity().assets, "G05.ramses", "G05.rlogic")

        resources.getStringArray(R.array.car_paints).mapIndexed { index, carPaintName ->
            Button(context).apply {
                text = carPaintName
                setOnClickListener {
                    mSceneThread.carPaint = (index + 1).toUInt()
                }
            }
        }.forEach { binding.buttonBar.addView(it) }

        // Add the addOnWindowFocusChangeListener as it is not supported by default by Fragments
        binding.root.viewTreeObserver.addOnWindowFocusChangeListener { hasFocus ->
            when(hasFocus)
            {
                true -> if (mSceneThread.isAlive) startRendering()
                false -> if (mSceneThread.isAlive) stopRunningRenderThread()
            }
        }

        // As it is certain that the view is created in the onViewCreated callback initialize the SurfaceView here
        binding.surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mSceneThread.onTouchDown(event.getX(0).toInt(), event.getY(0).toInt())
                MotionEvent.ACTION_UP -> {
                    mSceneThread.onTouchUp()
                    if (event.eventTime - event.downTime < MAX_CLICK_DURATION_MS){
                        mSceneThread.toggleDoors()
                    }
                }
                MotionEvent.ACTION_MOVE -> mSceneThread.onMotion(event.getX(0).toInt(), event.getY(0).toInt())
            }
            true
        }
        binding.surfaceView.holder.addCallback(this)
    }

    override fun onResume() {
        super.onResume()
        startRendering()
    }

    override fun onPause() {
        super.onPause()
        stopRunningRenderThread()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            if (this::mSceneThread.isInitialized) {
                mSceneThread.destroyRamsesBundleAndQuitThread()
            } else {
                Log.e("FragmentCar", "onDestroyView failed to destroy RamsesBundle since it's not initialized.")
            }
        } catch (e: InterruptedException) {
            Log.e("FragmentCar", "onDestroyView failed: ", e)
        }
        _binding = null
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        val surface = surfaceHolder.surface
        try {
            mSceneThread.createDisplayAndShowScene(surface, null)
            // Setting the framerate to 30 as every frame properties are updated in onUpdate and the default of 60 would do unnecessary work
            // Setting it directly after creating the display will make sure that it will be applied for the lifetime of the display
            mSceneThread.renderingFramerate = 30f
            startRendering()
        } catch (e: InterruptedException) {
            Log.e("FragmentCar", "surfaceCreated failed: ", e)
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        try {
            if (mSceneThread.isAlive)
                mSceneThread.destroyDisplay()
        } catch (e: InterruptedException) {
            Log.e("FragmentCar", "surfaceDestroyed failed: ", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mSceneThread.resizeDisplay(width, height)
    }

    // RamsesThreads startRendering will throw an IllegalSTateException if the display is not created
    // or the the renderer is already running. Thus the user has to check these two conditions before
    // calling RamsesThread.startRendering
    private fun startRendering() {
        mSceneThread.addRunnableToThreadQueue {
            if (mSceneThread.isDisplayCreated && !mSceneThread.isRendering) {
                mSceneThread.startRendering()
            }
        }
    }

    private fun stopRunningRenderThread() {
        mSceneThread.addRunnableToThreadQueue {
            if (mSceneThread.isRendering) {
                mSceneThread.stopRendering()
            }
        }
    }
}
