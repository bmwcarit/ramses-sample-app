//  -------------------------------------------------------------------------
//  Copyright (C) 2022 BMW AG
//  -------------------------------------------------------------------------
//  This Source Code Form is subject to the terms of the Mozilla Public
//  License, v. 2.0. If a copy of the MPL was not distributed with this
//  file, You can obtain one at https://mozilla.org/MPL/2.0/.
//  -------------------------------------------------------------------------
package com.bmwgroup.ramsessample.fragments

import android.content.Context
import android.util.Log
import com.bmwgroup.ramses.Property
import com.bmwgroup.ramses.RamsesThread

class VehicleSceneThread(threadName: String?, context: Context?) : RamsesThread(threadName, context) {

    /* Overrides the base class method which calls this based on thread scheduling
     * This method is executed from the correct thread (the one which talks to ramses)
     */
    override fun onUpdate() {
        /*
         * Set the camera yaw and pitch values based on user input (also see input callbacks below)
         */
        mCameraYaw?.set(mYawValue)
        mCameraPitch?.set(mPitchValue)
        mCameraDistance?.set(mCamDistValue)

        /*
         * Open or close the doors based on bool value (also see input callbacks below)
         */
        val doorsValue: Float
        if (mAllDoorsOpen) {
            doorsValue = 1.0F
        }
        else
        {
            doorsValue = 0.0F
        }
        mDoorL1?.set(doorsValue)
        mDoorL2?.set(doorsValue)
        mDoorR1?.set(doorsValue)
        mDoorR2?.set(doorsValue)
        mTrunk?.set(doorsValue)
    }

    /* Overrides the base class method which calls this based on thread scheduling
    * This method is executed from the correct thread (the one which talks to ramses)
    */
    override fun onSceneLoaded() {
        /// Find the script used to control the logic state (light animation and camera control)
        val doorsScriptRootInput: Property? = getLogicNodeRootInput("SceneControls")
        val cameraScriptRootInput: Property? = getLogicNodeRootInput("CameraCrane.Interface_CameraCrane")
        if (cameraScriptRootInput == null || doorsScriptRootInput == null) {
            Log.e("RamsesSampleApp", "Loaded scene does not contain expected interface scripts!")
        }
        else {
            mCameraViewportW = cameraScriptRootInput.getChild("Viewport").getChild("Width")
            mCameraViewportH = cameraScriptRootInput.getChild("Viewport").getChild("Height")
            mCameraYaw = cameraScriptRootInput.getChild("CraneGimbal").getChild("Yaw")
            mCameraPitch = cameraScriptRootInput.getChild("CraneGimbal").getChild("Pitch")
            mCameraDistance = cameraScriptRootInput.getChild("CraneGimbal").getChild("Distance")

            val yawOutput = getLogicNodeRootOutput("SceneControls").getChild("CameraPerspective").getChild("Yaw")
            val pitchOutput = getLogicNodeRootOutput("SceneControls").getChild("CameraPerspective").getChild("Pitch")
            val distanceOutput = getLogicNodeRootOutput("SceneControls").getChild("CameraPerspective").getChild("Distance")
            unlinkProperties(yawOutput, mCameraYaw)
            unlinkProperties(pitchOutput, mCameraPitch)
            unlinkProperties(distanceOutput, mCameraDistance)
            mCarPaintId = doorsScriptRootInput.getChild("CarPaint_ID")
            mDoorL1 = doorsScriptRootInput.getChild("Door_F_L_OpeningValue")
            mDoorL2 = doorsScriptRootInput.getChild("Door_B_L_OpeningValue")
            mDoorR1 = doorsScriptRootInput.getChild("Door_F_R_OpeningValue")
            mDoorR2 = doorsScriptRootInput.getChild("Door_B_R_OpeningValue")
            mTrunk = doorsScriptRootInput.getChild("Tailgate_OpeningValue")

            /// Initialize values from the scene defaults; in real apps the values should come from the application logic
            mYawValue = mCameraYaw?.float ?: 0f
            mPitchValue = mCameraPitch?.float ?: 0f
        }
    }

    /* Overrides the base class method which calls this based on thread scheduling
    * This method is executed from the correct thread (the one which talks to ramses)
    */
    override fun onSceneLoadFailed() {
        // Implement actions to react to failed scene load
        Log.e("RamsesSampleApp", "Loading Scene failed")
    }

    override fun onLogicUpdated() {
        // Here it's possible to read out (but not write!) scene state data, like LogicNode outputs (see RamsesThread::getLogicNodeRootOutput()).
    }

    /* Overrides the base class method which calls this based on thread scheduling
    * This method is executed from the correct thread (the one which talks to ramses)
    */
    override fun onDisplayResize(width: Int, height: Int) {
        mCameraViewportW?.set(width)
        mCameraViewportH?.set(height)
        mScreenWidth = width
        mScreenHeight = height
    }

    fun toggleDoors() {
        mAllDoorsOpen = !mAllDoorsOpen
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onTouchDown(x: Int, y: Int) {
        addRunnableToThreadQueue {
            mPrevX = x
            mPrevY = y
            mTouchDownX = x
            mTouchDownY = y
        }
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onTouchUp() {
        addRunnableToThreadQueue { /// Point-touch -> switch door open/close state
            mPrevX = -1
            mPrevY = -1
        }
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onMotion(x: Int, y: Int) {
        addRunnableToThreadQueue { // Incrementally shift the pitch/yaw values based on change in pixel position relative to the screen size
            val vpWidth = mScreenWidth
            val vpHeight = mScreenHeight
            val yawDiff = 100 * (mPrevX - x).toFloat() / vpWidth
            val pitchDiff = 100 * (y - mPrevY).toFloat() / vpHeight
            mPrevX = x
            mPrevY = y
            mYawValue += yawDiff
            mPitchValue += pitchDiff
        }
    }

    private var mYawValue = 0f
    private var mPitchValue = 0f
    private var mCamDistValue = 600f
    private var mPrevX = -1
    private var mPrevY = -1
    private var mTouchDownX = -1
    private var mTouchDownY = -1
    private var mAllDoorsOpen = false
    private var mScreenWidth = 1
    private var mScreenHeight = 1

    private var mCameraViewportW: Property? = null
    private var mCameraViewportH: Property? = null
    private var mCameraYaw: Property? = null
    private var mCameraPitch: Property? = null
    private var mCameraDistance: Property? = null
    private var mDoorL1: Property? = null
    private var mDoorL2: Property? = null
    private var mDoorR1: Property? = null
    private var mDoorR2: Property? = null
    private var mTrunk: Property? = null

    private var mCarPaintId: Property? = null

    /**
     * Set the car paint (values 1..7 are available)
     */
    var carPaint: UInt
        get() = mCarPaintId?.int?.toUInt() ?: 1u
        set(value) {
            addRunnableToThreadQueue {
                mCarPaintId?.set(value.toInt())
            }
        }
}
