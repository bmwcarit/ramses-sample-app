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
        m_cameraYaw?.set(m_yawValue)
        m_cameraPitch?.set(m_pitchValue)

        /*
         * Open or close the doors based on bool value (also see input callbacks below)
         */
        val doorsValue: Float
        if (m_allDoorsOpen) {
            doorsValue = 1.0F
        }
        else
        {
            doorsValue = 0.0F
        }
        m_doorL1?.set(doorsValue)
        m_doorL2?.set(doorsValue)
        m_doorR1?.set(doorsValue)
        m_doorR2?.set(doorsValue)
        m_trunk?.set(doorsValue)
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
            m_cameraViewportW = cameraScriptRootInput.getChild("Viewport").getChild("Width")
            m_cameraViewportH = cameraScriptRootInput.getChild("Viewport").getChild("Height")
            m_cameraYaw = cameraScriptRootInput.getChild("CraneGimbal").getChild("Yaw")
            m_cameraPitch = cameraScriptRootInput.getChild("CraneGimbal").getChild("Pitch")

            val yawOutput = getLogicNodeRootOutput("SceneControls").getChild("CameraPerspective").getChild("Yaw")
            val pitchOutput = getLogicNodeRootOutput("SceneControls").getChild("CameraPerspective").getChild("Pitch")
            unlinkProperties(yawOutput, m_cameraYaw)
            unlinkProperties(pitchOutput, m_cameraPitch)
            m_doorL1 = doorsScriptRootInput.getChild("Door_F_L_OpeningValue")
            m_doorL2 = doorsScriptRootInput.getChild("Door_B_L_OpeningValue")
            m_doorR1 = doorsScriptRootInput.getChild("Door_F_R_OpeningValue")
            m_doorR2 = doorsScriptRootInput.getChild("Door_B_R_OpeningValue")
            m_trunk = doorsScriptRootInput.getChild("Tailgate_OpeningValue")

            /// Initialize values from the scene defaults; in real apps the values should come from the application logic
            m_yawValue = m_cameraYaw?.float ?: 0f
            m_pitchValue = m_cameraPitch?.float ?: 0f
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
        m_cameraViewportW?.set(width)
        m_cameraViewportH?.set(height)
        m_screenWidth = width
        m_screenHeight = height
    }

    fun toggleDoors() {
        m_allDoorsOpen = !m_allDoorsOpen
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onTouchDown(x: Int, y: Int) {
        addRunnableToThreadQueue {
            m_prevX = x
            m_prevY = y
            m_touchDownX = x
            m_touchDownY = y
        }
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onTouchUp() {
        addRunnableToThreadQueue { /// Point-touch -> switch door open/close state
            m_prevX = -1
            m_prevY = -1
        }
    }

    // Have to pass user code to addRunnableToThreadQueue to execute from the correct thread (the one which talks to ramses)
    fun onMotion(x: Int, y: Int) {
        addRunnableToThreadQueue { // Incrementally shift the pitch/yaw values based on change in pixel position relative to the screen size
            val vpWidth = m_screenWidth
            val vpHeight = m_screenHeight
            val yawDiff = 100 * (m_prevX - x).toFloat() / vpWidth
            val pitchDiff = 100 * (y - m_prevY).toFloat() / vpHeight
            m_prevX = x
            m_prevY = y
            m_yawValue += yawDiff
            m_pitchValue += pitchDiff
        }
    }

    private var m_yawValue = 0f
    private var m_pitchValue = 0f
    private var m_prevX = -1
    private var m_prevY = -1
    private var m_touchDownX = -1
    private var m_touchDownY = -1
    private var m_allDoorsOpen = false
    private var m_screenWidth = 1
    private var m_screenHeight = 1

    private var m_cameraViewportW: Property? = null
    private var m_cameraViewportH: Property? = null
    private var m_cameraYaw: Property? = null
    private var m_cameraPitch: Property? = null
    private var m_doorL1: Property? = null
    private var m_doorL2: Property? = null
    private var m_doorR1: Property? = null
    private var m_doorR2: Property? = null
    private var m_trunk: Property? = null
}
