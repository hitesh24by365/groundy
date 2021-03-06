/*
 * Copyright 2012 CodeSlap
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeslap.groundy;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * A base class for Api Call resolvers
 *
 * @author evelio
 * @author cristian
 * @version 1.1
 */
public abstract class CallResolver {
    private Context mContext;
    private int mResultCode;
    private final Bundle mResultData = new Bundle();
    private Bundle mParameters;
    private ResultReceiver mReceiver;

    /**
     * Creates a CallResolver composed of
     */
    public CallResolver() {
        //Pessimistic by default
        setResultCode(Groundy.STATUS_ERROR);
    }

    /**
     * @return the resultCode
     */
    protected int getResultCode() {
        return mResultCode;
    }

    /**
     * @param resultCode the resultCode to set
     */
    protected void setResultCode(int resultCode) {
        mResultCode = resultCode;
    }

    /**
     * @return the resultData
     */
    protected Bundle getResultData() {
        return mResultData;
    }

    final void setContext(Context context) {
        mContext = context;
    }

    /**
     * @return Context instance to use
     */
    protected final Context getContext() {
        return mContext;
    }

    /**
     * Does its magic
     */
    protected final void execute() {
        if (requiresUpdate() && isOnline()) {
            try {
                updateData();
            } catch (Exception e) {
                e.printStackTrace();
                setResultCode(Groundy.STATUS_ERROR);
                mResultData.putString(Groundy.KEY_ERROR, String.valueOf(e.getMessage()));
                return;
            }
        } else {
            setResultCode(Groundy.STATUS_CONNECTIVITY_FAILED);
        }
        prepareResult();
    }

    /**
     * Determinate if there is Internet connection
     *
     * @return true if Online
     *         false otherwise
     */
    protected boolean isOnline() {
        return DeviceStatus.isOnline(mContext);
    }

    /**
     * @param parameters the parameters to set
     */
    void setParameters(Bundle parameters) {
        this.mParameters = parameters;
    }

    /**
     * @return the parameters
     */
    protected Bundle getParameters() {
        return mParameters;
    }

    /**
     * Indicates whatever if local data is fresh enough or not
     *
     * @return <code>true</code> if local data is not fresh
     *         <code>false</code> otherwise
     *         Note: default implementation always returns <code>true</code>
     *         Subclasses should override it according.
     */
    protected boolean requiresUpdate() {
        //By default it is hungry for updates
        return true;
    }

    void setReceiver(ResultReceiver receiver) {
        this.mReceiver = receiver;
    }

    protected ResultReceiver getReceiver() {
        return mReceiver;
    }

    /**
     * Prepare and sends a progress update to the current receiver.
     * Result code used is {@link Groundy#STATUS_PROGRESS} and it
     * will contain a bundle with an integer extra called {@link Groundy#KEY_PROGRESS}
     *
     * @param progress percentage to send to receiver
     */
    protected void updateProgress(int progress) {
        if (mReceiver == null) {
            return;
        }
        Bundle resultData = new Bundle();
        resultData.putInt(Groundy.KEY_PROGRESS, progress);
        mReceiver.send(Groundy.STATUS_PROGRESS, resultData);
    }

    protected boolean keepWifiOn() {
        return false;
    }

    /**
     * Override this if you want to cache the CallResolver instance. Do it only if you are
     * sure that {@link CallResolver#updateData()} and {@link CallResolver#prepareResult()}
     * methods won't need a fresh instance each time they are executed.
     *
     * @return true if this instance must be cached
     */
    protected boolean canBeCached() {
        return false;
    }

    /**
     * This must fetch and persist data. If an error occurs,
     * it must throws an error so that it can be catch by the executer
     *
     * @throws Exception
     */
    protected abstract void updateData();

    /**
     * This is responsible of preparing the result. Here you will retrieve info from a persistence source,
     * and call the {@link #setResultCode(int)} and put values to the result data bundle
     */
    protected abstract void prepareResult();
}
