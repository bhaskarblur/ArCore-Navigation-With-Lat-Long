package com.example.indoortracking;

import android.util.Log;

import java.util.Collection;

import es.situm.sdk.SitumSdk;
import es.situm.sdk.error.Error;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Poi;
import es.situm.sdk.utils.Handler;

/**
 * Created by alberto.penas on 10/07/17.
 */

public class GetPoisUseCase {

    public interface Callback{
        void onSuccess(Building building, Collection<Poi>pois);
        void onError(String error);
    }
    private Callback callback;
    private Building building_;

    public GetPoisUseCase(Building building) {
        this.building_ = building;
    }

    public void get(final Callback callback){
        if (hasCallback()){
            Log.d("GetPoisUseCase", "already running");
            return;
        }
        this.callback = callback;
        SitumSdk.communicationManager().fetchIndoorPOIsFromBuilding(building_, new Handler<Collection<Poi>>() {
            @Override
            public void onSuccess(Collection<Poi> pois) {
                if (hasCallback()) {
                    callback.onSuccess(building_, pois);
                }
                clearCallback();
            }

            @Override
            public void onFailure(Error error) {
                if (hasCallback()) {
                    callback.onError(error.getMessage());
                }
                clearCallback();
            }
        });
    }

    public void cancel(){
        callback = null;
    }

    private boolean hasCallback(){
        return callback != null;
    }

    private void clearCallback(){
        callback = null;
    }
}
