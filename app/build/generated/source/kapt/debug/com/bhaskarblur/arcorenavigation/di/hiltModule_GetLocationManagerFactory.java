// Generated by Dagger (https://dagger.dev).
package com.bhaskarblur.arcorenavigation.di;

import android.content.Context;
import android.location.LocationManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class hiltModule_GetLocationManagerFactory implements Factory<LocationManager> {
  private final hiltModule module;

  private final Provider<Context> contextProvider;

  public hiltModule_GetLocationManagerFactory(hiltModule module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public LocationManager get() {
    return getLocationManager(module, contextProvider.get());
  }

  public static hiltModule_GetLocationManagerFactory create(hiltModule module,
      Provider<Context> contextProvider) {
    return new hiltModule_GetLocationManagerFactory(module, contextProvider);
  }

  public static LocationManager getLocationManager(hiltModule instance, Context context) {
    return Preconditions.checkNotNullFromProvides(instance.getLocationManager(context));
  }
}
